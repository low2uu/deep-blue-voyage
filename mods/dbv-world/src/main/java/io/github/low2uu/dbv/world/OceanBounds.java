package io.github.low2uu.dbv.world;

/** Central story square and starter-island geometry; no longer a generation boundary. */
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
    public static final int HARBOUR_BLEND = 96;
    public static double harbourWeight(int x,int z) {
        double dx=Math.max(Math.max(-16-x,x-151),0);
        double dz=Math.max(Math.max(76-z,z-303),0);
        double t=Math.clamp(Math.hypot(dx,dz)/HARBOUR_BLEND,0.0,1.0);
        return 1-t*t*(3-2*t);
    }
    private static double landWeight(int x,int z) {
        double t=Math.clamp((Math.hypot(x/256.0,z/192.0)-1.0)/0.35,0.0,1.0);
        return 1-t*t*(3-2*t);
    }
    public static double islandWeight(int x,int z) {
        return Math.max(landWeight(x,z),harbourWeight(x,z));
    }
    public static int terrainHeight(int x,int z,int natural) {
        double land=landWeight(x,z);
        double ground=natural*(1-land)+IslandTerrain.groundHeight(x,z)*land;
        // Anchor to the nearest dock edge; never abruptly drop the harbour override.
        double harbour=harbourWeight(x,z);
        int edgeX=Math.clamp(x,-16,151),edgeZ=Math.clamp(z,76,303);
        if(!harbour(x,z)&&IslandTerrain.groundHeight(x,z)>=124)return (int)Math.round(ground);
        return (int)Math.round(ground*(1-harbour)+IslandTerrain.groundHeight(edgeX,edgeZ)*harbour);
    }
    private OceanBounds() {}
}
