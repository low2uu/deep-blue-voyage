var heightPlayer=null,heightAge=0,heightFinished=false;
PlayerEvents.loggedIn(e=>{heightPlayer=e.player;});
ServerEvents.tick(e=>{
if(heightPlayer==null||heightFinished)return;
++heightAge;if(heightAge!=400)return;
try{
var s=e.server,l=s.overworld(),P=Java.loadClass('net.minecraft.core.BlockPos');
var expect=(ok,msg)=>{if(!ok)throw Error(msg);};
expect(l.getMinBuildHeight()==-256,'min height');expect(l.getMaxBuildHeight()==320,'max height');expect(l.getSeaLevel()==128,'sea level');
var block=(x,y,z)=>String(l.getBlockState(new P(x,y,z)).getBlock().builtInRegistryHolder().key().location());
expect(block(2000,-256,2000)=='minecraft:bedrock','new chunk bottom');
expect(block(2000,127,2000)=='minecraft:water','new chunk sea water');
expect(block(2000,128,2000)=='minecraft:air','new chunk sea air');
expect(block(400,127,400)=='minecraft:water','migrated sea water');
var audit=Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l);
var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s),b=m.refit().currentBody(m);
expect(b!=null,'submarine missing');
var q=m.inspection().inspect(b).qualification();
expect(q.controller()&&q.oxygen()&&q.sealed()&&!q.pending(),'submarine qualification: '+q);
var result={done:true,minY:l.getMinBuildHeight(),maxYExclusive:l.getMaxBuildHeight(),sea:l.getSeaLevel(),audit:String(audit),pose:String(b.logicalPose()),qualification:String(m.inspection().inspect(b).qualification())};
s.saveEverything(false,true,true);JsonIO.write('kubejs/height-done.json',result);console.info('HEIGHT PASS '+JSON.stringify(result));heightFinished=true;
}catch(x){console.error('HEIGHT FAIL '+x);JsonIO.write('kubejs/height-done.json',{done:true,error:String(x)});heightFinished=true;}
});
