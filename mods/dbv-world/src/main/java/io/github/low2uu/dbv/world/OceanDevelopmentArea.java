package io.github.low2uu.dbv.world;

import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Temporary playable square for the on-demand development world. No pre-generation. */
public final class OceanDevelopmentArea {
    private OceanDevelopmentArea(){}
    public static void started(ServerStartedEvent event){
        var level=event.getServer().overworld();
        if(!(level.getChunkSource().getGenerator() instanceof FloodedWorldGenerator))return;
        level.getWorldBorder().setCenter(0,0);
        level.getWorldBorder().setSize(OceanBounds.HALF_SIZE*2);
        level.getWorldBorder().setWarningBlocks(32);
    }
}
