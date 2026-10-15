var dcOpened=false,dcTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!dcOpened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){dcOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Depth-Acceptance',()=>console.error('DBV_DEPTH OPEN FAILED'));}
 if(mc.level!=null&&++dcTicks>360)mc.stop();
});
