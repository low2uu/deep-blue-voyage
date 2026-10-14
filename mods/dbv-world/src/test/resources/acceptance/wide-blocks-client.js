let wOpened=false,wMenu=0,wTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!wOpened&&++wMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){wOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Wide-Blocks-Acceptance',()=>console.error('DBV_WIDE OPEN FAILED'));}
 if(mc.level!=null&&mc.player!=null&&++wTicks===900)mc.stop();
});
