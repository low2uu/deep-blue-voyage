// Formal-client acceptance copy only; fixture supplies water/energy/oxygen directly.
var QFleet = Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var QAssembly = Java.loadClass('dev.ryanhcode.sable.api.SubLevelAssemblyHelper');
var QBox = Java.loadClass('dev.ryanhcode.sable.companion.math.BoundingBox3i');
var QPos = Java.loadClass('net.minecraft.core.BlockPos');
var QBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks');
var QRegistry = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
var QResource = Java.loadClass('net.minecraft.resources.ResourceLocation');
var QList = Java.loadClass('java.util.ArrayList');
var QFluid = Java.loadClass('net.neoforged.neoforge.fluids.FluidStack');
var QAction = Java.loadClass('net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction');
var QFluids = Java.loadClass('net.minecraft.world.level.material.Fluids');
var QDeep = Java.loadClass('com.maxenonyme.createsubmarine.CreateSubmarine');
var QTracker = Java.loadClass('com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker');
function qAssert(ok, name) { if (!ok) throw new Error('DBV_QUAL_TEST FAIL ' + name); console.info('DBV_QUAL_TEST PASS ' + name); }
function qBlock(name) { return QRegistry.BLOCK.get(QResource.parse(name)).defaultBlockState(); }
PlayerEvents.loggedIn(event => {
 var server=event.server, level=server.overworld();
 if (String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Fleet-Qualification-Acceptance') < 0) return;
 event.player.connection.teleport(64,80,160,0,0); event.player.getAbilities().flying=true; event.player.onUpdateAbilities();
 server.scheduleInTicks(120, () => {
  var module=QFleet.get(server), source=server.createCommandSourceStack().withPermission(4), dispatcher=server.getCommands().getDispatcher();
  var restarting=module.registry().flagship().isPresent();
  var body;
  if (!restarting) {
   var blocks=new QList();
   for(var x=64;x<=72;x++) for(var y=70;y<=76;y++) for(var z=160;z<=168;z++) {
    var p=new QPos(x,y,z); var hull=x===64||x===72||y===70||y===76||z===160||z===168;
    level.setBlock(p,hull?QBlocks.IRON_BLOCK.defaultBlockState():QBlocks.AIR.defaultBlockState(),3);
    if(hull) blocks.add(p);
   }
   var names=['command_sub','electrolyzer','oxygene_diffuser','submarine_propeller','underwater_mine'];
   for(var n=0;n<names.length;n++){var p=new QPos(66+n,71,162);level.setBlock(p,qBlock('create_submarine:'+names[n]),3);blocks.add(p);}
   var power=new QPos(68,71,163);level.setBlock(power,QBlocks.REDSTONE_BLOCK.defaultBlockState(),3);blocks.add(power);
   body=QAssembly.assembleBlocks(level,new QPos(64,70,160),blocks,new QBox(64,70,160,72,76,168));
   qAssert(body!=null,'real Sable sealed hull assembly');
   qAssert(!module.inspection().inspect(body).qualification().eligible(),'unpowered unscanned hull denied');
   qAssert(dispatcher.execute('dbv flagship register '+body.getUniqueId()+' 不合格',source)===0,'invalid registration rejected');
   qAssert(module.registry().flagship().isEmpty(),'failed registration leaves identity empty');
  } else { body=module.adapter().find(level,module.registry().flagship().get().bodyId());qAssert(body!=null,'same body loaded after restart'); }
  var report=module.inspection().inspect(body), genPos=null,diffPos=null,controlPos=null;
  for(var i=0;i<report.devices().size();i++){var d=report.devices().get(i);if(String(d.id())==='create_submarine:electrolyzer')genPos=d.position();if(String(d.id())==='create_submarine:oxygene_diffuser')diffPos=d.position();if(String(d.id())==='create_submarine:command_sub')controlPos=d.position();}
  qAssert(genPos!=null&&diffPos!=null&&controlPos!=null,'devices found in assembled plot');
  var gen=level.getBlockEntity(genPos),diff=level.getBlockEntity(diffPos);
  if(gen.data.get(6)===0)gen.toggleEnabled();
  var ticks=0;
  function supply(){
   gen.energyStorage.receiveEnergy(1000,false);gen.waterTank.fill(new QFluid(QFluids.WATER,100),QAction.EXECUTE);
   diff.oxygenTank.fill(new QFluid(QDeep.OXYGEN.get(),100),QAction.EXECUTE);
   if(++ticks<430)server.scheduleInTicks(1,supply);
  }
  supply();
  server.scheduleInTicks(180,()=>{
   try {
   var r=module.inspection().inspect(body);
   console.info('DBV_QUAL_TEST REPORT '+r);
   qAssert(r.qualification().eligible(),'all three conditions accepted');
   qAssert(gen.oxygenTank.getFluidAmount()>0,'real electrolyzer produced oxygen');
   qAssert(r.devices().stream().anyMatch(d=>String(d.type())==='推进'),'propeller identified');
   qAssert(r.devices().stream().anyMatch(d=>String(d.type())==='武器'),'weapon identified');
   qAssert(dispatcher.execute('dbv flagship inspect '+body.getUniqueId(),source)===1,'inspection command');
   var RightClick=Java.loadClass('net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock');
   var Hand=Java.loadClass('net.minecraft.world.InteractionHand');
   var Hit=Java.loadClass('net.minecraft.world.phys.BlockHitResult');
   var Vec=Java.loadClass('net.minecraft.world.phys.Vec3');
   var Direction=Java.loadClass('net.minecraft.core.Direction');
   var Bus=Java.loadClass('net.neoforged.neoforge.common.NeoForge');
   var hit=new Hit(Vec.atCenterOf(controlPos),Direction.UP,controlPos,false);
   event.player.setShiftKeyDown(false);var normal=new RightClick(event.player,Hand.MAIN_HAND,controlPos,hit);Bus.EVENT_BUS.post(normal);
   qAssert(!normal.isCanceled(),'ordinary computer interaction preserved');
   event.player.setShiftKeyDown(true);var inspectEvent=new RightClick(event.player,Hand.MAIN_HAND,controlPos,hit);Bus.EVENT_BUS.post(inspectEvent);
   qAssert(inspectEvent.isCanceled(),'sneak interaction opens equipment report');event.player.setShiftKeyDown(false);
   var external=new QPos(64,79,160); level.setBlock(external,QBlocks.CHEST.defaultBlockState(),3);
   qAssert(module.inspection().inspect(body).devices().stream().noneMatch(d=>String(d.id())==='minecraft:chest'),'nearby world equipment excluded');
   var water=gen.waterTank.getFluidAmount();gen.waterTank.setFluid(QFluid.EMPTY);qAssert(!module.inspection().inspect(body).qualification().oxygen(),'generator without water rejected');gen.waterTank.setFluid(new QFluid(QFluids.WATER,water));
   gen.toggleEnabled(); qAssert(!module.inspection().inspect(body).qualification().oxygen(),'disabled generator rejected'); gen.toggleEnabled();
   var energy=gen.energyStorage.getEnergyStored();while(gen.energyStorage.getEnergyStored()>0)gen.energyStorage.extractEnergy(1000,false);
   qAssert(!module.inspection().inspect(body).qualification().oxygen(),'generator without energy rejected');gen.energyStorage.receiveEnergy(1000,false);
   diff.oxygenTank.setFluid(QFluid.EMPTY);qAssert(!module.inspection().inspect(body).qualification().oxygen(),'empty diffuser rejected');diff.oxygenTank.fill(new QFluid(QDeep.OXYGEN.get(),1000),QAction.EXECUTE);
   var power=diffPos.south();var powerState=level.getBlockState(power);level.setBlock(power,QBlocks.AIR.defaultBlockState(),3);
   qAssert(!module.inspection().inspect(body).qualification().oxygen(),'diffuser without redstone rejected');level.setBlock(power,powerState,3);
   var state=level.getBlockState(controlPos);level.setBlock(controlPos,QBlocks.AIR.defaultBlockState(),3);
   qAssert(!module.inspection().inspect(body).qualification().controller(),'missing controller rejected');level.setBlock(controlPos,state,3);
   var optional=[];
   for(var j=0;j<r.devices().size();j++){var device=r.devices().get(j);if(String(device.type())==='推进'||String(device.type())==='武器'){
    optional.push({pos:device.position(),state:level.getBlockState(device.position())});level.setBlock(device.position(),QBlocks.AIR.defaultBlockState(),3);
   }}
   server.scheduleInTicks(60,()=>{
    qAssert(module.inspection().inspect(body).qualification().eligible(),'propulsion and weapons are not admission requirements');
    if(!restarting)qAssert(dispatcher.execute('dbv flagship register '+body.getUniqueId()+' 酒狐资格验收艇',source)===1,'qualified registration');
    else qAssert(String(module.registry().flagship().get().name())==='酒狐资格验收艇','identity persisted after restart');
    qAssert(dispatcher.execute('dbv flagship register '+body.getUniqueId()+' 第二艘',source)===0,'second registration denied');
    for(var j=0;j<optional.length;j++)level.setBlock(optional[j].pos,optional[j].state,3);
    var identity=module.registry().flagship().get().vesselId();
    var bound=body.getPlot().getBoundingBox();var hole=new QPos(bound.minX(),bound.minY()+3,bound.minZ()+4);
    var wall=level.getBlockState(hole);level.setBlock(hole,QBlocks.AIR.defaultBlockState(),3);
    qAssert(!module.inspection().inspect(body).qualification().eligible(),'hull change denies stale qualification');
    server.scheduleInTicks(70,()=>{
     qAssert(!module.inspection().inspect(body).qualification().sealed(),'open hull rejected after rescan');
     qAssert(module.registry().flagship().get().vesselId().equals(identity),'damage retains permanent identity');
     level.setBlock(hole,wall,3);
     server.scheduleInTicks(60,()=>{
      qAssert(module.inspection().inspect(body).qualification().eligible(),'repaired hull qualifies again');
      qAssert(server.saveEverything(false,true,true),'save accepted world');
      console.info('DBV_QUAL_TEST COMPLETE');
     });
    });
   });
   } catch(error) { console.error('DBV_QUAL_TEST ERROR '+error); throw error; }
  });
 });
});
