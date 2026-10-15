var opened=false,ticks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!opened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){opened=true;mc.createWorldOpenFlows().openWorld('DBV-Dive-Diagnosis-Final',()=>console.error('DBV_UPGRADE FAIL open'));}
 if(mc.level!=null){++ticks;if(ticks==350)Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,'storage-terminal.png',mc.getMainRenderTarget(),msg=>console.info(String(msg)));if(ticks>3100)mc.stop();}
});
