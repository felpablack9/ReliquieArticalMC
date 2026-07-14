package it.reliccombat.listeners;

import it.corerelics.relics.Relic;
import it.reliccombat.RelicCombatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {

    private final RelicCombatPlugin plugin;
    private final Map<UUID, Long> respawnProtection;

    public CombatListener(RelicCombatPlugin plugin) {
        this.plugin = plugin;
        this.respawnProtection = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        boolean isPvP = killer != null;
        boolean dropInPvP = plugin.getConfig().getBoolean("settings.drop-relics-on-pvp-death", true);
        boolean dropInPvE = plugin.getConfig().getBoolean("settings.drop-relics-on-pve-death", false);

        if ((!isPvP && !dropInPvE) || (isPvP && !dropInPvP)) return;

        for (ItemStack item : event.getDrops()) {
            Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicFromItem(item);
            if (relic == null) continue;

            relic.setCurrentOwner(null, victim.getName());
            plugin.getCoreRelics().getDataManager().save();

            if (plugin.getConfig().getBoolean("death.relic-death-broadcast", true)) {
                String prefix = plugin.getConfig().getString("messages.prefix", "&6[Combat] &r");

                if (isPvP) {
                    String msg = plugin.getConfig().getString("messages.relic-stolen",
                            "&e⚔ {killer} &6ha rubato &c{relic} &6a &e{victim}!");
                    msg = msg.replace("{killer}", killer.getName())
                            .replace("{relic}", relic.getColoredDisplayName())
                            .replace("{victim}", victim.getName());
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
                } else {
                    String msg = plugin.getConfig().getString("messages.relic-dropped",
                            "&e⚔ {player} &6ha perso &c{relic} &6in combattimento!");
                    msg = msg.replace("{player}", victim.getName())
                            .replace("{relic}", relic.getColoredDisplayName());
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
                }
            }

            if (plugin.getConfig().getBoolean("death.death-particles", true)) {
                victim.getWorld().spawnParticle(Particle.TOTEM, victim.getLocation().add(0, 1, 0), 50, 1, 1, 1);
            }

            if (plugin.getConfig().getBoolean("death.death-sound", true)) {
                victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_TOTEM_USE, 1.5f, 0.8f);
            }

            // Handle bounty claim
            if (isPvP && plugin.getBountyManager().hasBounty(victim.getUniqueId())) {
                int bountyAmount = plugin.getBountyManager().getBounty(victim.getUniqueId());
                plugin.getBountyManager().claimBounty(victim.getUniqueId(), killer);

                String msg = plugin.getConfig().getString("messages.bounty-claimed",
                        "&6&l\uD83D\uDCB0 {player} &6ha riscosso la taglia su &e{target} &6(&f{amount} diamanti&6)!");
                msg = msg.replace("{player}", killer.getName())
                        .replace("{target}", victim.getName())
                        .replace("{amount}", String.valueOf(bountyAmount));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.prefix", "&6[Combat] &r") + msg));
            }
        }

        // Auto-bounty check for killer
        if (isPvP) {
            checkAutoBounty(killer);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        int graceSeconds = plugin.getConfig().getInt("settings.respawn-grace-seconds", 10);
        if (graceSeconds > 0) {
            respawnProtection.put(event.getPlayer().getUniqueId(),
                    System.currentTimeMillis() + (graceSeconds * 1000L));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageGracePeriod(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Long expiry = respawnProtection.get(victim.getUniqueId());
        if (expiry != null && System.currentTimeMillis() < expiry) {
            event.setCancelled(true);
            long remaining = (expiry - System.currentTimeMillis()) / 1000;
            String msg = plugin.getConfig().getString("messages.no-pvp-grace",
                    "&7Sei protetto per ancora {seconds}s dopo il respawn.");
            msg = msg.replace("{seconds}", String.valueOf(remaining));
            ((Player) event.getDamager()).sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    private void checkAutoBounty(Player player) {
        if (!plugin.getConfig().getBoolean("bounty.enabled", true)) return;

        int threshold = plugin.getConfig().getInt("bounty.auto-bounty-threshold", 2);
        int perRelic = plugin.getConfig().getInt("bounty.auto-bounty-per-relic", 5);

        int relicCount = 0;
        for (Relic relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            if (player.getUniqueId().equals(relic.getCurrentOwner())) {
                relicCount++;
            }
        }

        if (relicCount >= threshold && !plugin.getBountyManager().hasBounty(player.getUniqueId())) {
            int amount = relicCount * perRelic;
            plugin.getBountyManager().setBounty(player.getUniqueId(), player.getName(), amount);

            String msg = plugin.getConfig().getString("messages.auto-bounty",
                    "&c⚠ Taglia automatica di &f{amount} &cpiazzata su &e{player} &c(possiede {count} reliquie)!");
            msg = msg.replace("{amount}", String.valueOf(amount))
                    .replace("{player}", player.getName())
                    .replace("{count}", String.valueOf(relicCount));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.prefix", "&6[Combat] &r") + msg));
        }
    }
}
