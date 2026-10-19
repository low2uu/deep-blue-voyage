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
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
public final class FreshWorldProbe {
 public static final long SEED=1357924680L;
 static final List<ChunkPos> queue=new ArrayList<>();static int cursor=0;
 public static void create() {
  var mc=Minecraft.getInstance();
  var settings=new LevelSettings("新建世界生成验收",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
  mc.createWorldOpenFlows().createFreshLevel("DBV-Fresh-Seed",settings,new WorldOptions(SEED,true,false),
   access->Objects.requireNonNull(access.registryOrThrow(Registries.WORLD_PRESET).get(ResourceLocation.parse("dbv_world:starter_island"))).createWorldDimensions(),mc.screen);
 }
 public static int prepare(ServerLevel level) {
  if(level.getSeed()!=SEED||!(level.getChunkSource().getGenerator() instanceof FloodedWorldGenerator))throw new IllegalStateException("Wrong fresh generator/seed");
  var seen=new HashSet<Long>();
  for(var s:VoyageLayout.sites(SEED)){
   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)add(seen,(s.x()>>4)+dx,(s.z()>>4)+dz);
   if(s.ore()<0)for(int[] d:new int[][]{{s.radius(),0},{-s.radius(),0},{0,s.radius()},{0,-s.radius()}})add(seen,(s.x()+d[0])>>4,(s.z()+d[1])>>4);
   else {add(seen,(s.x()+s.radius())>>4,s.z()>>4);add(seen,s.x()>>4,(s.z()-s.radius())>>4);}
  }return queue.size();
 }
 static void add(Set<Long> seen,int x,int z){if(seen.add(ChunkPos.asLong(x,z)))queue.add(new ChunkPos(x,z));}
 public static String advance(ServerLevel level) {
  if(cursor==queue.size())return "done";
  var cp=queue.get(cursor++);var c=level.getChunk(cp.x,cp.z);var p=new BlockPos.MutableBlockPos();
  String[] names={"iron","copper","coal","gold","redstone","lapis","zinc"};
  for(var s:VoyageLayout.sites(SEED))if(s.ore()>=0&&VoyageTerrain.touches(s,cp.x,cp.z))
   for(int x=cp.getMinBlockX();x<=cp.getMaxBlockX();x++)for(int z=cp.getMinBlockZ();z<=cp.getMaxBlockZ();z++)if(VoyageLayout.shape(s,x,z)<1)for(int y=72;y<VoyageLayout.top(s,x,z)-3;y++) {
    var state=c.getBlockState(p.set(x,y,z));var id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();String ns=s.ore()==6?"create:":"minecraft:";
    boolean mineral=id.endsWith("_ore")||id.contains(":raw_");
    if(mineral&&!id.equals(ns+names[s.ore()]+"_ore")&&!id.equals(ns+"raw_"+names[s.ore()]+"_block"))throw new IllegalStateException("Fresh mixed ore "+p+id);
    if(VoyageLayout.vein(s,x,y,z)&&!mineral)throw new IllegalStateException("Fresh missing vein "+p);
   }
  return cursor+"/"+queue.size();
 }
 public static JsonObject finish(ServerLevel level) {
  var result=new JsonObject();var sites=new JsonArray();
  for(var s:VoyageLayout.sites(SEED)) {
   level.getChunk(s.x()>>4,s.z()>>4);
   if(s.ore()<0){if(!level.getBlockState(new BlockPos(s.x(),s.y(),s.z())).is(Blocks.DIAMOND_BLOCK))throw new IllegalStateException("Fresh main missing");
    for(int[] d:new int[][]{{s.radius(),0},{-s.radius(),0},{0,s.radius()},{0,-s.radius()}}){level.getChunk((s.x()+d[0])>>4,(s.z()+d[1])>>4);if(!level.getBlockState(new BlockPos(s.x()+d[0],128,s.z()+d[1])).is(Blocks.DIAMOND_BLOCK))throw new IllegalStateException("Fresh boundary missing");}
   }
   else {String id=BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(s.x(),112,s.z())).getBlock()).toString();if(!id.contains(new String[]{"iron","copper","coal","gold","redstone","lapis","zinc"}[s.ore()]))throw new IllegalStateException("Fresh ore mismatch");}
   var row=new JsonObject();row.addProperty("index",s.index());row.addProperty("x",s.x());row.addProperty("z",s.z());row.addProperty("ore",s.ore());sites.add(row);
  }
  result.addProperty("passed",true);result.addProperty("createdFromEmptyFolder",true);result.addProperty("seed",SEED);result.addProperty("sampledChunks",queue.size());result.add("sites",sites);return result;
 }
}
