let eOpened=false,eMenu=0,eTicks=0;
function eb(s,label){for(var i=0;i<s.children().size();i++){var b=s.children().get(i);if(typeof b.getMessage==='function'&&String(b.getMessage().getString())===label)return b;}throw new Error('Missing '+label);}
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!eOpened&&++eMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){eOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Expansion-Acceptance',()=>console.error('DBV_EXPAND OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;var t=++eTicks;
 if(t===1460){var button=eb(mc.screen,'出航');if(button.active){console.info('DBV_EXPAND PASS departure button enabled');button.onPress();}else console.error('DBV_EXPAND FAIL departure disabled');}
 if(t===1480){if(mc.screen.snapshot().has(4096)&&!eb(mc.screen,'已出航').active)console.info('DBV_EXPAND PASS departed UI disables duplicate');else console.error('DBV_EXPAND FAIL missing departed UI');Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_EXPAND SCREENSHOT '+c.getString()));}
 if(t===1620)mc.stop();
});
