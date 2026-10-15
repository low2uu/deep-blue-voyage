PlayerEvents.loggedIn(e=>{
 var s=e.server,l=s.overworld(),p=e.player;
 if(!String(s.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).includes('Angler-Acceptance'))return;
 s.scheduleInTicks(60,()=>{try{
 var Pos=Java.loadClass('net.minecraft.core.BlockPos'),G=Java.loadClass('net.minecraft.world.level.GameType'),Break=Java.loadClass('net.neoforged.neoforge.event.level.BlockEvent$BreakEvent'),bus=Java.loadClass('net.neoforged.neoforge.common.NeoForge').EVENT_BUS;
 var q=new Pos(0,68,0);p.setGameMode(G.SURVIVAL);var denied=new Break(l,q,l.getBlockState(q),p);bus.post(denied);if(!denied.isCanceled())throw Error('survival break allowed');
 p.setGameMode(G.CREATIVE);var allowed=new Break(l,q,l.getBlockState(q),p);bus.post(allowed);if(allowed.isCanceled())throw Error('creative admin blocked');
 var protection=Java.loadClass('io.github.low2uu.dbv.world.IslandProtection'),body=Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s).refit().currentBody(Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule').get(s));
 if(protection.protectedAt(l,body.getPlot().getCenterBlock()))throw Error('ship plot protected');
 if(protection.protectedAt(l,new Pos(500,68,500)))throw Error('ocean protected');
 console.info('DBV_PROTECTION PASS survival denied; creative admin allowed; ship and ocean excluded');
 }catch(x){console.error('DBV_PROTECTION FAIL '+x);}});
});
