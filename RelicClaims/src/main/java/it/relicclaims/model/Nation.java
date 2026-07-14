package it.relicclaims.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Nation {

    private final String id;
    private String name;
    private UUID leader;
    private final List<String> guildIds;
    private final List<String> allianceIds;

    public Nation(String id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.guildIds = new ArrayList<>();
        this.allianceIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<String> getGuildIds() { return guildIds; }
    public List<String> getAllianceIds() { return allianceIds; }

    public void addGuild(String guildId) {
        if (!guildIds.contains(guildId)) guildIds.add(guildId);
    }

    public void removeGuild(String guildId) {
        guildIds.remove(guildId);
    }

    public void addAlliance(String nationId) {
        if (!allianceIds.contains(nationId)) allianceIds.add(nationId);
    }

    public void removeAlliance(String nationId) {
        allianceIds.remove(nationId);
    }

    public boolean isAlliedWith(String nationId) {
        return allianceIds.contains(nationId);
    }

    public int getGuildCount() { return guildIds.size(); }
}
