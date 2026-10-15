PlayerEvents.loggedIn(e=>{
var s=e.server,p=e.player,l=s.overworld(),F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),D=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth'),B=Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties');
if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('DBV-Native-Upgrade'))return;
var start=null,forward=null,reverse=null,anchor=null;
p.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();
[100,200,400,600,800,1000,1080,1160,1240,1320,1500,1700,1900].forEach(t=>s.scheduleInTicks(t,()=>{try{var m=F.get(s),b=m.refit().currentBody(m),o=b.getPlot().getCenterBlock(),c=l.getBlockEntity(o.offset(7,4,3)),pos=D.worldPosition(b.logicalPose(),c.getBlockPos()),depth=c.surfaceY+1-pos.y();
var lever=(x,z,on)=>{var q=o.offset(x,5,z),state=l.getBlockState(q);if(String(state.getValue(B.POWERED))!=String(on))l.setBlock(q,state.cycle(B.POWERED),3);};
if(t<1700)p.connection.teleport(pos.x(),pos.y()+4,pos.z(),0,0);
if(t==100){if(c.targetDepth!=1||c.speedMode!=1||!m.starter().moored(b)||!m.inspection().inspect(b).qualification().eligible())throw Error('invalid delivered defaults');console.info('DELIVERY PASS defaults and restart mooring');}
if(t==200){Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'出航',seq:t});}
if(t>=400&&t<=1320)console.info('DELIVERY SAMPLE t='+t+' depth='+depth+' qual='+m.inspection().inspect(b).qualification()+' position='+pos+' yaw='+b.logicalPose().orientation());
if(t==1000){if(!m.starter().departed(m)||Math.abs(depth-1)>.5)throw Error('default departure/depth failed '+depth);start=pos.z();lever(2,5,false);lever(12,5,false);console.info('DELIVERY PASS default depth '+depth);}
if(t==1080){forward=pos.z();if(start-forward<.1)throw Error('no forward travel');console.info('DELIVERY PASS forward '+start+' -> '+forward);lever(3,6,true);lever(11,6,true);}
if(t==1160){reverse=pos.z();if(reverse-forward<.1)throw Error('no reverse travel');console.info('DELIVERY PASS reverse '+forward+' -> '+reverse);lever(3,6,false);}
if(t==1240){lever(3,6,true);lever(11,6,false);}
if(t==1320){lever(2,5,true);lever(12,5,true);lever(3,6,false);lever(11,6,false);}
if(t==1500){Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(p);JsonIO.write('kubejs/ui-action.json',{action:'停泊',seq:t});}
if(t==1700){if(m.refit().active()||!m.starter().moored(b)||m.starter().departed(m)||!m.inspection().inspect(b).qualification().eligible())throw Error('final mooring/qualification failed');anchor=[pos.x(),pos.y(),pos.z()];var cabin=D.worldPosition(b.logicalPose(),o.offset(7,4,9));p.connection.teleport(cabin.x(),cabin.y()-.5,cabin.z(),180,0);p.getAbilities().flying=false;p.onUpdateAbilities();m.storage().backup(m);s.saveEverything(false,true,true);console.info('DELIVERY PASS moored qualified player inside cabin identity='+m.registry().flagship().get().vesselId());}
if(t==1900){if(!m.starter().moored(b)||Math.hypot(pos.x()-anchor[0],pos.y()-anchor[1],pos.z()-anchor[2])>.3)throw Error('final anchor moved');s.saveEverything(false,true,true);console.info('DELIVERY PASS saved ready');}
}catch(x){console.error('DELIVERY FAIL '+x);}}));
});
