package it.reliccombat.bounty;

import it.reliccombat.RelicCombatPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyManager {

    private final RelicCombatPlugin plugin;
    private final Map<UUID, BountyEntry> bounties;
    private File dataFile;
    private FileConfiguration dataConfig;

    public BountyManager(RelicCombatPlugin plugin) {
        this.plugin = plugin;
        this.bounties = new LinkedHashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "bounties.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("bounties");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String name = section.getString(uuidStr + ".name", "");
                int amount = section.getInt(uuidStr + ".amount", 0);
                bounties.put(uuid, new BountyEntry(name, amount));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("bounties", null);

        for (Map.Entry<UUID, BountyEntry> entry : bounties.entrySet()) {
            String path = "bounties." + entry.getKey().toString();
            dataConfig.set(path + ".name", entry.getValue().playerName);
            dataConfig.set(path + ".amount", entry.getValue().amount);
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void setBounty(UUID target, String targetName, int amount) {
        BountyEntry existing = bounties.get(target);
        if (existing != null) {
            existing.amount += amount;
        } else {
            bounties.put(target, new BountyEntry(targetName, amount));
        }
        save();
    }

    public boolean hasBounty(UUID target) {
        return bounties.containsKey(target) && bounties.get(target).amount > 0;
    }

    public int getBounty(UUID target) {
        BountyEntry entry = bounties.get(target);
        return entry != null ? entry.amount : 0;
    }

    public void claimBounty(UUID target, Player claimer) {
        BountyEntry entry = bounties.remove(target);
        if (entry != null && entry.amount > 0) {
            claimer.getInventory().addItem(new ItemStack(Material.DIAMOND, entry.amount));
        }
        save();
    }

    public List<Map.Entry<UUID, BountyEntry>> getSortedBounties() {
        List<Map.Entry<UUID, BountyEntry>> sorted = new ArrayList<>(bounties.entrySet());
        sorted.sort((a, b) -> b.getValue().amount - a.getValue().amount);
        return sorted;
    }

    public static class BountyEntry {
        public String playerName;
        public int amount;

        public BountyEntry(String playerName, int amount) {
            this.playerName = playerName;
            this.amount = amount;
        }
    }
}
