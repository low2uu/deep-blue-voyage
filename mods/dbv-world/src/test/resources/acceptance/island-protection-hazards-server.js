PlayerEvents.loggedIn(e=>{
 var s=e.server,l=s.overworld();
 if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Angler-Acceptance'))return;
 s.runCommandSilent('forceload add 96 144 112 176');
 s.scheduleInTicks(100,()=>{
  console.info('DBV_PROTECTION SETUP fill='+s.runCommandSilent('fill 98 100 158 102 100 162 stone'));
  console.info('DBV_PROTECTION SETUP wood='+s.runCommandSilent('setblock 110 100 160 oak_planks'));
  s.runCommandSilent('setblock 110 101 160 fire');
  console.info('DBV_PROTECTION SETUP tnt='+s.runCommandSilent('summon tnt 100 101 160 {fuse:20}'));
 });
 s.scheduleInTicks(240,()=>{try{
  var Pos=Java.loadClass('net.minecraft.core.BlockPos');
  for(var x=98;x<=102;x++)for(var z=158;z<=162;z++)if(String(l.getBlockState(new Pos(x,100,z))).indexOf('minecraft:stone')<0)throw Error('explosion damaged '+x+','+z+' '+l.getBlockState(new Pos(x,100,z)));
  if(String(l.getBlockState(new Pos(110,100,160))).indexOf('oak_planks')<0)throw Error('fire burned wood');
  console.info('DBV_PROTECTION PASS actual TNT left 25 stone intact; fire left wood intact');
 }catch(x){console.error('DBV_PROTECTION FAIL '+x);}});
});
