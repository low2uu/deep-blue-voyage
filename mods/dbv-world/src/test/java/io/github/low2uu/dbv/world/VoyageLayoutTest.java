package io.github.low2uu.dbv.world;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VoyageLayoutTest {
 @Test void manySeedsHaveAllSitesWithinBoundsAndNoOverlaps() {
  for(long seed=0;seed<1000;seed++) {
   var sites=VoyageLayout.sites(seed); assertEquals(27,sites.size());
   for(int i=0;i<sites.size();i++) {
    var s=sites.get(i); assertTrue(Math.abs(s.x())+s.extent()<5000);assertTrue(Math.abs(s.z())+s.extent()<5000);
    assertTrue(Math.hypot(s.x(),s.z())>=s.extent()+650);
    if(i<3){assertEquals(-1,s.ore());assertEquals(i,s.index());assertTrue(Math.abs(Math.hypot(s.x(),s.z())-(2400+i*800))<=201);}
    for(int j=0;j<i;j++){var p=sites.get(j);assertTrue(s.distance(p.x(),p.z())>=s.extent()+p.extent()+400);}
   }
  }
 }
 @Test void reproducibleAcrossCacheEvictionAndDifferentSeeds() {
  var before=VoyageLayout.sites(7667568505341616740L);
  for(long i=0;i<30;i++)VoyageLayout.sites(i);
  assertEquals(before,VoyageLayout.sites(7667568505341616740L));assertNotEquals(before,VoyageLayout.sites(2));
 }
 @Test void DenseVeinIsCoveredAndSingleAssignedOre() {
  for(var s:VoyageLayout.sites(7667568505341616740L))if(s.ore()>=0){
   assertTrue(s.ore()<7);int count=0;
   for(int x=s.x()-s.radius();x<=s.x()+s.radius();x++)for(int z=s.z()-s.radius();z<=s.z()+s.radius();z++)for(int y=94;y<=130;y++)if(VoyageLayout.vein(s,x,y,z)){assertTrue(y<VoyageLayout.top(s,x,z)-6);count++;}
   assertTrue(count>15000);
  }
 }
}
