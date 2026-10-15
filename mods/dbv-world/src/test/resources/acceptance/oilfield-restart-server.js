PlayerEvents.loggedIn(event=>{
 var server=event.server,level=server.overworld();
 if(String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Oilfield-Acceptance')<0)return;
 server.scheduleInTicks(40,()=>{
  try{
   var policy=Java.loadClass('io.github.low2uu.dbv.world.oil.StarterOilfield');
   var data=Java.loadClass('com.jesz.createdieselgenerators.world.OilChunksSavedData');
   var chunk=Java.loadClass('net.minecraft.world.level.ChunkPos');
   for(var x=11;x<=12;x++)for(var z=-4;z<=-3;z++){
    if(data.getChunkOilAmount(level,new chunk(x,z))!==2147483647)throw new Error('infinite reserve lost');
   }
   if(data.getChunkOilAmount(level,new chunk(10,-4))!==2000)throw new Error('neighbor reserve changed');
   if(policy.initialize(level))throw new Error('initializer repeated');
   if(policy.mark(level)!==0)throw new Error('markers duplicated');
   console.info('DBV_OIL PASS restart preserves all four infinite fields, finite neighbor, and once-only initialization');
   console.info('DBV_OIL STATUS '+policy.status(level));
   if(server.getCommands().getDispatcher().execute('dbv oilfield',server.createCommandSourceStack())!==1)throw new Error('status command failed');
   console.info('DBV_OIL PASS status command registered alongside flagship commands');
   console.info('DBV_OIL RESTART COMPLETE');
  }catch(e){console.error('DBV_OIL FAIL restart '+e);}
 });
});
