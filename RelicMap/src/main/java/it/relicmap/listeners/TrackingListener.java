package it.relicmap.listeners;

import it.corerelics.relics.Relic;
import it.relicmap.RelicMapPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrackingListener implements Listener {

    private final RelicMapPlugin plugin;

    public TrackingListener(RelicMapPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item);
            if (relic != null) {
                String region = plugin.getRegionManager().getRegionName(player.getLocation());
                plugin.getSightingTracker().updateSighting(relic.getId(), region, player.getName());
            }
        }
    }
}
