package dbv.acceptance;
import java.util.*;
import com.google.gson.*;
import io.github.low2uu.dbv.world.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
public final class MapCheck {
 static final List<VoyageLayout.Site> selected=new ArrayList<>();
 static final List<ChunkPos> queue=new ArrayList<>();static int cursor;
 static final Map<VoyageLayout.Site,long[]> counts=new LinkedHashMap<>();
 public static int prepare(ServerLevel level){
  var gen=(FloodedWorldGenerator)level.getChunkSource().getGenerator();long seed=level.getSeed();
  selected.addAll(gen.sites(seed,0,0,0,0).stream().filter(s->s.ore()>=0).sorted(Comparator.comparingDouble((VoyageLayout.Site s)->Math.hypot(s.x(),s.z()))).limit(3).toList());
  for(int i=0;i<3;i++)if(!gen.minerals().get(selected.get(i).ore()).id().equals(List.of("iron","copper","gold").get(i)))throw new IllegalStateException("Nearest mineral mismatch");
  for(int[] dir:new int[][]{{1,0},{-1,0},{0,-1},{0,1}}){
   var candidates=gen.sites(seed,dir[0]*10000,dir[1]*10000,dir[0]*10000,dir[1]*10000).stream()
    .filter(s->s.ore()>=0&&(s.x()*dir[0]+s.z()*dir[1])>10000)
    .sorted(Comparator.comparingInt((VoyageLayout.Site s)->dir[0]==1&&!gen.minerals().get(s.ore()).id().equals("zinc")?1:0).thenComparingDouble(s->Math.hypot(s.x(),s.z()))).toList();
   selected.add(candidates.get(0));
  }
  var seen=new HashSet<Long>();
  for(var s:selected){counts.put(s,new long[3]);
   for(int x=(s.x()-s.extent())>>4;x<=(s.x()+s.extent())>>4;x++)for(int z=(s.z()-s.extent())>>4;z<=(s.z()+s.extent())>>4;z++)
    if(VoyageTerrain.touches(s,x,z)&&seen.add(ChunkPos.asLong(x,z)))queue.add(new ChunkPos(x,z));
  }return queue.size();
 }
 public static String advance(ServerLevel level){
  if(cursor==queue.size())return "done";
  var cp=queue.get(cursor++);var chunk=level.getChunk(cp.x,cp.z);var gen=(FloodedWorldGenerator)level.getChunkSource().getGenerator();var pos=new BlockPos.MutableBlockPos();
  for(var s:selected)if(VoyageTerrain.touches(s,cp.x,cp.z)){
   var m=gen.minerals().get(s.ore());var ore=m.oreBlock();var raw=m.rawBlock();var total=counts.get(s);
   for(int x=cp.getMinBlockX();x<=cp.getMaxBlockX();x++)for(int z=cp.getMinBlockZ();z<=cp.getMaxBlockZ();z++)for(int y=94;y<=130;y++)if(VoyageLayout.vein(s,x,y,z)){
    total[2]++;var state=chunk.getBlockState(pos.set(x,y,z));if(state.is(ore))total[0]++;else if(raw!=null&&state.is(raw))total[1]++;
   }
  }return cursor+"/"+queue.size();
 }
 static JsonObject row(ServerLevel level,VoyageLayout.Site s){
  var g=(FloodedWorldGenerator)level.getChunkSource().getGenerator();var r=new JsonObject();
  r.addProperty("index",s.index());r.addProperty("x",s.x());r.addProperty("z",s.z());r.addProperty("radius",s.radius());r.addProperty("kind",s.kind());r.addProperty("y",s.y());
  r.addProperty("mineral",s.ore()<0?"main":g.minerals().get(s.ore()).id());return r;
 }
 public static JsonObject finish(ServerLevel level){
  var result=new JsonObject();var sites=new JsonArray();var layout=new JsonArray();var g=(FloodedWorldGenerator)level.getChunkSource().getGenerator();
  for(var s:selected){var c=counts.get(s);if(c[0]<100)throw new IllegalStateException("Missing vein "+s);level.getChunk(s.x()>>4,s.z()>>4);
   int top=level.getHeight(Heightmap.Types.OCEAN_FLOOR,s.x(),s.z())-1;if(top<128)throw new IllegalStateException("No surfaced island "+s);
   var r=row(level,s);r.addProperty("oreBlocks",c[0]);r.addProperty("rawBlocks",c[1]);r.addProperty("veinVolume",c[2]);r.addProperty("surfaceY",top);r.addProperty("distance",Math.round(Math.hypot(s.x(),s.z())));sites.add(r);
  }
  for(int tx=-1;tx<=1;tx++)for(int tz=-1;tz<=1;tz++)for(var s:g.sites(level.getSeed(),tx*10000,tz*10000,tx*10000,tz*10000))layout.add(row(level,s));
  result.addProperty("passed",true);result.addProperty("seed",level.getSeed());result.addProperty("checkedChunks",queue.size());result.addProperty("borderSize",level.getWorldBorder().getSize());result.add("checkedSites",sites);result.add("seedLayout",layout);return result;
 }
}
