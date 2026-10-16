package io.github.low2uu.dbv.world;

/** Public ocean-world height contract. Legacy blueprints use the former sea level of 63. */
public final class WorldHeights {
    public static final int SEA_LEVEL = 128;
    public static final int MIN_Y = -256;
    public static final int MAX_Y_EXCLUSIVE = 320;
    public static final int HEIGHT = MAX_Y_EXCLUSIVE - MIN_Y;
    public static final int LEGACY_OFFSET = SEA_LEVEL - 63;
    private WorldHeights() {}
    public static int fromLegacy(int y) { return y + LEGACY_OFFSET; }
}
