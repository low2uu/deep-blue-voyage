package io.github.low2uu.dbv.world;

import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Restore normal exploration; the 10000-block square is a story range, not a generation limit. */
public final class OceanDevelopmentArea {
    private OceanDevelopmentArea(){}
    public static void started(ServerStartedEvent event){
        var level=event.getServer().overworld();
        if(!(level.getChunkSource().getGenerator() instanceof FloodedWorldGenerator))return;
        level.getWorldBorder().setCenter(0,0);
        level.getWorldBorder().setSize(59999968.0);
        level.getWorldBorder().setWarningBlocks(32);
    }
}
