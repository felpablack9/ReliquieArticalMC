package it.relicseason.data;

import it.relicseason.RelicSeasonPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HallOfFame {

    private final RelicSeasonPlugin plugin;
    private final List<HallEntry> entries;
    private File dataFile;
    private FileConfiguration dataConfig;

    public HallOfFame(RelicSeasonPlugin plugin) {
        this.plugin = plugin;
        this.entries = new ArrayList<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "hall_of_fame.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("hall");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;

            entries.add(new HallEntry(
                    entry.getInt("season-number"),
                    entry.getString("season-name", ""),
                    entry.getString("winner", ""),
                    entry.getInt("points", 0)
            ));
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("hall", null);

        for (int i = 0; i < entries.size(); i++) {
            HallEntry e = entries.get(i);
            String path = "hall." + i;
            dataConfig.set(path + ".season-number", e.seasonNumber);
            dataConfig.set(path + ".season-name", e.seasonName);
            dataConfig.set(path + ".winner", e.winner);
            dataConfig.set(path + ".points", e.points);
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void addEntry(int seasonNumber, String seasonName, String winner, int points) {
        int maxEntries = plugin.getConfig().getInt("hall-of-fame.max-entries", 10);
        entries.add(new HallEntry(seasonNumber, seasonName, winner, points));
        while (entries.size() > maxEntries) {
            entries.remove(0);
        }
    }

    public List<HallEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public static class HallEntry {
        public final int seasonNumber;
        public final String seasonName;
        public final String winner;
        public final int points;

        public HallEntry(int seasonNumber, String seasonName, String winner, int points) {
            this.seasonNumber = seasonNumber;
            this.seasonName = seasonName;
            this.winner = winner;
            this.points = points;
        }
    }
}
