package it.relicevents.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class DungeonBuilder {

    private DungeonBuilder() {}

    public static void buildDungeon(Location center, int radius, List<Location> placedBlocks) {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Clear interior and build walls
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 5; y++) {
                    Block block = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);
                    boolean isWall = Math.abs(x) == radius || Math.abs(z) == radius;
                    boolean isFloor = y == 0;
                    boolean isCeiling = y == 5;

                    if (isFloor) {
                        block.setType(Material.DEEPSLATE_BRICKS);
                        placedBlocks.add(block.getLocation().clone());
                    } else if (isCeiling) {
                        block.setType(Material.DEEPSLATE_TILES);
                        placedBlocks.add(block.getLocation().clone());
                    } else if (isWall) {
                        if (y == 3 && (x == 0 || z == 0) && Math.random() < 0.3) {
                            // Window openings
                            block.setType(Material.IRON_BARS);
                        } else {
                            block.setType(Material.DEEPSLATE_BRICKS);
                        }
                        placedBlocks.add(block.getLocation().clone());
                    } else {
                        block.setType(Material.AIR);
                        placedBlocks.add(block.getLocation().clone());
                    }
                }
            }
        }

        // Entrance (break hole in wall)
        for (int y = 1; y <= 3; y++) {
            Block entrance = center.getWorld().getBlockAt(cx + radius, cy + y, cz);
            entrance.setType(Material.AIR);
            placedBlocks.add(entrance.getLocation().clone());
        }

        // Internal pillars
        int pillarOffset = radius / 2;
        for (int px = -1; px <= 1; px += 2) {
            for (int pz = -1; pz <= 1; pz += 2) {
                for (int y = 1; y <= 4; y++) {
                    Block pillar = center.getWorld().getBlockAt(
                            cx + px * pillarOffset, cy + y, cz + pz * pillarOffset);
                    pillar.setType(Material.DEEPSLATE_BRICK_WALL);
                    placedBlocks.add(pillar.getLocation().clone());
                }
            }
        }

        // Torches on pillars
        placeTorch(center.getWorld().getBlockAt(cx + pillarOffset + 1, cy + 3, cz + pillarOffset), placedBlocks);
        placeTorch(center.getWorld().getBlockAt(cx - pillarOffset - 1, cy + 3, cz + pillarOffset), placedBlocks);
        placeTorch(center.getWorld().getBlockAt(cx + pillarOffset + 1, cy + 3, cz - pillarOffset), placedBlocks);
        placeTorch(center.getWorld().getBlockAt(cx - pillarOffset - 1, cy + 3, cz - pillarOffset), placedBlocks);

        // Spawners decoration (cobwebs and skulls)
        for (int i = 0; i < 10; i++) {
            int rx = cx + (int) ((Math.random() - 0.5) * (radius - 1) * 2);
            int rz = cz + (int) ((Math.random() - 0.5) * (radius - 1) * 2);
            Block decor = center.getWorld().getBlockAt(rx, cy + 4, rz);
            if (decor.getType() == Material.AIR) {
                decor.setType(Material.COBWEB);
                placedBlocks.add(decor.getLocation().clone());
            }
        }

        // Loot barrels
        for (int i = 0; i < 3; i++) {
            int bx = cx + (int) ((Math.random() - 0.5) * (radius - 2) * 2);
            int bz = cz + (int) ((Math.random() - 0.5) * (radius - 2) * 2);
            Block barrel = center.getWorld().getBlockAt(bx, cy + 1, bz);
            if (barrel.getType() == Material.AIR) {
                barrel.setType(Material.BARREL);
                placedBlocks.add(barrel.getLocation().clone());
            }
        }
    }

    private static void placeTorch(Block block, List<Location> placedBlocks) {
        if (block.getType() == Material.AIR) {
            block.setType(Material.SOUL_LANTERN);
            placedBlocks.add(block.getLocation().clone());
        }
    }
}
