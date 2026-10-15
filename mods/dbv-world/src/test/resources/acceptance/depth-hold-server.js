PlayerEvents.loggedIn(e=>{var s=e.server,l=s.overworld(),p=e.player,F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),H=Java.loadClass('com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper');
 if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Dive-Diagnosis'))return;
 p.connection.teleport(67,80,158,0,0);p.getAbilities().flying=true;p.onUpdateAbilities();
 [100,400,800,1200,1400,1800,2200,2600,3000].forEach(t=>s.scheduleInTicks(t,()=>{try{var m=F.get(s),b=m.refit().currentBody(m),o=b.getPlot().getCenterBlock(),c=l.getBlockEntity(o.offset(7,4,3));c.targetDepth=t>=1400?15:1;c.autopilot=false;
 var actualDepth=c.surfaceY+1-Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth').worldPosition(b.logicalPose(),c.getBlockPos()).y();
 if(t==1200||t==3000){if(Math.abs(actualDepth-c.targetDepth)>1.25)throw Error('depth did not settle '+actualDepth+' target '+c.targetDepth);console.info('DBV_DIVE PASS target='+c.targetDepth+' actual='+actualDepth+' moored='+m.starter().moored(b));}
 console.info('DBV_DIVE t='+t+' target='+c.targetDepth+' depth='+c.syncedDepth+' surface='+c.surfaceY+' fill='+c.syncedFill+' pumps='+c.syncedPumps+' command='+c.pumpCommand()+' moored='+m.starter().moored(b)+' mass='+H.readMass(b)+' velocity='+H.getVelocity(H.getHandle(b)));
 [[3,2,6],[11,2,6],[3,2,16],[11,2,16],[3,4,7],[11,4,7]].forEach(a=>console.info('DBV_DIVE machine '+a+' speed='+l.getBlockEntity(o.offset(a[0],a[1],a[2])).getSpeed()));
 [[3,1,4],[11,1,4],[3,1,14],[11,1,14]].forEach(a=>console.info('DBV_DIVE tank '+a+' water='+l.getBlockEntity(o.offset(a[0],a[1],a[2])).tank.getFluidAmount()));
 }catch(x){console.error('DBV_DIVE FAIL '+x);}}));
});
