var CFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var CPos=Java.loadClass('net.minecraft.core.BlockPos');
function ca(ok,msg){if(!ok){console.error('DBV_BLOCK_RESTART FAIL '+msg);return;}console.info('DBV_BLOCK_RESTART PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Block-Refit-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,69,153,0,0);
 server.scheduleInTicks(180,()=>{
  var m=CFleet.get(server),data=server.persistentData;
  if(!data.getBoolean('dbv_block_restart_seeded')){
   data.putString('dbv_block_vessel',String(m.registry().flagship().get().vesselId()));
   data.putString('dbv_block_body',String(m.registry().flagship().get().bodyId()));
   console.info('DBV_BLOCK_RESTART BEGIN '+m.refit().execute('begin',player,m));
   ca(m.refit().blockMode(),'saved in world block editing mode');
   level.setBlock(new CPos(64,69,152),Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState(),3);
   data.putBoolean('dbv_block_restart_seeded',true);server.saveEverything(false,true,true);
   console.info('DBV_BLOCK_RESTART SEEDED');
  }else{
   ca(!m.refit().active(),'unfinished static transaction recovered on restart');
   var id=m.registry().flagship().get(),body=m.refit().currentBody(m);
   ca(String(id.vesselId())===String(data.getString('dbv_block_vessel')),'restart preserves permanent identity');
   ca(String(id.bodyId())!==String(data.getString('dbv_block_body')),'restart restores as new physical carrier');
   ca(body!=null,'restored body loaded');ca(m.inspection().inspect(body).qualification().eligible(),'restored cabin and equipment work');
   ca(m.adapter().loaded(level).stream().filter(b=>!b.isRemoved()).count()===1,'restart has one carrier');
   ca(level.getBlockState(new CPos(64,69,152)).isAir(),'restart clears world block copy');
   Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);
   console.info('DBV_BLOCK_RESTART COMPLETE');
  }
  server.scheduleInTicks(40,()=>ca(player.isAlive()&&player.getX()>50&&player.getX()<80&&player.getY()>60&&player.getY()<85,'player inside ship remains alive at dock'));
 });
});
