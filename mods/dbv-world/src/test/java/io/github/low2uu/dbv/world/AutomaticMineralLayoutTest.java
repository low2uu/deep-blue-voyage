package io.github.low2uu.dbv.world;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AutomaticMineralLayoutTest {
 private static final List<String> POOL=List.of("amethyst","coal","copper","diamond","emerald","gold","iron","lapis","redstone","zinc");
 @Test void nearestThreeAreIronCopperGoldAcrossSeeds(){
  for(long seed=0;seed<1000;seed++){
   var sites=VoyageLayout.assignedSites(seed,0,0,POOL).stream().filter(s->s.ore()>=0).sorted(Comparator.comparingDouble((VoyageLayout.Site s)->Math.hypot(s.x(),s.z()))).toList();
   assertEquals(24,sites.size());
   for(int i=0;i<3;i++)assertEquals(List.of("iron","copper","gold").get(i),POOL.get(sites.get(i).ore()));
  }
 }
 @Test void outerTilesAreSeedStableAndSeparatedAcrossBoundaries(){
  var all=new ArrayList<VoyageLayout.Site>();
  for(int tx=-1;tx<=1;tx++)for(int tz=-1;tz<=1;tz++){
   var sites=VoyageLayout.assignedSites(428L,tx,tz,POOL);assertEquals(27,sites.size());
   assertEquals(sites,VoyageLayout.assignedSites(428L,tx,tz,POOL));
   assertNotEquals(sites,VoyageLayout.assignedSites(429L,tx,tz,POOL));
   for(var s:sites){assertEquals(tx,VoyageLayout.tile(s.x()));assertEquals(tz,VoyageLayout.tile(s.z()));assertTrue(s.ore()<POOL.size());}
   all.addAll(sites);
  }
  for(int i=0;i<all.size();i++)for(int j=0;j<i;j++){
   var a=all.get(i);var b=all.get(j);assertTrue(a.distance(b.x(),b.z())>=a.extent()+b.extent()+400);
  }
 }
 @Test void negativeAndPositiveBoundaryCoordinatesUseTheCorrectTile(){
  assertEquals(0,VoyageLayout.tile(-5000));assertEquals(-1,VoyageLayout.tile(-5001));
  assertEquals(0,VoyageLayout.tile(4999));assertEquals(1,VoyageLayout.tile(5000));
 }
 @Test void randomOuterMineralsCanUseTheWholePool(){
  var found=new HashSet<Integer>();
  for(int tx=1;tx<=20;tx++)for(var s:VoyageLayout.assignedSites(428,tx,0,POOL))if(s.ore()>=0)found.add(s.ore());
  assertEquals(POOL.size(),found.size());
 }
}
