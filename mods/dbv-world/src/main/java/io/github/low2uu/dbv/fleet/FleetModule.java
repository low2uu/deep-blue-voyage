package io.github.low2uu.dbv.fleet;

import com.mojang.logging.LogUtils;
import io.github.low2uu.dbv.fleet.command.FleetCommands;
import io.github.low2uu.dbv.fleet.compat.SableVesselAdapter;
import io.github.low2uu.dbv.fleet.domain.FlagshipRegistry;
import io.github.low2uu.dbv.fleet.persistence.RegistryStore;
import io.github.low2uu.dbv.fleet.telemetry.VesselTelemetry;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** One runtime per server; all callers run on the logical server thread. */
public final class FleetModule {
    private static final Map<MinecraftServer, FleetModule> SERVERS = new WeakHashMap<>();
    private final RegistryStore store;
    private final SableVesselAdapter adapter = new SableVesselAdapter();
    private final io.github.low2uu.dbv.fleet.compat.VesselInspection inspection = new io.github.low2uu.dbv.fleet.compat.VesselInspection();
    private FlagshipRegistry registry;
    private VesselTelemetry telemetry;

    private FleetModule(MinecraftServer server) throws IOException {
        store = new RegistryStore(server.getWorldPath(LevelResource.ROOT));
        registry = store.load();
    }

    public static void initialize() {
        NeoForge.EVENT_BUS.addListener(FleetCommands::register);
        NeoForge.EVENT_BUS.addListener(io.github.low2uu.dbv.fleet.command.ControllerInteraction::interact);
        NeoForge.EVENT_BUS.addListener(FleetModule::start);
        NeoForge.EVENT_BUS.addListener(FleetModule::tick);
        NeoForge.EVENT_BUS.addListener(FleetModule::stop);
    }

    public static FleetModule get(MinecraftServer server) throws IOException {
        FleetModule module = SERVERS.get(server);
        if (module == null) { module = new FleetModule(server); SERVERS.put(server, module); }
        return module;
    }

    public FlagshipRegistry registry() { return registry; }
    public SableVesselAdapter adapter() { return adapter; }
    public io.github.low2uu.dbv.fleet.compat.VesselInspection inspection() { return inspection; }

    public void bind(MinecraftServer server, dev.ryanhcode.sable.sublevel.ServerSubLevel body, String name) throws IOException {
        if (!server.isSameThread()) { throw new IllegalStateException("Registration requires the server thread"); }
        registry = store.load();
        inspection.inspect(body).requireEligible();
        for (var level : server.getAllLevels()) {
            if (adapter.loaded(level).stream().anyMatch(adapter::marked) && registry.flagship().isEmpty()) {
                throw new IllegalStateException("发现既有主艇标签但登记缺失，请先核对档案");
            }
        }
        FlagshipRegistry candidate = registry.register(body.getUniqueId(), body.getLevel().dimension().location().toString(), name);
        store.create(candidate);
        registry = candidate;
        adapter.mark(body, registry.flagship().orElseThrow());
    }

    public VesselTelemetry sample(MinecraftServer server) {
        try {
            telemetry = registry.flagship().map(identity -> adapter.sample(server, identity)).orElseGet(() ->
                    VesselTelemetry.unavailable(VesselTelemetry.State.UNREGISTERED, server.overworld().getGameTime(), "尚未登记主潜艇"));
        } catch (RuntimeException exception) {
            telemetry = VesselTelemetry.unavailable(VesselTelemetry.State.ADAPTER_ERROR, server.overworld().getGameTime(),
                    "船况读取失败，禁止据此认定损毁：" + exception.getClass().getSimpleName());
        }
        return telemetry;
    }

    private static void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) { return; }
        // A broken disk registry is reported by explicit commands; do not spam retries every tick.
        FleetModule module = SERVERS.get(event.getServer());
        if (module == null) { return; }
        VesselTelemetry.State before = module.telemetry == null ? null : module.telemetry.state();
        VesselTelemetry now = module.sample(event.getServer());
        if (before != now.state()) { LogUtils.getLogger().info("DBV flagship state: {} ({})", now.state(), now.detail()); }
    }

    private static void stop(ServerStoppedEvent event) { SERVERS.remove(event.getServer()); }

    private static void start(ServerStartedEvent event) {
        try { get(event.getServer()); }
        catch (IOException exception) {
            LogUtils.getLogger().error("DBV flagship registry unavailable; registration is blocked. Preserve dbv/fleet for recovery.", exception);
        }
    }
}
