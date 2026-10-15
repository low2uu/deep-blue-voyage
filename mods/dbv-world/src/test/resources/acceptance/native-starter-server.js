PlayerEvents.loggedIn(e=>{
var s=e.server,l=s.overworld(),p=e.player,F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),D=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth'),H=Java.loadClass('com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper');
if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Native-Acceptance'))return;
p.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);p.connection.teleport(67,80,158,0,0);p.getAbilities().flying=true;p.onUpdateAbilities();
s.scheduleInTicks(140,()=>{try{console.info('DBV_NATIVE INSTALL '+F.get(s).refit().execute('angler',p,F.get(s)));}catch(x){console.error('DBV_NATIVE FAIL '+x);}});
s.scheduleInTicks(400,()=>{try{console.info('DBV_NATIVE COMMIT '+F.get(s).refit().execute('commit',p,F.get(s)));}catch(x){console.error('DBV_NATIVE FAIL '+x);}});
Array.from({length:57},(_,i)=>600+i*100).forEach(t=>s.scheduleInTicks(t,()=>{try{var m=F.get(s),b=m.refit().currentBody(m),o=b.getPlot().getCenterBlock(),c=l.getBlockEntity(o.offset(7,4,3));
if(t==600){if(m.refit().active())throw Error('Refit not completed; voyage acceptance invalid');console.info('DBV_NATIVE CONFIG lift='+Java.loadClass('com.maxenonyme.createsubmarine.submarine.config.SubmarineConfig').BALLAST_LIFT_PER_TANK.get()+' speed='+Java.loadClass('com.maxenonyme.createsubmarine.submarine.config.SubmarineConfig').BALLAST_VERTICAL_SPEED.get());console.info('DBV_NATIVE QUAL '+m.inspection().inspect(b).qualification());c.targetDepth=1;c.speedMode=2;c.autopilot=false;console.info('DBV_NATIVE DEPART '+m.starter().depart(p,m));}
if(t==2200)c.targetDepth=15;if(t==4200)c.targetDepth=1;
[[3,2,5],[11,2,5],[3,2,15],[11,2,15]].forEach(a=>console.info('DBV_NATIVE WATER '+t+' '+a+'='+l.getBlockEntity(o.offset(a[0],a[1],a[2])).getClusterTotalAmount()));var pos=D.worldPosition(b.logicalPose(),c.getBlockPos());console.info('DBV_NATIVE t='+t+' target='+c.targetDepth+' depth='+(c.surfaceY+1-pos.y())+' fill='+c.syncedFill+' pumps='+c.syncedPumps+' command='+c.pumpCommand()+' mass='+H.readMass(b)+' pos='+pos+' qual='+m.inspection().inspect(b).qualification()+' weakest='+c.syncedWeakest);
if(t%400==0){[[3,4,7],[11,4,7],[3,2,6],[11,2,16],[5,4,5]].forEach(a=>{var be=l.getBlockEntity(o.offset(a[0],a[1],a[2]));console.info('DBV_NATIVE MACHINE '+a+' speed='+be.getSpeed()+' stressed='+be.isOverStressed());});var el=l.getBlockEntity(o.offset(5,4,5)),di=l.getBlockEntity(o.offset(5,4,8));console.info('DBV_NATIVE OXYGEN water='+el.waterTank.getFluidAmount()+' energy='+el.energyStorage.getEnergyStored()+' diffuser='+di.oxygenTank.getFluidAmount());}
}catch(x){console.error('DBV_NATIVE FAIL '+x);}}));
});
PlayerEvents.loggedIn(e=>{
var s=e.server,l=s.overworld(),p=e.player,F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),D=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth'),B=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Native-Acceptance-Final'))return;
var poses={},anchor=null;
[2000,4000,6000,6200,6280,6360,6440,6520,6720,6920,7120,7320].forEach(t=>s.scheduleInTicks(t,()=>{try{
var m=F.get(s),b=m.refit().currentBody(m),o=b.getPlot().getCenterBlock(),c=l.getBlockEntity(o.offset(7,4,3)),pos=D.worldPosition(b.logicalPose(),c.getBlockPos());p.connection.teleport(pos.x(),pos.y()+8,pos.z(),0,0);p.getAbilities().flying=true;p.onUpdateAbilities();
var lever=(x,z,on)=>{var q=o.offset(x,5,z);var state=l.getBlockState(q);if(String(state.getValue(B.POWERED))!=String(on))l.setBlock(q,state.cycle(B.POWERED),3);}
if(t==2000||t==4000||t==6000){var depth=c.surfaceY+1-pos.y();if(Math.abs(depth-c.targetDepth)>1.5)throw Error('depth '+depth+' target '+c.targetDepth);console.info('DBV_NATIVE PASS depth target='+c.targetDepth+' actual='+depth);}
if(t==6200){poses.start=[pos.x(),pos.z()];lever(2,5,false);lever(12,5,false);}
if(t==6280){poses.forward=[pos.x(),pos.z()];console.info('DBV_NATIVE FORWARD '+poses.start+' -> '+poses.forward);console.info('DBV_NATIVE MOOR_REJECT '+m.starter().mooringProblem(p,m));lever(3,6,true);lever(11,6,true);}
if(t==6360){poses.reverse=[pos.x(),pos.z()];console.info('DBV_NATIVE REVERSE '+poses.forward+' -> '+poses.reverse);lever(3,6,false);}
if(t==6440){console.info('DBV_NATIVE TURN_LEFT orientation='+b.logicalPose().orientation());lever(3,6,true);lever(11,6,false);}
if(t==6520){console.info('DBV_NATIVE TURN_RIGHT orientation='+b.logicalPose().orientation());lever(2,5,true);lever(12,5,true);}
if(t>=6200&&t<=6520){console.info('DBV_NATIVE DRIVE t='+t+' position='+pos+' yaw='+b.logicalPose().orientation()+' propL='+l.getBlockEntity(o.offset(0,4,13)).getSpeed()+' propR='+l.getBlockEntity(o.offset(14,4,13)).getSpeed()+' oxygen='+m.inspection().inspect(b).qualification());}
if(t==6720){Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'停泊',seq:t});anchor=[pos.x(),pos.y(),pos.z()];}
if(t==6920){if(!m.starter().moored(b)||m.starter().departed(m))throw Error('mooring not active');if(Math.hypot(pos.x()-anchor[0],pos.y()-anchor[1],pos.z()-anchor[2])>.3)throw Error('mooring moved');console.info('DBV_NATIVE PASS mooring anchor='+pos);Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'出航',seq:t});}
if(t==7120){Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'停泊',seq:t});}
if(t==7320){if(!m.starter().moored(b))throw Error('repeat mooring failed');console.info('DBV_NATIVE PASS repeated departure/mooring');s.saveEverything(false,true,true);}
}catch(x){console.error('DBV_NATIVE FAIL '+x);}}));
});
PlayerEvents.loggedIn(e=>{
var s=e.server,p=e.player;if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Native-Acceptance-Final'))return;
s.scheduleInTicks(7340,()=>{Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'驾驶指南',seq:7340});});
s.scheduleInTicks(7400,()=>{Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'存储终端',seq:7400});});
s.scheduleInTicks(7460,()=>console.info('DBV_NATIVE STORAGE_MENU correct='+(p.containerMenu instanceof Java.loadClass('io.github.low2uu.dbv.fleet.ui.StorageTerminalMenu'))));
});
