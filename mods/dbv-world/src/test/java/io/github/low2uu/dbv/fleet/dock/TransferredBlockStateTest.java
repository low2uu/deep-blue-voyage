package io.github.low2uu.dbv.fleet.dock;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TransferredBlockStateTest {
    @Test void movedTankMayTemporarilyLoseItsMultiblockShape() {
        var before=Map.of("bottom","true","shape","window_nw","top","false");
        var after=Map.of("bottom","true","shape","window","top","true");
        assertTrue(TransferredBlockState.matches("create:fluid_tank","create:fluid_tank",before,after));
        assertTrue(TransferredBlockState.matches("create:fluid_tank","create:fluid_tank",after,before));
        assertTrue(TransferredBlockState.matches("create:fluid_tank","create:fluid_tank",
            Map.of("bottom","false","top","false","shape","window_se"),after));
    }

    @Test void aMissingOrDifferentBlockStillFails() {
        var state=Map.of("bottom","true","shape","window","top","true");
        assertFalse(TransferredBlockState.matches("create:fluid_tank","minecraft:air",state,Map.of()));
        assertFalse(TransferredBlockState.matches("create:fluid_tank","other:fluid_tank",state,state));
    }

    @Test void unknownTankPropertiesAndPropertySetsStillMustMatch() {
        assertFalse(TransferredBlockState.matches("create:fluid_tank","create:fluid_tank",
            Map.of("shape","window","waterlogged","true"),Map.of("shape","window_nw","waterlogged","false")));
        assertFalse(TransferredBlockState.matches("create:fluid_tank","create:fluid_tank",
            Map.of("shape","window","top","true"),Map.of("shape","window")));
    }

    @Test void derivedPropertyNamesAreNotExemptOnOtherBlocks() {
        assertFalse(TransferredBlockState.matches("minecraft:oak_stairs","minecraft:oak_stairs",
            Map.of("shape","straight"),Map.of("shape","inner_left")));
        assertFalse(TransferredBlockState.matches("other:fluid_tank","other:fluid_tank",
            Map.of("top","true"),Map.of("top","false")));
    }

    @Test void existingCreateExceptionsKeepOrientationAndWaterloggingChecks() {
        assertTrue(TransferredBlockState.matches("create:fluid_pipe","create:fluid_pipe",
            Map.of("north","true","waterlogged","false"),Map.of("north","false","waterlogged","false")));
        assertFalse(TransferredBlockState.matches("create:fluid_pipe","create:fluid_pipe",
            Map.of("north","true","waterlogged","false"),Map.of("north","false","waterlogged","true")));
        for(var block:new String[]{"create:clutch","create:gearshift"}) {
            assertTrue(TransferredBlockState.matches(block,block,
                Map.of("powered","true","axis","x"),Map.of("powered","false","axis","x")));
            assertFalse(TransferredBlockState.matches(block,block,
                Map.of("powered","true","axis","x"),Map.of("powered","false","axis","z")));
        }
    }
}
