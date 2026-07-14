package it.relicclaims;

import it.relicclaims.commands.ClaimCommand;
import it.relicclaims.commands.GuildCommand;
import it.relicclaims.commands.NationCommand;
import it.relicclaims.data.ClaimManager;
import it.relicclaims.data.GuildManager;
import it.relicclaims.data.NationManager;
import it.relicclaims.listeners.ProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicClaimsPlugin extends JavaPlugin {

    private static RelicClaimsPlugin instance;
    private ClaimManager claimManager;
    private GuildManager guildManager;
    private NationManager nationManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        claimManager = new ClaimManager(this);
        guildManager = new GuildManager(this);
        nationManager = new NationManager(this);

        claimManager.load();
        guildManager.load();
        nationManager.load();

        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("nation").setExecutor(new NationCommand(this));

        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        getLogger().info("RelicClaims attivato! Claim: " + claimManager.getClaimCount()
                + ", Gilde: " + guildManager.getGuildCount()
                + ", Nazioni: " + nationManager.getNationCount());
    }

    @Override
    public void onDisable() {
        if (claimManager != null) claimManager.save();
        if (guildManager != null) guildManager.save();
        if (nationManager != null) nationManager.save();
        getLogger().info("RelicClaims disattivato!");
    }

    public static RelicClaimsPlugin getInstance() { return instance; }
    public ClaimManager getClaimManager() { return claimManager; }
    public GuildManager getGuildManager() { return guildManager; }
    public NationManager getNationManager() { return nationManager; }
}
