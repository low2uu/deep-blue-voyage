var p=null,age=0,done=false;
PlayerEvents.loggedIn(e=>{p=e.player;e.server.runCommandSilent("forceload add 0 112 128 240");});
ServerEvents.tick(e=>{if(p==null||done)return;++age;if(age<200||age%20!=0)return;
try{var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(e.server),b=m.refit().currentBody(m);
if(b==null||!m.inspection().inspect(b).qualification().eligible()){if(age<1200)return;throw Error('新存档潜艇不合格');}
if(m.adapter().loaded(e.server.overworld()).size()!=1||!m.starter().moored(b))throw Error('主艇数量或停泊异常');
var l=e.server.overworld(),probe=Java.loadClass('dbv.acceptance.MineralProbe');probe.prepare(l);probe.verifyNaturalOrePreservation(l);var result=probe.finish(l);
result.addProperty('savedAndReloaded',true);result.addProperty('naturalOrePreserved',true);result.addProperty('qualification',String(m.inspection().inspect(b).qualification()));result.addProperty('bodyId',String(b.getUniqueId()));result.addProperty('mass',b.getMassTracker().getMass());
result.addProperty('audit',String(Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l)));m.storage().backup(m);e.server.runCommandSilent("forceload remove 0 112 128 240");e.server.saveEverything(false,true,true);
result.addProperty('done',true);JsonIO.write('kubejs/mineral-reload-done.json',result);done=true;
}catch(x){done=true;JsonIO.write('kubejs/mineral-reload-done.json',{done:true,error:String(x)});console.error('复进验证失败 '+x);}});
