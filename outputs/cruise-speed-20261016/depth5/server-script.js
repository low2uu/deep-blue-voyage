var cruisePlayer=null,cruiseAge=0,cruiseFailed=false;
PlayerEvents.loggedIn(e=>{
if(!String(e.server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('DBV-Cruise-Trial'))return;
cruisePlayer=e.player;cruiseAge=0;
e.player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);
e.player.getAbilities().flying=true;e.player.onUpdateAbilities();
});
ServerEvents.tick(e=>{
if(cruisePlayer==null||cruiseFailed)return;
++cruiseAge;var t=cruiseAge;if(t%20!=0)return;
try{
var s=e.server,l=s.overworld(),p=cruisePlayer,F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),D=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth'),H=Java.loadClass('com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper'),R=Java.loadClass('dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle'),V=Java.loadClass('org.joml.Vector3d'),B=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
var m=F.get(s),b=m.refit().currentBody(m);if(b==null){if(t>300)throw Error('body missing');return;}
var o=b.getPlot().getCenterBlock(),c=l.getBlockEntity(o.offset(7,4,3)),pos=D.worldPosition(b.logicalPose(),c.getBlockPos());
var lever=(x,z,on)=>{var q=o.offset(x,5,z),state=l.getBlockState(q);if(String(state.getValue(B.POWERED))!=String(on))l.setBlock(q,state.cycle(B.POWERED),3);};
if(t==200){p.connection.teleport(pos.x(),pos.y()+4,pos.z(),0,0);console.info('CRUISE DEPART '+(m.starter().departed(m)?'already departed':m.starter().depart(p,m)));c.targetDepth=5;c.speedMode=1;c.autopilot=false;lever(2,5,true);lever(12,5,true);lever(3,6,false);lever(11,6,false);}
if(t==240){p.connection.teleport(1067,80,1153,0,0);for(var cx=64;cx<=68;cx++)for(var cz=69;cz<=73;cz++)l.getChunk(cx,cz);R.of(b).teleport(new V(b.logicalPose().position().x()+1000,b.logicalPose().position().y(),b.logicalPose().position().z()+1000),b.logicalPose().orientation());console.info('CRUISE RELOCATE '+b.logicalPose());}
if(t>=280){p.connection.teleport(pos.x(),pos.y()+10,pos.z(),0,0);p.getAbilities().flying=true;p.onUpdateAbilities();}
if(t==1200){console.info('CRUISE START_FORWARD');lever(2,5,false);lever(12,5,false);}
if(t==4800){console.info('CRUISE STOP');lever(2,5,true);lever(12,5,true);}
if(t>=300){var left=l.getBlockEntity(o.offset(0,4,13)),right=l.getBlockEntity(o.offset(14,4,13)),v=R.of(b).getLinearVelocity(),row={tick:t,wallMs:Date.now(),x:pos.x(),y:pos.y(),z:pos.z(),depth:c.surfaceY+1-pos.y(),rpmL:left.getSpeed(),rpmR:right.getSpeed(),thrustL:left.getThrust(),scaledL:left.getScaledThrust(),thrustR:right.getThrust(),scaledR:right.getScaledThrust(),mass:H.readMass(b),vx:v.x(),vy:v.y(),vz:v.z(),overstressL:left.isOverStressed(),overstressR:right.isOverStressed()};console.info('CRUISE SAMPLE '+JSON.stringify(row));}
if(t==5400){console.info('CRUISE QUAL '+m.inspection().inspect(b).qualification());s.saveEverything(false,true,true);JsonIO.write('kubejs/cruise-done.json',{done:true});cruiseFailed=true;}
}catch(x){console.error('CRUISE FAIL t='+t+' '+x);cruiseFailed=true;JsonIO.write('kubejs/cruise-done.json',{done:true,error:String(x)});}
});
