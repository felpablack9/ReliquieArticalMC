package it.reliccombat.listeners;

import it.reliccombat.RelicCombatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTagListener implements Listener {

    private final RelicCombatPlugin plugin;
    private final Map<UUID, Long> combatTags;

    public CombatTagListener(RelicCombatPlugin plugin) {
        this.plugin = plugin;
        this.combatTags = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvPHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        int tagSeconds = plugin.getConfig().getInt("settings.combat-tag-seconds", 15);
        long expiry = System.currentTimeMillis() + (tagSeconds * 1000L);

        boolean wasTagged1 = isTagged(victim.getUniqueId());
        boolean wasTagged2 = isTagged(attacker.getUniqueId());

        combatTags.put(victim.getUniqueId(), expiry);
        combatTags.put(attacker.getUniqueId(), expiry);

        String msg = plugin.getConfig().getString("messages.combat-tagged",
                "&c⚔ Sei in combattimento! Non disconnetterti per {seconds}s");
        msg = msg.replace("{seconds}", String.valueOf(tagSeconds));
        String colored = ChatColor.translateAlternateColorCodes('&', msg);

        if (!wasTagged1) victim.sendMessage(colored);
        if (!wasTagged2) attacker.sendMessage(colored);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!isTagged(player.getUniqueId())) return;

        String punishment = plugin.getConfig().getString("settings.combat-log-punishment", "KILL");

        String msg = plugin.getConfig().getString("messages.combat-log-punished",
                "&c{player} si è disconnesso durante il combattimento!");
        msg = msg.replace("{player}", player.getName());
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));

        switch (punishment.toUpperCase()) {
            case "KILL":
                player.setHealth(0);
                break;
            case "DROP_INVENTORY":
                for (var item : player.getInventory().getContents()) {
                    if (item != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                player.getInventory().clear();
                break;
            case "DROP_RELICS":
                for (var item : player.getInventory().getContents()) {
                    if (item != null && plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item) != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        player.getInventory().remove(item);
                    }
                }
                break;
            default:
                break;
        }

        combatTags.remove(player.getUniqueId());
    }

    public boolean isTagged(UUID uuid) {
        Long expiry = combatTags.get(uuid);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            combatTags.remove(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(UUID uuid) {
        Long expiry = combatTags.get(uuid);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }
}
