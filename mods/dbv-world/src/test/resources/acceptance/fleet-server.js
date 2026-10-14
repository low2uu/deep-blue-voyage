// Opt-in acceptance resource. Requires the client script and a COPY of the island save.
var FleetAPI = Java.loadClass('io.github.low2uu.dbv.fleet.FleetModule');
var FleetIdentity = Java.loadClass('io.github.low2uu.dbv.fleet.domain.VesselIdentity');
var FleetAssembly = Java.loadClass('dev.ryanhcode.sable.api.SubLevelAssemblyHelper');
var FleetBox = Java.loadClass('dev.ryanhcode.sable.companion.math.BoundingBox3i');
var FleetPos = Java.loadClass('net.minecraft.core.BlockPos');
var FleetBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks');
var FleetList = Java.loadClass('java.util.ArrayList');
var FleetUUID = Java.loadClass('java.util.UUID');
function fleetAssert(ok, description) { if (!ok) throw new Error('DBV_FLEET_TEST FAIL ' + description); console.info('DBV_FLEET_TEST PASS ' + description); }
PlayerEvents.loggedIn(event => {
    var server = event.server, level = server.overworld(), player = event.player;
    if (String(server.getWorldPath(Java.loadClass('net.minecraft.world.level.storage.LevelResource').ROOT)).indexOf('DBV-Fleet-M1-Acceptance') < 0) return;
    player.connection.teleport(64, 75, 160, 0, 0);
    player.getAbilities().flying = true; player.onUpdateAbilities();
    server.scheduleInTicks(120, () => {
        var module = FleetAPI.get(server);
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack().withPermission(4);
        if (module.registry().flagship().isEmpty()) {
            var positions = new FleetList();
            for (var x = 64; x < 67; x++) for (var y = 70; y < 73; y++) for (var z = 160; z < 163; z++) {
                var pos = new FleetPos(x, y, z); level.setBlock(pos, FleetBlocks.IRON_BLOCK.defaultBlockState(), 3); positions.add(pos);
            }
            var body = FleetAssembly.assembleBlocks(level, new FleetPos(64,70,160), positions, new FleetBox(64,70,160,66,72,162));
            fleetAssert(body != null, 'real Sable assembly');
            fleetAssert(dispatcher.execute('dbv flagship register ' + body.getUniqueId() + ' 酒狐验收艇', source) === 1, 'registration command');
            fleetAssert(dispatcher.execute('dbv flagship register ' + body.getUniqueId() + ' 第二艘', source) === 0, 'duplicate registration denied');
        } else {
            fleetAssert(String(module.registry().flagship().get().name()) === '酒狐验收艇', 'identity persisted across client restart');
        }
        server.scheduleInTicks(80, () => {
            var identity = module.registry().flagship().get();
            var telemetry = module.sample(server);
            fleetAssert(String(telemetry.state()) === 'ACTIVE', 'loaded identity and telemetry');
            fleetAssert(Number.isFinite(Number(telemetry.mass())) && telemetry.mass() > 0, 'physical mass');
            fleetAssert(telemetry.depth() == null, 'uninitialized pressure stays unknown');
            var absent = new FleetIdentity(identity.vesselId(), FleetUUID.randomUUID(), identity.dimension(), identity.name(), identity.generation());
            // Use a distinct campaign ID so the real ship is not interpreted as a copy of the synthetic missing body.
            var missing = new FleetIdentity(FleetUUID.randomUUID(), absent.bodyId(), absent.dimension(), absent.name(), absent.generation());
            fleetAssert(String(module.adapter().sample(server, missing).state()) === 'NOT_LOADED', 'missing body is not destroyed');
            var copied = module.adapter().find(level, identity.bodyId());
            var data = copied.getUserDataTag().copy(); data.getCompound('dbv_world:flagship').putLong('generation', 999);
            copied.setUserDataTag(data);
            fleetAssert(String(module.sample(server).state()) === 'IDENTITY_CONFLICT', 'wrong generation rejected');
            module.adapter().mark(copied, identity);
            var denied = false;
            try { dispatcher.execute('dbv flagship status', source.withPermission(0)); } catch (error) { denied = true; }
            fleetAssert(denied, 'operator permission required');
            fleetAssert(dispatcher.execute('dbv flagship status', source) === 1, 'status command');
            fleetAssert(server.saveEverything(false,true,true), 'world save');
            console.info('DBV_FLEET_TEST COMPLETE');
        });
    });
});
