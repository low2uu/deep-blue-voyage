let bOpened=false,bMenu=0,bTicks=0;
function bc(ok,msg){if(!ok){console.error('DBV_BLOCK FAIL '+msg);return;}console.info('DBV_BLOCK PASS '+msg);}
function bb(s,label){for(var i=0;i<s.children().size();i++){var b=s.children().get(i);if(typeof b.getMessage==='function'&&String(b.getMessage().getString())===label)return b;}throw new Error('Missing '+label);}
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!bOpened&&++bMenu>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){bOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Block-Refit-Acceptance',()=>console.error('DBV_BLOCK OPEN FAILED'));}
 if(mc.level==null||mc.player==null)return;var t=++bTicks;
 if(t===220){bc(!mc.screen.snapshot().has(2)&&!mc.screen.snapshot().has(4),'starts with oxygen and seal failing');bc(bb(mc.screen,'开始改装').active,'begin enabled for unqualified ship');bb(mc.screen,'开始改装').onPress();}
 if(t===280){bc(mc.screen.snapshot().has(512),'begin changes UI into world block mode');bc(bb(mc.screen,'完成改装').active,'completion available while editing');console.info('DBV_BLOCK VIEW '+mc.screen.snapshot());}
 if(t===300)mc.screen.onClose();
 if(t===310)mc.player.connection.sendCommand('dbv refit');
 if(t===350){bc(String(mc.screen).includes('FleetScreen'),'command reopens static refit UI');bb(mc.screen,'取消还原').onPress();}
 if(t===370)bb(mc.screen,'确认还原').onPress();
 if(t===430){bc(!mc.screen.snapshot().has(32),'cancel restored physical ship');bc(!mc.screen.snapshot().has(2),'cancel preserves original unqualified oxygen state');bb(mc.screen,'开始改装').onPress();}
 if(t===650)bb(mc.screen,'完成改装').onPress();
 if(t===700){bc(mc.screen.snapshot().has(128)||!mc.screen.snapshot().has(32),'completion is validating or delivered');console.info('DBV_BLOCK VALIDATING '+mc.screen.snapshot());}
 if(t===1150){bc(!mc.screen.snapshot().has(32),'valid completion finishes transaction');bc(mc.screen.snapshot().has(1)&&mc.screen.snapshot().has(2)&&mc.screen.snapshot().has(4),'delivered physical ship passes all conditions');}
 if(t===1200)bb(mc.screen,'开始改装').onPress();
 if(t===1250)mc.player.connection.sendCommand('setblock 64 69 152 air');
 if(t===1300)bb(mc.screen,'完成改装').onPress();
 if(t===1800){bc(!mc.screen.snapshot().has(32),'invalid completion rolled back');bc(mc.screen.snapshot().has(1),'missing controller restored');console.info('DBV_BLOCK RESULT '+mc.screen.snapshot());console.info('DBV_BLOCK CLIENT COMPLETE');}
 if(t===280||t===1180||t===1800)Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_BLOCK SCREENSHOT '+c.getString()));
 if(t===1900)mc.stop();
});
