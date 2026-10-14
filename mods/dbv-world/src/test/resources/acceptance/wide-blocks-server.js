var WFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var WPos=Java.loadClass('net.minecraft.core.BlockPos');
var WBlocks=Java.loadClass('net.minecraft.world.level.block.Blocks');
function wa(ok,msg){if(!ok)throw new Error('DBV_WIDE FAIL '+msg);console.info('DBV_WIDE PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Wide-Blocks-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,77,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 var chest=new WPos(61,69,154),barrel=new WPos(61,69,155),gear=new WPos(61,69,156);
 function checkItems(){wa(level.getBlockEntity(chest).getItem(0).getCount()===17,'chest inventory restored');wa(level.getBlockEntity(barrel).getItem(0).getCount()===9,'barrel inventory restored');}
 server.scheduleInTicks(140,()=>{
  var m=WFleet.get(server);m.refit().execute('begin',player,m);
  var registry=Java.loadClass('net.minecraft.core.registries.BuiltInRegistries').BLOCK;
  var cog=registry.get(Java.loadClass('net.minecraft.resources.ResourceLocation').parse('create:cogwheel')).defaultBlockState();
  wa(!cog.isAir(),'Create cogwheel registered');
  var states=[WBlocks.CHEST.defaultBlockState(),WBlocks.BARREL.defaultBlockState(),cog,WBlocks.OAK_DOOR.defaultBlockState(),WBlocks.TORCH.defaultBlockState(),WBlocks.OAK_STAIRS.defaultBlockState(),WBlocks.REDSTONE_WIRE.defaultBlockState()];
  states.forEach(s=>wa(m.refit().mayEdit(player,chest,s),'edit allowed '+s));
  level.setBlock(chest,states[0],3);level.setBlock(barrel,states[1],3);level.setBlock(gear,cog,3);
  var stack=Java.loadClass('net.minecraft.world.item.ItemStack'),items=Java.loadClass('net.minecraft.world.item.Items');
  level.getBlockEntity(chest).setItem(0,new stack(items.DIAMOND,17));level.getBlockEntity(barrel).setItem(0,new stack(items.IRON_INGOT,9));
  m.refit().execute('commit',player,m);
 });
 server.scheduleInTicks(620,()=>{
  var m=WFleet.get(server);wa(!m.refit().active(),'completion finished');
  wa(m.inspection().inspect(m.refit().currentBody(m)).qualification().eligible(),'expanded block ship qualifies');
  m.refit().execute('begin',player,m);checkItems();
  wa(String(level.getBlockState(gear)).includes('create:cogwheel'),'Create block survives physical round trip');
  level.getBlockEntity(chest).clearContent();level.setBlock(barrel,WBlocks.AIR.defaultBlockState(),3);
  m.refit().execute('cancel',player,m);
 });
 server.scheduleInTicks(660,()=>{
  var m=WFleet.get(server);
  m.refit().execute('begin',player,m);checkItems();
  m.refit().execute('cancel',player,m);
  wa(!m.refit().active(),'cancel returns physical carrier');
  wa(m.adapter().loaded(level).stream().filter(b=>!b.isRemoved()).count()===1,'one carrier after rollback');
  console.info('DBV_WIDE COMPLETE');
 });
});
