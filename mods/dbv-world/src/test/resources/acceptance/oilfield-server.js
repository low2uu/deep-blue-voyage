// Exercises the actual pinned CDG extraction API in a disposable copy, not the player's map.
var oilPolicy=Java.loadClass('io.github.low2uu.dbv.world.oil.StarterOilfield');
var oilData=Java.loadClass('com.jesz.createdieselgenerators.world.OilChunksSavedData');
var oilPos=Java.loadClass('net.minecraft.core.BlockPos');
var oilChunk=Java.loadClass('net.minecraft.world.level.ChunkPos');
var oilAudit=Java.loadClass('io.github.low2uu.dbv.world.oil.OilfieldAudit');
function oa(ok,msg){if(!ok)throw new Error('DBV_OIL FAIL '+msg);console.info('DBV_OIL PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Oilfield-Acceptance')<0)return;
 player.connection.teleport(191,85,-48,0,25);
 server.scheduleInTicks(50,()=>{
  try{
   for(var x=11;x<=12;x++)for(var z=-4;z<=-3;z++)oa(oilData.getChunkOilAmount(level,new oilChunk(x,z))===2147483647,'native infinite chunk '+x+','+z);
   oa(oilPolicy.contains(176,-64)&&oilPolicy.contains(207,-33)&&!oilPolicy.contains(175,-64)&&!oilPolicy.contains(208,-33)&&!oilPolicy.contains(176,-65)&&!oilPolicy.contains(207,-32),'exact block boundaries');
   var levels=server.getAllLevels().iterator(),nether=null;
   while(levels.hasNext()){var next=levels.next();if(String(next.dimension).indexOf('the_nether')>=0)nether=next;}
   oa(nether!=null&&!oilPolicy.initialize(nether),'nether untouched');
   oilData.setChunkOilAmount(level,new oilChunk(11,-4),1234);
   oa(!oilPolicy.initialize(level)&&oilData.getChunkOilAmount(level,new oilChunk(11,-4))===1234,'initialization is once-only, no refill');
   oilData.setChunkOilAmount(level,new oilChunk(11,-4),2147483647);
   oilData.setChunkOilAmount(level,new oilChunk(10,-4),3000);
   for(var x of [184,168]){
    server.runCommandSilent('fill '+x+' -63 -56 '+x+' 79 -56 create:glass_fluid_pipe[axis=y]');
    server.runCommandSilent('setblock '+x+' -64 -56 minecraft:bedrock');
    server.runCommandSilent('setblock '+x+' 80 -56 createdieselgenerators:pumpjack_hole');
   }
   server.scheduleInTicks(50,()=>{
    try{
     var hole=level.getBlockEntity(new oilPos(184,80,-56));hole.headPos=4;hole.bearingPos=4;
     oa(hole.pipeLength>0,'pipe reaches real bedrock');
     console.info('DBV_OIL extracting through native Java probe');
     oa(oilAudit.extract(level,184,20)===20000,'twenty real buckets through native extraction and capability');
     oa(oilData.getChunkOilAmount(level,new oilChunk(11,-4))===2147483647,'reserve unchanged after twenty buckets');
     oa(oilAudit.extract(level,168,1)===1000&&oilData.getChunkOilAmount(level,new oilChunk(10,-4))===2000,'neighbor finite oil still depletes');
     server.runCommandSilent('setblock 184 79 -56 minecraft:air');
     server.scheduleInTicks(40,()=>{
      try{
       oa(oilAudit.extract(level,184,1)===0,'broken well pipe prevents extraction');
       oa(oilPolicy.mark(level)===0,'marker re-run does not duplicate or rebuild');
       console.info('DBV_OIL STATUS '+oilPolicy.status(level));
       oa(server.saveEverything(false,true,true),'save completed');
       console.info('DBV_OIL ACCEPTANCE COMPLETE');
      }catch(e){console.error('DBV_OIL FAIL '+e);}
     });
    }catch(e){console.error('DBV_OIL FAIL '+e);}
   });
  }catch(e){console.error('DBV_OIL FAIL '+e);}
 });
});
