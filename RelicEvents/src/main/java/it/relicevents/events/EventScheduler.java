package it.relicevents.events;

import it.relicevents.RelicEventsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class EventScheduler {

    private final RelicEventsPlugin plugin;
    private BukkitTask schedulerTask;
    private boolean running;

    public EventScheduler(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.running = false;
    }

    public void start() {
        if (running) return;

        int intervalMinutes = plugin.getConfig().getInt("settings.event-interval-minutes", 60);
        long intervalTicks = intervalMinutes * 60L * 20L;

        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            plugin.getEventManager().triggerRandomEvent();
        }, intervalTicks, intervalTicks);

        running = true;
        plugin.getLogger().info("Scheduler eventi avviato. Intervallo: " + intervalMinutes + " minuti.");
    }

    public void stop() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
            schedulerTask = null;
        }
        running = false;
        plugin.getLogger().info("Scheduler eventi fermato.");
    }

    public boolean isRunning() {
        return running;
    }
}
