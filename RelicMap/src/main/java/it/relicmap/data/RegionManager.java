package it.relicmap.data;

import it.relicmap.RelicMapPlugin;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class RegionManager {

    private final RelicMapPlugin plugin;
    private final List<Region> regions;

    public RegionManager(RelicMapPlugin plugin) {
        this.plugin = plugin;
        this.regions = new ArrayList<>();
        loadRegions();
    }

    private void loadRegions() {
        List<String> customRegions = plugin.getConfig().getStringList("regions.custom");
        for (String entry : customRegions) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String name = parts[0];
            String[] coords = parts[1].split(",");
            if (coords.length != 4) continue;

            try {
                int x1 = Integer.parseInt(coords[0]);
                int z1 = Integer.parseInt(coords[1]);
                int x2 = Integer.parseInt(coords[2]);
                int z2 = Integer.parseInt(coords[3]);
                regions.add(new Region(name, x1, z1, x2, z2));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public String getRegionName(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();

        for (Region region : regions) {
            if (x >= region.x1 && x <= region.x2 && z >= region.z1 && z <= region.z2) {
                return region.name;
            }
        }
        return plugin.getConfig().getString("regions.default-name", "Terre Sconosciute");
    }

    private static class Region {
        final String name;
        final int x1, z1, x2, z2;

        Region(String name, int x1, int z1, int x2, int z2) {
            this.name = name;
            this.x1 = Math.min(x1, x2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.z2 = Math.max(z1, z2);
        }
    }
}
