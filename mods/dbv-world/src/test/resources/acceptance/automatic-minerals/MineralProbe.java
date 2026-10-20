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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.server.level.ServerLevel;
public final class MineralProbe {
 public static final long SEED=7667568505341616740L;
 static final List<ChunkPos> queue=new ArrayList<>();static int cursor=0;
 static final List<VoyageLayout.Site> sampled=new ArrayList<>();
 public static void create() {
  var mc=Minecraft.getInstance();
  var settings=new LevelSettings("深蓝远航·联合开发",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
  mc.createWorldOpenFlows().createFreshLevel("DBV-Minerals",settings,new WorldOptions(SEED,true,false),
   access->Objects.requireNonNull(access.registryOrThrow(Registries.WORLD_PRESET).get(ResourceLocation.parse("dbv_world:starter_island"))).createWorldDimensions(),mc.screen);
 }
 public static int prepare(ServerLevel level) {
  var gen=(FloodedWorldGenerator)level.getChunkSource().getGenerator();var ids=gen.minerals().ids();
  if(!new HashSet<>(ids).equals(Set.of("iron","copper","gold","coal","redstone","lapis","diamond","emerald","zinc","amethyst")))throw new IllegalStateException("Unexpected catalog "+ids);
  var local=gen.sites(SEED,0,0,0,0);
  var nearest=local.stream().filter(s->s.ore()>=0).sorted(Comparator.comparingDouble((VoyageLayout.Site s)->Math.hypot(s.x(),s.z()))).limit(3).toList();
  for(int i=0;i<3;i++)if(!ids.get(nearest.get(i).ore()).equals(List.of("iron","copper","gold").get(i)))throw new IllegalStateException("Starter order");
  sampled.addAll(nearest);var represented=new HashSet<String>();for(var s:sampled)represented.add(ids.get(s.ore()));
  for(int tx=0;tx<4;tx++)for(var s:gen.sites(SEED,tx*10000,0,tx*10000,0))if(s.ore()>=0&&represented.add(ids.get(s.ore())))sampled.add(s);
  if(represented.size()!=ids.size())throw new IllegalStateException("No test coverage for all minerals");
  sampled.add(gen.sites(SEED,10000,0,10000,0).get(3));
  var seen=new HashSet<Long>();
  for(var s:sampled)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)add(seen,(s.x()>>4)+dx,(s.z()>>4)+dz);
  for(int x:new int[]{4999,5000,5100,-5000,-5001,-5100,20000})add(seen,x>>4,0);
  return queue.size();
 }
 static void add(Set<Long> seen,int x,int z){if(seen.add(ChunkPos.asLong(x,z)))queue.add(new ChunkPos(x,z));}
 public static String advance(ServerLevel level){if(cursor==queue.size())return "done";var p=queue.get(cursor++);level.getChunk(p.x,p.z);return cursor+"/"+queue.size();}
 public static void verifyNaturalOrePreservation(ServerLevel level){
  var gen=(FloodedWorldGenerator)level.getChunkSource().getGenerator();
  var site=gen.sites(SEED,0,0,0,0).stream().filter(s->s.ore()>=0).findFirst().orElseThrow();
  var chunk=level.getChunk(site.x()>>4,site.z()>>4);var pos=new BlockPos(site.x(),112,site.z());
  var before=chunk.getBlockState(pos);
  chunk.setBlockState(pos,Blocks.EMERALD_ORE.defaultBlockState(),false);
  try{VoyageTerrain.decorate(gen,SEED,chunk);if(!chunk.getBlockState(pos).is(Blocks.EMERALD_ORE))throw new IllegalStateException("Existing natural ore overwritten");}
  finally{chunk.setBlockState(pos,before,false);}
 }
 public static JsonObject finish(ServerLevel level){
  var gen=(FloodedWorldGenerator)level.getChunkSource().getGenerator();var result=new JsonObject();var sites=new JsonArray();
  for(var s:sampled){var m=gen.minerals().get(s.ore());int count=0;var cp=new ChunkPos(s.x()>>4,s.z()>>4);var chunk=level.getChunk(cp.x,cp.z);
   for(int x=cp.getMinBlockX();x<=cp.getMaxBlockX();x++)for(int z=cp.getMinBlockZ();z<=cp.getMaxBlockZ();z++)for(int y=94;y<130;y++){
    var state=chunk.getBlockState(new BlockPos(x,y,z));if(state.is(m.oreBlock())){count++;if(m.crystal()&&!state.canSurvive(level,new BlockPos(x,y,z)))throw new IllegalStateException("Unsupported crystal");}
   }
   if(count==0)throw new IllegalStateException("No mineral "+m+" at "+s);
   var row=new JsonObject();row.addProperty("mineral",m.id());row.addProperty("x",s.x());row.addProperty("z",s.z());row.addProperty("sampledBlocks",count);sites.add(row);
  }
  for(int x:new int[]{4999,5000,5100,-5000,-5001,-5100,20000}){
   var c=level.getChunk(x>>4,0);if(c.getHeight(Heightmap.Types.OCEAN_FLOOR,x&15,0)<=-256)throw new IllegalStateException("Exterior void at "+x);
  }
  if(level.getWorldBorder().getSize()<59000000)throw new IllegalStateException("Exploration still bounded");
  result.addProperty("passed",true);result.addProperty("seed",SEED);result.addProperty("generatedChunks",queue.size());result.addProperty("borderSize",level.getWorldBorder().getSize());result.add("sites",sites);result.add("minerals",new Gson().toJsonTree(gen.minerals().minerals()));return result;
 }
}
