package io.github.low2uu.dbv.fleet.telemetry;

import java.util.function.IntUnaryOperator;

/** Water top is stored as a block Y; the actual surface is one metre above it. */
public final class DepthReference {
    private DepthReference() {}

    public static double depth(int waterTop, double controllerY) {
        return waterTop == Integer.MIN_VALUE ? Double.NaN : Math.max(0, waterTop + 1.0 - controllerY);
    }

    public static int displayed(int waterTop, double controllerY) {
        double depth = depth(waterTop, controllerY);
        return Double.isNaN(depth) ? -1 : (int)Math.floor(depth);
    }

    /** 0=air, 1=water, 2=solid. Never probe outside build bounds or beyond 400 blocks. */
    public static int surface(int referenceY, int minY, int maxY, IntUnaryOperator column) {
        int start = Math.max(minY, Math.min(maxY - 1, referenceY));
        int bottom = Math.max(minY, start - 400), top = Math.min(maxY - 1, start + 400);
        int water = Integer.MIN_VALUE;
        // Handles surfacing: an above-water controller must still see the water below.
        for (int y = start; y >= bottom; y--) {
            int material = column.applyAsInt(y);
            if (material == 1) { water = y; break; }
            if (material == 2) break;
        }
        if (water == Integer.MIN_VALUE) return water;
        for (int y = water + 1; y <= top; y++) {
            int material = column.applyAsInt(y);
            if (material == 0) return y - 1;
            if (material == 2) return Integer.MIN_VALUE;
        }
        // No observed water/air boundary; do not invent a surface at the scan limit.
        return Integer.MIN_VALUE;
    }
}
