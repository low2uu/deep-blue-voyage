package io.github.low2uu.dbv.world;

/** Temporary development area; independent of the eventual public-release story range. */
public final class OceanBounds {
    public static final int HALF_SIZE=5000;
    public static boolean contains(int x,int z) { return x>=-HALF_SIZE&&x<HALF_SIZE&&z>=-HALF_SIZE&&z<HALF_SIZE; }
    public static boolean chunkIntersects(int x,int z) {
        return x*16 < HALF_SIZE && x*16+15 >= -HALF_SIZE && z*16 < HALF_SIZE && z*16+15 >= -HALF_SIZE;
    }
    // Whole chunks covering all existing architecture, oil signs and the harbour exit.
    public static boolean preserved(int x,int z) { return x>=-288&&x<288&&z>=-224&&z<304; }
    /** Keep land and the working harbour, not the former rectangular empty sea. */
    public static boolean protectedSurface(int x,int z) {
        return IslandTerrain.groundHeight(x,z)>=124 || harbour(x,z)
                || (x>=176&&x<208&&z>=-64&&z<-32);
    }
    private static boolean harbour(int x,int z) { return x>=-16&&x<152&&z>=76&&z<304; }
    public static double islandWeight(int x,int z) {
        if(harbour(x,z))return 1;
        double radius=Math.hypot(x/256.0,z/192.0);
        double t=Math.clamp((radius-1.0)/0.35,0.0,1.0);
        return 1-t*t*(3-2*t);
    }
    private OceanBounds() {}
}
