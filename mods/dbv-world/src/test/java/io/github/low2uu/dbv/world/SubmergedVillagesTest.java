package io.github.low2uu.dbv.world;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubmergedVillagesTest {
    @Test void nestedScopeRestoresAfterFailure() {
        assertEquals(Heightmap.Types.WORLD_SURFACE_WG,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE_WG));
        boolean outer=SubmergedVillages.enter(true);
        try {
            assertEquals(Heightmap.Types.OCEAN_FLOOR_WG,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE_WG));
            boolean inner=SubmergedVillages.enter(false);
            try { assertEquals(Heightmap.Types.WORLD_SURFACE,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE)); }
            finally { SubmergedVillages.restore(inner); }
            assertEquals(Heightmap.Types.OCEAN_FLOOR,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE));
            assertEquals(Heightmap.Types.MOTION_BLOCKING,SubmergedVillages.projection(Heightmap.Types.MOTION_BLOCKING));
            throw new IllegalStateException("Simulated template failure");
        } catch(IllegalStateException expected) { assertEquals("Simulated template failure",expected.getMessage()); }
        finally { SubmergedVillages.restore(outer); }
        assertEquals(Heightmap.Types.WORLD_SURFACE_WG,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE_WG));
    }
    @Test void parallelWorldGenerationDoesNotInheritVillageProjection() {
        boolean before=SubmergedVillages.enter(true);
        try {
            assertEquals(Heightmap.Types.WORLD_SURFACE_WG,CompletableFuture.supplyAsync(
                ()->SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE_WG)).join());
            assertEquals(Heightmap.Types.OCEAN_FLOOR_WG,SubmergedVillages.projection(Heightmap.Types.WORLD_SURFACE_WG));
        } finally { SubmergedVillages.restore(before); }
    }
}
