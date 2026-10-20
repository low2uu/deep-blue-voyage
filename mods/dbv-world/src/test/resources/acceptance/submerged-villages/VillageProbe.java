package dbv.acceptance;
import java.util.*;
import com.google.gson.*;
import io.github.low2uu.dbv.world.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.server.level.ServerLevel;
public final class VillageProbe {
 public static final long SEED=7667568505341616740L;
 static final List<ChunkPos> queue=new ArrayList<>();static int cursor;
 static final List<StructureStart> villages=new ArrayList<>();
 public static void create() {
  var mc=Minecraft.getInstance();
  var settings=new LevelSettings("淹没村庄生成验收",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
  mc.createWorldOpenFlows().createFreshLevel("DBV-Villages",settings,new WorldOptions(SEED,true,false),
   access->Objects.requireNonNull(access.registryOrThrow(Registries.WORLD_PRESET).get(ResourceLocation.parse("dbv_world:starter_island"))).createWorldDimensions(),mc.screen);
 }
 public static int prepare(ServerLevel level) {
  if(level.getSeed()!=SEED)throw new IllegalStateException("Wrong seed");
  var seen=new HashSet<Long>();
  for(int[] cp:new int[][]{{38,38},{156,-26}}) {
   var c=level.getChunk(cp[0],cp[1]);
   var start=c.getAllStarts().values().stream().filter(s->s.isValid()&&SubmergedVillages.isVillage(level.registryAccess(),s.getStructure())).findFirst().orElseThrow();
   villages.add(start);var b=start.getBoundingBox();
   for(int x=(b.minX()>>4)-1;x<=(b.maxX()>>4)+1;x++)for(int z=(b.minZ()>>4)-1;z<=(b.maxZ()>>4)+1;z++)
    if(seen.add(ChunkPos.asLong(x,z)))queue.add(new ChunkPos(x,z));
  }
  return queue.size();
 }
 public static String advance(ServerLevel level) {if(cursor==queue.size())return "done";var cp=queue.get(cursor++);level.getChunk(cp.x,cp.z);return cursor+"/"+queue.size();}
 public static JsonObject finish(ServerLevel level) {
  var result=new JsonObject();var rows=new JsonArray();var gen=level.getChunkSource().getGenerator();var random=level.getChunkSource().randomState();
  for(var start:villages) {
   var b=start.getPieces().get(0).getBoundingBox();int x=(b.minX()+b.maxX())/2,z=(b.minZ()+b.maxZ())/2;
   int floor=gen.getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,level,random);
   int surface=gen.getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,level,random);
   if(surface!=128||Math.abs(b.minY()+1-floor)>1||b.minY()>=120)throw new IllegalStateException("Village not on terrain "+b+" floor="+floor+" surface="+surface);
   long air=0,water=0,paths=0,highPaths=0,roads=0,submergedRoadColumns=0;
   var pos=new BlockPos.MutableBlockPos();var checked=new HashSet<Long>();
   for(var piece:start.getPieces()) {var box=piece.getBoundingBox();
    if(piece instanceof PoolElementStructurePiece pool && pool.getElement().getProjection()==net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING) {
     roads++;
     for(int xx=box.minX();xx<=box.maxX();xx++)for(int zz=box.minZ();zz<=box.maxZ();zz++) {
      if(!level.getBlockState(pos.set(xx,127,zz)).is(Blocks.WATER))throw new IllegalStateException("Floating road surface at "+pos);
      int ground=level.getHeight(Heightmap.Types.OCEAN_FLOOR,xx,zz)-1;
      if(ground<120 && !level.getBlockState(pos.set(xx,ground,zz)).isAir() && level.getBlockState(pos.set(xx,127,zz)).is(Blocks.WATER))submergedRoadColumns++;
     }
    }
    for(int xx=box.minX();xx<=box.maxX();xx++)for(int zz=box.minZ();zz<=box.maxZ();zz++)for(int y=Math.max(-256,box.minY());y<=Math.min(127,box.maxY());y++) {
     pos.set(xx,y,zz);if(!checked.add(pos.asLong()))continue;var block=level.getBlockState(pos);
     if(block.isAir())air++;if(block.is(Blocks.WATER))water++;
     if(block.is(Blocks.DIRT_PATH)){paths++;if(y>=125)highPaths++;}
    }
   }
   if(air!=0||water==0||roads==0||submergedRoadColumns==0||highPaths!=0)throw new IllegalStateException("Invalid flooded village air="+air+" water="+water+" paths="+paths+" highPaths="+highPaths);
   var row=new JsonObject();row.addProperty("startChunk",start.getChunkPos().toString());row.addProperty("pieces",start.getPieces().size());row.addProperty("townBaseY",b.minY());row.addProperty("terrainFirstFreeY",floor);row.addProperty("normalWorldSurfaceY",surface);row.addProperty("remainingAir",air);row.addProperty("water",water);row.addProperty("survivingDirtPaths",paths);row.addProperty("terrainMatchingRoadPieces",roads);row.addProperty("submergedRoadColumns",submergedRoadColumns);rows.add(row);
  }
  result.addProperty("passed",true);result.addProperty("createdFromEmptyFolder",true);result.addProperty("seed",SEED);result.addProperty("generatedChunks",queue.size());result.add("villages",rows);return result;
 }
}
