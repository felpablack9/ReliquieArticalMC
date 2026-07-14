package it.relicevents.events.types;

import it.corerelics.relics.Relic;
import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.RelicEvent;
import it.relicevents.structures.ShipBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GhostShipEvent implements RelicEvent {

    private final RelicEventsPlugin plugin;
    private Location center;
    private boolean active;
    private final List<LivingEntity> spawnedMobs;
    private final List<Location> placedBlocks;

    public GhostShipEvent(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.active = false;
        this.spawnedMobs = new ArrayList<>();
        this.placedBlocks = new ArrayList<>();
    }

    @Override
    public String getId() {
        return "ghost_ship";
    }

    @Override
    public String getDisplayName() {
        return "Nave Fantasma";
    }

    @Override
    public void start(Location center) {
        // Try to find water nearby, otherwise place on the surface
        Location shipLoc = findWaterLocation(center);
        if (shipLoc == null) {
            shipLoc = center;
        }
        this.center = shipLoc;
        this.active = true;

        int length = plugin.getConfig().getInt("events.ghost-ship.ship-length", 15);
        int width = plugin.getConfig().getInt("events.ghost-ship.ship-width", 5);
        ShipBuilder.buildGhostShip(shipLoc, length, width, placedBlocks);

        int mobCount = plugin.getConfig().getInt("events.ghost-ship.mob-count", 10);
        spawnGhostCrew(shipLoc, length, width, mobCount);

        placeRelicChest(shipLoc);

        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.ghost-ship-spawned",
                "&b&l⛵ NAVE FANTASMA &3avvistata in mare!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }

    private Location findWaterLocation(Location center) {
        int searchRadius = 100;
        for (int dx = -searchRadius; dx <= searchRadius; dx += 10) {
            for (int dz = -searchRadius; dz <= searchRadius; dz += 10) {
                Location check = center.clone().add(dx, 0, dz);
                int y = center.getWorld().getHighestBlockYAt(check.getBlockX(), check.getBlockZ());
                check.setY(y);
                Material topBlock = check.getBlock().getType();
                if (topBlock == Material.WATER) {
                    check.setY(y + 1);
                    return check;
                }
            }
        }
        return null;
    }

    private void spawnGhostCrew(Location center, int length, int width, int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = (Math.random() - 0.5) * length;
            double offsetZ = (Math.random() - 0.5) * width;
            Location spawnLoc = center.clone().add(offsetX, 2, offsetZ);

            Skeleton skeleton = (Skeleton) center.getWorld().spawnEntity(spawnLoc, EntityType.SKELETON);
            skeleton.setCustomName(ChatColor.AQUA + "Fantasma Pirata");
            skeleton.setCustomNameVisible(true);
            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            skeleton.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

            spawnedMobs.add(skeleton);
        }

        // Spawn captain (boss)
        Skeleton captain = (Skeleton) center.getWorld().spawnEntity(
                center.clone().add(0, 2, 0), EntityType.SKELETON);
        captain.setCustomName(ChatColor.DARK_AQUA + "☠ Capitano Fantasma");
        captain.setCustomNameVisible(true);

        double healthMultiplier = plugin.getConfig().getDouble("events.boss.boss-health-multiplier", 5.0);
        captain.setMaxHealth(captain.getMaxHealth() * healthMultiplier);
        captain.setHealth(captain.getMaxHealth());

        captain.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        captain.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        captain.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

        captain.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "relic_boss"),
                org.bukkit.persistence.PersistentDataType.STRING, "ghost_captain");

        spawnedMobs.add(captain);
    }

    private void placeRelicChest(Location center) {
        Location chestLoc = center.clone().add(0, 1, 0);
        chestLoc.getBlock().setType(Material.CHEST);
        placedBlocks.add(chestLoc.clone());

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLoc.getBlock().getState();

        Collection<Relic> relics = plugin.getCoreRelics().getRelicRegistry().getAllRelics();
        for (Relic relic : relics) {
            if (relic.getCurrentOwner() == null) {
                chest.getInventory().setItem(13, relic.createItemStack());
                break;
            }
        }

        chest.getInventory().setItem(0, new ItemStack(Material.GOLD_INGOT, 16));
        chest.getInventory().setItem(4, new ItemStack(Material.EMERALD, 8));
        chest.getInventory().setItem(8, new ItemStack(Material.NAUTILUS_SHELL, 3));
    }

    @Override
    public void cleanup() {
        for (LivingEntity mob : spawnedMobs) {
            if (mob != null && !mob.isDead()) {
                mob.remove();
            }
        }
        spawnedMobs.clear();

        for (Location loc : placedBlocks) {
            if (loc.getBlock().getType() != Material.AIR) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        placedBlocks.clear();

        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Location getLocation() {
        return center;
    }
}
