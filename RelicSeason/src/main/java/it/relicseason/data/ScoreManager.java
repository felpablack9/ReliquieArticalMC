package it.relicseason.data;

import it.relicseason.RelicSeasonPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreManager {

    private final RelicSeasonPlugin plugin;
    private final Map<UUID, Integer> scores;
    private final Map<UUID, String> playerNames;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ScoreManager(RelicSeasonPlugin plugin) {
        this.plugin = plugin;
        this.scores = new HashMap<>();
        this.playerNames = new HashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "scores.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("scores");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                int score = section.getInt(uuidStr + ".points", 0);
                String name = section.getString(uuidStr + ".name", "");
                scores.put(uuid, score);
                playerNames.put(uuid, name);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("scores", null);

        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            String path = "scores." + entry.getKey().toString();
            dataConfig.set(path + ".points", entry.getValue());
            dataConfig.set(path + ".name", playerNames.getOrDefault(entry.getKey(), ""));
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void addPoints(UUID uuid, int points, String reason) {
        scores.merge(uuid, points, Integer::sum);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            playerNames.put(uuid, player.getName());
            String msg = plugin.getConfig().getString("messages.points-gained", "&a+{points} punti! &7({reason})");
            msg = msg.replace("{points}", String.valueOf(points)).replace("{reason}", reason);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.prefix", "&6[Season] &r") + msg));
        }
    }

    public int getPoints(UUID uuid) {
        return scores.getOrDefault(uuid, 0);
    }

    public List<Map.Entry<UUID, Integer>> getSortedScores() {
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sorted;
    }

    public Map.Entry<String, Integer> getTopPlayer() {
        if (scores.isEmpty()) return null;

        Map.Entry<UUID, Integer> top = scores.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        if (top == null) return null;
        String name = playerNames.getOrDefault(top.getKey(), "Sconosciuto");
        return new AbstractMap.SimpleEntry<>(name, top.getValue());
    }

    public String getPlayerName(UUID uuid) {
        return playerNames.getOrDefault(uuid, "Sconosciuto");
    }

    public void reset() {
        scores.clear();
        playerNames.clear();
        save();
    }
}
