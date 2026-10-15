var depthChecked=false;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post', event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(depthChecked||mc.screen==null||(!String(mc.screen).includes('TitleScreen')&&!String(mc.screen).includes('DeepSeasWelcomeScreen')))return;
 depthChecked=true;
 try{
  Java.loadClass('com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity');
  Java.loadClass('com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem');
  console.info('DBV_DEPTH PASS both mixin targets loaded');
  var Pose=Java.loadClass('dev.ryanhcode.sable.companion.math.Pose3d');
  var Pos=Java.loadClass('net.minecraft.core.BlockPos');
  var Depth=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth');
  var p=new Pose();p.position().set(10,40,20);p.rotationPoint().set(100,100,100);
  var a=Depth.worldPosition(p,new Pos(102,104,100));
  if(Math.abs(a.y()-44.5)>0.00001)throw new Error('translation '+a);
  console.info('DBV_DEPTH PASS plot controller centre transformed to world height');
  p.orientation().rotateZ(Math.PI/2);
  var b=Depth.worldPosition(p,new Pos(102,104,100));
  if(Math.abs(b.y()-42.5)>0.00001)throw new Error('rotation '+b);
  console.info('DBV_DEPTH PASS rotation changes reference height');
  if(Math.abs(p.position().y()-40)>0.00001)throw new Error('pose mutated');
  console.info('DBV_DEPTH PASS ship pose unchanged');
  console.info('DBV_DEPTH COMPLETE');
 }catch(e){console.error('DBV_DEPTH FAIL '+e);}
 mc.stop();
});
