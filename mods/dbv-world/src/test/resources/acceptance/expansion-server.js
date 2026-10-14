var EFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),EPos=Java.loadClass('net.minecraft.core.BlockPos'),EBlocks=Java.loadClass('net.minecraft.world.level.block.Blocks');
function ea(ok,msg){if(!ok)throw new Error('DBV_EXPAND FAIL '+msg);console.info('DBV_EXPAND PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Expansion-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,77,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(140,()=>{
  var m=EFleet.get(server);m.refit().execute('begin',player,m);
  level.setBlock(new EPos(69,69,154),EBlocks.CHEST.defaultBlockState(),3);
  level.getBlockEntity(new EPos(69,69,154)).setItem(0,new (Java.loadClass('net.minecraft.world.item.ItemStack'))(Java.loadClass('net.minecraft.world.item.Items').DIAMOND,11));
  level.setBlock(new EPos(59,70,154),EBlocks.GLASS.defaultBlockState(),3);
  level.setBlock(new EPos(80,69,154),EBlocks.GOLD_BLOCK.defaultBlockState(),3);
  ea(m.refit().storagePositions(m).contains(new EPos(69,69,154)),'scan includes exterior container');m.refit().execute('commit',player,m);
 });
 server.scheduleInTicks(620,()=>{
  var m=EFleet.get(server),body=m.refit().currentBody(m),center=body.getPlot().getCenterBlock();
  ea(!m.refit().active()&&m.inspection().inspect(body).qualification().eligible(),'expanded ship accepted');
  ea(level.getBlockEntity(center.offset(9,1,4)).getItem(0).getCount()===11,'exterior container and contents physically assembled');
  ea(String(level.getBlockState(center.offset(-1,2,4))).includes('minecraft:glass'),'negative-side extension physically assembled');
  ea(level.getBlockState(new EPos(69,69,154)).isAir(),'no exterior world copy');
  ea(String(level.getBlockState(new EPos(80,69,154))).includes('gold_block'),'detached block stays in world');
  var p=center.offset(4,1,6),state=EBlocks.BARREL.defaultBlockState();level.setBlock(p,state,3);
  level.getBlockEntity(p).setItem(0,new (Java.loadClass('net.minecraft.world.item.ItemStack'))(Java.loadClass('net.minecraft.world.item.Items').EMERALD,13));
  var snapshot=Java.loadClass('net.neoforged.neoforge.common.util.BlockSnapshot').create(Java.loadClass('net.minecraft.world.level.Level').OVERWORLD,level,p);
  var place=new (Java.loadClass('net.neoforged.neoforge.event.level.BlockEvent$EntityPlaceEvent'))(snapshot,EBlocks.AIR.defaultBlockState(),player);
  Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS.post(place);ea(!place.isCanceled(),'ordinary placement on physical ship allowed');
  var br=new (Java.loadClass('net.neoforged.neoforge.event.level.BlockEvent$BreakEvent'))(level,p,state,player);
  Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS.post(br);ea(!br.isCanceled(),'ordinary breaking on physical ship allowed');
 });
 server.scheduleInTicks(760,()=>{
  var m=EFleet.get(server),data=m.storage().store(m).read().getAsJsonObject('modules');
  ea(data.has('structure')&&data.has('automaticBackup'),'automatic structure backup exists');
  ea(String(data.getAsJsonObject('storage')).includes('minecraft:emerald'),'normal construction inventory automatically synchronized');
  ea(String(data.getAsJsonObject('structure')).includes('minecraft:barrel'),'normal construction geometry automatically synchronized');
  var metadata=data.getAsJsonObject('structure').getAsJsonArray('blocks');
  for(var i=0;i<metadata.size();i++){var b=metadata.get(i).getAsJsonObject();if(b.has('metadata')){var adapter=Java.loadClass('io.github.low2uu.dbv.fleet.storage.BlockEntityJson');var encoded=b.getAsJsonObject('metadata');ea(String(adapter.encode(adapter.decode(encoded)))===String(encoded),'block metadata JSON round trip');break;}}
  m.refit().execute('begin',player,m);level.setBlock(new EPos(71,69,154),EBlocks.IRON_BLOCK.defaultBlockState(),3);m.refit().execute('cancel',player,m);
  ea(level.getBlockState(new EPos(71,69,154)).isAir(),'cancel cleans exterior additions');
 });
 server.scheduleInTicks(820,()=>{
  var m=EFleet.get(server);m.refit().execute('begin',player,m);level.setBlock(new EPos(71,69,154),EBlocks.IRON_BLOCK.defaultBlockState(),3);
  level.setBlock(new EPos(65,69,152),EBlocks.AIR.defaultBlockState(),3);m.refit().execute('commit',player,m);
 });
 server.scheduleInTicks(1360,()=>{
  var m=EFleet.get(server);ea(!m.refit().active(),'failed expanded refit rolled back');
  ea(m.inspection().inspect(m.refit().currentBody(m)).qualification().eligible(),'rollback restored qualified original');
  ea(level.getBlockState(new EPos(71,69,154)).isAir(),'failed validation cleans exterior additions');
  ea(String(level.getBlockState(new EPos(80,69,154))).includes('gold_block'),'rollback preserves detached world object');
  Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);
 });
 server.scheduleInTicks(1560,()=>{
  var m=EFleet.get(server);ea(m.starter().departed(m),'departure saved');ea(!m.starter().moored(m.refit().currentBody(m)),'departure remains unmoored');
  var fresh=new (Java.loadClass('io.github.low2uu.dbv.fleet.dock.StarterSubmarine'))(server);ea(fresh.departed(m),'fresh service reads departure JSON');
  console.info('DBV_EXPAND COMPLETE');
 });
});
