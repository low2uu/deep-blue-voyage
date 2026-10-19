var p=null,age=0,done=false;
PlayerEvents.loggedIn(e=>{p=e.player;});
ServerEvents.tick(e=>{if(p==null||done)return;++age;if(age<200)return;
try{var l=e.server.overworld();if(String(l.getSeed())!='1357924680'||l.getSeaLevel()!=128||l.getMinBuildHeight()!=-256||l.getWorldBorder().getSize()!=10000)throw Error('新地图复进参数错误');
var report=Java.loadClass('dbv.acceptance.FreshWorldProbe').finish(l);report.addProperty('savedReload',true);report.addProperty('seaLevel',l.getSeaLevel());report.addProperty('minY',l.getMinBuildHeight());report.addProperty('borderSize',l.getWorldBorder().getSize());report.addProperty('done',true);
e.server.saveEverything(false,true,true);JsonIO.write('kubejs/fresh-done.json',report);done=true;
}catch(x){done=true;JsonIO.write('kubejs/fresh-done.json',{done:true,error:String(x)});}});
