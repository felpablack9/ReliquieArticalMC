package it.corerelics.commands;

import it.corerelics.CoreRelicsPlugin;
import it.corerelics.relics.OwnerRecord;
import it.corerelics.relics.Relic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RelicCommand implements CommandExecutor {

    private final CoreRelicsPlugin plugin;

    public RelicCommand(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                handleList(sender);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "history":
                handleHistory(sender, args);
                break;
            case "fame":
                handleFame(sender);
                break;
            case "spawn":
                handleSpawn(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== CoreRelics - Comandi ===");
        sender.sendMessage(ChatColor.YELLOW + "/relic list" + ChatColor.GRAY + " - Lista tutte le reliquie");
        sender.sendMessage(ChatColor.YELLOW + "/relic give <player> <relic>" + ChatColor.GRAY + " - Dai una reliquia");
        sender.sendMessage(ChatColor.YELLOW + "/relic remove <player> <relic>" + ChatColor.GRAY + " - Rimuovi una reliquia");
        sender.sendMessage(ChatColor.YELLOW + "/relic spawn <relic>" + ChatColor.GRAY + " - Spawna una reliquia a terra");
        sender.sendMessage(ChatColor.YELLOW + "/relic info <relic>" + ChatColor.GRAY + " - Info su una reliquia");
        sender.sendMessage(ChatColor.YELLOW + "/relic history <relic>" + ChatColor.GRAY + " - Storico proprietari");
        sender.sendMessage(ChatColor.YELLOW + "/relic fame" + ChatColor.GRAY + " - Classifica reliquie");
        sender.sendMessage(ChatColor.YELLOW + "/relic reload" + ChatColor.GRAY + " - Ricarica configurazione");
    }

    private void handleList(CommandSender sender) {
        Collection<Relic> relics = plugin.getRelicRegistry().getAllRelics();

        sender.sendMessage(ChatColor.GOLD + "=== Reliquie Registrate (" + relics.size() + ") ===");

        for (Relic relic : relics) {
            String ownerName = "Nessuno";
            if (relic.getCurrentOwner() != null) {
                Player owner = Bukkit.getPlayer(relic.getCurrentOwner());
                ownerName = owner != null ? owner.getName() : "Offline";
            }

            sender.sendMessage(ChatColor.YELLOW + "• " + relic.getColoredDisplayName()
                    + ChatColor.GRAY + " [" + relic.getId() + "] "
                    + ChatColor.WHITE + "→ " + ownerName);
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /relic give <player> <relic_id>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato: " + args[1]);
            return;
        }

        Relic relic = plugin.getRelicRegistry().getRelicById(args[2]);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + args[2]);
            return;
        }

        if (relic.getCurrentOwner() != null) {
            Player currentOwner = Bukkit.getPlayer(relic.getCurrentOwner());
            String ownerName = currentOwner != null ? currentOwner.getName() : "un giocatore offline";
            sender.sendMessage(ChatColor.RED + "Questa reliquia è già posseduta da " + ownerName + "!");
            sender.sendMessage(ChatColor.GRAY + "Usa /relic remove prima di darla a qualcun altro.");
            return;
        }

        ItemStack relicItem = relic.createItemStack();
        target.getInventory().addItem(relicItem);
        relic.setCurrentOwner(target.getUniqueId(), target.getName());

        sender.sendMessage(ChatColor.GREEN + "Reliquia " + relic.getColoredDisplayName()
                + ChatColor.GREEN + " data a " + target.getName());
        target.sendMessage(ChatColor.GOLD + "Hai ricevuto la reliquia: " + relic.getColoredDisplayName());

        plugin.getDataManager().save();
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /relic remove <player> <relic_id>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato: " + args[1]);
            return;
        }

        Relic relic = plugin.getRelicRegistry().getRelicById(args[2]);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + args[2]);
            return;
        }

        // Remove the relic item from player inventory
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && plugin.getRelicRegistry().getRelicFromItem(item) != null) {
                Relic itemRelic = plugin.getRelicRegistry().getRelicFromItem(item);
                if (itemRelic.getId().equals(relic.getId())) {
                    target.getInventory().remove(item);
                    break;
                }
            }
        }

        relic.setCurrentOwner(null, target.getName());
        sender.sendMessage(ChatColor.GREEN + "Reliquia " + relic.getColoredDisplayName()
                + ChatColor.GREEN + " rimossa da " + target.getName());

        plugin.getDataManager().save();
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /relic info <relic_id>");
            return;
        }

        Relic relic = plugin.getRelicRegistry().getRelicById(args[1]);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + args[1]);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Info Reliquia ===");
        sender.sendMessage(ChatColor.YELLOW + "Nome: " + relic.getColoredDisplayName());
        sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + relic.getId());
        sender.sendMessage(ChatColor.YELLOW + "Materiale: " + ChatColor.WHITE + relic.getMaterial().name());
        sender.sendMessage(ChatColor.YELLOW + "Abilità: " + ChatColor.WHITE + relic.getAbilityType().name());
        sender.sendMessage(ChatColor.YELLOW + "Cooldown: " + ChatColor.WHITE + relic.getAbilityCooldown() + "s");

        String ownerName = "Nessuno (libera)";
        if (relic.getCurrentOwner() != null) {
            Player owner = Bukkit.getPlayer(relic.getCurrentOwner());
            ownerName = owner != null ? owner.getName() : "Giocatore Offline";
        }
        sender.sendMessage(ChatColor.YELLOW + "Possessore: " + ChatColor.WHITE + ownerName);
        sender.sendMessage(ChatColor.YELLOW + "Proprietari totali: " + ChatColor.WHITE + relic.getOwnerHistory().size());
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /relic history <relic_id>");
            return;
        }

        Relic relic = plugin.getRelicRegistry().getRelicById(args[1]);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + args[1]);
            return;
        }

        List<OwnerRecord> history = relic.getOwnerHistory();

        sender.sendMessage(ChatColor.GOLD + "=== Storico: " + relic.getColoredDisplayName() + ChatColor.GOLD + " ===");

        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessuno storico disponibile.");
            return;
        }

        for (int i = 0; i < history.size(); i++) {
            OwnerRecord record = history.get(i);
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". "
                    + ChatColor.WHITE + record.getPlayerName()
                    + ChatColor.GRAY + " - " + record.getFormattedDate());
        }
    }

    private void handleFame(CommandSender sender) {
        Map<UUID, Integer> relicCounts = new HashMap<>();
        Map<UUID, String> playerNames = new HashMap<>();

        for (Relic relic : plugin.getRelicRegistry().getAllRelics()) {
            if (relic.getCurrentOwner() != null) {
                relicCounts.merge(relic.getCurrentOwner(), 1, Integer::sum);
                Player owner = Bukkit.getPlayer(relic.getCurrentOwner());
                if (owner != null) {
                    playerNames.put(relic.getCurrentOwner(), owner.getName());
                }
            }
        }

        String header = plugin.getConfig().getString("messages.fame-header", "&6=== Classifica Reliquie ===");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        if (relicCounts.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Nessun giocatore possiede reliquie al momento.");
            return;
        }

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(relicCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String entryFormat = plugin.getConfig().getString("messages.fame-entry",
                "&e{position}. &f{player} &7- &c{count} reliquie");

        int position = 1;
        for (Map.Entry<UUID, Integer> entry : sorted) {
            String name = playerNames.getOrDefault(entry.getKey(), "Sconosciuto");
            String msg = entryFormat
                    .replace("{position}", String.valueOf(position))
                    .replace("{player}", name)
                    .replace("{count}", String.valueOf(entry.getValue()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            position++;
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Questo comando può essere usato solo da un giocatore.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /relic spawn <relic_id>");
            return;
        }

        Relic relic = plugin.getRelicRegistry().getRelicById(args[1]);
        if (relic == null) {
            sender.sendMessage(ChatColor.RED + "Reliquia non trovata: " + args[1]);
            return;
        }

        if (relic.getCurrentOwner() != null) {
            sender.sendMessage(ChatColor.RED + "Questa reliquia è già nel mondo!");
            return;
        }

        Player player = (Player) sender;
        player.getWorld().dropItemNaturally(player.getLocation(), relic.createItemStack());

        sender.sendMessage(ChatColor.GREEN + "Reliquia " + relic.getColoredDisplayName()
                + ChatColor.GREEN + " spawnata nella tua posizione!");
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getRelicRegistry().loadRelicsFromConfig();
        sender.sendMessage(ChatColor.GREEN + "Configurazione ricaricata!");
    }
}
