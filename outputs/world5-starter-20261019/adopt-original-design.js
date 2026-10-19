var age=0,done=false,player=null;
PlayerEvents.loggedIn(e=>{player=e.player;player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);});
ServerEvents.tick(e=>{if(done||player==null)return;var s=e.server;++age;
try{var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s),l=s.overworld();
if(age==1)s.runCommandSilent('forceload add 0 112 128 352');
if(age==120){Java.loadClass('dbv.acceptance.World5Probe').adopt(s);console.info('WORLD5 ADOPTED original design');}
if(age==250){var b=m.refit().currentBody(m),report=m.inspection().inspect(b);if(!report.qualification().eligible())throw Error('资格未满足 '+report.qualification());if(!m.starter().moored(b))throw Error('not moored');
var c=report.devices().stream().filter(d=>String(d.id())=='dbv_world:jiuhu_console').findFirst().orElseThrow();
var pos=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth').worldPosition(b.logicalPose(),c.position().offset(2,0,0));player.connection.teleport(pos.x(),pos.y(),pos.z(),0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);console.info('WORLD5 DELIVERY console='+c.id()+' qualification='+report.qualification()+' body='+b.getUniqueId()+' count='+m.adapter().loaded(l).size());}
if(age==300){m.storage().backup(m);s.saveEverything(false,true,true);console.info('WORLD5 DELIVERY PASS saved');done=true;JsonIO.write('kubejs/done.json',{done:true});}
}catch(x){console.error('WORLD5 DELIVERY FAIL '+x);done=true;JsonIO.write('kubejs/done.json',{done:true});}});
