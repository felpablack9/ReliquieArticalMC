package it.relicevents.events.types;

import it.corerelics.relics.Relic;
import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.RelicEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MeteorEvent implements RelicEvent {

    private final RelicEventsPlugin plugin;
    private Location center;
    private boolean active;
    private final List<Location> modifiedBlocks;

    public MeteorEvent(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.active = false;
        this.modifiedBlocks = new ArrayList<>();
    }

    @Override
    public String getId() {
        return "meteor";
    }

    @Override
    public String getDisplayName() {
        return "Meteora";
    }

    @Override
    public void start(Location center) {
        this.center = center;
        this.active = true;

        int warningSeconds = plugin.getConfig().getInt("events.meteor.particle-warning-seconds", 10);

        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.meteor-incoming",
                "&c&l☄ METEORA IN ARRIVO! &eImpatto previsto a X:{x} Z:{z}!");
        msg = msg.replace("{x}", String.valueOf(center.getBlockX()))
                .replace("{z}", String.valueOf(center.getBlockZ()));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));

        // Warning phase with particles falling from sky
        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = warningSeconds * 20;

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    cancel();
                    impact(center);
                    return;
                }

                Location skyLoc = center.clone().add(
                        (Math.random() - 0.5) * 10,
                        50 + Math.random() * 30,
                        (Math.random() - 0.5) * 10);

                center.getWorld().spawnParticle(Particle.LAVA, skyLoc, 5);
                center.getWorld().spawnParticle(Particle.FLAME, skyLoc, 10, 1, 2, 1);

                // Falling blocks for visual effect
                if (ticks % 10 == 0) {
                    Location fallLoc = center.clone().add(
                            (Math.random() - 0.5) * 8, 60, (Math.random() - 0.5) * 8);
                    FallingBlock fb = center.getWorld().spawnFallingBlock(
                            fallLoc, Material.MAGMA_BLOCK.createBlockData());
                    fb.setDropItem(false);
                    fb.setVelocity(new Vector(0, -0.5, 0));
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void impact(Location center) {
        int craterRadius = plugin.getConfig().getInt("events.meteor.crater-radius", 5);
        boolean fireSpread = plugin.getConfig().getBoolean("events.meteor.fire-spread", true);

        // Explosion effect
        center.getWorld().createExplosion(center, 0F, false, false);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
        center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, center, 10);
        center.getWorld().spawnParticle(Particle.LAVA, center, 100, craterRadius, craterRadius, craterRadius);

        // Create crater
        for (int x = -craterRadius; x <= craterRadius; x++) {
            for (int y = -craterRadius; y <= craterRadius; y++) {
                for (int z = -craterRadius; z <= craterRadius; z++) {
                    if (x * x + y * y + z * z <= craterRadius * craterRadius) {
                        Block block = center.getWorld().getBlockAt(
                                center.getBlockX() + x,
                                center.getBlockY() + y,
                                center.getBlockZ() + z);

                        if (block.getType() != Material.BEDROCK &&
                                block.getType() != Material.BARRIER) {
                            modifiedBlocks.add(block.getLocation().clone());
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }

        // Place meteor core blocks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location coreLoc = center.clone().add(x, -craterRadius, z);
                coreLoc.getBlock().setType(Material.MAGMA_BLOCK);
                modifiedBlocks.add(coreLoc.clone());
            }
        }

        // Place obsidian center
        center.clone().add(0, -craterRadius, 0).getBlock().setType(Material.CRYING_OBSIDIAN);
        modifiedBlocks.add(center.clone().add(0, -craterRadius, 0));

        if (fireSpread) {
            for (int i = 0; i < 15; i++) {
                int fx = center.getBlockX() + (int) ((Math.random() - 0.5) * craterRadius * 2);
                int fz = center.getBlockZ() + (int) ((Math.random() - 0.5) * craterRadius * 2);
                int fy = center.getWorld().getHighestBlockYAt(fx, fz) + 1;
                Location fireLoc = new Location(center.getWorld(), fx, fy, fz);
                if (fireLoc.getBlock().getType() == Material.AIR) {
                    fireLoc.getBlock().setType(Material.FIRE);
                    modifiedBlocks.add(fireLoc.clone());
                }
            }
        }

        // Place relic chest in crater center
        Location chestLoc = center.clone().add(0, -craterRadius + 1, 0);
        chestLoc.getBlock().setType(Material.CHEST);
        modifiedBlocks.add(chestLoc.clone());

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestLoc.getBlock().getState();
        Collection<Relic> relics = plugin.getCoreRelics().getRelicRegistry().getAllRelics();
        for (Relic relic : relics) {
            if (relic.getCurrentOwner() == null) {
                chest.getInventory().setItem(13, relic.createItemStack());
                break;
            }
        }
        chest.getInventory().setItem(0, new ItemStack(Material.NETHERITE_SCRAP, 2));
        chest.getInventory().setItem(4, new ItemStack(Material.DIAMOND, 5));

        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.meteor-landed",
                "&6La meteora è atterrata! Corri a cercare la reliquia tra le macerie!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }

    @Override
    public void cleanup() {
        // Don't restore crater blocks - they're permanent terrain changes
        // Only remove fire
        for (Location loc : modifiedBlocks) {
            if (loc.getBlock().getType() == Material.FIRE) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        modifiedBlocks.clear();
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
