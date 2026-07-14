package it.relicseason.commands;

import it.relicseason.RelicSeasonPlugin;
import it.relicseason.data.HallOfFame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SeasonCommand implements CommandExecutor {

    private final RelicSeasonPlugin plugin;

    public SeasonCommand(RelicSeasonPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showSeasonInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showSeasonInfo(sender);
                break;
            case "leaderboard":
            case "classifica":
                showLeaderboard(sender);
                break;
            case "halloffame":
            case "hof":
                showHallOfFame(sender);
                break;
            case "end":
                if (sender.hasPermission("relicseason.admin")) {
                    plugin.endSeason();
                } else {
                    sender.sendMessage(ChatColor.RED + "Non hai il permesso.");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Season - Comandi ===");
        sender.sendMessage(ChatColor.YELLOW + "/season info" + ChatColor.GRAY + " - Info stagione corrente");
        sender.sendMessage(ChatColor.YELLOW + "/season leaderboard" + ChatColor.GRAY + " - Classifica");
        sender.sendMessage(ChatColor.YELLOW + "/season hof" + ChatColor.GRAY + " - Hall of Fame");
        if (sender.hasPermission("relicseason.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/season end" + ChatColor.GRAY + " - Forza fine stagione");
        }
    }

    private void showSeasonInfo(CommandSender sender) {
        String seasonName = plugin.getConfig().getString("settings.current-season-name", "Stagione 1");
        int totalDays = plugin.getConfig().getInt("settings.season-duration-days", 90);
        int currentDay = plugin.getCurrentDay();
        int remaining = plugin.getRemainingDays();

        String msg = plugin.getConfig().getString("messages.season-info",
                "&6Stagione: &f{name} &7| Giorno &f{day}/{total} &7| Fine tra &f{remaining} giorni");
        msg = msg.replace("{name}", seasonName)
                .replace("{day}", String.valueOf(currentDay))
                .replace("{total}", String.valueOf(totalDays))
                .replace("{remaining}", String.valueOf(remaining));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void showLeaderboard(CommandSender sender) {
        String header = plugin.getConfig().getString("messages.leaderboard-header", "&6=== Classifica Stagionale ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<Map.Entry<UUID, Integer>> sorted = plugin.getScoreManager().getSortedScores();

        if (sorted.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessun punteggio registrato.");
            return;
        }

        int pos = 1;
        for (Map.Entry<UUID, Integer> entry : sorted) {
            if (pos > 10) break;

            String name = plugin.getScoreManager().getPlayerName(entry.getKey());
            int relicCount = 0;
            for (var relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
                if (entry.getKey().equals(relic.getCurrentOwner())) relicCount++;
            }

            String line = plugin.getConfig().getString("messages.leaderboard-entry",
                    "&e{pos}. &f{player} &7- &c{points} punti &7({relics} reliquie)");
            line = line.replace("{pos}", String.valueOf(pos))
                    .replace("{player}", name)
                    .replace("{points}", String.valueOf(entry.getValue()))
                    .replace("{relics}", String.valueOf(relicCount));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            pos++;
        }
    }

    private void showHallOfFame(CommandSender sender) {
        String header = plugin.getConfig().getString("messages.hall-of-fame-header",
                "&6&l=== ✦ Hall of Fame ✦ ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        List<HallOfFame.HallEntry> entries = plugin.getHallOfFame().getEntries();

        if (entries.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessuna stagione completata ancora.");
            return;
        }

        for (HallOfFame.HallEntry entry : entries) {
            String line = plugin.getConfig().getString("messages.hall-of-fame-entry",
                    "&eStagione {season}: &f{winner} &7- &c{points} punti");
            line = line.replace("{season}", entry.seasonName)
                    .replace("{winner}", entry.winner)
                    .replace("{points}", String.valueOf(entry.points));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
}
