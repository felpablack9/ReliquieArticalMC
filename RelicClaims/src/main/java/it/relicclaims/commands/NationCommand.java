package it.relicclaims.commands;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Guild;
import it.relicclaims.model.Nation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NationCommand implements CommandExecutor {

    private final RelicClaimsPlugin plugin;

    public NationCommand(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo giocatori.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInviteGuild(player, args);
                break;
            case "ally":
                handleAlly(player, args);
                break;
            case "unally":
                handleUnally(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Nazione - Comandi ===");
        player.sendMessage(ChatColor.YELLOW + "/nation create <nome>" + ChatColor.GRAY + " - Crea una nazione");
        player.sendMessage(ChatColor.YELLOW + "/nation invite <gilda>" + ChatColor.GRAY + " - Invita una gilda");
        player.sendMessage(ChatColor.YELLOW + "/nation ally <nazione>" + ChatColor.GRAY + " - Proponi alleanza");
        player.sendMessage(ChatColor.YELLOW + "/nation unally <nazione>" + ChatColor.GRAY + " - Rompi alleanza");
        player.sendMessage(ChatColor.YELLOW + "/nation info [nome]" + ChatColor.GRAY + " - Info nazione");
        player.sendMessage(ChatColor.YELLOW + "/nation list" + ChatColor.GRAY + " - Lista nazioni");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /nation create <nome>");
            return;
        }

        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (guild == null || !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader di una gilda!");
            return;
        }

        if (guild.getNationId() != null) {
            player.sendMessage(ChatColor.RED + "La tua gilda è già in una nazione!");
            return;
        }

        int cost = plugin.getConfig().getInt("nation.creation-cost", 20);
        if (cost > 0 && !player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
            player.sendMessage(ChatColor.RED + "Servono " + cost + " diamanti per creare una nazione!");
            return;
        }

        String name = args[1];
        Nation nation = plugin.getNationManager().createNation(name, player.getUniqueId());
        if (nation == null) {
            player.sendMessage(ChatColor.RED + "Una nazione con questo nome esiste già!");
            return;
        }

        if (cost > 0) player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));

        nation.addGuild(guild.getId());
        guild.setNationId(nation.getId());
        plugin.getGuildManager().save();
        plugin.getNationManager().save();

        String msg = plugin.getConfig().getString("messages.nation-created", "&aNazione &e{name} &acreata!");
        msg = msg.replace("{name}", name);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleInviteGuild(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /nation invite <gilda_id>");
            return;
        }

        Nation nation = plugin.getNationManager().getPlayerNation(player.getUniqueId());
        if (nation == null || !nation.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader della nazione!");
            return;
        }

        Guild targetGuild = plugin.getGuildManager().getGuildById(args[1]);
        if (targetGuild == null) {
            player.sendMessage(ChatColor.RED + "Gilda non trovata.");
            return;
        }

        if (targetGuild.getNationId() != null) {
            player.sendMessage(ChatColor.RED + "Questa gilda è già in una nazione!");
            return;
        }

        int maxGuilds = plugin.getConfig().getInt("nation.max-guilds", 10);
        if (nation.getGuildCount() >= maxGuilds) {
            player.sendMessage(ChatColor.RED + "La nazione ha raggiunto il massimo di gilde!");
            return;
        }

        nation.addGuild(targetGuild.getId());
        targetGuild.setNationId(nation.getId());
        plugin.getGuildManager().save();
        plugin.getNationManager().save();

        player.sendMessage(ChatColor.GREEN + "Gilda " + targetGuild.getName() + " aggiunta alla nazione!");
        Player guildLeader = Bukkit.getPlayer(targetGuild.getLeader());
        if (guildLeader != null) {
            guildLeader.sendMessage(ChatColor.GREEN + "La tua gilda è entrata nella nazione " + nation.getName() + "!");
        }
    }

    private void handleAlly(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /nation ally <nazione_id>");
            return;
        }

        Nation myNation = plugin.getNationManager().getPlayerNation(player.getUniqueId());
        if (myNation == null || !myNation.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader della nazione!");
            return;
        }

        Nation targetNation = plugin.getNationManager().getNationById(args[1]);
        if (targetNation == null) {
            player.sendMessage(ChatColor.RED + "Nazione non trovata.");
            return;
        }

        int maxAlliances = plugin.getConfig().getInt("alliance.max-alliances", 3);
        if (myNation.getAllianceIds().size() >= maxAlliances) {
            player.sendMessage(ChatColor.RED + "Hai raggiunto il massimo di alleanze!");
            return;
        }

        myNation.addAlliance(targetNation.getId());
        targetNation.addAlliance(myNation.getId());
        plugin.getNationManager().save();

        String msg = plugin.getConfig().getString("messages.alliance-formed", "&aAlleanza formata con &e{nation}!");
        msg = msg.replace("{nation}", targetNation.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleUnally(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /nation unally <nazione_id>");
            return;
        }

        Nation myNation = plugin.getNationManager().getPlayerNation(player.getUniqueId());
        if (myNation == null || !myNation.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader della nazione!");
            return;
        }

        Nation targetNation = plugin.getNationManager().getNationById(args[1]);
        if (targetNation == null) {
            player.sendMessage(ChatColor.RED + "Nazione non trovata.");
            return;
        }

        myNation.removeAlliance(targetNation.getId());
        targetNation.removeAlliance(myNation.getId());
        plugin.getNationManager().save();

        String msg = plugin.getConfig().getString("messages.alliance-broken", "&cAlleanza con &e{nation} &cterminata.");
        msg = msg.replace("{nation}", targetNation.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleInfo(Player player, String[] args) {
        Nation nation;
        if (args.length >= 2) {
            nation = plugin.getNationManager().getNationById(args[1].toLowerCase());
        } else {
            nation = plugin.getNationManager().getPlayerNation(player.getUniqueId());
        }

        if (nation == null) {
            player.sendMessage(ChatColor.RED + "Nazione non trovata.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Nazione: " + nation.getName() + " ===");
        player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(nation.getLeader()).getName());
        player.sendMessage(ChatColor.YELLOW + "Gilde: " + ChatColor.WHITE + nation.getGuildCount());
        player.sendMessage(ChatColor.YELLOW + "Alleanze: " + ChatColor.WHITE + nation.getAllianceIds().size());
    }

    private void handleList(Player player) {
        var nations = plugin.getNationManager().getAllNations();
        player.sendMessage(ChatColor.GOLD + "=== Nazioni (" + nations.size() + ") ===");
        for (Nation nation : nations) {
            player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + nation.getName()
                    + ChatColor.GRAY + " [" + nation.getGuildCount() + " gilde]");
        }
    }
}
