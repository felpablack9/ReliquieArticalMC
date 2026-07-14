package it.reliccombat.commands;

import it.reliccombat.RelicCombatPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CombatCommand implements CommandExecutor {

    private final RelicCombatPlugin plugin;

    public CombatCommand(RelicCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo giocatori.");
            return true;
        }

        Player player = (Player) sender;
        boolean tagged = plugin.getCombatTagListener().isTagged(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "=== Stato Combattimento ===");
        player.sendMessage(ChatColor.YELLOW + "Combat Tag: " +
                (tagged ? ChatColor.RED + "ATTIVO (" + plugin.getCombatTagListener().getRemainingSeconds(player.getUniqueId()) + "s)"
                        : ChatColor.GREEN + "Inattivo"));
        player.sendMessage(ChatColor.YELLOW + "Taglia su di te: " +
                (plugin.getBountyManager().hasBounty(player.getUniqueId()) ?
                        ChatColor.RED + "" + plugin.getBountyManager().getBounty(player.getUniqueId()) + " diamanti"
                        : ChatColor.GREEN + "Nessuna"));

        int relicCount = 0;
        for (var relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            if (player.getUniqueId().equals(relic.getCurrentOwner())) relicCount++;
        }
        player.sendMessage(ChatColor.YELLOW + "Reliquie possedute: " + ChatColor.WHITE + relicCount);

        return true;
    }
}
