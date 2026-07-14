package it.corerelics;

import it.corerelics.commands.RelicCommand;
import it.corerelics.commands.RelicTabCompleter;
import it.corerelics.data.DataManager;
import it.corerelics.listeners.RelicListener;
import it.corerelics.relics.RelicRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class CoreRelicsPlugin extends JavaPlugin {

    private static CoreRelicsPlugin instance;
    private RelicRegistry relicRegistry;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        dataManager = new DataManager(this);
        dataManager.load();

        relicRegistry = new RelicRegistry(this);
        relicRegistry.loadRelicsFromConfig();

        getCommand("relic").setExecutor(new RelicCommand(this));
        getCommand("relic").setTabCompleter(new RelicTabCompleter(this));

        getServer().getPluginManager().registerEvents(new RelicListener(this), this);

        getLogger().info("CoreRelics attivato! Reliquie caricate: " + relicRegistry.getRelicCount());
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("CoreRelics disattivato!");
    }

    public static CoreRelicsPlugin getInstance() {
        return instance;
    }

    public RelicRegistry getRelicRegistry() {
        return relicRegistry;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
