package it.relicclaims.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Guild {

    private final String id;
    private String name;
    private UUID leader;
    private final List<UUID> members;
    private String nationId;

    public Guild(String id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.nationId = null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<UUID> getMembers() { return members; }
    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public boolean isMember(UUID uuid) { return members.contains(uuid); }
    public boolean isLeader(UUID uuid) { return leader.equals(uuid); }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int getMemberCount() { return members.size(); }
}
