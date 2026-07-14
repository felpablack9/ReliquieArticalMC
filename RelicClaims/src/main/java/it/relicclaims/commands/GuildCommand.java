package it.relicclaims.commands;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {

    private final RelicClaimsPlugin plugin;
    private final Map<UUID, String> pendingInvites;

    public GuildCommand(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
        this.pendingInvites = new HashMap<>();
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
                handleInvite(player, args);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Gilda - Comandi ===");
        player.sendMessage(ChatColor.YELLOW + "/guild create <nome>" + ChatColor.GRAY + " - Crea una gilda");
        player.sendMessage(ChatColor.YELLOW + "/guild invite <player>" + ChatColor.GRAY + " - Invita un giocatore");
        player.sendMessage(ChatColor.YELLOW + "/guild accept" + ChatColor.GRAY + " - Accetta un invito");
        player.sendMessage(ChatColor.YELLOW + "/guild leave" + ChatColor.GRAY + " - Lascia la gilda");
        player.sendMessage(ChatColor.YELLOW + "/guild info [nome]" + ChatColor.GRAY + " - Info sulla gilda");
        player.sendMessage(ChatColor.YELLOW + "/guild list" + ChatColor.GRAY + " - Lista gilde");
        player.sendMessage(ChatColor.YELLOW + "/guild disband" + ChatColor.GRAY + " - Sciogli la gilda");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /guild create <nome>");
            return;
        }

        if (plugin.getGuildManager().getPlayerGuild(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Sei già in una gilda!");
            return;
        }

        int cost = plugin.getConfig().getInt("guild.creation-cost", 5);
        if (cost > 0 && !player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
            player.sendMessage(ChatColor.RED + "Servono " + cost + " diamanti per creare una gilda!");
            return;
        }

        String name = args[1];
        Guild guild = plugin.getGuildManager().createGuild(name, player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "Una gilda con questo nome esiste già!");
            return;
        }

        if (cost > 0) {
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));
        }

        String msg = plugin.getConfig().getString("messages.guild-created", "&aGilda &e{name} &acreata!");
        msg = msg.replace("{name}", name);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /guild invite <player>");
            return;
        }

        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (guild == null || !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader di una gilda!");
            return;
        }

        int maxMembers = plugin.getConfig().getInt("guild.max-members", 20);
        if (guild.getMemberCount() >= maxMembers) {
            player.sendMessage(ChatColor.RED + "La gilda è piena!");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Giocatore non trovato.");
            return;
        }

        pendingInvites.put(target.getUniqueId(), guild.getId());
        String msg = plugin.getConfig().getString("messages.guild-invited",
                "&eSei stato invitato nella gilda &f{name}&e. Usa /guild accept");
        msg = msg.replace("{name}", guild.getName());
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        player.sendMessage(ChatColor.GREEN + "Invito inviato a " + target.getName());
    }

    private void handleAccept(Player player) {
        String guildId = pendingInvites.remove(player.getUniqueId());
        if (guildId == null) {
            player.sendMessage(ChatColor.RED + "Nessun invito pendente.");
            return;
        }

        if (plugin.getGuildManager().getPlayerGuild(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Sei già in una gilda! Lasciala prima.");
            return;
        }

        Guild guild = plugin.getGuildManager().getGuildById(guildId);
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "La gilda non esiste più.");
            return;
        }

        guild.addMember(player.getUniqueId());
        plugin.getGuildManager().save();

        String msg = plugin.getConfig().getString("messages.guild-joined", "&a{player} è entrato nella gilda!");
        msg = msg.replace("{player}", player.getName());
        for (UUID member : guild.getMembers()) {
            Player p = Bukkit.getPlayer(member);
            if (p != null) p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    private void handleLeave(Player player) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatColor.RED + "Non sei in una gilda.");
            return;
        }

        if (guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Sei il leader! Usa /guild disband per sciogliere la gilda.");
            return;
        }

        guild.removeMember(player.getUniqueId());
        plugin.getGuildManager().save();

        String msg = plugin.getConfig().getString("messages.guild-left", "&c{player} ha lasciato la gilda.");
        msg = msg.replace("{player}", player.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleInfo(Player player, String[] args) {
        Guild guild;
        if (args.length >= 2) {
            guild = plugin.getGuildManager().getGuildById(args[1].toLowerCase());
        } else {
            guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        }

        if (guild == null) {
            player.sendMessage(ChatColor.RED + "Gilda non trovata.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Gilda: " + guild.getName() + " ===");
        player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(guild.getLeader()).getName());
        player.sendMessage(ChatColor.YELLOW + "Membri: " + ChatColor.WHITE + guild.getMemberCount());
        player.sendMessage(ChatColor.YELLOW + "Claim: " + ChatColor.WHITE + plugin.getClaimManager().getGuildClaims(guild.getId()).size());
        if (guild.getNationId() != null) {
            var nation = plugin.getNationManager().getNationById(guild.getNationId());
            if (nation != null) {
                player.sendMessage(ChatColor.YELLOW + "Nazione: " + ChatColor.WHITE + nation.getName());
            }
        }
    }

    private void handleList(Player player) {
        var guilds = plugin.getGuildManager().getAllGuilds();
        player.sendMessage(ChatColor.GOLD + "=== Gilde (" + guilds.size() + ") ===");
        for (Guild guild : guilds) {
            player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + guild.getName()
                    + ChatColor.GRAY + " [" + guild.getMemberCount() + " membri]");
        }
    }

    private void handleDisband(Player player) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (guild == null || !guild.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Devi essere il leader della gilda!");
            return;
        }

        plugin.getGuildManager().deleteGuild(guild.getId());
        player.sendMessage(ChatColor.RED + "Gilda " + guild.getName() + " sciolta.");
    }
}
