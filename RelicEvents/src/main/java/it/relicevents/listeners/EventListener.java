package it.relicevents.listeners;

import it.corerelics.relics.Relic;
import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.RelicEvent;
import it.relicevents.events.types.BossEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class EventListener implements Listener {

    private final RelicEventsPlugin plugin;

    public EventListener(RelicEventsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        NamespacedKey key = new NamespacedKey(plugin, "relic_boss");
        String bossTag = entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (bossTag == null) return;

        Player killer = entity.getKiller();
        if (killer == null) return;

        // Drop a relic from the boss
        Collection<Relic> relics = plugin.getCoreRelics().getRelicRegistry().getAllRelics();
        Relic droppedRelic = null;

        for (Relic relic : relics) {
            if (relic.getCurrentOwner() == null) {
                droppedRelic = relic;
                break;
            }
        }

        if (droppedRelic != null) {
            ItemStack relicItem = droppedRelic.createItemStack();
            entity.getWorld().dropItemNaturally(entity.getLocation(), relicItem);

            String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
            String msg = plugin.getConfig().getString("messages.boss-killed",
                    "&a{player} &2ha sconfitto &c{boss} &2e ha ottenuto una reliquia!");
            msg = msg.replace("{player}", killer.getName())
                    .replace("{boss}", ChatColor.stripColor(entity.getCustomName()));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
        }

        // Check if this was the main event boss
        RelicEvent activeEvent = plugin.getEventManager().getActiveEvent();
        if (activeEvent instanceof BossEvent) {
            BossEvent bossEvent = (BossEvent) activeEvent;
            if (entity.equals(bossEvent.getBoss())) {
                bossEvent.setActive(false);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getEventManager().endActiveEvent();
                }, 100L); // End event 5 seconds after boss death
            }
        }
    }
}
