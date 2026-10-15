// Temporary acceptance harness: copy into runtime/client/kubejs/client_scripts only while validating.
var oilOpened=false,oilTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
 var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
 if(!oilOpened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){
  oilOpened=true;mc.createWorldOpenFlows().openWorld('DBV-Oilfield-Acceptance',()=>console.error('DBV_OIL FAIL open'));
 }
 if(mc.level!=null&&++oilTicks>500)mc.stop();
});
