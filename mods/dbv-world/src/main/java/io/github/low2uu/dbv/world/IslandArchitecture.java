package io.github.low2uu.dbv.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Permanent starter-island architecture, baked into newly generated chunks. No active machines. */
public final class IslandArchitecture {
    private IslandArchitecture() {}
    private static final int FLOOR = 68;

    public static void decorate(ChunkAccess chunk) {
        Painter p = new Painter(chunk);
        if (Math.abs(p.x0) > 288 || p.z0 < -208 || p.z0 > 256) return;
        landscape(p);
        roads(p);
        cottage(p, -108, -60, 28, 22, false);
        cottage(p, -46, -61, 34, 24, true);
        plaza(p);
        factory(p,80,-64,48,48,18);
        factory(p,80,-112,32,32,15);
        warehouse(p);
        kitchen(p);
        farm(p);
        timberYard(p);
        dock(p);
        // Flat machine envelopes are left completely empty above their floors.
        outline(p,82,-62,106,-38,Blocks.LIGHT_GRAY_TERRACOTTA);
        outline(p,108,-62,124,-46,Blocks.LIGHT_GRAY_TERRACOTTA);
        outline(p,108,-42,124,-26,Blocks.LIGHT_GRAY_TERRACOTTA);
        outline(p,86,-106,98,-94,Blocks.LIGHT_GRAY_TERRACOTTA);
        ruinFoundations(p);
    }

    private static int hash(int x, int z, int salt) {
        int v = x * 73428767 ^ z * 912931 ^ salt * 19349663;
        v ^= v >>> 13; v *= 1274126177; v ^= v >>> 16;
        return v & 0x7fffffff;
    }
    private static boolean rect(int x,int z,int a,int b,int c,int d,int margin) {
        return x>=a-margin && x<c+margin && z>=b-margin && z<d+margin;
    }
    private static boolean reserved(int x,int z,int margin) {
        return rect(x,z,-114,-68,-60,-18,margin) || rect(x,z,-52,-66,-4,-26,margin)
            || rect(x,z,0,-24,48,24,margin) || rect(x,z,76,-116,180,36,margin)
            || rect(x,z,-164,12,-124,52,margin) || rect(x,z,-84,52,-44,92,margin)
            || rect(x,z,-28,52,-4,76,margin) || rect(x,z,-12,76,144,256,margin);
    }
    private static final int[][] ROUTE = {
        {-80,-36,-48,-24},{-48,-24,-24,-24},{-24,-24,12,-32},{12,-32,64,-8},{64,-8,104,-8},
        {-24,-24,-40,12},{-40,12,-56,48},{-56,48,-24,48},{-24,48,12,56},{12,56,64,76},
        {-40,12,-100,0},{-100,0,-144,8},{64,-8,64,-76},{64,-76,96,-76},
        {-100,0,-136,-48},{-136,-48,-150,-72}
    };
    private static double segment(int x,int z,int[] r) {
        double dx=r[2]-r[0], dz=r[3]-r[1];
        double t=Math.max(0,Math.min(1,((x-r[0])*dx+(z-r[1])*dz)/(dx*dx+dz*dz)));
        return Math.hypot(x-r[0]-dx*t,z-r[1]-dz*t);
    }
    private static boolean nearRoad(int x,int z,double distance) {
        for(int[] r:ROUTE) if(segment(x,z,r)<distance) return true;
        return false;
    }

