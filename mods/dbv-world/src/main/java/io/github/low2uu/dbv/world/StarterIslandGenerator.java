package io.github.low2uu.dbv.world;

import com.mojang.serialization.MapCodec;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** A real world preset: terrain is generated per chunk, never pasted over an existing save. */
public final class StarterIslandGenerator extends FlatLevelSource {
    public static final MapCodec<StarterIslandGenerator> CODEC = FlatLevelGeneratorSettings.CODEC
            .fieldOf("settings").xmap(StarterIslandGenerator::new, StarterIslandGenerator::settings);

    public StarterIslandGenerator(FlatLevelGeneratorSettings settings) { super(settings); }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public int getMinY() { return WorldHeights.MIN_Y; }

    @Override
    public int getGenDepth() { return WorldHeights.HEIGHT; }

    @Override
    public int getSeaLevel() { return IslandTerrain.SEA_LEVEL; }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) { return IslandTerrain.PLATFORM_Y + 1; }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
            StructureManager structures, ChunkAccess chunk) {
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunk.getPos().getMinBlockX() + localX;
                int z = chunk.getPos().getMinBlockZ() + localZ;
                int ground = IslandTerrain.groundHeight(x, z);
                int top = Math.min(chunk.getMaxBuildHeight() - 1, Math.max(ground, getSeaLevel() - 1));
                for (int y = chunk.getMinBuildHeight(); y <= top; y++) {
                    BlockState state = blockAt(y, ground, chunk.getMinBuildHeight());
                    chunk.setBlockState(pos.set(localX, y, localZ), state, false);
                    floor.update(localX, y, localZ, state);
                    surface.update(localX, y, localZ, state);
                }
            }
        }
        IslandArchitecture.decorate(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int ground = IslandTerrain.groundHeight(x, z);
        for (int y = Math.min(level.getMaxBuildHeight() - 1, Math.max(ground, getSeaLevel() - 1));
                y >= level.getMinBuildHeight(); y--) {
            if (type.isOpaque().test(blockAt(y, ground, level.getMinBuildHeight()))) { return y + 1; }
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int ground = IslandTerrain.groundHeight(x, z);
        int top = Math.min(level.getMaxBuildHeight() - 1, Math.max(ground, getSeaLevel() - 1));
        BlockState[] states = new BlockState[Math.max(0, top - level.getMinBuildHeight() + 1)];
        for (int i = 0; i < states.length; i++) {
            states[i] = blockAt(level.getMinBuildHeight() + i, ground, level.getMinBuildHeight());
        }
        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    private BlockState blockAt(int y, int ground, int minY) {
        if (y == minY) { return Blocks.BEDROCK.defaultBlockState(); }
        if (y > ground) {
            return (y < getSeaLevel() ? Blocks.WATER : Blocks.AIR).defaultBlockState();
        }
        // Construction-world seabed: no sand, vegetation, ores or structures underwater.
        if (ConstructionMode.ENABLED && ground < getSeaLevel() && y == ground) { return Blocks.BEDROCK.defaultBlockState(); }
        if (y == ground && ground >= getSeaLevel() + 2) { return Blocks.GRASS_BLOCK.defaultBlockState(); }
        if (y >= ground - 3) {
            return (ground >= getSeaLevel() + 2 ? Blocks.DIRT : Blocks.SAND).defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }
}
