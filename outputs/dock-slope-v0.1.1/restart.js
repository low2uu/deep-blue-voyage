var p=null,age=0,done=false;
PlayerEvents.loggedIn(e=>{p=e.player;e.server.runCommandSilent("forceload add 0 112 128 240");});
ServerEvents.tick(e=>{if(p==null||done)return;++age;if(age<200||age%20!=0)return;
try{var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(e.server),b=m.refit().currentBody(m);
if(b==null){if(age<1200)return;throw Error('潜艇未加载');}var q=m.inspection().inspect(b).qualification();
if(!q.eligible()){if(age<1200)return;throw Error('复进资格不合格 '+q);}
if(String(b.getUniqueId())!='66d01998-fbc7-47d8-b8ac-7097dadc716a'||m.adapter().loaded(e.server.overworld()).size()!=1)throw Error('主艇身份或数量不正确');
if(!m.starter().moored(b))throw Error('停泊未恢复');
var devices=m.inspection().inspect(b).devices();if(!devices.stream().anyMatch(d=>String(d.id())=='dbv_world:jiuhu_console'))throw Error('控制台丢失');
var l=e.server.overworld();if(l.getWorldBorder().getSize()!=10000||l.getSeaLevel()!=128||l.getMinBuildHeight()!=-256)throw Error('世界参数未保持');
for(var cz=18;cz<=25;cz++)l.getChunk(4,cz);
var HT=Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types'),previous=l.getHeight(HT.OCEAN_FLOOR,64,303)-1,maxStep=0;
for(var z=304;z<=399;z++){var height=l.getHeight(HT.OCEAN_FLOOR,64,z)-1;maxStep=Math.max(maxStep,Math.abs(height-previous));previous=height;}
if(maxStep>2)throw Error('出口坡面仍有断崖 '+maxStep);
var audit=Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l);m.storage().backup(m);e.server.runCommandSilent("forceload remove 0 112 128 240");e.server.saveEverything(false,true,true);
JsonIO.write('kubejs/flooded-done.json',{done:true,passed:true,exitMaxStep:maxStep,bodyId:String(b.getUniqueId()),qualification:String(q),moored:true,vesselCount:1,audit:String(audit),mass:b.getMassTracker().getMass(),tick:age});done=true;console.info('联合复进验证通过');
}catch(x){done=true;JsonIO.write('kubejs/flooded-done.json',{done:true,error:String(x)});console.error('联合复进验证失败 '+x);}});
