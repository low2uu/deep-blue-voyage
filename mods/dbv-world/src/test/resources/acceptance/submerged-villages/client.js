var opened=false,ticks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
if(!opened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){opened=true;Java.loadClass('dbv.acceptance.VillageProbe').create();}
if(mc.level==null)return;++ticks;if(ticks%40==0){var done=JsonIO.read('kubejs/village-done.json');if(done!=null&&done.done)mc.stop();}if(ticks>20000)mc.stop();});
