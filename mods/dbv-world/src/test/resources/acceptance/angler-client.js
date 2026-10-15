var aOpen=false,aTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!aOpen&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){aOpen=true;mc.createWorldOpenFlows().openWorld('DBV-Angler-Acceptance8',()=>console.error('DBV_ANGLER FAIL open'));}
 if(mc.level!=null&&++aTicks>850)mc.stop();
});
