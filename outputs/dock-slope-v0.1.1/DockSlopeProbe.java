package dbv.acceptance;

import com.google.gson.JsonObject;
import io.github.low2uu.dbv.world.OceanBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** One-off copy-only save upgrade; never included in the production source set. */
public final class DockSlopeProbe {
    static final java.util.Set<String> MATERIALS=java.util.Set.of("minecraft:bubble_column","minecraft:sea_pickle","minecraft:glow_lichen","minecraft:raw_copper_block","minecraft:raw_iron_block","minecraft:smooth_basalt","create:asurine","create:crimsite","create:ochrum","create:veridium","create:limestone","create:scoria","create:scorchia");
    static final int X=-112,Z=112,W=361,H=289;
    static boolean natural(BlockState s) {
        String id=BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
        return (id.startsWith("minecraft:")&&id.contains("coral"))||MATERIALS.contains(id)||s.isAir()||id.equals("minecraft:lava")||id.equals("minecraft:obsidian")||id.equals("minecraft:dripstone_block")||id.equals("minecraft:pointed_dripstone")||id.equals("minecraft:magma_block")||id.equals("minecraft:water")||id.equals("minecraft:bedrock")
            ||id.equals("minecraft:stone")||id.equals("minecraft:deepslate")
            ||id.equals("minecraft:granite")||id.equals("minecraft:andesite")||id.equals("minecraft:diorite")
            ||id.equals("minecraft:tuff")||id.equals("minecraft:calcite")||id.equals("minecraft:gravel")
            ||id.equals("minecraft:sand")||id.equals("minecraft:sandstone")||id.equals("minecraft:clay")
            ||id.equals("minecraft:dirt")||id.equals("minecraft:grass_block")
            ||id.equals("minecraft:kelp")||id.equals("minecraft:kelp_plant")
            ||id.equals("minecraft:seagrass")||id.equals("minecraft:tall_seagrass")
            ||((id.startsWith("minecraft:")||id.startsWith("create:"))&&id.endsWith("_ore"));
    }
    static int steepest(int[] h,boolean[] active) {
        int max=0;
        for(int z=1;z<H-1;z++)for(int x=1;x<W-1;x++){
            int i=z*W+x;
            if(active[i]||active[i+1])max=Math.max(max,Math.abs(h[i]-h[i+1]));
            if(active[i]||active[i+W])max=Math.max(max,Math.abs(h[i]-h[i+W]));
        }
        return max;
    }
    public static JsonObject apply(ServerLevel level) {
        for(int cz=(Z-32)>>4;cz<=(Z+H+32)>>4;cz++)for(int cx=(X-32)>>4;cx<=(X+W+32)>>4;cx++)level.getChunk(cx,cz);
        int[] old=new int[W*H];double[] values=new double[W*H],next=new double[W*H];boolean[] active=new boolean[W*H];
        for(int z=0;z<H;z++)for(int x=0;x<W;x++){
            int wx=X+x,wz=Z+z,i=z*W+x;
            old[i]=level.getHeight(Heightmap.Types.OCEAN_FLOOR,wx,wz)-1;values[i]=old[i];
            active[i]=x>0&&x<W-1&&z>0&&z<H-1&&old[i]<128
                && !OceanBounds.protectedSurface(wx,wz)&&OceanBounds.harbourWeight(wx,wz)>0;
        }
        int[] target=new int[old.length];int columns=0,blocks=0,protectedColumns=0;var pos=new BlockPos.MutableBlockPos();
        boolean validated=false;
        for(int pass=0;pass<12&&!validated;pass++){
            for(int z=0;z<H;z++)for(int x=0;x<W;x++){
                int i=z*W+x,wx=X+x,wz=Z+z;target[i]=old[i];if(!active[i])continue;
                int ax=Math.clamp(wx,-16,151),az=Math.clamp(wz,76,303);
                double dx=wx-ax,dz=wz-az,distance=Math.hypot(dx,dz);
                double t=Math.clamp(distance/96.0,0.0,1.0);t=t*t*(3-2*t);
                double ex=Math.clamp(ax+dx*96/distance-X,0,W-1),ez=Math.clamp(az+dz*96/distance-Z,0,H-1);
                int ix=(int)Math.floor(ex),iz=(int)Math.floor(ez),jx=Math.min(ix+1,W-1),jz=Math.min(iz+1,H-1);
                double fx=ex-ix,fz=ez-iz;
                double edge=(old[iz*W+ix]*(1-fx)+old[iz*W+jx]*fx)*(1-fz)+(old[jz*W+ix]*(1-fx)+old[jz*W+jx]*fx)*fz;
                int anchor=old[Math.clamp(az-Z,0,H-1)*W+Math.clamp(ax-X,0,W-1)];
                target[i]=(int)Math.round(anchor*(1-t)+edge*t);
            }
            java.util.Set<Integer> obstacles=new java.util.HashSet<>();
            for(int z=0;z<H;z++)for(int x=0;x<W;x++){
                int i=z*W+x;if(!active[i]||old[i]==target[i])continue;
                for(int y=Math.min(old[i],target[i])-3;y<128;y++){
                    pos.set(X+x,y,Z+z);
                    if(level.getBlockEntity(pos)!=null||!natural(level.getBlockState(pos))){obstacles.add(i);break;}
                }
            }
            if(obstacles.isEmpty()){validated=true;break;}
            // Preserve structures and geodes with a six-block horizontal buffer, then solve again.
            for(int obstacle:obstacles){int ox=obstacle%W,oz=obstacle/W;
                for(int z=Math.max(0,oz-6);z<=Math.min(H-1,oz+6);z++)for(int x=Math.max(0,ox-6);x<=Math.min(W-1,ox+6);x++){
                    int i=z*W+x;if(active[i]){active[i]=false;protectedColumns++;}
                }
            }
        }
        if(!validated)throw new IllegalStateException("Could not safely resolve all structure buffers");
        for(int z=0;z<H;z++)for(int x=0;x<W;x++){
            int i=z*W+x;if(!active[i]||old[i]==target[i])continue;columns++;
            for(int y=Math.min(old[i],target[i])-3;y<128;y++){
                pos.set(X+x,y,Z+z);
                BlockState block=(y>target[i]?Blocks.WATER:y>=target[i]-2?Blocks.SAND:Blocks.STONE).defaultBlockState();
                if(!level.getBlockState(pos).equals(block)){level.setBlock(pos,block,18);blocks++;}
            }
            // Sparse native seagrass, safely outside the dry facilities and dock channel.
            if(Math.floorMod((X+x)*73428767^(Z+z)*912931,17)==0&&target[i]<125)
                level.setBlock(pos.set(X+x,target[i]+1,Z+z),Blocks.SEAGRASS.defaultBlockState(),18);
        }
        int[] actual=new int[old.length];
        for(int z=0;z<H;z++)for(int x=0;x<W;x++){
            int i=z*W+x;actual[i]=level.getHeight(Heightmap.Types.OCEAN_FLOOR,X+x,Z+z)-1;
            if(active[i]&&actual[i]!=target[i])throw new IllegalStateException("Height mismatch at "+(X+x)+","+(Z+z));
        }
        JsonObject result=new JsonObject();result.addProperty("columns",columns);result.addProperty("protectedStructureColumns",protectedColumns);result.addProperty("blocks",blocks);
        result.addProperty("maxAdjacentStepBefore",steepest(old,active));result.addProperty("maxAdjacentStepAfter",steepest(actual,active));
        result.addProperty("blendWidth",OceanBounds.HARBOUR_BLEND);
        var profile=new com.google.gson.JsonArray();
        for(int z=288;z<=400;z+=4){int i=(z-Z)*W+(64-X);var row=new JsonObject();row.addProperty("z",z);row.addProperty("before",old[i]);row.addProperty("after",actual[i]);profile.add(row);}
        result.add("exitProfileX64",profile);return result;
    }
}