    private static void landscape(Painter p) {
        for(int x=p.x0;x<p.x0+16;x++) for(int z=p.z0;z<p.z0+16;z++) {
            int y=IslandTerrain.groundHeight(x,z), h=hash(x,z,1);
            if(y<65 || reserved(x,z,2) || nearRoad(x,z,4)) continue;
            if(y<68 && h%7<3) p.set(x,y,z,Blocks.COARSE_DIRT);
            else if(h%89==0) p.set(x,y,z,Blocks.MOSS_BLOCK);
            if(h%19==0) p.set(x,y+1,z,Blocks.SHORT_GRASS);
            if(h%127==0) p.set(x,y+1,z, h%2==0?Blocks.DANDELION:Blocks.OXEYE_DAISY);
            if(h%353==0) p.set(x,y+1,z,Blocks.FERN);
        }
        // Anchor trees on a global grid, so crowns are identical across chunk seams.
        for(int x=Math.floorDiv(p.x0-6,11)*11;x<=p.x0+21;x+=11)
            for(int z=Math.floorDiv(p.z0-6,11)*11;z<=p.z0+21;z+=11) {
                int h=hash(x,z,7), tx=x+h%5-2,tz=z+(h/7)%5-2;
                int y=IslandTerrain.groundHeight(tx,tz);
                if(y<68 || y>96 || reserved(tx,tz,7) || nearRoad(tx,tz,8) || h%5>1) continue;
                tree(p,tx,y+1,tz,5+h%4,h%3==0);
            }
        for(int x=Math.floorDiv(p.x0-4,19)*19;x<=p.x0+20;x+=19)
            for(int z=Math.floorDiv(p.z0-4,19)*19;z<=p.z0+20;z+=19) {
                int y=IslandTerrain.groundHeight(x,z);
                if(y<65 || reserved(x,z,5) || nearRoad(x,z,5) || hash(x,z,9)%4!=0) continue;
                for(int dx=-3;dx<=3;dx++) for(int dz=-3;dz<=3;dz++) for(int dy=-2;dy<=2;dy++)
                    if(dx*dx+dz*dz+dy*dy*2<9) p.set(x+dx,y+dy,z+dz,hash(x+dx,z+dz,dy)%3==0?Blocks.MOSSY_COBBLESTONE:Blocks.ANDESITE);
            }
    }

