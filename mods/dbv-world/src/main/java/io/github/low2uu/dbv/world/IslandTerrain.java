package io.github.low2uu.dbv.world;

/** Deterministic first-release terrain. All coordinates are overworld block coordinates. */
public final class IslandTerrain {
    public static final int SEA_LEVEL = WorldHeights.SEA_LEVEL;
    public static final int SEABED_Y = WorldHeights.fromLegacy(-45);
    public static final int PLATFORM_Y = WorldHeights.fromLegacy(68);
    public static final int SPAWN_X = -84;
    public static final int SPAWN_Z = -28;
    private static final int[][] PADS = {{-112,-64,-64,-24},{-48,-64,-8,-32},{8,-16,40,16},
            {80,-64,128,-16},{80,-112,112,-80},{-160,16,-128,48},
            {-80,56,-48,88},{-24,56,-8,72},{16,80,112,112}};

    private IslandTerrain() {}

    public static int groundHeight(int x, int z) {
        return WorldHeights.fromLegacy(legacyGroundHeight(x, z));
    }

    /** Blueprint-space terrain, retained so the existing island is translated without reshaping it. */
    static int legacyGroundHeight(int x, int z) {
        double radius = Math.sqrt(square(x / 256.0) + square(z / 192.0));
        radius += 0.012 * Math.sin(x / 37.0) * Math.sin(z / 29.0);
        int height;
        if (radius <= 0.76) {
            height = (int) Math.round(inlandHeight(x,z));
        } else if (radius <= 1.0) {
            double inland = inlandHeight(x,z);
            double t = (radius - 0.76) / 0.24;
            height = (int) Math.round(inland * (1 - t) + 61 * t);
        } else {
            height = Math.max(-45, 61 - (int) Math.floor((radius - 1.0) / 0.55 * 106.0));
        }
        // Small foundations blend into the landscape; the remainder is rolling terrain.
        for (int[] p : PADS) {
            double dx = Math.max(Math.max(p[0] - x, 0), x - (p[2] - 1));
            double dz = Math.max(Math.max(p[1] - z, 0), z - (p[3] - 1));
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < 12) {
                double t = distance / 12;
                t = t * t * (3 - 2 * t);
                height = (int) Math.round(68 + (height - 68) * t);
            }
        }
        // Ease down the shores beside the dock instead of exposing a rectangular cliff.
        if (z >= 112 && x > -24 && x < 152) {
            double side = Math.max(Math.max(-x, x - 127), 0) / 24.0;
            height = Math.min(height, (int) Math.round(12 + 56 * side));
        }
        if (x >= 0 && x < 128 && z >= 112) {
            height = Math.min(height, 12);
        }
        return height;
    }

    private static double square(double n) { return n * n; }
    private static double inlandHeight(int x,int z) {
        return 71 + 4 * Math.sin(x / 43.0) * Math.cos(z / 39.0)
            + 2.2 * Math.sin((x + z) / 23.0)
            + 18 * Math.exp(-square((x + 120) / 75.0) - square((z + 116) / 38.0));
    }
}
