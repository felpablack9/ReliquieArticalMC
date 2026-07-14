package it.relicevents.events.types;

import it.corerelics.relics.Relic;
import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.RelicEvent;
import it.relicevents.structures.DungeonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DungeonEvent implements RelicEvent {

    private final RelicEventsPlugin plugin;
    private Location center;
    private boolean active;
    private final List<LivingEntity> spawnedMobs;
    private final List<Location> placedBlocks;

    public DungeonEvent(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.active = false;
        this.spawnedMobs = new ArrayList<>();
        this.placedBlocks = new ArrayList<>();
    }

    @Override
    public String getId() {
        return "dungeon";
    }

    @Override
    public String getDisplayName() {
        return "Dungeon Temporaneo";
    }

    @Override
    public void start(Location center) {
        this.center = center;
        this.active = true;

        int radius = plugin.getConfig().getInt("events.dungeon.size-radius", 8);
        DungeonBuilder.buildDungeon(center, radius, placedBlocks);

        int mobCount = plugin.getConfig().getInt("events.dungeon.mob-count", 15);
        spawnMobs(center, radius, mobCount);

        if (plugin.getConfig().getBoolean("events.dungeon.boss-at-end", true)) {
            spawnDungeonBoss(center);
        }

        placeRelicChest(center);

        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.dungeon-spawned",
                "&5&l\uD83C\uDFF0 DUNGEON TEMPORANEO &dè apparso nelle vicinanze!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }

    private void spawnMobs(Location center, int radius, int count) {
        EntityType[] mobTypes = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CAVE_SPIDER};

        for (int i = 0; i < count; i++) {
            double offsetX = (Math.random() - 0.5) * radius * 2;
            double offsetZ = (Math.random() - 0.5) * radius * 2;
            Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);

            EntityType type = mobTypes[(int) (Math.random() * mobTypes.length)];
            LivingEntity mob = (LivingEntity) center.getWorld().spawnEntity(spawnLoc, type);
            mob.setCustomName(ChatColor.DARK_PURPLE + "Guardiano del Dungeon");
            mob.setCustomNameVisible(true);

            spawnedMobs.add(mob);
        }
    }

    private void spawnDungeonBoss(Location center) {
        Zombie boss = (Zombie) center.getWorld().spawnEntity(center.clone().add(0, 1, 0), EntityType.ZOMBIE);
        boss.setCustomName(ChatColor.DARK_RED + "☠ Custode del Dungeon");
        boss.setCustomNameVisible(true);
        boss.setBaby(false);

        double healthMultiplier = plugin.getConfig().getDouble("events.boss.boss-health-multiplier", 5.0);
        boss.setMaxHealth(boss.getMaxHealth() * healthMultiplier);
        boss.setHealth(boss.getMaxHealth());

        boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));

        boss.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "relic_boss"), 
                org.bukkit.persistence.PersistentDataType.STRING, "dungeon_boss");

        spawnedMobs.add(boss);
    }

    private void placeRelicChest(Location center) {
        Location chestLoc = center.clone().add(0, 0, 0);
        chestLoc.getBlock().setType(Material.CHEST);
        placedBlocks.add(chestLoc.clone());

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLoc.getBlock().getState();

        // Place a random available relic in the chest
        Collection<Relic> relics = plugin.getCoreRelics().getRelicRegistry().getAllRelics();
        for (Relic relic : relics) {
            if (relic.getCurrentOwner() == null) {
                chest.getInventory().setItem(13, relic.createItemStack());
                break;
            }
        }

        // Add some loot around the relic
        chest.getInventory().setItem(0, new ItemStack(Material.GOLD_INGOT, 8));
        chest.getInventory().setItem(4, new ItemStack(Material.DIAMOND, 3));
        chest.getInventory().setItem(22, new ItemStack(Material.EMERALD, 5));
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
