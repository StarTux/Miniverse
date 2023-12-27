package com.cavetale.miniverse;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;

@RequiredArgsConstructor
public final class Miniverse {
    private final MiniversePlugin plugin;
    protected String sourceWorldName = "world";
    protected String mapWorldName = "map";
    protected Set<Long> processedBlocks = new TreeSet<>();
    protected List<Long> blockQueue = new ArrayList<>();
    protected MaterialMap materialMap = new MaterialMap();
    protected int scaleY = 4;
    protected int scaleX = 16;
    protected int scaleZ = 16;
    protected int offsetY = 64;
    protected int ticks = 0;
    protected boolean loading;
    protected int minX = 0;
    protected int maxX = 0;
    protected int minZ = 0;
    protected int maxZ = 0;
    protected FillTask fillTask = null;

    protected static final class FillTask {
        protected int x;
        protected int z;

        @Override
        public String toString() {
            return "fill: " + x + "," + z;
        }
    }

    protected boolean initialize() {
        final World sourceWorld = getSourceWorld();
        if (sourceWorld == null) return false;
        final World mapWorld = getMapWorld();
        if (mapWorld == null) return false;
        WorldBorder sourceBorder = sourceWorld.getWorldBorder();
        WorldBorder mapBorder = mapWorld.getWorldBorder();
        Location sourceCenter = sourceBorder.getCenter();
        mapBorder.setCenter(sourceCenter.getX() / (double) scaleX,
                            sourceCenter.getZ() / (double) scaleZ);
        mapBorder.setSize(sourceBorder.getSize() / (double) scaleX);
        Location mapCenter = mapBorder.getCenter();
        int centerX = mapCenter.getBlockX();
        int centerZ = mapCenter.getBlockZ();
        int radius = (int) Math.ceil(mapBorder.getSize() * 0.5);
        minX = centerX - radius;
        maxX = centerX + radius;
        minZ = centerZ - radius;
        maxZ = centerZ + radius;
        loadFillTask();
        return true;
    }

    public World getSourceWorld() {
        return Bukkit.getWorld(sourceWorldName);
    }

    public World getMapWorld() {
        return Bukkit.getWorld(mapWorldName);
    }

    public boolean isSourceWorld(World world) {
        return world.getName().equals(sourceWorldName);
    }

    public boolean isMapWorld(World world) {
        return world.getName().equals(mapWorldName);
    }

    public int inputSourceChunk(Chunk chunk) {
        int count = 0;
        for (int z = 0; z < 16; z += scaleZ) {
            for (int x = 0; x < 16; x += scaleX) {
                final int mapX = (chunk.getX() * 16 + x) / scaleX;
                final int mapZ = (chunk.getZ() * 16 + z) / scaleZ;
                count += inputMapPillar(mapX, mapZ);
            }
        }
        return count;
    }

    public int inputMapPillar(final int mapX, final int mapZ) {
        final long key = Util.toLong(mapX, mapZ);
        if (processedBlocks.contains(key)) return 0;
        if (blockQueue.contains(key)) return 0;
        blockQueue.add(key);
        return 1;
    }

    protected void tick() {
        if (ticks++ == 0) initialize();
        if (loading) return;
        if (!blockQueue.isEmpty()) tickQueue();
        if (fillTask != null) tickFilling();
    }

    protected void tickQueue() {
        final long key = blockQueue.remove(0);
        final int mapX = Util.xFromLong(key);
        final int mapZ = Util.zFromLong(key);
        if (processedBlocks.contains(key)) return;
        final int chunkX = (mapX * scaleX) / 16;
        final int chunkZ = (mapZ * scaleZ) / 16;
        World sourceWorld = getSourceWorld();
        if (sourceWorld == null) return;
        final int chunkScaleX = (scaleX - 1) / 16 + 1;
        final int chunkScaleZ = (scaleZ - 1) / 16 + 1;
        CompletableFuture[] futures = new CompletableFuture[chunkScaleX * chunkScaleZ];
        for (int z = 0; z < chunkScaleZ; z += 1) {
            for (int x = 0; x < chunkScaleX; x += 1) {
                futures[z * chunkScaleX + x] = sourceWorld.getChunkAtAsync(chunkX + x, chunkZ + z);
            }
        }
        loading = true;
        CompletableFuture.allOf(futures).thenRun(() -> {
                loading = false;
                processedBlocks.add(key);
                processMapPillar(sourceWorld, mapX, mapZ);
            });
    }

    protected void tickFilling() {
        if (!processMapPillar(fillTask.x, fillTask.z)) {
            plugin.getLogger().warning("Miniverse filling failed at " + fillTask.x + "," + fillTask.z + ": " + info());
            stopFilling();
            return;
        }
        fillTask.x += 1;
        if (fillTask.x > maxX) {
            fillTask.x = minX;
            fillTask.z += 1;
            if (fillTask.z > maxZ) {
                plugin.getLogger().info("Miniverse filling completed: " + info());
                stopFilling(); // implies save
            } else {
                saveFillTask();
                plugin.getLogger().info("Miniverse filling " + mapWorldName + ": " + fillTask);
            }
        }
    }

    protected boolean processMapPillar(int mapX, int mapZ) {
        World sourceWorld = getSourceWorld();
        if (sourceWorld == null) return false;
        return processMapPillar(sourceWorld, mapX, mapZ);
    }

