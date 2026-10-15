var opened=false,ticks=0,guideShot=false,actionSeq=-1;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();if(!opened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){opened=true;mc.createWorldOpenFlows().openWorld('DBV-Native-Upgrade',()=>console.error('DBV_NATIVE FAIL open'));}
if(mc.level==null)return;++ticks;
if(ticks%20==0&&mc.screen!=null){try{var ui=JsonIO.read('kubejs/ui-action.json'),action=ui==null?null:ui.action;
if(action!=null&&ui.seq!=actionSeq&&String(mc.screen).includes('FleetScreen')){var children=mc.screen.children().toArray();for(var i=0;i<children.length;i++){var b=children[i];if(String(b.getMessage().getString())==action){console.info('DBV_NATIVE UI '+action+' enabled='+b.active);if(b.active){Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),message=>console.info('DBV_NATIVE SCREENSHOT '+message));b.onPress();actionSeq=ui.seq;}break;}}}
if(!guideShot&&String(mc.screen).includes('StarterGuideScreen')){guideShot=true;Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),message=>console.info('DBV_NATIVE GUIDE '+message));}
}catch(x){console.error('DBV_NATIVE UI_FAIL '+x);}}
if(ticks>2050)mc.stop();});
