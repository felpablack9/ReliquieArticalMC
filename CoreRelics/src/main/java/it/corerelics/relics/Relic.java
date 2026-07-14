package it.corerelics.relics;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.abilities.AbilityType;

public class Relic {

    private final String id;
    private final String displayName;
    private final List<String> lore;
    private final Material material;
    private final AbilityType abilityType;
    private final int abilityCooldown;
    private final double abilityRadius;
    private final double abilityDamage;
    private final int abilityRange;

    private UUID currentOwner;
    private final List<OwnerRecord> ownerHistory;

    public Relic(String id, String displayName, List<String> lore, Material material,
                 AbilityType abilityType, int abilityCooldown, double abilityRadius,
                 double abilityDamage, int abilityRange) {
        this.id = id;
        this.displayName = displayName;
        this.lore = lore;
        this.material = material;
        this.abilityType = abilityType;
        this.abilityCooldown = abilityCooldown;
        this.abilityRadius = abilityRadius;
        this.abilityDamage = abilityDamage;
        this.abilityRange = abilityRange;
        this.currentOwner = null;
        this.ownerHistory = new ArrayList<>();
    }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            coloredLore.add("");
            coloredLore.add(ChatColor.DARK_PURPLE + "✦ Reliquia Leggendaria ✦");
            meta.setLore(coloredLore);

            NamespacedKey key = new NamespacedKey(CoreRelicsPlugin.getInstance(), "relic_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

            meta.setUnbreakable(true);

            item.setItemMeta(meta);
        }

        return item;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public Material getMaterial() {
        return material;
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }

    public int getAbilityCooldown() {
        return abilityCooldown;
    }

    public double getAbilityRadius() {
        return abilityRadius;
    }

    public double getAbilityDamage() {
        return abilityDamage;
    }

    public int getAbilityRange() {
        return abilityRange;
    }

    public UUID getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(UUID owner, String playerName) {
        if (this.currentOwner != null) {
            ownerHistory.add(new OwnerRecord(this.currentOwner, playerName, System.currentTimeMillis()));
        }
        this.currentOwner = owner;
        if (owner != null) {
            ownerHistory.add(new OwnerRecord(owner, playerName, System.currentTimeMillis()));
        }
    }

    public List<OwnerRecord> getOwnerHistory() {
        return new ArrayList<>(ownerHistory);
    }

    public void addOwnerRecord(OwnerRecord record) {
        ownerHistory.add(record);
    }

    public List<String> getLore() {
        return lore;
    }
}
