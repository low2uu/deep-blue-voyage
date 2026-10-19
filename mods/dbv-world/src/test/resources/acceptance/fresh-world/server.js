var p=null,age=0,started=false,done=false,count=0;
PlayerEvents.loggedIn(e=>{p=e.player;});
ServerEvents.tick(e=>{if(p==null||done)return;++age;if(age<300)return;
try{var probe=Java.loadClass('dbv.acceptance.FreshWorldProbe'),l=e.server.overworld();if(!started){console.info('新建世界目标区块 '+probe.prepare(l));started=true;}
var status=probe.advance(l);++count;if(count%25==0)console.info('新建世界检查 '+status);if(status!='done')return;
var audit=Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l);var report=probe.finish(l);report.addProperty('audit',String(audit));report.addProperty('done',true);
e.server.saveEverything(false,true,true);JsonIO.write('kubejs/fresh-done.json',report);done=true;
}catch(x){done=true;JsonIO.write('kubejs/fresh-done.json',{done:true,error:String(x)});console.error('新地图失败 '+x);}});
