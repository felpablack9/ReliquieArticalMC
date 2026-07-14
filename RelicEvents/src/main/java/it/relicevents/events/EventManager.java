package it.relicevents.events;

import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.types.BossEvent;
import it.relicevents.events.types.DungeonEvent;
import it.relicevents.events.types.GhostShipEvent;
import it.relicevents.events.types.MeteorEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class EventManager {

    private final RelicEventsPlugin plugin;
    private RelicEvent activeEvent;
    private final Map<String, Integer> eventWeights;
    private final Random random;

    public EventManager(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.eventWeights = new LinkedHashMap<>();
        loadWeights();
    }

    private void loadWeights() {
        eventWeights.clear();
        if (plugin.getConfig().getBoolean("events.dungeon.enabled", true)) {
            eventWeights.put("dungeon", plugin.getConfig().getInt("events.dungeon.weight", 30));
        }
        if (plugin.getConfig().getBoolean("events.ghost-ship.enabled", true)) {
            eventWeights.put("ghost_ship", plugin.getConfig().getInt("events.ghost-ship.weight", 25));
        }
        if (plugin.getConfig().getBoolean("events.meteor.enabled", true)) {
            eventWeights.put("meteor", plugin.getConfig().getInt("events.meteor.weight", 25));
        }
        if (plugin.getConfig().getBoolean("events.boss.enabled", true)) {
            eventWeights.put("boss", plugin.getConfig().getInt("events.boss.weight", 20));
        }
    }

    public void triggerRandomEvent() {
        if (activeEvent != null && activeEvent.isActive()) {
            return;
        }

        int minPlayers = plugin.getConfig().getInt("settings.min-players", 1);
        if (Bukkit.getOnlinePlayers().size() < minPlayers) {
            return;
        }

        String eventType = pickWeightedRandom();
        if (eventType == null) return;

        forceEvent(eventType);
    }

    public boolean forceEvent(String eventType) {
        Location location = generateEventLocation();
        if (location == null) return false;

        RelicEvent event = createEvent(eventType);
        if (event == null) return false;

        activeEvent = event;
        event.start(location);

        announceEvent(event, location);

        int durationMinutes = plugin.getConfig().getInt("settings.event-duration-minutes", 30);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeEvent != null && activeEvent.isActive() && activeEvent == event) {
                announceEventEnding(event);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (activeEvent == event) {
                        endActiveEvent();
                    }
                }, 60 * 20L); // 1 minute warning
            }
        }, (durationMinutes - 1) * 60 * 20L);

        return true;
    }

    private RelicEvent createEvent(String eventType) {
        switch (eventType) {
            case "dungeon":
                return new DungeonEvent(plugin);
            case "ghost_ship":
                return new GhostShipEvent(plugin);
            case "meteor":
                return new MeteorEvent(plugin);
            case "boss":
                return new BossEvent(plugin);
            default:
                return null;
        }
    }

    private Location generateEventLocation() {
        String worldName = plugin.getConfig().getString("settings.event-world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Mondo non trovato: " + worldName);
            return null;
        }

        int maxRadius = plugin.getConfig().getInt("settings.max-event-radius", 2000);
        int minRadius = plugin.getConfig().getInt("settings.min-event-radius", 200);

        int x, z;
        int attempts = 0;
        do {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minRadius + random.nextDouble() * (maxRadius - minRadius);
            x = (int) (Math.cos(angle) * distance);
            z = (int) (Math.sin(angle) * distance);
            attempts++;
        } while (attempts < 10 && !world.isChunkLoaded(x >> 4, z >> 4));

        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    private String pickWeightedRandom() {
        int totalWeight = eventWeights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight == 0) return null;

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : eventWeights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void announceEvent(RelicEvent event, Location loc) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.event-announce",
                "&e&l⚡ EVENTO! &6{event} &eè apparso nel mondo!");
        msg = msg.replace("{event}", event.getDisplayName());

        String locMsg = plugin.getConfig().getString("messages.event-location",
                "&7Coordinate approssimative: &fX:{x} Z:{z}");
        // Approximate coords (add some noise)
        int approxX = ((int) loc.getX() / 50) * 50;
        int approxZ = ((int) loc.getZ() / 50) * 50;
        locMsg = locMsg.replace("{x}", String.valueOf(approxX))
                .replace("{z}", String.valueOf(approxZ));

        String fullMsg = ChatColor.translateAlternateColorCodes('&', prefix + msg);
        String fullLocMsg = ChatColor.translateAlternateColorCodes('&', prefix + locMsg);

        Bukkit.broadcastMessage(fullMsg);
        Bukkit.broadcastMessage(fullLocMsg);
    }

    private void announceEventEnding(RelicEvent event) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.event-ending",
                "&c⚠ L'evento &6{event} &csta per terminare!");
        msg = msg.replace("{event}", event.getDisplayName());
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }

    public void endActiveEvent() {
        if (activeEvent != null) {
            String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
            String msg = plugin.getConfig().getString("messages.event-ended",
                    "&7L'evento &6{event} &7è terminato.");
            msg = msg.replace("{event}", activeEvent.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));

            activeEvent.cleanup();
            activeEvent = null;
        }
    }

    public void cleanupAll() {
        if (activeEvent != null) {
            activeEvent.cleanup();
            activeEvent = null;
        }
    }

    public boolean hasActiveEvent() {
        return activeEvent != null && activeEvent.isActive();
    }

    public String getActiveEventName() {
        return activeEvent != null ? activeEvent.getDisplayName() : "Nessuno";
    }

    public RelicEvent getActiveEvent() {
        return activeEvent;
    }

    public int getEventTypeCount() {
        return eventWeights.size();
    }
}
