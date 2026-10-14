var DFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
function da(ok,msg){if(!ok)throw new Error('DBV_DEPART FAIL '+msg);console.info('DBV_DEPART PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Departure-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,77,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(140,()=>{
  var m=DFleet.get(server);da(m.starter().moored(m.refit().currentBody(m)),'starts moored');
  Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);
 });
 server.scheduleInTicks(360,()=>{
  var m=DFleet.get(server),body=m.refit().currentBody(m);
  da(m.starter().departed(m),'UI request persisted departure');
  da(!m.starter().moored(body),'constraint remains released after subsequent ticks');
  da(m.storage().store(m).read().getAsJsonObject('modules').getAsJsonObject('navigation').get('departed').getAsBoolean(),'departure stored in independent JSON');
  var reloaded=new (Java.loadClass('io.github.low2uu.dbv.fleet.dock.StarterSubmarine'))(server);
  da(reloaded.departed(m),'fresh service loads departure state');
  var rejected=false;try{m.starter().depart(player,m);}catch(e){rejected=true;}da(rejected,'duplicate departure rejected');
  da(m.adapter().loaded(server.overworld()).stream().filter(b=>!b.isRemoved()).count()===1,'one physical carrier retained');
  console.info('DBV_DEPART COMPLETE');
 });
});
