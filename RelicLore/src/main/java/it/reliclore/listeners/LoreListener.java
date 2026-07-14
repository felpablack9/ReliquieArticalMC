package it.reliclore.listeners;

import it.corerelics.relics.Relic;
import it.reliclore.RelicLorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class LoreListener implements Listener {

    private final RelicLorePlugin plugin;

    public LoreListener(RelicLorePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRelicPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item);
        if (relic == null) return;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("relic", relic.getColoredDisplayName());
        placeholders.put("location", getSimpleLocation(player));

        plugin.getChronicleManager().recordEvent("relic-found", placeholders);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathWithRelic(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        for (ItemStack item : event.getDrops()) {
            Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item);
            if (relic == null) continue;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("relic", relic.getColoredDisplayName());

            if (killer != null) {
                placeholders.put("player", killer.getName());
                placeholders.put("victim", victim.getName());
                plugin.getChronicleManager().recordEvent("relic-stolen", placeholders);
            } else {
                placeholders.put("player", victim.getName());
                placeholders.put("location", getSimpleLocation(victim));
                plugin.getChronicleManager().recordEvent("relic-lost-death", placeholders);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBossKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "relic_boss");
        String bossTag = event.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (bossTag == null) return;

        String bossName = event.getEntity().getCustomName();
        if (bossName == null) bossName = event.getEntity().getType().name();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", killer.getName());
        placeholders.put("boss", bossName);
        placeholders.put("relic", "una reliquia");

        plugin.getChronicleManager().recordEvent("boss-killed", placeholders);
    }

    private String getSimpleLocation(Player player) {
        return "X:" + player.getLocation().getBlockX()
                + " Y:" + player.getLocation().getBlockY()
                + " Z:" + player.getLocation().getBlockZ();
    }
}
