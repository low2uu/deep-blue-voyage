var floodPlayer=null,floodAge=0,floodDone=false,floodMoored=false,floodReport=null;
PlayerEvents.loggedIn(e=>{floodPlayer=e.player;});
ServerEvents.tick(e=>{
if(floodPlayer==null||floodDone)return;
++floodAge;
if(!floodMoored&&floodAge%2==0&&floodAge<100){
try{var module=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(e.server);var body=module.refit().currentBody(module);
if(body!=null){if(module.starter().departed(module)){var issue=module.starter().mooringProblem(floodPlayer,module);if(issue==null){console.info('FLOODED MOOR '+module.starter().moor(floodPlayer,module));floodMoored=true;}else console.info('FLOODED MOOR waiting '+issue);}else floodMoored=true;}
}catch(problem){console.info('FLOODED MOOR waiting '+problem);}}
if(floodReport!=null&&floodAge%20==0){
try{var mm=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(e.server),bb=mm.refit().currentBody(mm),qq=mm.inspection().inspect(bb).qualification();
var ct=Java.loadClass('com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker');
if(qq.eligible()){floodReport.qualification=String(qq);floodReport.qualificationTick=floodAge;e.server.saveEverything(false,true,true);JsonIO.write('kubejs/flooded-done.json',floodReport);console.info('FLOODED PASS '+JSON.stringify(floodReport));floodDone=true;}
else if(floodAge>=1200){throw Error('qualification '+qq+' scan='+ct.isScanActive(bb.getUniqueId())+' dirty='+ct.isStructureDirty(bb.getUniqueId())+' updated='+ct.lastUpdateTick(bb.getUniqueId())+' now='+e.server.overworld().getGameTime());}
}catch(problem){JsonIO.write('kubejs/flooded-done.json',{done:true,error:String(problem)});console.error('FLOODED FAIL '+problem);floodDone=true;}
return;}
if(floodAge!=400)return;
try{
var s=e.server,l=s.overworld(),P=Java.loadClass('net.minecraft.core.BlockPos'),HT=Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types');
var expect=(ok,msg)=>{if(!ok)throw Error(msg);};
expect(floodMoored,'mooring did not finish');
var block=(x,y,z)=>String(l.getBlockState(new P(x,y,z)).getBlock().builtInRegistryHolder().key().location());
expect(l.getMinBuildHeight()==-256&&l.getMaxBuildHeight()==320,'height');expect(l.getSeaLevel()==128,'sea');
expect(l.getWorldBorder().getSize()==10000,'border');
var audit=Java.loadClass('io.github.low2uu.dbv.world.IslandBuildAudit').verify(l);
var results=[],oceanCount=0;
[[1200,0],[-1200,0],[0,1600],[2000,2000]].forEach(pair=>{
var x=pair[0],z=pair[1];l.getChunk(x>>4,z>>4);
var floor=l.getHeight(HT.OCEAN_FLOOR,x,z)-1;
expect(block(x,-256,z)=='minecraft:bedrock','bottom '+pair);
var biome=String(l.getBiome(new P(x,Math.max(floor+5,100),z)).unwrapKey().get().location());
var plants=0,heights=[];
for(var dx=0;dx<16;dx+=4)for(var dz=0;dz<16;dz+=4)heights.push(l.getHeight(HT.OCEAN_FLOOR,x+dx,z+dz)-1);
if(floor<127){expect(block(x,127,z)=='minecraft:water'||block(x,127,z)=='minecraft:ice','flood surface '+pair);expect(biome.includes('ocean'),'ocean biome '+biome);oceanCount++;}
for(var dx=0;dx<16;dx++)for(var dz=0;dz<16;dz++)for(var y=0;y<128;y++){
var id=block(x+dx,y,z+dz);if(id.includes('kelp')||id.includes('seagrass')||id.includes('coral'))plants++;
}
results.push({x:x,z:z,floor:floor,biome:biome,minFloor:Math.min.apply(null,heights),maxFloor:Math.max.apply(null,heights),marinePlantBlocks:plants});
});
expect(oceanCount>0,'no flooded samples');
expect(results.some(v=>v.maxFloor>v.minFloor),'flat terrain');
expect(results.some(v=>v.marinePlantBlocks>0),'marine vegetation absent');
expect(block(4999,127,0)!='minecraft:air','inside boundary sea');
for(var x of [5000,5010])for(var y of [-256,20,64,127,200])expect(block(x,y,0)=='minecraft:air','exterior terrain '+x+','+y);
var m=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s),b=m.refit().currentBody(m);expect(b!=null,'submarine');
var q=m.inspection().inspect(b).qualification();
var oil=Java.loadClass('io.github.low2uu.dbv.world.oil.StarterOilfield').status(l);
var report={done:true,audit:String(audit),border:l.getWorldBorder().getSize(),samples:results,qualification:String(q),oil:String(oil),mobSpawning:String(s.getGameRules().getBoolean(Java.loadClass('net.minecraft.world.level.GameRules').RULE_DOMOBSPAWNING))};
expect(report.mobSpawning=='true','natural spawning disabled');floodReport=report;console.info('FLOODED TERRAIN PASS '+JSON.stringify(report));
}catch(x){console.error('FLOODED FAIL '+x);JsonIO.write('kubejs/flooded-done.json',{done:true,error:String(x)});floodDone=true;}
});
