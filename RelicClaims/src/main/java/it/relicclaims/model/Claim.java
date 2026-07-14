package it.relicclaims.model;

import java.util.UUID;

public class Claim {

    private final int chunkX;
    private final int chunkZ;
    private final String world;
    private UUID owner;
    private String guildId;

    public Claim(int chunkX, int chunkZ, String world, UUID owner, String guildId) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.world = world;
        this.owner = owner;
        this.guildId = guildId;
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public String getWorld() { return world; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }

    public String getKey() {
        return world + ":" + chunkX + ":" + chunkZ;
    }

    public boolean isOwnedBy(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }
}
