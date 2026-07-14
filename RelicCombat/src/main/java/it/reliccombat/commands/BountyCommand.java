package it.reliccombat.commands;

import it.reliccombat.RelicCombatPlugin;
import it.reliccombat.bounty.BountyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final RelicCombatPlugin plugin;

    public BountyCommand(RelicCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showBountyList(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                showBountyList(sender);
                break;
            case "place":
            case "set":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Solo giocatori.");
                    return true;
                }
                handlePlace((Player) sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.GOLD + "=== Taglie ===");
                sender.sendMessage(ChatColor.YELLOW + "/bounty list" + ChatColor.GRAY + " - Lista taglie");
                sender.sendMessage(ChatColor.YELLOW + "/bounty place <player> <diamanti>" + ChatColor.GRAY + " - Piazza taglia");
                break;
        }
        return true;
    }

    private void showBountyList(CommandSender sender) {
        String header = plugin.getConfig().getString("messages.bounty-list-header", "&6=== Taglie Attive ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<Map.Entry<UUID, BountyManager.BountyEntry>> sorted = plugin.getBountyManager().getSortedBounties();

        if (sorted.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessuna taglia attiva.");
            return;
        }

        int pos = 1;
        for (Map.Entry<UUID, BountyManager.BountyEntry> entry : sorted) {
            String line = plugin.getConfig().getString("messages.bounty-list-entry",
                    "&e{pos}. &f{player} &7- &c{amount} diamanti");
            line = line.replace("{pos}", String.valueOf(pos))
                    .replace("{player}", entry.getValue().playerName)
                    .replace("{amount}", String.valueOf(entry.getValue().amount));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            pos++;
        }
    }

    private void handlePlace(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /bounty place <player> <diamanti>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato.");
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "Non puoi mettere una taglia su te stesso!");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Inserisci un numero valido.");
            return;
        }

        int min = plugin.getConfig().getInt("bounty.min-bounty", 1);
        int max = plugin.getConfig().getInt("bounty.max-bounty", 64);

        if (amount < min || amount > max) {
            sender.sendMessage(ChatColor.RED + "La taglia deve essere tra " + min + " e " + max + " diamanti.");
            return;
        }

        if (!sender.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), amount)) {
            sender.sendMessage(ChatColor.RED + "Non hai abbastanza diamanti!");
            return;
        }

        sender.getInventory().removeItem(new ItemStack(Material.DIAMOND, amount));
        plugin.getBountyManager().setBounty(target.getUniqueId(), target.getName(), amount);

        String msg = plugin.getConfig().getString("messages.bounty-placed",
                "&aTaglia di &f{amount} diamanti &apiazzata su &e{target}!");
        msg = msg.replace("{amount}", String.valueOf(amount))
                .replace("{target}", target.getName());

        if (plugin.getConfig().getBoolean("bounty.broadcast-bounty-claim", true)) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.prefix", "&6[Combat] &r") + msg));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
