package it.reliccombat;

import it.corerelics.CoreRelicsPlugin;
import it.reliccombat.bounty.BountyManager;
import it.reliccombat.commands.BountyCommand;
import it.reliccombat.commands.CombatCommand;
import it.reliccombat.listeners.CombatListener;
import it.reliccombat.listeners.CombatTagListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicCombatPlugin extends JavaPlugin {

    private static RelicCombatPlugin instance;
    private CoreRelicsPlugin coreRelics;
    private CombatTagListener combatTagListener;
    private BountyManager bountyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        coreRelics = (CoreRelicsPlugin) getServer().getPluginManager().getPlugin("CoreRelics");
        if (coreRelics == null) {
            getLogger().severe("CoreRelics non trovato!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bountyManager = new BountyManager(this);
        bountyManager.load();

        combatTagListener = new CombatTagListener(this);

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(combatTagListener, this);

        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("combat").setExecutor(new CombatCommand(this));

        getLogger().info("RelicCombat attivato!");
    }

    @Override
    public void onDisable() {
        if (bountyManager != null) bountyManager.save();
        getLogger().info("RelicCombat disattivato!");
    }

    public static RelicCombatPlugin getInstance() { return instance; }
    public CoreRelicsPlugin getCoreRelics() { return coreRelics; }
    public CombatTagListener getCombatTagListener() { return combatTagListener; }
    public BountyManager getBountyManager() { return bountyManager; }
}
