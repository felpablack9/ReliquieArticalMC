package it.relicclaims.data;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Guild;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuildManager {

    private final RelicClaimsPlugin plugin;
    private final Map<String, Guild> guilds;
    private File dataFile;
    private FileConfiguration dataConfig;

    public GuildManager(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
        this.guilds = new HashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "guilds.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("guilds");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection gs = section.getConfigurationSection(id);
            if (gs == null) continue;

            String name = gs.getString("name", id);
            UUID leader = UUID.fromString(gs.getString("leader"));
            Guild guild = new Guild(id, name, leader);

            List<String> memberStrs = gs.getStringList("members");
            for (String m : memberStrs) {
                try { guild.addMember(UUID.fromString(m)); } catch (IllegalArgumentException ignored) {}
            }

            guild.setNationId(gs.getString("nation", null));
            guilds.put(id, guild);
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("guilds", null);

        for (Guild guild : guilds.values()) {
            String path = "guilds." + guild.getId();
            dataConfig.set(path + ".name", guild.getName());
            dataConfig.set(path + ".leader", guild.getLeader().toString());
            List<String> memberStrs = new ArrayList<>();
            for (UUID m : guild.getMembers()) memberStrs.add(m.toString());
            dataConfig.set(path + ".members", memberStrs);
            if (guild.getNationId() != null) {
                dataConfig.set(path + ".nation", guild.getNationId());
            }
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public Guild createGuild(String name, UUID leader) {
        String id = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        if (guilds.containsKey(id)) return null;

        Guild guild = new Guild(id, name, leader);
        guilds.put(id, guild);
        save();
        return guild;
    }

    public Guild getGuildById(String id) { return guilds.get(id); }

    public Guild getPlayerGuild(UUID uuid) {
        for (Guild guild : guilds.values()) {
            if (guild.isMember(uuid)) return guild;
        }
        return null;
    }

    public boolean deleteGuild(String id) {
        boolean removed = guilds.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public Collection<Guild> getAllGuilds() { return guilds.values(); }
    public int getGuildCount() { return guilds.size(); }
}
