var p=null,age=0,done=false;
PlayerEvents.loggedIn(e=>{p=e.player;});
ServerEvents.tick(e=>{if(p==null||done)return;++age;if(age<200||age%20!=0)return;
try{var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(e.server),b=m.refit().currentBody(m);
if(b==null)throw Error('潜艇未加载');var q=m.inspection().inspect(b).qualification();
if(!q.eligible()){if(age<1200)return;throw Error('复进资格不合格 '+q);}
if(String(b.getUniqueId())!='66d01998-fbc7-47d8-b8ac-7097dadc716a'||m.adapter().loaded(e.server.overworld()).size()!=1)throw Error('主艇身份或数量不正确');
if(!m.starter().moored(b))throw Error('停泊未恢复');
var devices=m.inspection().inspect(b).devices();if(!devices.stream().anyMatch(d=>String(d.id())=='dbv_world:jiuhu_console'))throw Error('控制台丢失');
var l=e.server.overworld();if(l.getWorldBorder().getSize()!=10000||l.getSeaLevel()!=128||l.getMinBuildHeight()!=-256)throw Error('世界参数未保持');
var audit=Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l);m.storage().backup(m);e.server.saveEverything(false,true,true);
JsonIO.write('kubejs/flooded-done.json',{done:true,passed:true,bodyId:String(b.getUniqueId()),qualification:String(q),moored:true,vesselCount:1,audit:String(audit),mass:b.getMassTracker().getMass(),tick:age});done=true;console.info('联合复进验证通过');
}catch(x){done=true;JsonIO.write('kubejs/flooded-done.json',{done:true,error:String(x)});console.error('联合复进验证失败 '+x);}});
