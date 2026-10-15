package io.github.low2uu.dbv.fleet;

import io.github.low2uu.dbv.fleet.telemetry.BallastDepthControl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BallastDepthControlTest {
    @Test void stationaryAtWrongDepthContinuesToRequestDrain(){
        var control=new BallastDepthControl();
        for(double fill=.9;fill>.01;fill-=.01)assertTrue(control.targetFill(45,1,0,fill,.25,1)<fill);
    }
    @Test void risingTooFastDemandsMoreWater(){
        var control=new BallastDepthControl();
        assertTrue(control.targetFill(1,1,2,.3,.25,1)>.3);
    }
    @Test void divingTowardDeeperTargetDemandsWater(){
        var control=new BallastDepthControl();
        assertTrue(control.targetFill(1,15,0,.3,.25,1)>.3);
    }
    @Test void resetAndDiscontinuityDoNotReuseOldVelocity(){
        var control=new BallastDepthControl();
        for(int i=0;i<500;i++)control.targetFill(45,1,0,.18,.25,1);
        assertEquals(.4,control.targetFill(5,5,0,.4,10,1));
        assertEquals(.4,control.targetFill(5,5,0,.4,.25,1),1e-9);
    }
}
