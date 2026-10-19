var age=0,done=false,newBody=null;
ServerEvents.tick(e=>{if(done)return;var s=e.server;++age;
try {
var F=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule'),m=F.get(s),l=s.overworld();
if(age==1){s.runCommandSilent('forceload add 0 112 128 352');s.runCommandSilent('gamerule doMobSpawning false');}
if(age==120){m.adapter().loaded(l).forEach(b=>console.info('WORLD5 ORIGINAL '+b.getUniqueId()+' '+m.inspection().inspect(b).qualification()+' mass='+b.getMassTracker().getMass()));}
if(age==160){
var old=m.refit().currentBody(m);if(old==null)throw Error('old body not loaded');
m.storage().backup(m);m.starter().releaseMooring();
Java.loadClass('dbv.acceptance.World5Probe').remove(old);
newBody=Java.loadClass('io.github.low2uu.dbv.fleet.dock.DesignedSubmarine').create(l);
m.rebindForRefit(old.getUniqueId(),newBody);m.starter().adoptRefitBody(newBody);
console.info('WORLD5 IMPORT '+newBody.getUniqueId()+' bounds='+newBody.boundingBox());}
if(age>=200&&age%100==0){console.info('WORLD5 CHECK t='+age+' '+m.inspection().inspect(newBody).qualification()+' mass='+newBody.getMassTracker().getMass()+' moored='+m.starter().moored(newBody));}
if(age==600){var q=m.inspection().inspect(newBody).qualification();m.storage().backup(m);s.saveEverything(false,true,true);if(!q.eligible())throw Error('qualification failed');console.info('WORLD5 FINAL '+q+' devices='+m.inspection().inspect(newBody).devices().size());console.info('WORLD5 CONSOLE '+m.inspection().inspect(newBody).devices().stream().filter(d=>String(d.id())=='dbv_world:jiuhu_console').count());done=true;JsonIO.write('kubejs/done.json',{done:true});}
}catch(x){console.error('WORLD5 FAIL t='+age+' '+x);done=true;JsonIO.write('kubejs/done.json',{done:true});}});
