var PFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var PMode=Java.loadClass('net.minecraft.world.level.GameType');
function pa(ok,name){if(!ok)throw new Error('DBV_PANEL FAIL '+name);console.info('DBV_PANEL PASS '+name);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Panel-Acceptance')<0)return;
 player.setGameMode(PMode.CREATIVE);player.connection.teleport(64,77,156,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 server.scheduleInTicks(120,()=>{
  var m=PFleet.get(server),body=m.adapter().find(level,m.registry().flagship().get().bodyId());
  var box=body.getPlot().getBoundingBox(),Pos=Java.loadClass('net.minecraft.core.BlockPos');
  var Blocks=Java.loadClass('net.minecraft.world.level.block.Blocks'),Props=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
  var hatch=new Pos(box.minX()+4,box.minY()+6,box.minZ()+7);
  pa(level.getBlockState(hatch).getBlock()===Blocks.OAK_TRAPDOOR,'acceptance copy roof hatch found');
  level.setBlock(hatch,level.getBlockState(hatch).setValue(Props.OPEN,Java.loadClass('java.lang.Boolean').FALSE),3);
 });
 server.scheduleInTicks(120,()=>{
  var m=PFleet.get(server),body=m.adapter().find(level,m.registry().flagship().get().bodyId());
  pa(body!=null,'real registered ship loaded');var devices=m.inspection().inspect(body).devices(),cp=null;
  for(var i=0;i<devices.size();i++)if(String(devices.get(i).id())==='create_submarine:command_sub')cp=devices.get(i).position();
  pa(cp!=null,'controller found');console.info('DBV_PANEL REPORT '+m.inspection().inspect(body));console.info('DBV_PANEL ACCESS '+m.refit().accessProblem(player,m));
  var Hit=Java.loadClass('net.minecraft.world.phys.BlockHitResult'),Vec=Java.loadClass('net.minecraft.world.phys.Vec3');
  var Hand=Java.loadClass('net.minecraft.world.InteractionHand'),Direction=Java.loadClass('net.minecraft.core.Direction');
  var Right=Java.loadClass('net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock');
  var bus=Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS;
  var hit=new Hit(Vec.atCenterOf(cp),Direction.UP,cp,false);
  player.setShiftKeyDown(false);var normal=new Right(player,Hand.MAIN_HAND,cp,hit);bus.post(normal);pa(!normal.isCanceled(),'normal controller interaction preserved');
  player.setShiftKeyDown(true);var shift=new Right(player,Hand.MAIN_HAND,cp,hit);bus.post(shift);player.setShiftKeyDown(false);pa(shift.isCanceled(),'shift interaction opens panel route');
 });
 server.scheduleInTicks(760,()=>{pa(!PFleet.get(server).refit().active(),'no transaction leaked');server.saveEverything(false,true,true);console.info('DBV_PANEL SERVER COMPLETE');});
});
