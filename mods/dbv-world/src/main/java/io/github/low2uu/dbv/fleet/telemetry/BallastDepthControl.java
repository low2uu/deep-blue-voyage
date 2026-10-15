package io.github.low2uu.dbv.fleet.telemetry;

/** Depth/velocity feedback with acceleration damping. The actuator is the real ballast pump. */
public final class BallastDepthControl {
    private double filteredVelocity;
    private boolean initialized;
    public void reset(){initialized=false;}
    public double targetFill(double depth,double target,double velocity,double fill,double seconds,int speedMode){
        if(!Double.isFinite(depth)||!Double.isFinite(velocity)||!Double.isFinite(fill)||seconds<=0||seconds>1){reset();return fill;}
        if(!initialized){filteredVelocity=velocity;initialized=true;}
        double previous=filteredVelocity;
        filteredVelocity+=.5*(velocity-filteredVelocity);
        double acceleration=(filteredVelocity-previous)/seconds;
        double limit=speedMode==0?.6:speedMode==2?1.5:1;
        double desired=Math.clamp((depth-target)*.2,-limit,limit);
        double signal=desired-filteredVelocity-1.5*acceleration;
        return Math.clamp(fill+(signal>.06?-.015:signal<-.06?.015:0),0,1);
    }
}
