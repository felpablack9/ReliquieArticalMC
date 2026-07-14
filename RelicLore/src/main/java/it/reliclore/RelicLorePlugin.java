package it.reliclore;

import it.corerelics.CoreRelicsPlugin;
import it.reliclore.commands.LoreCommand;
import it.reliclore.data.ChronicleManager;
import it.reliclore.listeners.LoreListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicLorePlugin extends JavaPlugin {

    private static RelicLorePlugin instance;
    private CoreRelicsPlugin coreRelics;
    private ChronicleManager chronicleManager;

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

        chronicleManager = new ChronicleManager(this);
        chronicleManager.load();

        getCommand("lore").setExecutor(new LoreCommand(this));
        getServer().getPluginManager().registerEvents(new LoreListener(this), this);

        getLogger().info("RelicLore attivato! Eventi registrati: " + chronicleManager.getEntryCount());
    }

    @Override
    public void onDisable() {
        if (chronicleManager != null) chronicleManager.save();
        getLogger().info("RelicLore disattivato!");
    }

    public static RelicLorePlugin getInstance() { return instance; }
    public CoreRelicsPlugin getCoreRelics() { return coreRelics; }
    public ChronicleManager getChronicleManager() { return chronicleManager; }
}
