package it.relicclaims.data;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Claim;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClaimManager {

    private final RelicClaimsPlugin plugin;
    private final Map<String, Claim> claims;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ClaimManager(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
        this.claims = new HashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(key);
            if (cs == null) continue;

            int chunkX = cs.getInt("chunkX");
            int chunkZ = cs.getInt("chunkZ");
            String world = cs.getString("world", "world");
            UUID owner = UUID.fromString(cs.getString("owner"));
            String guildId = cs.getString("guild", null);

            Claim claim = new Claim(chunkX, chunkZ, world, owner, guildId);
            claims.put(claim.getKey(), claim);
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("claims", null);

        int i = 0;
        for (Claim claim : claims.values()) {
            String path = "claims." + i;
            dataConfig.set(path + ".chunkX", claim.getChunkX());
            dataConfig.set(path + ".chunkZ", claim.getChunkZ());
            dataConfig.set(path + ".world", claim.getWorld());
            dataConfig.set(path + ".owner", claim.getOwner().toString());
            if (claim.getGuildId() != null) {
                dataConfig.set(path + ".guild", claim.getGuildId());
            }
            i++;
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public Claim getClaimAt(Location location) {
        Chunk chunk = location.getChunk();
        String key = location.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return claims.get(key);
    }

    public Claim getClaimAt(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return claims.get(key);
    }

    public boolean createClaim(Chunk chunk, UUID owner, String guildId) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        if (claims.containsKey(key)) return false;

        Claim claim = new Claim(chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), owner, guildId);
        claims.put(key, claim);
        save();
        return true;
    }

    public boolean removeClaim(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        boolean removed = claims.remove(key) != null;
        if (removed) save();
        return removed;
    }

    public int getPlayerClaimCount(UUID uuid) {
        return (int) claims.values().stream().filter(c -> c.isOwnedBy(uuid)).count();
    }

    public List<Claim> getPlayerClaims(UUID uuid) {
        return claims.values().stream().filter(c -> c.isOwnedBy(uuid)).collect(Collectors.toList());
    }

    public List<Claim> getGuildClaims(String guildId) {
        return claims.values().stream().filter(c -> guildId.equals(c.getGuildId())).collect(Collectors.toList());
    }

    public boolean canBuild(UUID player, Location location) {
        Claim claim = getClaimAt(location);
        if (claim == null) return true;

        if (claim.isOwnedBy(player)) return true;

        if (claim.getGuildId() != null) {
            it.relicclaims.model.Guild guild = plugin.getGuildManager().getGuildById(claim.getGuildId());
            if (guild != null && guild.isMember(player)) return true;
        }

        return false;
    }

    public int getClaimCount() { return claims.size(); }
}
