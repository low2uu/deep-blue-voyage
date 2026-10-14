// Opt-in automation in the formal client; do not distribute with the pack.
let refitOpened=false,refitMenuTicks=0,refitWorldTicks=0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',event=>{
    // Resolve client classes after mod registration, not in parallel startup-script loading.
    const mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
    if(!refitOpened && ++refitMenuTicks>100 && mc.screen!=null &&
       (String(mc.screen).includes('TitleScreen') || String(mc.screen).includes('DeepSeasWelcomeScreen'))){
        refitOpened=true;
        if(!mc.getLevelSource().levelExists('DBV-Refit-Acceptance')){console.error('DBV_REFIT missing acceptance save');mc.stop();return;}
        mc.createWorldOpenFlows().openWorld('DBV-Refit-Acceptance',()=>console.error('DBV_REFIT opening cancelled'));
    }
    if(mc.level!=null && mc.player!=null && ++refitWorldTicks===1300)mc.stop();
});
