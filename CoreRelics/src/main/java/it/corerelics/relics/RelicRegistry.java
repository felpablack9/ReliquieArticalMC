package it.corerelics.relics;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.abilities.AbilityType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class RelicRegistry {

    private final CoreRelicsPlugin plugin;
    private final Map<String, Relic> relics;

    public RelicRegistry(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
        this.relics = new LinkedHashMap<>();
    }

    public void loadRelicsFromConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("relics");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection relicSection = section.getConfigurationSection(key);
            if (relicSection == null) continue;

            String displayName = relicSection.getString("display-name", key);
            List<String> lore = relicSection.getStringList("lore");
            Material material = Material.valueOf(relicSection.getString("material", "DIAMOND_SWORD"));
            AbilityType abilityType = AbilityType.valueOf(relicSection.getString("ability", "NONE"));
            int cooldown = relicSection.getInt("ability-cooldown", 30);
            double radius = relicSection.getDouble("ability-radius", 3.0);
            double damage = relicSection.getDouble("ability-damage", 6.0);
            int range = relicSection.getInt("ability-range", 30);

            Relic relic = new Relic(key, displayName, lore, material, abilityType,
                    cooldown, radius, damage, range);
            relics.put(key, relic);

            plugin.getLogger().info("Reliquia caricata: " + key);
        }
    }

    public void registerRelic(Relic relic) {
        relics.put(relic.getId(), relic);
    }

    public Relic getRelicById(String id) {
        return relics.get(id);
    }

    public Relic getRelicFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        NamespacedKey key = new NamespacedKey(plugin, "relic_id");
        String relicId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (relicId == null) return null;
        return relics.get(relicId);
    }

    public Collection<Relic> getAllRelics() {
        return relics.values();
    }

    public Set<String> getRelicIds() {
        return relics.keySet();
    }

    public int getRelicCount() {
        return relics.size();
    }

    public boolean relicExists(String id) {
        return relics.containsKey(id);
    }

    public boolean isRelicInWorld(String relicId) {
        Relic relic = relics.get(relicId);
        return relic != null && relic.getCurrentOwner() != null;
    }
}
