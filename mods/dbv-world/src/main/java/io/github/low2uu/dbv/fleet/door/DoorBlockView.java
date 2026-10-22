package io.github.low2uu.dbv.fleet.door;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Read the containing ship's plot, never the overworld chunk at its storage coordinates. */
final class DoorBlockView {
    private final Level level;
    final ServerSubLevel body;
    DoorBlockView(Level level,BlockPos origin){
        this.level=level;
        var owner=SableCompanion.INSTANCE.getContaining(level,origin);
        body=owner instanceof ServerSubLevel ship?ship:null;
    }
    boolean exterior(BlockPos p){
        if(body==null)return false;
        var b=body.getPlot().getBoundingBox();
        return p.getX()<b.minX()||p.getX()>b.maxX()||p.getY()<b.minY()||p.getY()>b.maxY()||p.getZ()<b.minZ()||p.getZ()>b.maxZ();
    }
    BlockState state(BlockPos p){
        if(body!=null){
            if(exterior(p))return Blocks.AIR.defaultBlockState();
            var plot=body.getPlot();
            var chunk=plot.getChunk(plot.toLocal(new ChunkPos(p)));
            if(chunk==null)throw new IllegalStateException("unloaded");
            return chunk.getBlockState(p);
        }
        if(!level.hasChunkAt(p))throw new IllegalStateException("unloaded");
        return level.getBlockState(p);
    }
}
