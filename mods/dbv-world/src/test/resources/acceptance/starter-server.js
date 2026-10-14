// Opt-in new-world spawn-distance acceptance; no direct submarine creation call.
var SFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
function sa(ok,name){if(!ok)throw new Error('DBV_STARTER FAIL '+name);console.info('DBV_STARTER PASS '+name);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Starter-Spawn-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);
 player.connection.teleport(-84,72,-28,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(300,()=>{
  var m=SFleet.get(server);sa(m.registry().flagship().isPresent(),'fresh island automatically registers while player stays at spawn');
  player.connection.teleport(54,78,142,-35,25);server.scheduleInTicks(80,()=>{var body=m.adapter().find(level,m.registry().flagship().get().bodyId());
  sa(body!=null,'starter remains loaded for initial validation');sa(m.adapter().loaded(level).size()===1,'one ship after fresh world creation');
  sa(m.inspection().inspect(body).qualification().eligible(),'fresh starter three conditions');
  player.connection.teleport(54,78,142,-35,25);player.getAbilities().flying=true;player.onUpdateAbilities();
  server.scheduleInTicks(80,()=>{
   sa(m.starter().moored(body),'fresh starter moored');
   Java.loadClass('io.github.low2uu.dbv.fleet.dock.DockInteraction').menu(player,body);
   sa(!m.refit().active(),'playable save has no test refit');server.saveEverything(false,true,true);console.info('DBV_STARTER COMPLETE');
  });
  });
 });
});
