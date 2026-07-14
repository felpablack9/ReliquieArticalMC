package it.relicmap;

import it.corerelics.CoreRelicsPlugin;
import it.relicmap.commands.RelicMapCommand;
import it.relicmap.data.RegionManager;
import it.relicmap.data.SightingTracker;
import it.relicmap.listeners.TrackingListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicMapPlugin extends JavaPlugin {

    private static RelicMapPlugin instance;
    private CoreRelicsPlugin coreRelics;
    private RegionManager regionManager;
    private SightingTracker sightingTracker;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        coreRelics = (CoreRelicsPlugin) getServer().getPluginManager().getPlugin("CoreRelics");
        if (coreRelics == null) {
            getLogger().severe("CoreRelics non trovato! RelicMap richiede CoreRelics.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        regionManager = new RegionManager(this);
        sightingTracker = new SightingTracker(this);
        sightingTracker.load();

        getCommand("relicmap").setExecutor(new RelicMapCommand(this));

        getServer().getPluginManager().registerEvents(new TrackingListener(this), this);

        int interval = getConfig().getInt("settings.tracking-interval-seconds", 60) * 20;
        getServer().getScheduler().runTaskTimer(this, sightingTracker::updateAllSightings, 100L, interval);

        getLogger().info("RelicMap attivato!");
    }

    @Override
    public void onDisable() {
        if (sightingTracker != null) {
            sightingTracker.save();
        }
        getLogger().info("RelicMap disattivato!");
    }

    public static RelicMapPlugin getInstance() { return instance; }
    public CoreRelicsPlugin getCoreRelics() { return coreRelics; }
    public RegionManager getRegionManager() { return regionManager; }
    public SightingTracker getSightingTracker() { return sightingTracker; }
}
