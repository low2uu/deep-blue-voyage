package io.github.low2uu.dbv.world;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes only the current chunk. Neighbour order cannot split islands or their veins. */
public final class VoyageTerrain {
    private static final class Palette {
        private static final Block[] ORES={Blocks.IRON_ORE,Blocks.COPPER_ORE,Blocks.COAL_ORE,Blocks.GOLD_ORE,Blocks.REDSTONE_ORE,Blocks.LAPIS_ORE,registered("create:zinc_ore")};
        private static final Block[] RAW={Blocks.RAW_IRON_BLOCK,Blocks.RAW_COPPER_BLOCK,null,Blocks.RAW_GOLD_BLOCK,null,null,registered("create:raw_zinc_block")};
    }
    private static Block registered(String id) {
        var key=net.minecraft.resources.ResourceLocation.parse(id);
        var registry=net.minecraft.core.registries.BuiltInRegistries.BLOCK;
        if(!registry.containsKey(key))throw new IllegalStateException("Required pack mineral missing: "+id);
        return registry.get(key);
    }
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
    public static void decorate(long seed,ChunkAccess chunk) {
        boolean touched=false;
        var p=new BlockPos.MutableBlockPos();
        for(var s:VoyageLayout.sites(seed)) {
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
                double r=VoyageLayout.shape(s,x,z);
                if(r>=1.5)continue;
                int natural=chunk.getHeight(chunk instanceof net.minecraft.world.level.chunk.LevelChunk?Heightmap.Types.OCEAN_FLOOR:Heightmap.Types.OCEAN_FLOOR_WG,lx,lz);
                // Existing FULL chunks have the runtime heightmap instead of the generation one.
                if(natural<WorldHeights.MIN_Y)natural=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR,lx,lz);
                double t=Math.clamp((r-1)/.5,0,1); t=t*t*(3-2*t);
                int ground=(int)Math.round(VoyageLayout.top(s,x,z)*(1-t)+natural*t);
                for(int y=Math.min(72,natural);y<=Math.max(natural,ground)+8;y++) {
                    Block block;
                    if(y>ground)block=y<128?Blocks.WATER:Blocks.AIR;
                    else if(y==ground)block=ground>=130?Blocks.GRASS_BLOCK:Blocks.SAND;
                    else if(y>=ground-3)block=ground>=130?Blocks.DIRT:Blocks.SAND;
                    else if(VoyageLayout.vein(s,x,y,z)) {
                        block=Palette.RAW[s.ore()]!=null && Math.floorMod(x*31+y*17+z*13,7)==0?Palette.RAW[s.ore()]:Palette.ORES[s.ore()];
                    } else block=Blocks.STONE;
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
    /** Neighbour ore features use BulkSectionAccess, bypassing WorldGenRegion.setBlock.
     * Finish only newly generated chunks after their feature neighbourhood is complete.
     * Existing player-mined chunks are never replenished on reload. */
    public static void onChunkLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
        if(event.isNewChunk() && event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && level.dimension()==net.minecraft.world.level.Level.OVERWORLD
                && level.getChunkSource().getGenerator() instanceof FloodedWorldGenerator)
            finishOres(level.getSeed(),event.getChunk());
    }
    // Raw section writes avoid Sable physics callbacks loading neighbours during ChunkEvent.Load.
    // Only non-emissive solid mineral states change; terrain/lighting heights are unchanged.
    public static void finishOres(long seed,ChunkAccess chunk) {
        var p=new BlockPos.MutableBlockPos();
        for(var s:VoyageLayout.sites(seed))if(s.ore()>=0&&touches(s,chunk.getPos().x,chunk.getPos().z)) {
            for(int x=chunk.getPos().getMinBlockX();x<=chunk.getPos().getMaxBlockX();x++)for(int z=chunk.getPos().getMinBlockZ();z<=chunk.getPos().getMaxBlockZ();z++) {
                if(VoyageLayout.shape(s,x,z)>=1.5)continue;
                int top=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR,x&15,z&15);
                for(int y=72;y<top-3;y++) {
                    var state=chunk.getBlockState(p.set(x,y,z));
                    var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                    if(VoyageLayout.vein(s,x,y,z)) {
                        Block block=Palette.RAW[s.ore()]!=null&&Math.floorMod(x*31+y*17+z*13,7)==0?Palette.RAW[s.ore()]:Palette.ORES[s.ore()];
                        chunk.getSection(chunk.getSectionIndex(y)).setBlockState(x&15,y&15,z&15,block.defaultBlockState());
                    } else if(state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)||id.endsWith("_ore")||id.startsWith("raw_"))chunk.getSection(chunk.getSectionIndex(y)).setBlockState(x&15,y&15,z&15,Blocks.STONE.defaultBlockState());
                }
            }
            chunk.setUnsaved(true);
        }
    }
    private static void put(ChunkAccess chunk,BlockPos.MutableBlockPos p,int x,int y,int z,Block block) {
        chunk.setBlockState(p.set(x,y,z),block.defaultBlockState(),false);
    }
}
