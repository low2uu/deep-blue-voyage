package io.github.low2uu.dbv.world;

import net.minecraft.world.level.chunk.ChunkGenerator;

public final class OceanWorlds {
    public static boolean isProjectWorld(ChunkGenerator generator){
        return generator instanceof StarterIslandGenerator||generator instanceof FloodedWorldGenerator;
    }
    private OceanWorlds(){}
}
