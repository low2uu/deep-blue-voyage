package io.github.low2uu.dbv.world;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DeepBlueVoyageWorld.MOD_ID)
public final class DeepBlueVoyageWorld {
    public static final String MOD_ID = "dbv_world";
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);

    static { GENERATORS.register("starter_island", () -> StarterIslandGenerator.CODEC); }

    public DeepBlueVoyageWorld(IEventBus modBus) {
        GENERATORS.register(modBus);
        io.github.low2uu.dbv.fleet.FleetModule.initialize();
        NeoForge.EVENT_BUS.addListener(ConstructionMode::onServerStarted);
        NeoForge.EVENT_BUS.addListener(DeepBlueVoyageWorld::onCreateSpawn);
        LogUtils.getLogger().info("Deep Blue Voyage world module loaded; starter island preset registered.");
    }

    private static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD
                && level.getChunkSource().getGenerator() instanceof StarterIslandGenerator) {
            level.setDefaultSpawnPos(new BlockPos(IslandTerrain.SPAWN_X,
                    IslandTerrain.PLATFORM_Y + 1, IslandTerrain.SPAWN_Z), 0);
            event.setCanceled(true);
            try { io.github.low2uu.dbv.fleet.dock.StarterSubmarine.requestNewWorld(level.getServer()); }
            catch (java.io.IOException error) { LogUtils.getLogger().error("DBV starter submarine request failed", error); }
        }
    }
}
