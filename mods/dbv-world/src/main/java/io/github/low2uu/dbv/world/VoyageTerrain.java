package io.github.low2uu.dbv.world;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes only the current chunk. Neighbour order cannot split islands or their veins. */
public final class VoyageTerrain {
    public static boolean touches(VoyageLayout.Site s,int cx,int cz) {
        int x=Math.clamp(s.x(),cx*16,cx*16+15),z=Math.clamp(s.z(),cz*16,cz*16+15);
        return s.distance(x,z)<=s.extent();
    }
    public static boolean marker(VoyageLayout.Site s,int x,int z) {
        int dx=Math.abs(x-s.x()),dz=Math.abs(z-s.z());
        return (Math.abs(dx-s.radius())<=2&&dz<=2)||(Math.abs(dz-s.radius())<=2&&dx<=2);
    }
    public static boolean required(VoyageLayout.Site s,int cx,int cz) {
        if(s.ore()>=0)return touches(s,cx,cz);
        for(int x=cx*16;x<cx*16+16;x++)for(int z=cz*16;z<cz*16+16;z++)if(s.distance(x,z)<=24||marker(s,x,z))return true;
        return false;
    }
    public static void decorate(FloodedWorldGenerator generator,long seed,ChunkAccess chunk) {
        boolean touched=false;
        var p=new BlockPos.MutableBlockPos();
        for(var s:generator.sites(seed,chunk.getPos())) {
            if(!touches(s,chunk.getPos().x,chunk.getPos().z))continue;
            touched=true;
            for(int lx=0;lx<16;lx++)for(int lz=0;lz<16;lz++) {
                int x=chunk.getPos().getMinBlockX()+lx,z=chunk.getPos().getMinBlockZ()+lz;
                double d=s.distance(x,z);
                if(d>s.extent())continue;
                if(s.ore()<0) {
                    if(marker(s,x,z))for(int y=128;y<=135;y++)put(chunk,p,x,y,z,Blocks.DIAMOND_BLOCK);
                    if(d<=24)for(int y=s.y();y<s.y()+8;y++)put(chunk,p,x,y,z,Blocks.DIAMOND_BLOCK);
                    if(Math.abs(x-s.x())<=2&&Math.abs(z-s.z())<=2)for(int y=128;y<=132;y++)put(chunk,p,x,y,z,Blocks.DIAMOND_BLOCK);
                    continue;
                }
                var mineral=generator.minerals().get(s.ore());
                Block mineralBlock=mineral.oreBlock(),rawBlock=mineral.rawBlock();
                double r=VoyageLayout.shape(s,x,z);
                if(r>=1.5)continue;
                int natural=chunk.getHeight(chunk instanceof net.minecraft.world.level.chunk.LevelChunk?Heightmap.Types.OCEAN_FLOOR:Heightmap.Types.OCEAN_FLOOR_WG,lx,lz);
                // Existing FULL chunks have the runtime heightmap instead of the generation one.
                if(natural<WorldHeights.MIN_Y)natural=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR,lx,lz);
                double t=Math.clamp((r-1)/.5,0,1); t=t*t*(3-2*t);
                int ground=(int)Math.round(VoyageLayout.top(s,x,z)*(1-t)+natural*t);
                // Add island material above the natural surface. Keep pre-existing ores,
                // caves and structures; only ordinary stone hosts receive our extra vein.
                ground=Math.max(ground,natural);
                for(int y=Math.min(72,natural+1);y<=ground;y++) {
                    var old=chunk.getBlockState(p.set(x,y,z));
                    boolean vein=VoyageLayout.vein(s,x,y,z);
                    boolean original=y<=natural;
                    if(original&&(!vein||!(old.is(Blocks.STONE)||old.is(Blocks.DEEPSLATE))))continue;
                    if(vein&&mineral.crystal()) {
                        // A flooded crystal pocket with supported, mature clusters on its floor.
                        if(!VoyageLayout.vein(s,x,y-1,z)) {
                            var state=mineralBlock.defaultBlockState();
                            var facing=net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
                            var wet=net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
                            if(state.hasProperty(facing))state=state.setValue(facing,net.minecraft.core.Direction.UP);
                            if(state.hasProperty(wet))state=state.setValue(wet,y<128);
                            chunk.setBlockState(p,state,false);
                        } else put(chunk,p,x,y,z,y<128?Blocks.WATER:Blocks.AIR);
                        continue;
                    }
                    Block block;
                    if(vein)block=rawBlock!=null&&Math.floorMod(x*31+y*17+z*13,7)==0?rawBlock:mineralBlock;
                    else if(y==ground)block=ground>=130?Blocks.GRASS_BLOCK:Blocks.SAND;
                    else if(y>=ground-3)block=ground>=130?Blocks.DIRT:Blocks.SAND;
                    else block=Blocks.STONE;
                    put(chunk,p,x,y,z,block);
                }
                // Low vegetation and wind-bent, small oak canopies; all writes are column-local.
                if(r<.85&&ground>=131) {
                    if(Math.floorMod(x*17+z*29+s.index(),41)==0)put(chunk,p,x,ground+1,z,Blocks.SHORT_GRASS);
                    for(int tx=Math.floorDiv(x-2,23);tx<=Math.floorDiv(x+2,23);tx++)for(int tz=Math.floorDiv(z-2,23);tz<=Math.floorDiv(z+2,23);tz++) {
                        int ax=tx*23,az=tz*23;
                        if(Math.floorMod(tx*31+tz*13+s.index(),4)!=0||VoyageLayout.shape(s,ax,az)>.73)continue;
                        int top=VoyageLayout.top(s,ax,az);
                        if(x==ax&&z==az)for(int y=ground+1;y<=top+4;y++)put(chunk,p,x,y,z,Blocks.OAK_LOG);
                        for(int y=top+3;y<=top+5;y++)if(y>ground && !(x==ax&&z==az&&y<=top+4))
                            chunk.setBlockState(p.set(x,y,z),Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true),false);
                    }
                }
            }
        }
        if(!touched)return;
        Heightmap.primeHeightmaps(chunk,EnumSet.allOf(Heightmap.Types.class));
        chunk.setUnsaved(true);
    }
    private static void put(ChunkAccess chunk,BlockPos.MutableBlockPos p,int x,int y,int z,Block block) {
        chunk.setBlockState(p.set(x,y,z),block.defaultBlockState(),false);
    }
}
