let dOpened=false,dMenu=0,dTicks=0;
function db(s,label){for(var i=0;i<s.children().size();i++){var b=s.children().get(i);if(typeof b.getMessage==='function'&&String(b.getMessage().getString())===label)return b;}throw new Error('Missing '+label);}
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!dOpened&&++dMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){dOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Departure-Acceptance',()=>console.error('DBV_DEPART OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;var t=++dTicks;
 if(t===220){var button=db(mc.screen,'出航');if(!button.active)throw new Error('DBV_DEPART FAIL disabled button');console.info('DBV_DEPART PASS departure button enabled');button.onPress();}
 if(t===240){if(!mc.screen.snapshot().has(4096)||db(mc.screen,'已出航').active)throw new Error('DBV_DEPART FAIL wrong departed UI');console.info('DBV_DEPART PASS departed UI disables repeat action');Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_DEPART SCREENSHOT '+c.getString()));}
 if(t===440)mc.stop();
});
