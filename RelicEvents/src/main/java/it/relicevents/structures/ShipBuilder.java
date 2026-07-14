package it.relicevents.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class ShipBuilder {

    private ShipBuilder() {}

    public static void buildGhostShip(Location center, int length, int width, List<Location> placedBlocks) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int halfLength = length / 2;
        int halfWidth = width / 2;

        // Hull (boat shape)
        for (int x = -halfLength; x <= halfLength; x++) {
            // Taper the ends
            int localWidth = halfWidth;
            if (Math.abs(x) > halfLength - 3) {
                localWidth = Math.max(1, halfWidth - (Math.abs(x) - (halfLength - 3)));
            }

            for (int z = -localWidth; z <= localWidth; z++) {
                // Floor
                Block floor = center.getWorld().getBlockAt(cx + x, cy, cz + z);
                floor.setType(Material.DARK_OAK_PLANKS);
                placedBlocks.add(floor.getLocation().clone());

                // Hull bottom
                Block hull = center.getWorld().getBlockAt(cx + x, cy - 1, cz + z);
                hull.setType(Material.DARK_OAK_LOG);
                placedBlocks.add(hull.getLocation().clone());

                // Railings (walls)
                if (Math.abs(z) == localWidth) {
                    for (int y = 1; y <= 2; y++) {
                        Block railing = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);
                        if (y == 2) {
                            railing.setType(Material.DARK_OAK_FENCE);
                        } else {
                            railing.setType(Material.DARK_OAK_PLANKS);
                        }
                        placedBlocks.add(railing.getLocation().clone());
                    }
                }

                // Clear above deck
                for (int y = 1; y <= 4; y++) {
                    if (Math.abs(z) < localWidth) {
                        Block air = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);
                        if (air.getType() != Material.DARK_OAK_FENCE &&
                                air.getType() != Material.DARK_OAK_PLANKS) {
                            air.setType(Material.AIR);
                            placedBlocks.add(air.getLocation().clone());
                        }
                    }
                }
            }
        }

        // Mast
        for (int y = 1; y <= 8; y++) {
            Block mast = center.getWorld().getBlockAt(cx, cy + y, cz);
            mast.setType(Material.DARK_OAK_LOG);
            placedBlocks.add(mast.getLocation().clone());
        }

        // Sail (white wool)
        for (int y = 4; y <= 7; y++) {
            int sailWidth = y <= 5 ? 3 : 2;
            for (int z = -sailWidth; z <= sailWidth; z++) {
                Block sail = center.getWorld().getBlockAt(cx + 1, cy + y, cz + z);
                sail.setType(Material.WHITE_WOOL);
                placedBlocks.add(sail.getLocation().clone());
            }
        }

        // Captain's cabin at stern
        for (int x = halfLength - 3; x <= halfLength; x++) {
            for (int z = -halfWidth + 1; z <= halfWidth - 1; z++) {
                // Cabin walls
                boolean isWall = x == halfLength - 3 || x == halfLength ||
                        z == -halfWidth + 1 || z == halfWidth - 1;
                for (int y = 1; y <= 3; y++) {
                    Block cabin = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);
                    if (y == 3 || isWall) {
                        cabin.setType(Material.DARK_OAK_PLANKS);
                    } else {
                        cabin.setType(Material.AIR);
                    }
                    placedBlocks.add(cabin.getLocation().clone());
                }
            }
        }

        // Cabin door (opening)
        for (int y = 1; y <= 2; y++) {
            Block door = center.getWorld().getBlockAt(cx + halfLength - 3, cy + y, cz);
            door.setType(Material.AIR);
            placedBlocks.add(door.getLocation().clone());
        }

        // Decoration - soul lanterns
        Block lantern1 = center.getWorld().getBlockAt(cx - halfLength + 1, cy + 2, cz);
        lantern1.setType(Material.SOUL_LANTERN);
        placedBlocks.add(lantern1.getLocation().clone());

        Block lantern2 = center.getWorld().getBlockAt(cx + halfLength - 1, cy + 2, cz);
        lantern2.setType(Material.SOUL_LANTERN);
        placedBlocks.add(lantern2.getLocation().clone());

        // Cobwebs for ghostly effect
        for (int i = 0; i < 8; i++) {
            int rx = cx + (int) ((Math.random() - 0.5) * length);
            int ry = cy + 1 + (int) (Math.random() * 3);
            int rz = cz + (int) ((Math.random() - 0.5) * width);
            Block web = center.getWorld().getBlockAt(rx, ry, rz);
            if (web.getType() == Material.AIR) {
                web.setType(Material.COBWEB);
                placedBlocks.add(web.getLocation().clone());
            }
        }
    }
}
