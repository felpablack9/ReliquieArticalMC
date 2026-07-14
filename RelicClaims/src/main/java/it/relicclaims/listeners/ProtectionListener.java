package it.relicclaims.listeners;

import it.relicclaims.RelicClaimsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProtectionListener implements Listener {

    private final RelicClaimsPlugin plugin;

    public ProtectionListener(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("relicclaims.admin")) return;

        if (!plugin.getClaimManager().canBuild(player.getUniqueId(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            var claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
            String ownerName = claim != null ?
                    org.bukkit.Bukkit.getOfflinePlayer(claim.getOwner()).getName() : "Sconosciuto";
            String msg = plugin.getConfig().getString("messages.protected",
                    "&cNon puoi costruire/distruggere qui! Terreno protetto da &e{owner}");
            msg = msg.replace("{owner}", ownerName);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("relicclaims.admin")) return;

        if (!plugin.getClaimManager().canBuild(player.getUniqueId(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            var claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
            String ownerName = claim != null ?
                    org.bukkit.Bukkit.getOfflinePlayer(claim.getOwner()).getName() : "Sconosciuto";
            String msg = plugin.getConfig().getString("messages.protected",
                    "&cNon puoi costruire/distruggere qui! Terreno protetto da &e{owner}");
            msg = msg.replace("{owner}", ownerName);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getConfig().getBoolean("settings.protect-explosions", true)) return;

        event.blockList().removeIf(block -> plugin.getClaimManager().getClaimAt(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPvPInClaim(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("settings.protect-pvp-in-claims", true)) return;

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        var claim = plugin.getClaimManager().getClaimAt(victim.getLocation());
        if (claim == null) return;

        if (plugin.getNationManager().areAllied(attacker.getUniqueId(), victim.getUniqueId())) {
            return;
        }

        if (claim.isOwnedBy(victim.getUniqueId()) || claim.isOwnedBy(attacker.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        attacker.sendMessage(ChatColor.RED + "PvP non permesso in terreni protetti!");
    }
}
