let pOpened=false,pMenuTicks=0,pWorldTicks=0;
function pc(ok,name){if(!ok){console.error('DBV_PANEL FAIL '+name);return;}console.info('DBV_PANEL PASS '+name);}
function button(screen,label){for(var i=0;i<screen.children().size();i++){var b=screen.children().get(i);if(typeof b.getMessage==='function'&&String(b.getMessage().getString())===label)return b;}throw new Error('Missing button '+label);}
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 const mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!pOpened&&++pMenuTicks>100&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){
  pOpened=true;if(!mc.getLevelSource().levelExists('DBV-Panel-Acceptance')){mc.stop();return;}
  mc.createWorldOpenFlows().openWorld('DBV-Panel-Acceptance',()=>console.error('DBV_PANEL open cancelled'));
 }
 if(mc.level==null||mc.player==null)return;
 var t=++pWorldTicks;
 if(t===180)Java.loadClass('org.lwjgl.glfw.GLFW').glfwSetCursorPos(mc.getWindow().getWindow(),12,12);
 if(t===220){
  pc(String(mc.screen).includes('FleetScreen'),'shift right click opens real screen');pc(!mc.screen.isPauseScreen(),'panel does not pause server');
  var C=Java.loadClass('net.minecraft.network.chat.Component');pc(String(C.translatable('create_submarine.command_sub.tab.navigation').getString())==='导航','controller navigation translated');
  pc(String(C.translatable('create_submarine.command_sub.diag.run').getString())==='开始诊断','controller diagnostic translated');
  pc(String(C.translatable('create_submarine.command_sub.speed.cruise').getString())==='巡航','controller speed translated');
  console.info('DBV_PANEL VIEW '+mc.screen.snapshot());pc(button(mc.screen,'开始改装').active,'begin enabled for creative owner');button(mc.screen,'开始改装').onPress();
 }
 if(t===200||t===270){Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_PANEL SCREENSHOT '+c.getString()));}
 if(t===300)pc(mc.screen.snapshot().has(32),'GUI begin created server snapshot');
 if(t===400)pc(!mc.screen.snapshot().has(32),'GUI commit completed on server');
 if(t===500)pc(mc.screen.snapshot().has(32),'cancel first click keeps transaction');
 if(t===580){pc(!mc.screen.snapshot().has(32),'confirmed cancel completed on server');mc.player.connection.sendCommand('gamemode survival');}
 if(t===660)pc(!mc.screen.snapshot().has(32),'forged survival begin rejected by server');
 if(t===680){mc.player.connection.sendCommand('gamemode creative');mc.player.connection.sendCommand('tp @s -84 72 -28');}
 if(t===330){pc(button(mc.screen,'提交改装').active,'commit enabled after backup');button(mc.screen,'提交改装').onPress();}
 if(t===420){button(mc.screen,'艇内设备').onPress();}
 if(t===430){Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,mc.getMainRenderTarget(),c=>console.info('DBV_PANEL SCREENSHOT '+c.getString()));}
 if(t===450){button(mc.screen,'船况与改装').onPress();button(mc.screen,'开始改装').onPress();}
 if(t===475){button(mc.screen,'取消还原').onPress();pc(button(mc.screen,'确认还原').active,'rollback confirmation shown');}
 if(t===510){button(mc.screen,'确认还原').onPress();}
 if(t===610){
  pc(!button(mc.screen,'开始改装').active,'survival UI is read only');
  var Action=Java.loadClass('io.github.low2uu.dbv.fleet.ui.PanelAction');
  Java.loadClass('net.neoforged.neoforge.network.PacketDistributor').sendToServer(new Action(mc.screen.session(),1));
 }
 if(t===720){pc(!button(mc.screen,'开始改装').active,'out of range actions disabled');}
 if(t===740){mc.screen.onClose();pc(mc.screen==null,'escape close restores gameplay');console.info('DBV_PANEL CLIENT COMPLETE');}
 if(t===850)mc.stop();
});
