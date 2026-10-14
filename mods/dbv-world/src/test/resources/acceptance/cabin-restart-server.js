var RFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
function ra(ok,msg){if(!ok)throw new Error('DBV_CABIN_RESTART FAIL '+msg);console.info('DBV_CABIN_RESTART PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Cabin-Storage-Acceptance')<0)return;
 player.connection.teleport(64,77,157,0,0);
 server.scheduleInTicks(140,()=>{
  var m=RFleet.get(server),origin=m.refit().storageOrigin(m),panel=origin.offset(0,2,4),chest=level.getBlockEntity(origin.offset(1,1,4));
  ra(chest.getItem(0).getCount()===23,'restored inventory persists after restart');
  ra(String(chest.getItem(0).getHoverName().getString())==='酒狐的储备','restored component persists after restart');
  var state=level.getBlockState(panel);level.setBlock(panel,Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState(),3);
  ra(String(m.storage().store(m).read()).includes('酒狐的储备'),'removing terminal preserves independent data');level.setBlock(panel,state,3);
  var hit=new (Java.loadClass('net.minecraft.world.phys.BlockHitResult'))(Java.loadClass('net.minecraft.world.phys.Vec3').atCenterOf(panel),Java.loadClass('net.minecraft.core.Direction').NORTH,panel,false);
  var click=new (Java.loadClass('net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock'))(player,Java.loadClass('net.minecraft.world.InteractionHand').MAIN_HAND,panel,hit);
  Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS.post(click);
  ra(click.isCanceled(),'panel right click handled by terminal');console.info('DBV_CABIN_RESTART SERVER COMPLETE');
 });
});
