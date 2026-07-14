package it.reliclore.commands;

import it.corerelics.relics.Relic;
import it.reliclore.RelicLorePlugin;
import it.reliclore.data.ChronicleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class LoreCommand implements CommandExecutor {

    private final RelicLorePlugin plugin;

    public LoreCommand(RelicLorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showChronicle(sender, 20);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "chronicle":
            case "cronaca":
                int count = args.length >= 2 ? parseIntSafe(args[1], 20) : 20;
                showChronicle(sender, count);
                break;
            case "relic":
            case "reliquia":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lore relic <relic_id>");
                } else {
                    showRelicStory(sender, args[1]);
                }
                break;
            case "player":
            case "giocatore":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lore player <nome>");
                } else {
                    showPlayerStory(sender, args[1]);
                }
                break;
            case "book":
            case "libro":
                handleBook(sender);
                break;
            case "record":
                if (sender.hasPermission("reliclore.admin") && args.length >= 3) {
                    handleManualRecord(sender, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "Uso: /lore record <tipo> <messaggio>");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Lore - Comandi ===");
        sender.sendMessage(ChatColor.YELLOW + "/lore" + ChatColor.GRAY + " - Mostra ultimi eventi");
        sender.sendMessage(ChatColor.YELLOW + "/lore relic <id>" + ChatColor.GRAY + " - Storia di una reliquia");
        sender.sendMessage(ChatColor.YELLOW + "/lore player <nome>" + ChatColor.GRAY + " - Gesta di un giocatore");
        sender.sendMessage(ChatColor.YELLOW + "/lore book" + ChatColor.GRAY + " - Ottieni il libro della cronaca");
        if (sender.hasPermission("reliclore.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/lore record <tipo> <msg>" + ChatColor.GRAY + " - Registra evento manuale");
        }
    }

    private void showChronicle(CommandSender sender, int count) {
        String header = plugin.getConfig().getString("messages.chronicle-header",
                "&6&l=== ✦ Cronaca del Server ✦ ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<ChronicleManager.ChronicleEntry> recent = plugin.getChronicleManager().getRecentEntries(count);

        if (recent.isEmpty()) {
            String noEntries = plugin.getConfig().getString("messages.no-entries", "&7Nessun evento registrato.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noEntries));
            return;
        }

        String entryFormat = plugin.getConfig().getString("messages.chronicle-entry", "&7[{date}] {event}");
        for (ChronicleManager.ChronicleEntry entry : recent) {
            String line = entryFormat.replace("{date}", entry.getFormattedDate())
                    .replace("{event}", entry.message);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private void showRelicStory(CommandSender sender, String relicId) {
        Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicById(relicId);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata.");
            return;
        }

        String header = plugin.getConfig().getString("messages.relic-story-header",
                "&6=== Storia di {relic} ===");
        header = header.replace("{relic}", relic.getColoredDisplayName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<ChronicleManager.ChronicleEntry> entries = plugin.getChronicleManager().getEntriesByRelic(relicId);

        if (entries.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessun evento registrato per questa reliquia.");
            return;
        }

        for (ChronicleManager.ChronicleEntry entry : entries) {
            String line = ChatColor.GRAY + "[" + entry.getFormattedDate() + "] "
                    + ChatColor.translateAlternateColorCodes('&', entry.message);
            sender.sendMessage(line);
        }
    }

    private void showPlayerStory(CommandSender sender, String playerName) {
        String header = plugin.getConfig().getString("messages.player-story-header",
                "&6=== Gesta di {player} ===");
        header = header.replace("{player}", playerName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<ChronicleManager.ChronicleEntry> entries = plugin.getChronicleManager().getEntriesByPlayer(playerName);

        if (entries.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessun evento registrato per questo giocatore.");
            return;
        }

        for (ChronicleManager.ChronicleEntry entry : entries) {
            String line = ChatColor.GRAY + "[" + entry.getFormattedDate() + "] "
                    + ChatColor.translateAlternateColorCodes('&', entry.message);
            sender.sendMessage(line);
        }
    }

    private void handleBook(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo giocatori possono ottenere il libro.");
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.enable-lore-book", true)) {
            sender.sendMessage(ChatColor.RED + "Il libro della cronaca è disabilitato.");
            return;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(plugin.getChronicleManager().createLoreBook());
        sender.sendMessage(ChatColor.GOLD + "Hai ricevuto il libro della Cronaca del Mondo!");
    }

    private void handleManualRecord(CommandSender sender, String[] args) {
        String type = args[1];
        StringBuilder message = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) message.append(" ");
            message.append(args[i]);
        }

        plugin.getChronicleManager().recordEvent(type, message.toString(), sender.getName(), null);

        String msg = plugin.getConfig().getString("messages.event-recorded", "&7Evento registrato nella cronaca.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private int parseIntSafe(String s, int defaultValue) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultValue; }
    }
}
