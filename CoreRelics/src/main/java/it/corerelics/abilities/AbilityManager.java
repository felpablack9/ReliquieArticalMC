package it.corerelics.abilities;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.relics.Relic;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    private final CoreRelicsPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns;

    private static final java.util.Set<Material> UNBREAKABLE = java.util.EnumSet.of(
            Material.BEDROCK, Material.BARRIER, Material.END_PORTAL_FRAME,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK
    );

    public AbilityManager(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    public boolean activateAbility(Player player, Relic relic) {
        if (isOnCooldown(player, relic)) {
            long remaining = getRemainingCooldown(player, relic);
            String msg = plugin.getConfig().getString("messages.relic-ability-cooldown",
                    "&cAbilità in ricarica! Attendi {seconds} secondi.");
            msg = msg.replace("{seconds}", String.valueOf(remaining));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return false;
        }

        boolean success = false;

        switch (relic.getAbilityType()) {
            case SWEEPING_STRIKE:
                success = activateSweepingStrike(player, relic);
                break;
            case TELEPORT:
                success = activateTeleport(player, relic);
                break;
            case AREA_BREAK:
                success = activateAreaBreak(player, relic);
                break;
            default:
                break;
        }

        if (success) {
            setCooldown(player, relic);
        }

        return success;
    }

    private boolean activateSweepingStrike(Player player, Relic relic) {
        double radius = relic.getAbilityRadius();
        double damage = relic.getAbilityDamage();

        Location loc = player.getLocation();
        boolean hitSomething = false;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                hitSomething = true;

                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                        target.getLocation().add(0, 1, 0), 3);
            }
        }

        if (hitSomething) {
            player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
            player.sendMessage(ChatColor.RED + "⚔ Colpo Devastante attivato!");
        } else {
            player.sendMessage(ChatColor.GRAY + "Nessun nemico nel raggio d'azione.");
            return false;
        }

        return true;
    }

    private boolean activateTeleport(Player player, Relic relic) {
        int range = relic.getAbilityRange();

        RayTraceResult result = player.rayTraceBlocks(range);

        Location targetLoc;
        if (result != null && result.getHitBlock() != null) {
            targetLoc = result.getHitBlock().getLocation().add(0.5, 1, 0.5);
        } else {
            targetLoc = player.getLocation().add(player.getLocation().getDirection().multiply(range));
        }

        Location fromLoc = player.getLocation();

        player.getWorld().spawnParticle(Particle.PORTAL, fromLoc, 50, 0.5, 1, 0.5);
        player.getWorld().playSound(fromLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        player.teleport(targetLoc);

        player.getWorld().spawnParticle(Particle.PORTAL, targetLoc, 50, 0.5, 1, 0.5);
        player.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ Teletrasporto completato!");
        return true;
    }

    private boolean activateAreaBreak(Player player, Relic relic) {
        int radius = (int) relic.getAbilityRadius();

        RayTraceResult result = player.rayTraceBlocks(5);
        if (result == null || result.getHitBlock() == null) {
            player.sendMessage(ChatColor.GRAY + "Devi guardare un blocco da distruggere.");
            return false;
        }

        Block center = result.getHitBlock();
        Location centerLoc = center.getLocation();
        int broken = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = centerLoc.getWorld().getBlockAt(
                            centerLoc.getBlockX() + x,
                            centerLoc.getBlockY() + y,
                            centerLoc.getBlockZ() + z);

                    if (!UNBREAKABLE.contains(block.getType()) &&
                            block.getType() != Material.AIR) {
                        block.breakNaturally();
                        broken++;
                    }
                }
            }
        }

        if (broken > 0) {
            player.getWorld().playSound(centerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.6f);
            player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, centerLoc, 5);
            player.sendMessage(ChatColor.AQUA + "⚒ Frantumazione! " + broken + " blocchi distrutti!");
        }

        return true;
    }

    private boolean isOnCooldown(Player player, Relic relic) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;

        Long expiry = playerCooldowns.get(relic.getId());
        if (expiry == null) return false;

        return System.currentTimeMillis() < expiry;
    }

    private long getRemainingCooldown(Player player, Relic relic) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long expiry = playerCooldowns.get(relic.getId());
        if (expiry == null) return 0;

        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }

    private void setCooldown(Player player, Relic relic) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(relic.getId(), System.currentTimeMillis() + (relic.getAbilityCooldown() * 1000L));
    }
}
