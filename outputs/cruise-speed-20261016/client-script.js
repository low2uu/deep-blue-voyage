var cruiseOpened=false,cruiseTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{
var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
if(!cruiseOpened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){cruiseOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Cruise-Trial',()=>console.error('CRUISE FAIL open'));}
if(mc.level==null)return;
++cruiseTicks;
if(cruiseTicks%40==0){var done=JsonIO.read('kubejs/cruise-done.json');if(done!=null&&done.done)mc.stop();}
if(cruiseTicks>12000)mc.stop();
});
