package it.relicclaims.data;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Nation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NationManager {

    private final RelicClaimsPlugin plugin;
    private final Map<String, Nation> nations;
    private File dataFile;
    private FileConfiguration dataConfig;

    public NationManager(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
        this.nations = new HashMap<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "nations.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("nations");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection ns = section.getConfigurationSection(id);
            if (ns == null) continue;

            String name = ns.getString("name", id);
            UUID leader = UUID.fromString(ns.getString("leader"));
            Nation nation = new Nation(id, name, leader);

            ns.getStringList("guilds").forEach(nation::addGuild);
            ns.getStringList("alliances").forEach(nation::addAlliance);

            nations.put(id, nation);
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("nations", null);

        for (Nation nation : nations.values()) {
            String path = "nations." + nation.getId();
            dataConfig.set(path + ".name", nation.getName());
            dataConfig.set(path + ".leader", nation.getLeader().toString());
            dataConfig.set(path + ".guilds", nation.getGuildIds());
            dataConfig.set(path + ".alliances", nation.getAllianceIds());
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public Nation createNation(String name, UUID leader) {
        String id = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        if (nations.containsKey(id)) return null;

        Nation nation = new Nation(id, name, leader);
        nations.put(id, nation);
        save();
        return nation;
    }

    public Nation getNationById(String id) { return nations.get(id); }

    public Nation getPlayerNation(UUID uuid) {
        for (Nation nation : nations.values()) {
            for (String guildId : nation.getGuildIds()) {
                it.relicclaims.model.Guild guild = plugin.getGuildManager().getGuildById(guildId);
                if (guild != null && guild.isMember(uuid)) return nation;
            }
        }
        return null;
    }

    public boolean areAllied(UUID player1, UUID player2) {
        Nation n1 = getPlayerNation(player1);
        Nation n2 = getPlayerNation(player2);
        if (n1 == null || n2 == null) return false;
        if (n1.getId().equals(n2.getId())) return true;
        return n1.isAlliedWith(n2.getId());
    }

    public boolean deleteNation(String id) {
        boolean removed = nations.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public Collection<Nation> getAllNations() { return nations.values(); }
    public int getNationCount() { return nations.size(); }
}
