var AF=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),AP=Java.loadClass('net.minecraft.core.BlockPos');
PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld(),p=event.player;
 if(!String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Angler-Acceptance'))return;
 p.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);p.connection.teleport(67,80,158,0,45);p.getAbilities().flying=true;p.onUpdateAbilities();
 server.scheduleInTicks(140,()=>{try{var m=AF.get(server);console.info('DBV_ANGLER '+m.refit().execute('angler',p,m));console.info('DBV_ANGLER PASS template installed');}catch(e){console.error('DBV_ANGLER FAIL '+e);}});
  server.scheduleInTicks(300,()=>{try{
  var detector=Java.loadClass('com.maxenonyme.createsubmarine.submarine.compartment.CompartmentDetector');
  var queue=[[65,72,159]],seen={'65,72,159':true},parent={},head=0,dirs=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]];
  while(head<queue.length){var a=queue[head++],key=a.join(',');if(a[0]<60||a[0]>74||a[1]<68||a[1]>78||a[2]<150||a[2]>173){var path=[];while(key){path.push(key);key=parent[key];}console.info('DBV_ANGLER LEAK '+path.join(' <- '));return;}
   dirs.forEach(d=>{var n=[a[0]+d[0],a[1]+d[1],a[2]+d[2]],k=n.join(',');if(!seen[k]&&detector.isPermeable(level.getBlockState(new AP(n[0],n[1],n[2])))){seen[k]=true;parent[k]=key;queue.push(n);}});
  }console.info('DBV_ANGLER SEALED static cells='+queue.length);
 }catch(e){console.error('DBV_ANGLER LEAKTEST '+e);}});
 server.scheduleInTicks(400,()=>{try{
  var m=AF.get(server);var devices=[[63,72,157],[71,72,157],[65,72,155],[63,70,156],[71,70,166],[60,72,163],[74,72,163],[65,72,157]];
  devices.forEach(q=>{var be=level.getBlockEntity(new AP(q[0],q[1],q[2]));console.info('DBV_ANGLER DEVICE '+q+' '+(be==null?'missing':be.getSpeed()));});
  var g=level.getBlockEntity(new AP(65,72,155)),d=level.getBlockEntity(new AP(65,72,158));console.info('DBV_ANGLER SUPPLY water='+g.waterTank.getFluidAmount()+' energy='+g.energyStorage.getEnergyStored()+' oxygen='+g.oxygenTank.getFluidAmount()+' diffuser='+d.oxygenTank.getFluidAmount());console.info('DBV_ANGLER '+m.refit().execute('commit',p,m));
 }catch(e){console.error('DBV_ANGLER FAIL '+e);}});
 server.scheduleInTicks(600,()=>{try{var m=AF.get(server),body=m.refit().currentBody(m);console.info('DBV_ANGLER MIDQUAL '+m.inspection().inspect(body).qualification());console.info('DBV_ANGLER MID moored='+m.starter().moored(body)+' bounds='+body.boundingBox());m.inspection().inspect(body).devices().forEach(d=>console.info('DBV_ANGLER REPORT '+d));}catch(e){console.error('DBV_ANGLER MID '+e);}});
  server.scheduleInTicks(1000,()=>{try{
  var m=AF.get(server),body=m.refit().currentBody(m);if(m.refit().active())throw new Error('still refitting');
  var origin=body.getPlot().getCenterBlock();
  [2,12].forEach(x=>{var q=origin.offset(x,5,5),s=level.getBlockState(q);level.setBlock(q,s.cycle(Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties').POWERED),3);});
  console.info('DBV_ANGLER PROP clutches enabled');
 }catch(e){console.error('DBV_ANGLER FAIL '+e);}});
 server.scheduleInTicks(1100,()=>{try{
  var m=AF.get(server),body=m.refit().currentBody(m),o=body.getPlot().getCenterBlock();
  [0,14].forEach(x=>console.info('DBV_ANGLER PROP speed '+x+'='+level.getBlockEntity(o.offset(x,4,13)).getSpeed()));
  p.connection.teleport(83,82,140,38,22);
 }catch(e){console.error('DBV_ANGLER FAIL '+e);}});
 server.scheduleInTicks(900,()=>{try{var m=AF.get(server);console.info('DBV_ANGLER FINAL '+m.refit().status());var body=m.refit().currentBody(m);console.info('DBV_ANGLER QUAL '+m.inspection().inspect(body).qualification());}catch(e){console.error('DBV_ANGLER FAIL '+e);}});
});
