package it.relicclaims.commands;

import it.relicclaims.RelicClaimsPlugin;
import it.relicclaims.model.Claim;
import it.relicclaims.model.Guild;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClaimCommand implements CommandExecutor {

    private final RelicClaimsPlugin plugin;

    public ClaimCommand(RelicClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Solo giocatori possono usare questo comando.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showInfo(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
            case "claim":
                handleClaim(player);
                break;
            case "remove":
            case "unclaim":
                handleUnclaim(player);
                break;
            case "info":
                showInfo(player);
                break;
            case "list":
                showList(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Claim - Comandi ===");
        player.sendMessage(ChatColor.YELLOW + "/claim add" + ChatColor.GRAY + " - Claima il chunk attuale");
        player.sendMessage(ChatColor.YELLOW + "/claim remove" + ChatColor.GRAY + " - Rimuovi il claim");
        player.sendMessage(ChatColor.YELLOW + "/claim info" + ChatColor.GRAY + " - Info sul chunk attuale");
        player.sendMessage(ChatColor.YELLOW + "/claim list" + ChatColor.GRAY + " - Lista i tuoi claim");
    }

    private void handleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        Claim existing = plugin.getClaimManager().getClaimAt(chunk);
        if (existing != null) {
            String msg = plugin.getConfig().getString("messages.claim-fail-owned", "&cQuesto terreno è già claimato!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return;
        }

        int maxClaims = plugin.getConfig().getInt("settings.max-claims-per-player", 5);
        if (plugin.getClaimManager().getPlayerClaimCount(player.getUniqueId()) >= maxClaims) {
            String msg = plugin.getConfig().getString("messages.claim-fail-max", "&cHai raggiunto il massimo di claim!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return;
        }

        int cost = plugin.getConfig().getInt("settings.claim-cost", 3);
        if (cost > 0) {
            if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), cost)) {
                String msg = plugin.getConfig().getString("messages.claim-fail-cost",
                        "&cNon hai abbastanza diamanti! Costo: {cost}");
                msg = msg.replace("{cost}", String.valueOf(cost));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, cost));
        }

        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        String guildId = guild != null ? guild.getId() : null;

        plugin.getClaimManager().createClaim(chunk, player.getUniqueId(), guildId);

        String msg = plugin.getConfig().getString("messages.claim-success", "&aTerreno claimato con successo!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void handleUnclaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = plugin.getClaimManager().getClaimAt(chunk);

        if (claim == null || !claim.isOwnedBy(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Non possiedi questo terreno.");
            return;
        }

        plugin.getClaimManager().removeClaim(chunk);
        String msg = plugin.getConfig().getString("messages.claim-removed", "&aClaim rimosso.");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void showInfo(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        Claim claim = plugin.getClaimManager().getClaimAt(chunk);

        player.sendMessage(ChatColor.GOLD + "=== Info Chunk ===");
        player.sendMessage(ChatColor.YELLOW + "Chunk: " + ChatColor.WHITE + chunk.getX() + ", " + chunk.getZ());

        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "Stato: " + ChatColor.GREEN + "Libero");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Stato: " + ChatColor.RED + "Claimato");
            org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(claim.getOwner());
            player.sendMessage(ChatColor.YELLOW + "Proprietario: " + ChatColor.WHITE + owner.getName());
            if (claim.getGuildId() != null) {
                Guild guild = plugin.getGuildManager().getGuildById(claim.getGuildId());
                if (guild != null) {
                    player.sendMessage(ChatColor.YELLOW + "Gilda: " + ChatColor.WHITE + guild.getName());
                }
            }
        }
    }

    private void showList(Player player) {
        var claims = plugin.getClaimManager().getPlayerClaims(player.getUniqueId());
        player.sendMessage(ChatColor.GOLD + "=== I tuoi Claim (" + claims.size() + ") ===");
        for (Claim claim : claims) {
            player.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE
                    + claim.getWorld() + " [" + claim.getChunkX() + ", " + claim.getChunkZ() + "]");
        }
    }
}
