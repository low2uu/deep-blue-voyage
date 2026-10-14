var CFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var CPos=Java.loadClass('net.minecraft.core.BlockPos');
function ca(ok,msg){if(!ok)throw new Error('DBV_CABIN FAIL '+msg);console.info('DBV_CABIN PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Cabin-Storage-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,77,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(140,()=>{
  var m=CFleet.get(server);m.refit().execute('begin',player,m);
  var block=Java.loadClass('io.github.low2uu.dbv.fleet.ui.CabinPanelBlock').PANEL.get();
  level.setBlock(new CPos(60,70,154),block.defaultBlockState(),3);
  var chest=level.getBlockEntity(new CPos(61,69,154));
  var item=new (Java.loadClass('net.minecraft.world.item.ItemStack'))(Java.loadClass('net.minecraft.world.item.Items').DIAMOND,23);
  item.set(Java.loadClass('net.minecraft.core.component.DataComponents').CUSTOM_NAME,Java.loadClass('net.minecraft.network.chat.Component').literal('酒狐的储备'));
  chest.setItem(0,item);m.refit().execute('commit',player,m);
 });
 server.scheduleInTicks(620,()=>{
  var m=CFleet.get(server);ca(!m.refit().active(),'refit completed with panel');
  var origin=m.refit().storageOrigin(m),cp=origin.offset(1,1,4),panel=origin.offset(0,2,4),chest=level.getBlockEntity(cp);
  ca(String(level.getBlockState(panel)).includes('dbv_world:cabin_panel'),'panel survives physical assembly');
  m.storage().manualBackup(player,m);var data=m.storage().store(m).read();
  ca(data.getAsJsonObject('modules').has('storage'),'independent storage module exists');
  ca(String(data).includes('酒狐的储备'),'JSON preserves custom component');
  chest.clearContent();m.storage().restoreLatest(player,m);
  ca(chest.getItem(0).getCount()===23,'JSON restore recovers count');
  ca(String(chest.getItem(0).getHoverName().getString())==='酒狐的储备','JSON restore recovers item components');
  m.storage().store(m).requireStorageReady();ca(true,'recovery marker cleared after world save');
  var rows=m.storage().lines(m);ca(rows.stream().anyMatch(s=>String(s).includes('酒狐的储备')),'central storage summary includes named item');
  Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);
  console.info('DBV_CABIN SERVER COMPLETE');
 });
});
