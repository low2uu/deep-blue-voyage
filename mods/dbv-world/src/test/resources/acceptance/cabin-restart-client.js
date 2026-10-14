let rOpened=false,rMenu=0,rTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!rOpened&&++rMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){rOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Cabin-Storage-Acceptance',()=>console.error('DBV_CABIN_RESTART OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;var t=++rTicks;
 if(t===240){if(mc.screen!=null&&String(mc.screen).includes('FleetScreen')&&mc.screen.snapshot().storage().size()>0)console.info('DBV_CABIN_RESTART PASS right click opened populated UI');else console.error('DBV_CABIN_RESTART FAIL panel UI absent');}
 if(t===300)mc.stop();
});
