package io.github.low2uu.dbv.fleet;

import io.github.low2uu.dbv.fleet.telemetry.DepthReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DepthReferenceTest {
    @Test void submergedAndSurfacedUseSameWaterSurface() {
        assertEquals(62, DepthReference.surface(48, -64, 320, y -> y <= 62 ? 1 : 0));
        assertEquals(62, DepthReference.surface(70, -64, 320, y -> y <= 62 ? 1 : 0));
        assertEquals(14.5, DepthReference.depth(62, 48.5));
        assertEquals(14, DepthReference.displayed(62, 48.5));
        assertEquals(0, DepthReference.depth(62, 70.5));
    }
    @Test void missingSurfaceNeverBecomesZeroDepth() {
        assertEquals(Integer.MIN_VALUE, DepthReference.surface(48, -64, 320, y -> 0));
        assertEquals(Integer.MIN_VALUE, DepthReference.surface(48, -64, 320, y -> 1));
        assertTrue(Double.isNaN(DepthReference.depth(Integer.MIN_VALUE, 48.5)));
        assertEquals(-1, DepthReference.displayed(Integer.MIN_VALUE, 48.5));
    }
    @Test void solidBarrierDoesNotFindUnrelatedWaterBelow() {
        assertEquals(Integer.MIN_VALUE, DepthReference.surface(70, -64, 320, y -> y == 65 ? 2 : y < 65 ? 1 : 0));
    }
    @Test void probesStayInBuildBounds() {
        assertEquals(62, DepthReference.surface(500, -64, 80, y -> {
            assertTrue(y >= -64 && y < 80); return y <= 62 ? 1 : 0;
        }));
    }
}
