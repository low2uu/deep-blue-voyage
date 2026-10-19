var age=0,done=false,player=null;
PlayerEvents.loggedIn(e=>{player=e.player;player.setGameMode(Java.loadClass('net.minecraft.world.level.GameType').CREATIVE);});
ServerEvents.tick(e=>{if(done||player==null)return;var s=e.server;++age;
try{var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s),b=m.refit().currentBody(m);
if(age==1)s.runCommandSilent('forceload add 0 112 128 240');
if(age==180){if(b==null||m.adapter().loaded(s.overworld()).size()!=1)throw Error('fresh creation failed');var q=m.inspection().inspect(b).qualification();if(!q.eligible()||!m.starter().moored(b))throw Error('fresh qualification failed '+q);
var c=m.inspection().inspect(b).devices().stream().filter(d=>String(d.id())=='dbv_world:jiuhu_console').findFirst().orElseThrow();
var pos=Java.loadClass('io.github.low2uu.dbv.fleet.compat.ControllerDepth').worldPosition(b.logicalPose(),c.position().offset(2,0,0));player.connection.teleport(pos.x(),pos.y(),pos.z(),0,0);player.getAbilities().flying=true;player.onUpdateAbilities();
Java.loadClass('io.github.low2uu.dbv.fleet.ui.FleetPanel').openMain(player);console.info('WORLD5 FRESH PASS '+q+' mass='+b.getMassTracker().getMass());JsonIO.write('kubejs/ui-ready.json',{ready:true});}
if(age==320){m.storage().backup(m);s.runCommandSilent('forceload remove all');s.saveEverything(false,true,true);console.info('WORLD5 FRESH SAVED');done=true;JsonIO.write('kubejs/done.json',{done:true});}
}catch(x){console.error('WORLD5 FRESH FAIL '+x);done=true;JsonIO.write('kubejs/done.json',{done:true});}});