    private static void tree(Painter p,int x,int y,int z,int height,boolean birch) {
        for(int dy=height-3;dy<=height+1;dy++) {
            int radius=dy==height+1?1:dy==height?2:3;
            for(int dx=-radius;dx<=radius;dx++) for(int dz=-radius;dz<=radius;dz++)
                if(dx*dx+dz*dz<=radius*radius+1 && hash(x+dx,z+dz,dy)%9!=0)
                    p.set(x+dx,y+dy,z+dz,(birch?Blocks.BIRCH_LEAVES:Blocks.OAK_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        }
        p.fill(x,y,z,x,y+height-1,z,birch?Blocks.BIRCH_LOG:Blocks.OAK_LOG);
    }

    private static void roads(Painter p) {
        for(int x=p.x0;x<p.x0+16;x++) for(int z=p.z0;z<p.z0+16;z++) {
            if(!nearRoad(x,z,2.7)) continue;
            int y=IslandTerrain.groundHeight(x,z);
            if(y<65)continue;
            int h=hash(x,z,18)%10;
            p.set(x,y,z,h<6?Blocks.DIRT_PATH:h<8?Blocks.COARSE_DIRT:Blocks.GRAVEL);
            p.fill(x,y+1,z,x,y+3,z,Blocks.AIR);
            // Low rough steps handle the natural slopes without flattening whole roads.
            if(IslandTerrain.groundHeight(x,z+1)>y+1) p.set(x,y+1,z,Blocks.COBBLESTONE_SLAB);
        }
        int[][] lamps={{-64,-27},{-20,-29},{16,-37},{60,-14},{-48,22},{-100,5},{-140,12},{10,60},{63,73},{68,-62}};
        for(int[] a:lamps) lamp(p,a[0],IslandTerrain.groundHeight(a[0],a[1])+1,a[1]);
    }

    private static void foundation(Painter p,int x,int z,int w,int d,Block floor) {
        if(!p.intersects(x,z,x+w-1,z+d-1))return;
        for(int px=Math.max(x,p.x0);px<Math.min(x+w,p.x0+16);px++)
            for(int pz=Math.max(z,p.z0);pz<Math.min(z+d,p.z0+16);pz++) {
                int g=IslandTerrain.groundHeight(px,pz);
                p.fill(px,Math.min(g,FLOOR)-1,pz,px,FLOOR-1,pz,Blocks.STONE_BRICKS);
                p.set(px,FLOOR,pz,floor);
                p.fill(px,FLOOR+1,pz,px,Math.max(g+1,FLOOR+1),pz,Blocks.AIR);
            }
    }
    private static void outline(Painter p,int x,int z,int x2,int z2,Block block) {
        p.fill(x,FLOOR,z,x2-1,FLOOR,z,block);p.fill(x,FLOOR,z2-1,x2-1,FLOOR,z2-1,block);
        p.fill(x,FLOOR,z,x,FLOOR,z2-1,block);p.fill(x2-1,FLOOR,z,x2-1,FLOOR,z2-1,block);
    }
    private static void lamp(Painter p,int x,int y,int z) {
        p.fill(x,y,z,x,y+2,z,Blocks.SPRUCE_FENCE);p.set(x,y+3,z,Blocks.LANTERN);
    }
    private static void cottage(Painter p,int x,int z,int w,int d,boolean shop) {
        if(!p.intersects(x-3,z-3,x+w+3,z+d+6))return;
        foundation(p,x,z,w,d,Blocks.SPRUCE_PLANKS);
        int top=76;
        for(int y=69;y<=top;y++) for(int px=x;px<x+w;px++)for(int pz=z;pz<z+d;pz++) {
            if(px!=x&&px!=x+w-1&&pz!=z&&pz!=z+d-1)continue;
            boolean post=(px-x)%7==0 || px==x+w-1;
            Block wall=y<71?Blocks.STONE_BRICKS:post?Blocks.STRIPPED_SPRUCE_LOG:shop?Blocks.BRICKS:Blocks.STRIPPED_BIRCH_WOOD;
            if(!post&&y>=72&&y<=74&&((px-x)%7>=2&&(px-x)%7<=4 || (pz-z)%7>=2&&(pz-z)%7<=4))wall=Blocks.GLASS;
            p.set(px,y,pz,wall);
        }
        // Walk-through entrances avoid closed-door orientation problems in imported builds.
        int door=x+w/2;
        p.fill(door-1,69,z+d-1,door+1,72,z+d-1,Blocks.AIR);
        p.fill(door-2,73,z+d-1,door+2,73,z+d-1,Blocks.DARK_OAK_PLANKS);
        // Layered gabled roof, with real eaves and visible timber ends.
        for(int rz=-2;rz<=d+1;rz++) {
            int rise=Math.min(rz+2,d+1-rz)/2, y=77+rise;
            for(int rx=-2;rx<=w+1;rx++) {
                p.set(x+rx,y,z+rz,shop?Blocks.DARK_OAK_PLANKS:Blocks.SPRUCE_PLANKS);
                if(rz==-2 || rz==d+1) p.set(x+rx,y,z+rz,(shop?Blocks.DARK_OAK_STAIRS:Blocks.SPRUCE_STAIRS).defaultBlockState().setValue(StairBlock.FACING,rz==-2?Direction.SOUTH:Direction.NORTH));
            }
        }
        for(int rz=0;rz<d;rz++) {
            int roofY=77+Math.min(rz+2,d+1-rz)/2;
            for(int y=77;y<roofY;y++){p.set(x,y,z+rz,Blocks.SPRUCE_PLANKS);p.set(x+w-1,y,z+rz,Blocks.SPRUCE_PLANKS);}
        }
        p.fill(x+w-6,69,z+3,x+w-4,87,z+5,Blocks.BRICKS);
        p.fill(x+w-7,87,z+2,x+w-3,87,z+6,Blocks.BRICK_SLAB);
        foundation(p,door-7,z+d,15,5,Blocks.SPRUCE_PLANKS);
        p.fill(door-7,69,z+d+4,door-3,69,z+d+4,Blocks.SPRUCE_FENCE);
        p.fill(door+3,69,z+d+4,door+7,69,z+d+4,Blocks.SPRUCE_FENCE);
        lamp(p,door-5,69,z+d+2);lamp(p,door+5,69,z+d+2);
        // Warm lighting, seating and counters; no processing machinery or NPCs.
        for(int px=x+4;px<x+w-3;px+=7){
            p.fill(px,76,z+3,px,79,z+3,Blocks.CHAIN);p.fill(px,76,z+d-4,px,79,z+d-4,Blocks.CHAIN);
            p.set(px,75,z+3,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
            p.set(px,75,z+d-4,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        }
        if(shop) {
            p.fill(x+5,69,z+6,x+w-7,69,z+6,Blocks.BARREL);
            p.fill(x+5,70,z+6,x+w-7,70,z+6,Blocks.SPRUCE_SLAB);
            p.fill(x+3,69,z+2,x+w-8,71,z+2,Blocks.BOOKSHELF);
            for(int px=door-5;px<=door+5;px++)p.set(px,74,z+d+2,(px%2==0?Blocks.WHITE_WOOL:Blocks.ORANGE_WOOL));
        } else {
            for(int px=x+5;px<x+w-7;px+=8){p.set(px,69,z+9,Blocks.SPRUCE_FENCE);p.set(px,70,z+9,Blocks.SPRUCE_PRESSURE_PLATE);p.set(px,69,z+7,Blocks.SPRUCE_STAIRS);}
            p.fill(x+3,69,z+2,x+9,70,z+2,Blocks.BOOKSHELF);
            p.fill(x+4,69,z+d-5,x+11,69,z+d-3,Blocks.RED_CARPET);
        }
        for(int px=x+3;px<x+w-2;px+=7){p.set(px,69,z-1,Blocks.MOSS_BLOCK);p.set(px,70,z-1,Blocks.POPPY);}
    }
    private static void plaza(Painter p) {
        for(int x=-106;x<-65;x++)for(int z=-35;z<-24;z++) {
            p.set(x,68,z,hash(x,z,20)%5==0?Blocks.MOSSY_STONE_BRICKS:Blocks.STONE_BRICKS);
            p.fill(x,69,z,x,71,z,Blocks.AIR);
        }
        lamp(p,-102,69,-28);lamp(p,-68,69,-28);
        p.fill(-104,69,-31,-99,69,-31,Blocks.SPRUCE_STAIRS);
        p.fill(-74,69,-31,-69,69,-31,Blocks.SPRUCE_STAIRS);
        // Spawn remains dry, outside the cottage, under open sky.
    }
    private static void factory(Painter p,int x,int z,int w,int d,int height) {
        if(!p.intersects(x-2,z-2,x+w+5,z+d+2))return;
        foundation(p,x,z,w,d,Blocks.ANDESITE);
        for(int px=x;px<x+w;px++)for(int pz=z;pz<z+d;pz++) {
            if(px!=x&&px!=x+w-1&&pz!=z&&pz!=z+d-1)continue;
            boolean column=(px-x)%8==0&&(pz==z||pz==z+d-1)||(pz-z)%8==0&&(px==x||px==x+w-1);
            for(int y=69;y<=68+height;y++) {
                int h=hash(px+y,pz,30);
                Block wall=column?Blocks.POLISHED_BASALT:y<72?Blocks.MOSSY_STONE_BRICKS:h%7==0?Blocks.GRANITE:h%13==0?Blocks.CRACKED_STONE_BRICKS:Blocks.BRICKS;
                if(!column&&y>=73&&y<=78){wall=h%9==0?Blocks.AIR:h%7==0?Blocks.IRON_BARS:Blocks.GRAY_STAINED_GLASS;}
                p.set(px,y,pz,wall);
            }
        }
        int mid=x+w/2;
        p.fill(mid-4,69,z+d-1,mid+3,76,z+d-1,Blocks.AIR);
        p.fill(mid-5,77,z+d-1,mid+4,78,z+d-1,Blocks.POLISHED_ANDESITE);
        p.fill(x,69,z+d/2-3,x,74,z+d/2+2,Blocks.AIR);
        for(int px=x-1;px<=x+w;px++) for(int pz=z-1;pz<=z+d;pz++) {
            int ridge=Math.max(0,4-Math.abs(pz-(z+d/2))/5), y=69+height+ridge;
            int h=hash(px,pz,41);
            boolean hole=px>x+w-12&&px<x+w-5&&pz>z+5&&pz<z+11&&h%5!=0;
            if(!hole)p.set(px,y,pz,h%5==0?Blocks.WAXED_EXPOSED_CUT_COPPER:Blocks.WAXED_WEATHERED_CUT_COPPER);
            if((px-x)%8==0)p.set(px,68+height,pz,Blocks.DARK_OAK_LOG);
            if((px==x||px==x+w-1))p.fill(px,68+height,pz,px,y-1,pz,Blocks.BRICKS);
        }
        // Skylights remain well above all reserved machine volumes.
        for(int px=x+5;px<x+w-4;px++)p.set(px,73+height,z+d/2,Blocks.GLASS);
        for(int px=x+3;px<x+w-2;px+=8){
            p.fill(px,68+height,z,px,68+height,z+d-1,Blocks.DARK_OAK_LOG);
            for(int lz:new int[]{z+1,z+d-2}){
                p.fill(px,80,lz,px,67+height,lz,Blocks.CHAIN);
                p.set(px,79,lz,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
            }
        }
        p.fill(x+w+1,66,z+3,x+w+3,98,z+5,Blocks.BRICKS);
        p.fill(x+w,98,z+2,x+w+4,98,z+6,Blocks.STONE_BRICK_SLAB);
        // Exterior weathering stays outside machine bays and entrances.
        for(int px=x+2;px<x+w-1;px+=9){p.set(px,69,z-1,Blocks.COBBLESTONE_SLAB);p.set(px+1,69,z-1,Blocks.DEAD_BUSH);}
    }
    private static void warehouse(Painter p) {
        factory(p,8,-16,32,32,17);
        p.fill(11,69,-13,13,71,-9,Blocks.BARREL);
        p.fill(35,69,-13,37,70,-9,Blocks.BARREL);
        outline(p,16,-8,32,8,Blocks.YELLOW_TERRACOTTA);
    }
    private static void kitchen(Painter p) {
        cottage(p,-24,56,16,16,false);
    }
    private static void farm(Painter p) {
        // A resting field, not an automatic or pre-populated crop machine.
        foundation(p,-80,56,32,32,Blocks.GRASS_BLOCK);
        for(int x=-77;x<-51;x++)for(int z=59;z<85;z++) {
            p.set(x,68,z,(x+z)%5==0?Blocks.COARSE_DIRT:Blocks.PODZOL);
        }
        p.fill(-79,69,57,-49,69,57,Blocks.OAK_FENCE);
        p.fill(-79,69,86,-49,69,86,Blocks.OAK_FENCE);
        p.fill(-79,69,57,-79,69,86,Blocks.OAK_FENCE);
        p.fill(-49,69,57,-49,69,70,Blocks.OAK_FENCE);
        lamp(p,-48,69,77);
    }
    private static void timberYard(Painter p) {
        foundation(p,-160,16,32,32,Blocks.GRASS_BLOCK);
        outline(p,-158,18,-130,46,Blocks.COARSE_DIRT);
        for(int z=18;z<45;z+=7){p.set(-161,69,z,Blocks.OAK_LOG);p.set(-127,69,z,Blocks.OAK_LOG);}
        p.fill(-164,68,19,-162,70,30,Blocks.OAK_LOG);
    }
    private static void dock(Painter p) {
        // Central X[0,128), Z[112,240) and the southern mouth stay entirely clear.
        if(!p.intersects(-12,76,140,248))return;
        foundation(p,16,80,96,32,Blocks.STONE_BRICKS);
        for(int x=16;x<112;x++)for(int z=80;z<112;z++)if(hash(x,z,70)%13==0)p.set(x,68,z,Blocks.CRACKED_STONE_BRICKS);
        for(int side:new int[]{-8,128}) {
            p.fill(side,63,108,side+7,64,239,Blocks.STONE_BRICKS);
            p.fill(side,65,108,side+7,65,239,Blocks.SPRUCE_PLANKS);
            for(int z=112;z<240;z+=16) {
                p.fill(side+1,13,z,side+2,64,z+1,Blocks.POLISHED_BASALT);
                p.fill(side+5,13,z,side+6,64,z+1,Blocks.POLISHED_BASALT);
                lamp(p,side+3,66,z+2);
                p.set(side+3,66,z+5,Blocks.POLISHED_BLACKSTONE_WALL);
            }
            int rail=side<0?side:side+7;
            p.fill(rail,66,108,rail,66,239,Blocks.SPRUCE_FENCE);
        }
        p.fill(-8,65,104,135,65,111,Blocks.SPRUCE_PLANKS);
        // Stepped north apron connects the Y68 island to the Y65 quays.
        p.fill(16,66,108,111,66,109,Blocks.STONE_BRICKS);
        p.fill(16,67,106,111,67,107,Blocks.STONE_BRICKS);
        p.fill(16,68,104,111,68,105,Blocks.STONE_BRICKS);
        // Low dry assembly pads stay above the apron; the middle remains unobstructed.
        outline(p,24,84,104,100,Blocks.LIGHT_GRAY_TERRACOTTA);
        p.fill(18,69,82,22,70,86,Blocks.BARREL);
        for(int x:new int[]{20,108})lamp(p,x,69,102);
        // Decorative, inert dockside jib confined to the east quay, not across the wet dock.
        p.fill(131,66,119,133,89,121,Blocks.POLISHED_BASALT);
        p.fill(131,89,118,134,90,133,Blocks.WAXED_EXPOSED_CUT_COPPER);
        p.fill(133,77,132,133,88,132,Blocks.CHAIN);
        p.set(133,76,132,Blocks.POLISHED_BLACKSTONE_SLAB);
    }
    private static void ruinFoundations(Painter p) {
        // Only scattered old footings: the future expansion plot is not paved flat.
        for(int x=140;x<=172;x+=16)for(int z=-56;z<=24;z+=16) {
            int y=IslandTerrain.groundHeight(x,z);
            p.fill(x,y,z,x+2,y+1,z+2,Blocks.MOSSY_STONE_BRICKS);
        }
        // Closed, scenic former quarry entrance; there is no functional mine here yet.
        int x=-150,z=-78,y=IslandTerrain.groundHeight(x,z);
        p.fill(x-5,y,z-2,x+5,y+7,z,Blocks.MOSSY_COBBLESTONE);
        p.fill(x-2,y+1,z,x+2,y+5,z,Blocks.DARK_OAK_PLANKS);
        p.fill(x-3,y+6,z,x+3,y+6,z,Blocks.STRIPPED_SPRUCE_LOG);
    }

    private static final class Painter {
        final ChunkAccess chunk; final int x0,z0;
        final BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        Painter(ChunkAccess chunk){this.chunk=chunk;x0=chunk.getPos().getMinBlockX();z0=chunk.getPos().getMinBlockZ();}
        boolean intersects(int x,int z,int x2,int z2){return x2>=x0&&x<x0+16&&z2>=z0&&z<z0+16;}
        void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
        void set(int x,int y,int z,BlockState state){
            if(x<x0||x>=x0+16||z<z0||z>=z0+16||y<chunk.getMinBuildHeight()||y>=chunk.getMaxBuildHeight())return;
            chunk.setBlockState(pos.set(x,y,z),state,false);
            chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG).update(x-x0,y,z-z0,state);
            chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG).update(x-x0,y,z-z0,state);
        }
        void fill(int x,int y,int z,int x2,int y2,int z2,Block block){
            if(!intersects(x,z,x2,z2))return;
            for(int px=Math.max(x,x0);px<=Math.min(x2,x0+15);px++)for(int pz=Math.max(z,z0);pz<=Math.min(z2,z0+15);pz++)
                for(int py=y;py<=y2;py++)set(px,py,pz,block);
        }
    }
}
