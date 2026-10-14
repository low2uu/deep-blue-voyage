// Temporary opt-in script, formal client only. Never shipped in the gameplay pack.
var RFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var RStarter=Java.loadClass('io.github.low2uu.dbv.fleet.dock.StarterSubmarine');
var RPos=Java.loadClass('net.minecraft.core.BlockPos');
var RBlocks=Java.loadClass('net.minecraft.world.level.block.Blocks');
function ra(ok,name){if(!ok)throw new Error('DBV_REFIT FAIL '+name);console.info('DBV_REFIT PASS '+name);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Refit-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);
 player.connection.teleport(64,78,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(60,()=>{
  var module=RFleet.get(server),reopen=module.registry().flagship().isPresent();
  if(!reopen){var spawnEvent=new (Java.loadClass('net.neoforged.neoforge.event.level.LevelEvent$CreateSpawnPosition'))(level,level.getLevelData());Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS.post(spawnEvent);ra(spawnEvent.isCanceled(),'starter preset generation hook');}
  server.scheduleInTicks(300,()=>{
   try{
    ra(module.registry().flagship().isPresent(),'starter automatically qualified and registered');
    var identity=module.registry().flagship().get(),body=module.adapter().find(level,identity.bodyId());
    ra(body!=null,'starter Sable body loaded');ra(module.starter().moored(body),'local fixed mooring active');
    ra(module.inspection().inspect(body).qualification().eligible(),'real Deep Seas three conditions pass with shore service');
    ra(module.adapter().loaded(level).size()===1,'exactly one vessel');
    var bounds=body.getPlot().getBoundingBox();
    var wall=new RPos(bounds.minX(),bounds.minY()+2,bounds.minZ()+4),original=level.getBlockState(wall);
    var inner=new RPos(bounds.minX()+4,bounds.minY()+1,bounds.minZ()+7);
    var refit=module.refit();
    if(reopen){
     ra(!refit.active(),'interrupted session recovered on restart');
     ra(level.getBlockState(wall).getBlock()===RBlocks.GLASS,'restart restored wall');
     ra(level.getBlockState(inner).isAir(),'restart removed added block');
     player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').SURVIVAL);
     var denied=false;try{refit.execute('begin',player,module);}catch(e){denied=true;}
     ra(denied&&!refit.active(),'survival player rejected without snapshot');player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);
     var report=module.inspection().inspect(body),dp=null;
     for(var j=0;j<report.devices().size();j++)if(String(report.devices().get(j).id())==='create_submarine:oxygene_diffuser')dp=report.devices().get(j).position();
     Java.loadClass('io.github.low2uu.dbv.fleet.dock.DockInteraction').menu(player,body);
     refit.execute('begin',player,module);level.setBlock(dp.south(),RBlocks.AIR.defaultBlockState(),3);
     console.info('DBV_REFIT PENDING '+refit.execute('commit',player,module));
     ra(refit.active(),'pending scan keeps backup');ra(!refit.mayEdit(player,wall,RBlocks.IRON_BLOCK.defaultBlockState()),'pending scan locks editing');
     server.scheduleInTicks(240,()=>{
      ra(!refit.active(),'oxygen outage scan timeout rolls back');ra(level.getBlockState(dp.south()).getBlock()===RBlocks.REDSTONE_BLOCK,'timeout restores redstone');
      server.scheduleInTicks(100,()=>{
       refit.execute('begin',player,module);level.setBlock(wall,RBlocks.IRON_BLOCK.defaultBlockState(),3);
       console.info('DBV_REFIT ASYNC '+refit.execute('commit',player,module));
       server.scheduleInTicks(100,()=>{
        ra(!refit.active(),'valid commit after recovery finishes');ra(level.getBlockState(wall).getBlock()===RBlocks.IRON_BLOCK,'valid edit after recovery retained');
        refit.execute('begin',player,module);level.setBlock(wall,RBlocks.GLASS.defaultBlockState(),3);refit.execute('commit',player,module);
        server.scheduleInTicks(100,()=>{ra(!refit.active(),'final session closed');server.saveEverything(false,true,true);console.info('DBV_REFIT REOPEN COMPLETE');});
       });
      });
     });return;
    }
    refit.execute('begin',player,module);ra(refit.active(),'begin writes durable active snapshot');
    ra(!refit.mayEdit(player,new RPos(bounds.minX(),bounds.minY(),bounds.minZ()),RBlocks.AIR.defaultBlockState()),'anchor protected');
    ra(!refit.mayEdit(player,wall,RBlocks.CHEST.defaultBlockState()),'unsupported container denied');
    ra(!refit.mayEdit(player,wall.west(),RBlocks.IRON_BLOCK.defaultBlockState()),'outside envelope denied');
    level.setBlock(wall,RBlocks.IRON_BLOCK.defaultBlockState(),3);level.setBlock(inner,RBlocks.IRON_BLOCK.defaultBlockState(),3);
    refit.execute('cancel',player,module);
    ra(!refit.active(),'cancel closes session');ra(level.getBlockState(wall).getBlock()===original.getBlock(),'cancel restores wall');
    ra(level.getBlockState(inner).isAir(),'cancel removes added block');
    server.scheduleInTicks(100,()=>{
     refit.execute('begin',player,module);level.setBlock(wall,RBlocks.IRON_BLOCK.defaultBlockState(),3);
     server.scheduleInTicks(100,()=>{
      console.info('DBV_REFIT COMMIT '+refit.execute('commit',player,module));ra(!refit.active(),'valid modification committed');
      ra(level.getBlockState(wall).getBlock()===RBlocks.IRON_BLOCK,'committed block retained');
      refit.execute('begin',player,module);
      var devices=module.inspection().inspect(body).devices(),cp=null;
      for(var i=0;i<devices.size();i++)if(String(devices.get(i).id())==='create_submarine:command_sub')cp=devices.get(i).position();
      level.setBlock(cp,RBlocks.AIR.defaultBlockState(),3);
      server.scheduleInTicks(100,()=>{
       console.info('DBV_REFIT REJECT '+refit.execute('commit',player,module));ra(!refit.active(),'invalid modification automatically rolls back');
       ra(String(level.getBlockState(cp).getBlock()).indexOf('command_sub')>=0,'controller restored');
       ra(module.registry().flagship().get().vesselId().equals(identity.vesselId()),'permanent identity unchanged');
       server.scheduleInTicks(100,()=>{
        ra(module.inspection().inspect(body).qualification().eligible(),'restored block entities work');
        // Restore the original glass via a committed refit, then leave a dirty session for restart testing.
        refit.execute('begin',player,module);level.setBlock(wall,original,3);
        server.scheduleInTicks(100,()=>{
         refit.execute('commit',player,module);ra(!refit.active(),'baseline restored');
         refit.execute('begin',player,module);level.setBlock(wall,RBlocks.IRON_BLOCK.defaultBlockState(),3);level.setBlock(inner,RBlocks.IRON_BLOCK.defaultBlockState(),3);
         server.saveEverything(false,true,true);console.info('DBV_REFIT FIRST COMPLETE');
        });
       });
      });
     });
    });
   }catch(e){console.error('DBV_REFIT ERROR '+e);throw e;}
  });
 });
});
