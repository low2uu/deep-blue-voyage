let cOpened=false,cMenu=0,cTicks=0;
function cb(s,label){for(var i=0;i<s.children().size();i++){var b=s.children().get(i);if(typeof b.getMessage==='function'&&String(b.getMessage().getString())===label)return b;}throw new Error('Missing '+label);}
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!cOpened&&++cMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){cOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Cabin-Storage-Acceptance',()=>console.error('DBV_CABIN OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;var t=++cTicks;
 if(t>=720&&mc.screen==null){console.error('DBV_CABIN FAIL server did not open panel');mc.stop();return;}
 if(t===720){cb(mc.screen,'舱内存储').onPress();if(mc.screen.snapshot().storage().size()===0)throw new Error('DBV_CABIN FAIL empty storage network payload');console.info('DBV_CABIN PASS storage tab and payload');}
 if(t===760||t===820)Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_CABIN SCREENSHOT '+c.getString()));
 if(t===800)cb(mc.screen,'船况与改装').onPress();
 if(t===900)mc.stop();
});
