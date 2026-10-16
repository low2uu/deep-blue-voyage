package io.github.low2uu.dbv.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Read-only acceptance checks for the permanent construction layout. */
public final class IslandBuildAudit {
    private IslandBuildAudit() {}
    public static String verify(ServerLevel level) {
        int checked=0;
        int[][] bays={{82,-62,106,-38,16},{108,-62,124,-46,12},{108,-42,124,-26,12},
                {86,-106,98,-94,12},{16,-8,32,8,12}};
        for(int[] b:bays)for(int x=b[0];x<b[2];x++)for(int z=b[1];z<b[3];z++) {
            if(level.getBlockState(new BlockPos(x,WorldHeights.fromLegacy(68),z)).isAir())throw new IllegalStateException("Missing bay floor "+x+","+z);
            for(int y=69;y<69+b[4];y++) {
                if(!level.getBlockState(new BlockPos(x,WorldHeights.fromLegacy(y),z)).isAir())throw new IllegalStateException("Blocked machine bay "+x+","+y+","+z);
                checked++;
            }
        }
        for(int x=0;x<128;x++)for(int z=112;z<240;z++) {
            for(int y:new int[]{13,62}) {
                if(!level.getBlockState(new BlockPos(x,WorldHeights.fromLegacy(y),z)).is(Blocks.WATER))throw new IllegalStateException("Dock water blocked "+x+","+y+","+z);
                checked++;
            }
            for(int y:new int[]{63,85,105}) {
                if(!level.getBlockState(new BlockPos(x,WorldHeights.fromLegacy(y),z)).isAir())throw new IllegalStateException("Dock overhead blocked "+x+","+y+","+z);
                checked++;
            }
        }
        for(int x=8;x<120;x+=8)for(int z=240;z<=288;z+=8)
            if(!level.getBlockState(new BlockPos(x,WorldHeights.fromLegacy(62),z)).is(Blocks.WATER))throw new IllegalStateException("Closed sea exit");
        BlockPos spawn=new BlockPos(IslandTerrain.SPAWN_X,WorldHeights.fromLegacy(69),IslandTerrain.SPAWN_Z);
        if(!level.getBlockState(spawn).isAir() || !level.getBlockState(spawn.above()).isAir()
                || level.getBlockState(spawn.below()).isAir())throw new IllegalStateException("Unsafe spawn");
        return "PASS "+checked+" block checks: five empty machine bays, 128x128 dock water/sky, open sea exit, dry spawn";
    }
}
