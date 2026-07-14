package it.corerelics.data;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.relics.OwnerRecord;
import it.corerelics.relics.Relic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final CoreRelicsPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "relics_data.yml");

        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare il file dati: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load owner data after relics are loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, this::loadRelicData, 1L);
    }

    private void loadRelicData() {
        ConfigurationSection relicsSection = dataConfig.getConfigurationSection("relics");
        if (relicsSection == null) return;

        for (String relicId : relicsSection.getKeys(false)) {
            Relic relic = plugin.getRelicRegistry().getRelicById(relicId);
            if (relic == null) continue;

            ConfigurationSection relicData = relicsSection.getConfigurationSection(relicId);
            if (relicData == null) continue;

            // Load current owner
            String ownerStr = relicData.getString("current-owner");
            if (ownerStr != null && !ownerStr.isEmpty()) {
                try {
                    UUID ownerUuid = UUID.fromString(ownerStr);
                    String ownerName = relicData.getString("current-owner-name", "Sconosciuto");
                    relic.setCurrentOwner(ownerUuid, ownerName);
                } catch (IllegalArgumentException ignored) {
                }
            }

            // Load history
            List<?> historyList = relicData.getList("history");
            if (historyList != null) {
                ConfigurationSection historySection = relicData.getConfigurationSection("history");
                if (historySection != null) {
                    for (String key : historySection.getKeys(false)) {
                        ConfigurationSection entry = historySection.getConfigurationSection(key);
                        if (entry == null) continue;

                        String uuidStr = entry.getString("uuid");
                        String name = entry.getString("name", "Sconosciuto");
                        long timestamp = entry.getLong("timestamp", 0);

                        if (uuidStr != null) {
                            try {
                                UUID uuid = UUID.fromString(uuidStr);
                                relic.addOwnerRecord(new OwnerRecord(uuid, name, timestamp));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }

            plugin.getLogger().info("Dati caricati per reliquia: " + relicId);
        }
    }

    public void save() {
        if (dataConfig == null || dataFile == null) return;

        for (Relic relic : plugin.getRelicRegistry().getAllRelics()) {
            String path = "relics." + relic.getId();

            if (relic.getCurrentOwner() != null) {
                dataConfig.set(path + ".current-owner", relic.getCurrentOwner().toString());
                // Find the current owner name from history
                List<OwnerRecord> history = relic.getOwnerHistory();
                String ownerName = !history.isEmpty() ?
                        history.get(history.size() - 1).getPlayerName() : "Sconosciuto";
                dataConfig.set(path + ".current-owner-name", ownerName);
            } else {
                dataConfig.set(path + ".current-owner", null);
                dataConfig.set(path + ".current-owner-name", null);
            }

            // Save history
            List<OwnerRecord> history = relic.getOwnerHistory();
            for (int i = 0; i < history.size(); i++) {
                OwnerRecord record = history.get(i);
                String historyPath = path + ".history." + i;
                dataConfig.set(historyPath + ".uuid", record.getPlayerUuid().toString());
                dataConfig.set(historyPath + ".name", record.getPlayerName());
                dataConfig.set(historyPath + ".timestamp", record.getTimestamp());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Errore nel salvataggio dati: " + e.getMessage());
        }
    }
}
