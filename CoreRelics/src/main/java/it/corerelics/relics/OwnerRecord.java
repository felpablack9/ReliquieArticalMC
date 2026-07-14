package it.corerelics.relics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OwnerRecord {

    private final UUID playerUuid;
    private final String playerName;
    private final long timestamp;

    public OwnerRecord(UUID playerUuid, String playerName, long timestamp) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.timestamp = timestamp;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
