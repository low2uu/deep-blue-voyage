package io.github.low2uu.dbv.fleet.compat;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.domain.VesselIdentity;
import io.github.low2uu.dbv.fleet.telemetry.VesselTelemetry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/** All third-party types stay inside this adapter and the command boundary. */
public final class SableVesselAdapter {
    public static final String TAG = "dbv_world:flagship";

    public List<ServerSubLevel> loaded(ServerLevel level) {
        var container = SubLevelContainer.getContainer(level);
        return container == null ? List.of() : List.copyOf(container.getAllSubLevels());
    }

    public ServerSubLevel find(ServerLevel level, UUID id) {
        return loaded(level).stream().filter(body -> body.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    public boolean marked(ServerSubLevel body) {
        return body.getUserDataTag() != null && body.getUserDataTag().contains(TAG);
    }

    public void mark(ServerSubLevel body, VesselIdentity identity) {
        CompoundTag root = body.getUserDataTag() == null ? new CompoundTag() : body.getUserDataTag().copy();
        CompoundTag tag = new CompoundTag();
        tag.putUUID("vesselId", identity.vesselId());
        tag.putLong("generation", identity.generation());
        root.put(TAG, tag);
        body.setUserDataTag(root);
    }

    public VesselTelemetry sample(MinecraftServer server, VesselIdentity identity) {
        long tick = server.overworld().getGameTime();
        List<ServerSubLevel> matches = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerSubLevel body : loaded(level)) {
                if (body.getUniqueId().equals(identity.bodyId())) { matches.add(body); }
                else if (marked(body)) {
                    CompoundTag tag = body.getUserDataTag().getCompound(TAG);
                    if (tag.hasUUID("vesselId") && tag.getUUID("vesselId").equals(identity.vesselId())) {
                        return VesselTelemetry.unavailable(VesselTelemetry.State.IDENTITY_CONFLICT, tick, "检测到主艇身份副本，请人工核对");
                    }
                }
            }
        }
        if (matches.isEmpty()) {
            return VesselTelemetry.unavailable(VesselTelemetry.State.NOT_LOADED, tick, "未找到已加载载体；不等于损毁，不允许重新登记");
        }
        ServerSubLevel body = matches.getFirst();
        if (matches.size() != 1 || !body.getLevel().dimension().location().toString().equals(identity.dimension())) {
            return VesselTelemetry.unavailable(VesselTelemetry.State.IDENTITY_CONFLICT, tick, "载体或维度与登记不符");
        }
        if (marked(body)) {
            CompoundTag tag = body.getUserDataTag().getCompound(TAG);
            if (!tag.hasUUID("vesselId") || !tag.getUUID("vesselId").equals(identity.vesselId())
                    || tag.getLong("generation") != identity.generation()) {
                return VesselTelemetry.unavailable(VesselTelemetry.State.IDENTITY_CONFLICT, tick, "载体标签与登记不符");
            }
        } else {
            // Repair the tag only on the exact authoritative body after an interrupted registration/save.
            mark(body, identity);
        }
        boolean hasPressureData = CompartmentTracker.getSub(identity.bodyId()) != null;
        return new VesselTelemetry(VesselTelemetry.State.ACTIVE, tick, "Sable已加载；压力读数取自Deep Seas缓存",
                body.getMassTracker().getMass(), body.latestLinearVelocity.length(),
                hasPressureData ? SubmarinePressureSystem.getCachedDepth(identity.bodyId()) : null,
                hasPressureData ? SubmarinePressureSystem.getCrackCount(identity.bodyId()) : null,
                hasPressureData ? SubmarinePressureSystem.isBreached(identity.bodyId()) : null);
    }
}
