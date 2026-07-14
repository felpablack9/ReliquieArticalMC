package it.relicevents.events;

import org.bukkit.Location;

public interface RelicEvent {

    String getId();

    String getDisplayName();

    void start(Location center);

    void cleanup();

    boolean isActive();

    Location getLocation();
}
