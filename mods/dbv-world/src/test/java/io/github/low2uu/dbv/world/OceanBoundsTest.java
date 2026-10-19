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
}
