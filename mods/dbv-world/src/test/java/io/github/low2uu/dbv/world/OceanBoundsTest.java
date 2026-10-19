package io.github.low2uu.dbv.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OceanBoundsTest {
    @Test void halfOpenSquareAndPartialChunks(){
        assertTrue(OceanBounds.contains(-5000,-5000));
        assertTrue(OceanBounds.contains(4999,4999));
        assertFalse(OceanBounds.contains(5000,0));
        assertFalse(OceanBounds.contains(-5001,0));
        assertTrue(OceanBounds.chunkIntersects(-313,312));
        assertFalse(OceanBounds.chunkIntersects(-314,0));
        assertFalse(OceanBounds.chunkIntersects(313,0));
    }
    @Test void nearIslandSeaIsNotTheLegacyRectangularExclusion(){
        assertTrue(OceanBounds.protectedSurface(207,-64));
        assertTrue(OceanBounds.protectedSurface(127,288));
        assertTrue(OceanBounds.protectedSurface(-84,-28));
        assertFalse(OceanBounds.protectedSurface(272,272));
        assertFalse(OceanBounds.protectedSurface(-272,-208));
        assertEquals(0,OceanBounds.islandWeight(272,272));
        assertEquals(1,OceanBounds.islandWeight(0,0));
        assertTrue(OceanBounds.islandWeight(288,0)>0);
        assertTrue(OceanBounds.islandWeight(288,0)<1);
        assertEquals(0,OceanBounds.islandWeight(352,0));
    }
    @Test void dockBlendHasNoCliffAtSouthEdge(){
        assertEquals(1,OceanBounds.harbourWeight(64,303));
        assertEquals(0,OceanBounds.harbourWeight(64,399));
        int previous=OceanBounds.terrainHeight(64,303,72);
        for(int z=304;z<=400;z++){
            int height=OceanBounds.terrainHeight(64,z,72);
            assertTrue(Math.abs(height-previous)<=2,"cliff at z="+z);
            previous=height;
        }
        assertEquals(72,previous);
        assertEquals(IslandTerrain.groundHeight(64,160),OceanBounds.terrainHeight(64,160,100));
    }
    @Test void dockSidesAlsoFadeWithoutChangingTheWorkingBasin(){
        for(int direction:new int[]{-1,1}){
            int edge=direction<0?-16:151;
            int previous=OceanBounds.terrainHeight(edge,280,72);
            for(int distance=1;distance<=96;distance++){
                int next=OceanBounds.terrainHeight(edge+direction*distance,280,72);
                assertTrue(Math.abs(next-previous)<=2,"side cliff at "+distance);
                previous=next;
            }
            assertEquals(72,previous);
        }
    }
}
