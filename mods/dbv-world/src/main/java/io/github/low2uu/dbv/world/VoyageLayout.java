package io.github.low2uu.dbv.world;

import java.util.*;

/** Seed-stable development layout. Changing this algorithm requires an explicit save migration. */
public final class VoyageLayout {
    public record Site(int index, String kind, int x, int z, int radius, int y, int ore) {
        public double distance(int px,int pz) { return Math.hypot(px-x,pz-z); }
        public int extent() { return ore<0?radius+2:(int)Math.ceil(radius*1.5); }
    }
    private static final Map<Long,List<Site>> CACHE=new LinkedHashMap<>();
    public static synchronized List<Site> sites(long seed) {
        var cached=CACHE.get(seed); if(cached!=null)return cached;
        var random=new Random(seed^0x44425649534c414eL);
        var result=new ArrayList<Site>();
        String[] kinds={"急流海域","深海盆地","冰川海域"};
        for(int i=0;i<3;i++) {
            boolean added=false;
            for(int tries=0;tries<10000;tries++) {
                double angle=random.nextDouble()*Math.PI*2;
                int distance=2200+i*800+random.nextInt(401);
                var s=new Site(i,kinds[i],(int)Math.round(Math.cos(angle)*distance),(int)Math.round(Math.sin(angle)*distance),i==0?450:550,new int[]{100,-160,136}[i],-1);
                if(fits(s,result)){result.add(s);added=true;break;}
            }
            if(!added)throw new IllegalStateException("Cannot place main region for seed "+seed);
        }
        for(int i=0;i<24;i++) {
            boolean added=false;
            for(int tries=0;tries<20000;tries++) {
                int radius=48+random.nextInt(49),x,z;
                // Fifteen islands near the three outbound routes; the rest explore open water.
                if(i<15 && tries<3000) {
                    var main=result.get(i%3); double t=0.25+random.nextDouble()*0.62;
                    double length=Math.hypot(main.x,main.z),side=(random.nextBoolean()?1:-1)*(180+random.nextInt(171));
                    x=(int)(main.x*t-main.z/length*side); z=(int)(main.z*t+main.x/length*side);
                } else {x=random.nextInt(9201)-4600;z=random.nextInt(9201)-4600;}
                var s=new Site(i+3,i<10?"补给岛":i<18?"工业岛":"挑战岛",x,z,radius,0,i%7);
                if(fits(s,result)){result.add(s);added=true;break;}
            }
            if(!added)throw new IllegalStateException("Cannot place side island for seed "+seed);
        }
        var immutable=List.copyOf(result); if(CACHE.size()>=16)CACHE.remove(CACHE.keySet().iterator().next()); CACHE.put(seed,immutable); return immutable;
    }
    private static boolean fits(Site s,List<Site> previous) {
        if(Math.abs(s.x)+s.extent()>=5000||Math.abs(s.z)+s.extent()>=5000||Math.hypot(s.x,s.z)<s.extent()+650)return false;
        for(var p:previous)if(s.distance(p.x,p.z)<Math.max(s.ore<0&&p.ore<0?1600:800,s.extent()+p.extent()+400))return false;
        return true;
    }
    public static boolean protects(long seed,int x,int y,int z) {
        if(y<72)return false;
        for(var s:sites(seed))if(s.ore>=0 && s.distance(x,z)<=s.extent())return true;
        return false;
    }
    public static double shape(Site s,int x,int z) {
        double dx=x-s.x,dz=z-s.z,angle=Math.atan2(dz,dx);
        return Math.hypot(dx,dz)/(s.radius*(0.91+0.055*Math.sin(angle*3+s.index)+0.035*Math.sin(angle*7-s.index)));
    }
    public static int top(Site s,int x,int z) {
        double r=shape(s,x,z);
        return (int)Math.round(126+29*Math.max(0,1-r*r)+2*Math.sin(x*.09)*Math.sin(z*.08)*Math.max(0,1-r));
    }
    /** One continuous, dense, irregular ellipsoid per island, beneath at least six blocks of cover. */
    public static boolean vein(Site s,int x,int y,int z) {
        double dx=(x-s.x)/(s.radius*.43),dz=(z-s.z)/(s.radius*.36),dy=(y-112)/17.0;
        return dx*dx+dz*dz+dy*dy < 0.87+0.09*Math.sin(x*.3)*Math.sin(z*.27);
    }
}
