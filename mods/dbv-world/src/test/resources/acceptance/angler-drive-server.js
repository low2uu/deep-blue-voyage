var AF=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
PlayerEvents.loggedIn(e=>{
 var s=e.server,l=s.overworld(),p=e.player;
 if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Angler-Acceptance'))return;
 function test(t,fn){s.scheduleInTicks(t,()=>{try{fn(AF.get(s));}catch(x){console.error('DBV_ANGLER FAIL '+x);}});}
 function flip(m,x,y,z){var o=m.refit().currentBody(m).getPlot().getCenterBlock(),q=o.offset(x,y,z),st=l.getBlockState(q);l.setBlock(q,st.cycle(Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties').POWERED),3);}
 function speed(m,x){return l.getBlockEntity(m.refit().currentBody(m).getPlot().getCenterBlock().offset(x,4,13)).getSpeed();}
 test(100,m=>{if(!m.inspection().inspect(m.refit().currentBody(m)).qualification().eligible())throw Error('qualification');flip(m,2,5,5);flip(m,12,5,5);});
 test(180,m=>{var a=speed(m,0),b=speed(m,14);if(a==0||b==0)throw Error('propellers stopped '+a+','+b);console.info('DBV_ANGLER PASS propellers '+a+','+b);flip(m,3,5,6);});
 test(260,m=>{var a=speed(m,0),b=speed(m,14);if(a*b>=0)throw Error('reverse failed '+a+','+b);console.info('DBV_ANGLER PASS independent reverse '+a+','+b);flip(m,3,5,6);flip(m,2,5,5);flip(m,12,5,5);p.connection.teleport(67,80,158,0,45);});
 test(320,m=>console.info('DBV_ANGLER DEPART '+m.starter().depart(p,m)));
 test(720,m=>{var body=m.refit().currentBody(m),o=body.getPlot().getCenterBlock(),g=l.getBlockEntity(o.offset(5,4,5));if(g.waterTank.getFluidAmount()==0||g.energyStorage.getEnergyStored()==0)throw Error('no independent oxygen supply');console.info('DBV_ANGLER PASS independent supply water='+g.waterTank.getFluidAmount()+' energy='+g.energyStorage.getEnergyStored()+' qualification='+m.inspection().inspect(body).qualification()+' bounds='+body.boundingBox());});
});
