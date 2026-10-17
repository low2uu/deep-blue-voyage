package io.github.low2uu.dbv.fleet.dock;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Compare transferred states without mistaking recalculated Create connections for lost blocks. */
final class TransferredBlockState {
    private TransferredBlockState() {}

    static boolean matches(String expectedBlock, String actualBlock,
                           Map<String, ?> expected, Map<String, ?> actual) {
        if (!expectedBlock.equals(actualBlock) || !expected.keySet().equals(actual.keySet())) return false;
        Set<String> derived = switch (expectedBlock) {
            case "create:fluid_pipe" -> Set.of("north", "south", "east", "west", "up", "down");
            case "create:clutch", "create:gearshift" -> Set.of("powered");
            // Create resets these in removeController(true) when a tank changes position,
            // then recomputes them as the multiblock reconnects. Fluid data is in the BE.
            case "create:fluid_tank" -> Set.of("top", "bottom", "shape");
            default -> Set.of();
        };
        return expected.entrySet().stream().allMatch(entry -> derived.contains(entry.getKey())
            || Objects.equals(entry.getValue(), actual.get(entry.getKey())));
    }
}
