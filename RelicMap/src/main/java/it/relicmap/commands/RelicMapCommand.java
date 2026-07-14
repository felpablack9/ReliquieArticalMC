package it.relicmap.commands;

import it.corerelics.relics.Relic;
import it.relicmap.RelicMapPlugin;
import it.relicmap.data.SightingTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class RelicMapCommand implements CommandExecutor {

    private final RelicMapPlugin plugin;

    public RelicMapCommand(RelicMapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showBoard(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "board":
            case "bacheca":
                showBoard(sender);
                break;
            case "ranking":
            case "classifica":
                showRanking(sender);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /relicmap info <relic_id>");
                } else {
                    showRelicInfo(sender, args[1]);
                }
                break;
            case "tag":
                handleTag(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RelicMap - Comandi ===");
        sender.sendMessage(ChatColor.YELLOW + "/relicmap" + ChatColor.GRAY + " - Mostra la bacheca");
        sender.sendMessage(ChatColor.YELLOW + "/relicmap ranking" + ChatColor.GRAY + " - Classifica possessori");
        sender.sendMessage(ChatColor.YELLOW + "/relicmap info <relic>" + ChatColor.GRAY + " - Info dettagliate reliquia");
        sender.sendMessage(ChatColor.YELLOW + "/relicmap tag <testo>" + ChatColor.GRAY + " - Imposta tag personale");
        sender.sendMessage(ChatColor.YELLOW + "/relicmap tag remove" + ChatColor.GRAY + " - Rimuovi tag");
    }

    private void showBoard(CommandSender sender) {
        String header = plugin.getConfig().getString("messages.board-header",
                "&6&l=== ✦ Bacheca Reliquie ✦ ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        for (Relic relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            UUID owner = relic.getCurrentOwner();

            if (owner != null) {
                Player ownerPlayer = Bukkit.getPlayer(owner);
                String ownerName = ownerPlayer != null ? ownerPlayer.getName() : "Offline";

                String tag = plugin.getSightingTracker().getPlayerTag(owner);
                if (!tag.isEmpty()) {
                    ownerName = ownerName + " " + ChatColor.GRAY + "[" + tag + "]";
                }

                SightingTracker.SightingRecord sighting = plugin.getSightingTracker().getLastSighting(relic.getId());
                String region = sighting != null ? sighting.region : "Sconosciuta";

                String entry = plugin.getConfig().getString("messages.board-entry",
                        "&e{relic} &7→ Possessore: &f{owner} &7| Regione: &f{region}");
                entry = entry.replace("{relic}", relic.getColoredDisplayName())
                        .replace("{owner}", ownerName)
                        .replace("{region}", region);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', entry));
            } else {
                String entry = plugin.getConfig().getString("messages.board-no-owner",
                        "&e{relic} &7→ &cDispersa nel mondo...");
                entry = entry.replace("{relic}", relic.getColoredDisplayName());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', entry));
            }
        }
    }

    private void showRanking(CommandSender sender) {
        Map<UUID, Integer> counts = new HashMap<>();
        Map<UUID, String> names = new HashMap<>();

        for (Relic relic : plugin.getCoreRelics().getRelicRegistry().getAllRelics()) {
            UUID owner = relic.getCurrentOwner();
            if (owner != null) {
                counts.merge(owner, 1, Integer::sum);
                Player p = Bukkit.getPlayer(owner);
                if (p != null) names.put(owner, p.getName());
            }
        }

        String header = plugin.getConfig().getString("messages.ranking-header",
                "&6&l=== Classifica Possessori ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        if (counts.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessun possessore al momento.");
            return;
        }

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int pos = 1;
        for (Map.Entry<UUID, Integer> entry : sorted) {
            String name = names.getOrDefault(entry.getKey(), "Sconosciuto");
            String tag = plugin.getSightingTracker().getPlayerTag(entry.getKey());
            if (!tag.isEmpty()) name = name + " " + ChatColor.GRAY + "[" + tag + "]";

            String line = plugin.getConfig().getString("messages.ranking-entry",
                    "&e{pos}. &f{player} &7- &c{count} reliquie");
            line = line.replace("{pos}", String.valueOf(pos))
                    .replace("{player}", name)
                    .replace("{count}", String.valueOf(entry.getValue()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            pos++;
        }
    }

    private void showRelicInfo(CommandSender sender, String relicId) {
        Relic relic = plugin.getCoreRelics().getRelicRegistry().getRelicById(relicId);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + relicId);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== " + relic.getColoredDisplayName() + ChatColor.GOLD + " ===");

        UUID owner = relic.getCurrentOwner();
        if (owner != null) {
            Player p = Bukkit.getPlayer(owner);
            sender.sendMessage(ChatColor.YELLOW + "Possessore: " + ChatColor.WHITE + (p != null ? p.getName() : "Offline"));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Possessore: " + ChatColor.RED + "Nessuno (dispersa)");
        }

        SightingTracker.SightingRecord sighting = plugin.getSightingTracker().getLastSighting(relicId);
        if (sighting != null) {
            String sightingMsg = plugin.getConfig().getString("messages.sighting-format",
                    "&7Ultimo avvistamento: &f{region} &7({time} fa)");
            sightingMsg = sightingMsg.replace("{region}", sighting.region)
                    .replace("{time}", sighting.getTimeAgo());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sightingMsg));
        }

        sender.sendMessage(ChatColor.YELLOW + "Proprietari totali: " + ChatColor.WHITE + relic.getOwnerHistory().size());
    }

    private void handleTag(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo giocatori possono usare i tag.");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /relicmap tag <testo> oppure /relicmap tag remove");
            return;
        }

        if (args[1].equalsIgnoreCase("remove")) {
            plugin.getSightingTracker().removePlayerTag(player.getUniqueId());
            String msg = plugin.getConfig().getString("messages.tag-removed", "&cTag rimosso.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else {
            String tag = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (tag.length() > 16) {
                sender.sendMessage(ChatColor.RED + "Tag troppo lungo! Massimo 16 caratteri.");
                return;
            }
            plugin.getSightingTracker().setPlayerTag(player.getUniqueId(), tag);
            String msg = plugin.getConfig().getString("messages.tag-set", "&aTag personalizzato impostato: &f{tag}");
            msg = msg.replace("{tag}", tag);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
