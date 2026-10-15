var dFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
function da(ok,msg){if(!ok)throw new Error('DBV_DEPTH FAIL '+msg);console.info('DBV_DEPTH PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Depth-Acceptance')<0)return;
 player.connection.teleport(64,77,157,0,0);
 server.scheduleInTicks(140,()=>{
  try{
   var m=dFleet.get(server),body=m.refit().currentBody(m);
   var c=Java.loadClass('com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity').onSub(body.getUniqueId());
   da(c!=null,'controller registered on copied vessel');
   var registry=Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
   var tankPos=body.getPlot().getCenterBlock().offset(3,1,5);
   var tankState=registry.BLOCK.get(Java.loadClass('net.minecraft.resources.ResourceLocation').parse('create_submarine:ballast_tank')).defaultBlockState();
   level.setBlock(tankPos,tankState,3);
   var tank=level.getBlockEntity(tankPos);
   tank.tank.setFluid(new (Java.loadClass('net.neoforged.neoforge.fluids.FluidStack'))(Java.loadClass('net.minecraft.world.level.material.Fluids').WATER,4000));
   server.scheduleInTicks(60,()=>{
    try{
     var helper=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth');
     var world=helper.worldPosition(body.logicalPose(),c.getBlockPos());
     c.surfaceY=Math.floor(world.y())+15;c.autopilot=false;
     var depth=Java.loadClass('io.github.low2uu.dbv.fleet.telemetry.DepthReference').depth(c.surfaceY,world.y());
     da(Math.abs(depth-(c.surfaceY+1-world.y()))<0.00001,'controller reference converts to expected depth');
     var Pump=Java.loadClass('com.maxenonyme.createsubmarine.submarine.block.entity.PumpControllerBlockEntity');
     c.targetDepth=5;var drains=false;for(var k=0;k<15;k++){c.surfaceY=Math.floor(world.y())+15;c.tick();drains=drains||c.pumpCommand()===Pump.DRAIN;}da(drains,'shallower target commands drain and ascent');
     c.targetDepth=30;var fills=false;for(var k=0;k<15;k++){c.surfaceY=Math.floor(world.y())+15;c.tick();fills=fills||c.pumpCommand()===Pump.FILL;}da(fills,'deeper target commands fill and descent');
     var holds=false;for(var k=0;k<15;k++){c.surfaceY=-2147483648;c.tick();if(c.surfaceY===-2147483648&&c.pumpCommand()===Pump.HOLD)holds=true;}da(holds,'missing surface stops pump commands');
     da(isNaN(Java.loadClass('io.github.low2uu.dbv.fleet.telemetry.DepthReference').depth(-2147483648,world.y())),'unknown depth is not zero');
     c.surfaceY=-2147483648;c.tick();console.info('DBV_DEPTH PASS trim safely skips unknown depth');
     console.info('DBV_DEPTH WORLD COMPLETE');
    }catch(e){console.error('DBV_DEPTH FAIL '+e);}
   });
  }catch(e){console.error('DBV_DEPTH FAIL '+e);}
 });
});
