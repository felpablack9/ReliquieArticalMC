package it.relicmap.data;

import it.corerelics.relics.Relic;
import it.relicmap.RelicMapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SightingTracker {

    private final RelicMapPlugin plugin;
    private final Map<String, SightingRecord> lastSightings;
    private final Map<String, String> playerTags;
    private File dataFile;
    private FileConfiguration dataConfig;

    public SightingTracker(RelicMapPlugin plugin) {
        this.plugin = plugin;
        this.lastSightings = new HashMap<>();
        this.playerTags = new HashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "sightings.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.isConfigurationSection("sightings")) {
            for (String relicId : dataConfig.getConfigurationSection("sightings").getKeys(false)) {
                String region = dataConfig.getString("sightings." + relicId + ".region", "");
                long timestamp = dataConfig.getLong("sightings." + relicId + ".timestamp", 0);
                String playerName = dataConfig.getString("sightings." + relicId + ".player", "");
                lastSightings.put(relicId, new SightingRecord(region, timestamp, playerName));
            }
        }

        if (dataConfig.isConfigurationSection("tags")) {
            for (String uuid : dataConfig.getConfigurationSection("tags").getKeys(false)) {
                playerTags.put(uuid, dataConfig.getString("tags." + uuid, ""));
            }
        }
    }

    public void save() {
        if (dataConfig == null) return;

        for (Map.Entry<String, SightingRecord> entry : lastSightings.entrySet()) {
            String path = "sightings." + entry.getKey();
            dataConfig.set(path + ".region", entry.getValue().region);
            dataConfig.set(path + ".timestamp", entry.getValue().timestamp);
            dataConfig.set(path + ".player", entry.getValue().playerName);
        }

        for (Map.Entry<String, String> entry : playerTags.entrySet()) {
            dataConfig.set("tags." + entry.getKey(), entry.getValue());
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void updateAllSightings() {
        for (Relic relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            UUID owner = relic.getCurrentOwner();
            if (owner == null) continue;

            Player player = Bukkit.getPlayer(owner);
            if (player == null || !player.isOnline()) continue;

            String region = plugin.getRegionManager().getRegionName(player.getLocation());
            lastSightings.put(relic.getId(), new SightingRecord(region, System.currentTimeMillis(), player.getName()));
        }
        save();
    }

    public void updateSighting(String relicId, String region, String playerName) {
        lastSightings.put(relicId, new SightingRecord(region, System.currentTimeMillis(), playerName));
    }

    public SightingRecord getLastSighting(String relicId) {
        return lastSightings.get(relicId);
    }

    public void setPlayerTag(UUID uuid, String tag) {
        playerTags.put(uuid.toString(), tag);
        save();
    }

    public String getPlayerTag(UUID uuid) {
        return playerTags.getOrDefault(uuid.toString(), "");
    }

    public void removePlayerTag(UUID uuid) {
        playerTags.remove(uuid.toString());
        save();
    }

    public static class SightingRecord {
        public final String region;
        public final long timestamp;
        public final String playerName;

        public SightingRecord(String region, long timestamp, String playerName) {
            this.region = region;
            this.timestamp = timestamp;
            this.playerName = playerName;
        }

        public String getTimeAgo() {
            long diff = System.currentTimeMillis() - timestamp;
            long seconds = diff / 1000;
            if (seconds < 60) return seconds + "s";
            long minutes = seconds / 60;
            if (minutes < 60) return minutes + "m";
            long hours = minutes / 60;
            if (hours < 24) return hours + "h";
            long days = hours / 24;
            return days + "g";
        }
    }
}
