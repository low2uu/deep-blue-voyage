let cOpened=false,cMenu=0,cTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!cOpened&&++cMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){cOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Block-Refit-Acceptance',()=>console.error('DBV_BLOCK_RESTART OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;
 if(++cTicks===400)mc.stop();
});
