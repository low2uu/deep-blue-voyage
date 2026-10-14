var BFleet=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var BPos=Java.loadClass('net.minecraft.core.BlockPos');
function ba(ok,msg){if(!ok){console.error('DBV_BLOCK FAIL '+msg);return;}console.info('DBV_BLOCK PASS '+msg);}
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),player=event.player;
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Block-Refit-Acceptance')<0)return;
 player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);player.connection.teleport(64,77,157,0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
 var vessel=null,oldBody=null;
 server.scheduleInTicks(140,()=>{
  var m=BFleet.get(server),id=m.registry().flagship().get(),body=m.refit().currentBody(m),b=body.getPlot().getBoundingBox();vessel=id.vesselId();oldBody=id.bodyId();
  var hatch=new BPos(b.minX()+4,b.minY()+6,b.minZ()+7),props=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
  level.setBlock(hatch,level.getBlockState(hatch).setValue(props.OPEN,Java.loadClass('java.lang.Boolean').TRUE),3);
  level.getBlockEntity(new BPos(b.minX()+2,b.minY()+1,b.minZ()+10)).data.set(6,0);
  Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);
 });
 server.scheduleInTicks(600,()=>{
  var m=BFleet.get(server);ba(m.refit().blockMode(),'second refit really uses world blocks');
  var cp=new BPos(64,69,152);ba(level.getBlockEntity(cp)!=null,'controller exists at ordinary world coordinate');
  ba(Java.loadClass('dev.ryanhcode.sable.companion.SableCompanion').INSTANCE.getContaining(level,cp)==null,'controller is not in a Sable plot');
  ba(m.adapter().loaded(level).stream().filter(b=>!b.isRemoved()).count()===0,'no live physical carrier during block editing');
  var hatch=new BPos(64,74,157),props=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
  level.setBlock(hatch,level.getBlockState(hatch).setValue(props.OPEN,Java.loadClass('java.lang.Boolean').FALSE),3);
  level.getBlockEntity(new BPos(62,69,160)).data.set(6,1);
  ba(m.refit().mayEdit(player,new BPos(60,70,154),Java.loadClass('net.minecraft.world.level.block.Blocks').IRON_BLOCK.defaultBlockState()),'owner can edit world blocks');
 });
 server.scheduleInTicks(1880,()=>{
  var m=BFleet.get(server),id=m.registry().flagship().get();ba(id.vesselId().equals(vessel),'permanent vessel identity unchanged');ba(!id.bodyId().equals(oldBody),'physics carrier replaced after reassembly');
  ba(m.adapter().loaded(level).stream().filter(b=>!b.isRemoved()).count()===1,'exactly one live carrier after rollback');ba(!m.refit().active(),'no unfinished transaction');
  ba(m.inspection().inspect(m.refit().currentBody(m)).qualification().eligible(),'rollback restored controller oxygen and sealed cabin');
  ba(level.getBlockState(new BPos(64,69,152)).isAir(),'no duplicate static ship remains');
  server.saveEverything(false,true,true);console.info('DBV_BLOCK SERVER COMPLETE');
 });
});