    protected boolean processMapPillar(World sourceWorld, int mapX, int mapZ) {
        if (mapX < minX || mapX > maxX) return false;
        if (mapZ < minZ || mapZ > maxZ) return false;
        World mapWorld = getMapWorld();
        if (mapWorld == null) return false;
        final int ax = mapX * scaleX;
        final int az = mapZ * scaleZ;
        final int bx = ax + scaleX - 1;
        final int bz = az + scaleZ - 1;
        Map<Biome, Integer> biomes = new EnumMap<>(Biome.class);
        final int minY = sourceWorld.getMinHeight();
        final int maxY = sourceWorld.getMaxHeight();
        BlockData[] pillar = new BlockData[maxY - minY];
        for (int ay = minY; ay < maxY; ay += scaleY) {
            Map<Material, Integer> mats = new EnumMap<>(Material.class);
            for (int z = az; z <= bz; z += 1) {
                for (int x = ax; x <= bx; x += 1) {
                    int top = Math.min(ay + scaleY - 1, 255);
                    biomes.compute(sourceWorld.getBlockAt(x, top, z).getBiome(),
                                   (m, i) -> i == null ? 1 : i + 1);
                    Material liquid = null;
                    Material nonLiquid = null;
                    for (int y = top; y >= ay; y -= 1) {
                        Block block = sourceWorld.getBlockAt(x, y, z);
                        if (block.isEmpty()) continue;
                        if (block.isLiquid()) {
                            liquid = block.getType();
                            continue;
                        }
                        Material mat = materialMap.of(block.getType());
                        switch (mat) {
                            //case SHORT_GRASS:
                            //case SEAGRASS:
                        case TALL_SEAGRASS:
                            //case KELP_PLANT:
                        case TALL_GRASS:
                        case KELP:
                            continue;
                        default:
                            break;
                        }
                        if (Tag.DOORS.isTagged(mat)) continue;
                        // if (Tag.FLOWERS.isTagged(mat)) continue;
                        if (Tag.TALL_FLOWERS.isTagged(mat)) continue;
                        nonLiquid = mat;
                        break;
                    }
                    if (nonLiquid != null) {
                        mats.compute(nonLiquid, (m, i) -> i == null ? 1 : i + 1);
                    } else if (liquid != null) {
                        mats.compute(liquid, (m, i) -> i == null ? 1 : i + 1);
                    }
                }
            }
            List<Material> rank = mats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            Material material = null;
            for (Material mat : rank) {
                material = mat;
                if (mat != Material.WATER) break;
            }
            if (material == null || !material.isBlock()) continue;
            BlockData blockData = material.createBlockData();
            if (blockData instanceof Leaves) {
                Leaves leaves = (Leaves) blockData;
                leaves.setPersistent(true);
            }
            pillar[(ay / scaleY) - minY] = blockData;
        }
        Biome biome = biomes.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(Biome.THE_VOID);
        for (int y = 0; y < pillar.length; y += 1) {
            Block mapBlock = mapWorld.getBlockAt(mapX, y + offsetY + minY, mapZ);
            BlockData blockData = pillar[y];
            if (blockData == null) {
                mapBlock.setType(Material.AIR);
            } else {
                mapBlock.setBlockData(blockData, false);
            }
            if (mapBlock.getBiome() != biome) {
                mapBlock.setBiome(biome);
            }
        }
        return true;
    }

    public String info() {
        return sourceWorldName + "/" + mapWorldName + ":" + scaleX + "x" + scaleY
            + "(" + minX + "," + minZ + "-" + maxX + "," + maxZ + ")"
            + (fillTask != null ? "/" + fillTask : "");
    }

    protected void startFilling() {
        fillTask = new FillTask();
        fillTask.x = minX;
        fillTask.z = minZ;
        saveFillTask();
    }

    protected void stopFilling() {
        fillTask = null;
        saveFillTask();
    }

    protected File getFillTaskFile() {
        World world = getMapWorld();
        if (world == null) return null;
        return new File(world.getWorldFolder(), "miniverse.fill.json");
    }

    protected void loadFillTask() {
        File file = getFillTaskFile();
        if (file == null) {
            fillTask = null;
            return;
        }
        fillTask = Json.load(file, FillTask.class, () -> null);
    }

    protected void saveFillTask() {
        File file = getFillTaskFile();
        if (file == null) return;
        if (fillTask == null) {
            if (file.exists()) file.delete();
        } else {
            Json.save(file, fillTask);
        }
    }

    public Location translateLocation(Location from) {
        String worldName = from.getWorld().getName();
        if (worldName.equals(sourceWorldName)) {
            World target = getMapWorld();
            if (target == null) return null;
            return new Location(target,
                                from.getX() / (double) scaleX,
                                from.getY() / (double) scaleY + (double) offsetY,
                                from.getZ() / (double) scaleZ,
                                from.getYaw(), from.getPitch());
        } else if (worldName.equals(mapWorldName)) {
            World target = getSourceWorld();
            if (target == null) return null;
            return new Location(target,
                                from.getX() * (double) scaleX,
                                from.getY() * (double) scaleY - (double) offsetY,
                                from.getZ() * (double) scaleZ,
                                from.getYaw(), from.getPitch());
        }
        return null;
    }
}
