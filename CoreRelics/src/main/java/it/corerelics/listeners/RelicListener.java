package it.corerelics.listeners;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.abilities.AbilityManager;
import it.corerelics.abilities.AbilityType;
import it.corerelics.relics.Relic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class RelicListener implements Listener {

    private final CoreRelicsPlugin plugin;
    private final AbilityManager abilityManager;

    public RelicListener(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
        this.abilityManager = new AbilityManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        Relic relic = plugin.getRelicRegistry().getRelicFromItem(item);
        if (relic == null) return;

        if (relic.getAbilityType() == AbilityType.NONE) return;

        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            abilityManager.activateAbility(player, relic);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("settings.drop-on-death", true)) return;

        Player player = event.getEntity();

        for (ItemStack item : event.getDrops()) {
            Relic relic = plugin.getRelicRegistry().getRelicFromItem(item);
            if (relic != null) {
                relic.setCurrentOwner(null, player.getName());

                if (plugin.getConfig().getBoolean("settings.broadcast-on-pickup", true)) {
                    String msg = plugin.getConfig().getString("messages.relic-drop",
                            "&e{player} &6ha perso la reliquia: &c{relic}!");
                    msg = msg.replace("{player}", player.getName())
                            .replace("{relic}", relic.getColoredDisplayName());
                    String prefix = plugin.getConfig().getString("messages.prefix", "&6[CoreRelics] &r");
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
                }

                plugin.getDataManager().save();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        Relic relic = plugin.getRelicRegistry().getRelicFromItem(item);
        if (relic == null) return;

        if (relic.getCurrentOwner() != null && !relic.getCurrentOwner().equals(player.getUniqueId())) {
            // Someone else already owns it - this means it was stolen/dropped
        }

        relic.setCurrentOwner(player.getUniqueId(), player.getName());

        if (plugin.getConfig().getBoolean("settings.broadcast-on-pickup", true)) {
            String msg = plugin.getConfig().getString("messages.relic-pickup",
                    "&e{player} &6ha ottenuto la reliquia: &c{relic}!");
            msg = msg.replace("{player}", player.getName())
                    .replace("{relic}", relic.getColoredDisplayName());
            String prefix = plugin.getConfig().getString("messages.prefix", "&6[CoreRelics] &r");
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        }

        plugin.getDataManager().save();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        Relic relic = plugin.getRelicRegistry().getRelicFromItem(item);
        if (relic != null) {
            relic.setCurrentOwner(null, player.getName());
            plugin.getDataManager().save();
        }
    }
}
