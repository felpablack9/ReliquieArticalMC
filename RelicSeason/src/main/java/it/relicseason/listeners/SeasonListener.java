package it.relicseason.listeners;

import it.corerelics.relics.Relic;
import it.relicseason.RelicSeasonPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class SeasonListener implements Listener {

    private final RelicSeasonPlugin plugin;

    public SeasonListener(RelicSeasonPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onRelicPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item);
        if (relic != null) {
            int points = plugin.getConfig().getInt("scoring.points-per-relic-steal", 15);
            plugin.getScoreManager().addPoints(player.getUniqueId(), points, "recupero reliquia");

            // Check all-relics bonus
            checkAllRelicsBonus(player);
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "relic_boss");
        if (event.getEntity().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            int points = plugin.getConfig().getInt("scoring.points-per-boss-kill", 25);
            plugin.getScoreManager().addPoints(killer.getUniqueId(), points, "boss ucciso");
        }
    }

    private void checkAllRelicsBonus(Player player) {
        int totalRelics = plugin.getCoreRelics().getRelicRegistry().getRelicCount();
        int ownedRelics = 0;

        for (Relic relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            if (player.getUniqueId().equals(relic.getCurrentOwner())) {
                ownedRelics++;
            }
        }

        if (ownedRelics >= totalRelics && totalRelics > 0) {
            int bonus = plugin.getConfig().getInt("scoring.all-relics-bonus", 500);
            plugin.getScoreManager().addPoints(player.getUniqueId(), bonus, "tutte le reliquie possedute!");
        }
    }
}
