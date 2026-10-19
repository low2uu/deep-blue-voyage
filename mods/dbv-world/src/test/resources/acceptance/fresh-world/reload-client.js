var opened=false,ticks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
if(!opened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){opened=true;mc.createWorldOpenFlows().openWorld('DBV-Fresh-Seed',()=>console.error('新地图复进失败'));}
if(mc.screen!=null&&String(mc.screen).includes('BackupConfirmScreen')) {
var Button=Java.loadClass('net.minecraft.client.gui.components.Button'),Component=Java.loadClass('net.minecraft.network.chat.Component');
var children=mc.screen.children();for(var i=0;i<children.size();i++){var button=children.get(i);if(button instanceof Button&&String(button.getMessage().getString())==String(Component.translatable('selectWorld.backupJoinSkipButton').getString())){button.onPress();break;}}
}
if(mc.level==null)return;++ticks;if(ticks%40==0){var done=JsonIO.read('kubejs/fresh-done.json');if(done!=null&&done.done)mc.stop();}if(ticks>20000)mc.stop();});
