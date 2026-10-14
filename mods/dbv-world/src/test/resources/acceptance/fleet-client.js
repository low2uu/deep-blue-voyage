// Opt-in test resource: copy temporarily into runtime/client/kubejs/client_scripts.
// Opens ONLY the named acceptance copy, then closes the client. Never distribute in the pack.
const FleetMinecraft = Java.loadClass('net.minecraft.client.Minecraft');
const FleetTitle = Java.loadClass('net.minecraft.client.gui.screens.TitleScreen');
let fleetStarted = false, fleetMenuTicks = 0, fleetWorldTicks = 0;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post', event => {
    const mc = FleetMinecraft.getInstance();
    if (!fleetStarted && ++fleetMenuTicks > 100 &&
            (mc.screen instanceof FleetTitle || String(mc.screen).includes('DeepSeasWelcomeScreen'))) {
        fleetStarted = true;
        if (!mc.getLevelSource().levelExists('DBV-Fleet-M1-Acceptance')) {
            console.error('DBV_FLEET_TEST missing acceptance save'); mc.stop(); return;
        }
        mc.createWorldOpenFlows().openWorld('DBV-Fleet-M1-Acceptance', () => console.error('DBV_FLEET_TEST open cancelled'));
    }
    if (mc.level != null && mc.player != null && ++fleetWorldTicks === 500) mc.stop();
});
