package io.github.low2uu.dbv.fleet.compat;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.OxygeneDiffuserBlockEntity;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.domain.Qualification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Read-only inspection of the owning plot, never nearby world blocks. */
public final class VesselInspection {
    private static final Map<String, String> TYPES = Map.ofEntries(
            Map.entry("command_sub", "中央控制器"), Map.entry("electrolyzer", "氧气制造"),
            Map.entry("oxygene_diffuser", "供氧"), Map.entry("creative_oxygenator", "创造供氧（不计入制氧资格）"),
            Map.entry("submarine_propeller", "推进"), Map.entry("water_thruster", "推进"),
            Map.entry("rudder", "转向"), Map.entry("underwater_mine", "武器"),
            Map.entry("sonar", "声呐"), Map.entry("ballast_tank", "压载"),
            Map.entry("ballast_vent", "压载"), Map.entry("pump_controller", "泵控制"),
            Map.entry("decompression_chamber", "气闸"), Map.entry("industrial_alarm", "报警"));

    public record Device(String id, String type, BlockPos position, BlockPos relativePosition, String status) {}
    public record Report(Qualification qualification, List<Device> devices, long tick) {
        public Report { devices = List.copyOf(devices); }
        public void requireEligible() {
            if (!qualification.eligible()) throw new IllegalStateException(String.join("；", qualification.reasons()));
        }
    }

    public Report inspect(ServerSubLevel body) {
        var level = body.getLevel();
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Inspection requires the server thread");
        var id = body.getUniqueId();
        long now = level.getGameTime();
        long updated = CompartmentTracker.lastUpdateTick(id);
        boolean pending = CompartmentTracker.getSub(id) == null || updated == 0 || now < updated || now - updated > 220
                || CompartmentTracker.isScanActive(id) || CompartmentTracker.isStructureDirty(id);
        boolean sealed = !pending && CompartmentTracker.getCompartments(id).stream().anyMatch(c ->
                c.sealed() && !c.internal().isEmpty() && !CompartmentTracker.isCompromised(id, c.anchor())
                        && !CompartmentTracker.isBreached(id, c));
        boolean controller = false, generator = false, diffuser = false;
        var devices = new ArrayList<Device>();
        var bounds = body.getPlot().getBoundingBox();
        var origin = new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());
        for (int cx = bounds.minX() >> 4; cx <= (bounds.maxX() >> 4); cx++) {
            for (int cz = bounds.minZ() >> 4; cz <= (bounds.maxZ() >> 4); cz++) {
                if (body.getPlot().getChunk(body.getPlot().toLocal(new net.minecraft.world.level.ChunkPos(cx, cz))) == null) pending = true;
            }
        }
        // Iterate occupied chunk sections. No chunk loads, no full ocean/bounding-box sweep.
        for (var holder : body.getPlot().getLoadedChunks()) {
            var chunk = holder.getChunk();
            if (chunk == null) { pending = true; continue; }
            var sections = chunk.getSections();
            for (int s = 0; s < sections.length; s++) {
                var section = sections[s];
                if (section.hasOnlyAir()) continue;
                for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                    var state = section.getBlockState(x, y, z);
                    if (state.isAir()) continue;
                    var key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    String type = key.getNamespace().equals("create_submarine") ? TYPES.get(key.getPath()) : null;
                    // Unknown block entities remain visible without pretending to know their function.
                    if (type == null && !state.hasBlockEntity()) continue;
                    var pos = new BlockPos(chunk.getPos().getMinBlockX() + x, chunk.getMinBuildHeight() + s * 16 + y,
                            chunk.getPos().getMinBlockZ() + z);
                    var owner = SableCompanion.INSTANCE.getContaining(level, pos);
                    if (owner == null || !id.equals(owner.getUniqueId())) continue;
                    BlockEntity be = chunk.getBlockEntity(pos);
                    String status = "已识别；运行状态未适配";
                    if (be instanceof CommandSubBlockEntity && !be.isRemoved()) { controller = true; status = "有效"; }
                    if (be instanceof ElectrolyzerBlockEntity electrolyzer) {
                        boolean ready = !be.isRemoved() && electrolyzer.data.get(6) == 1
                                && electrolyzer.waterTank.getFluidAmount() >= 10
                                && electrolyzer.energyStorage.getEnergyStored() >= 250;
                        generator |= ready;
                        status = ready ? "可制氧/满罐待机" : "未就绪：检查开启、水和能量";
                    }
                    if (be instanceof OxygeneDiffuserBlockEntity oxygen) {
                        var room = CompartmentTracker.findCompartmentAdjacent(id, pos);
                        boolean serving = !pending && room != null && !CompartmentTracker.isBreached(id, room)
                                && room.internal().stream().anyMatch(p -> level.getFluidState(p).isEmpty());
                        boolean active = !be.isRemoved() && level.hasNeighborSignal(pos) && oxygen.oxygenTank.getFluidAmount() > 0
                                && oxygen.saveWithoutMetadata(level.registryAccess()).getInt("ActiveTicks") >= 100;
                        diffuser |= active && serving;
                        status = "氧气=" + oxygen.oxygenTank.getFluidAmount() + "毫桶；" + (active?"已启动":"未启动") + "；" + (serving?"正在向密闭舱供氧":"未向密闭舱供氧");
                    }
                    devices.add(new Device(key.toString(), type == null ? "未适配设备" : type, pos, pos.subtract(origin), status));
                }
            }
        }
        devices.sort(java.util.Comparator.comparing(Device::id).thenComparing(d -> d.position().asLong()));
        return new Report(new Qualification(controller, generator && diffuser, sealed, pending), devices, now);
    }
}
