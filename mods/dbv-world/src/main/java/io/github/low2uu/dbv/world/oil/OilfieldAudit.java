package io.github.low2uu.dbv.world.oil;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Opt-in acceptance probe, guarded to the disposable test save; never a gameplay oil producer. */
public final class OilfieldAudit {
    private OilfieldAudit() {}
    public static int extract(ServerLevel level, int x, int turns) {
        if (!level.getServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString()
                .equals("DBV-Oilfield-Acceptance"))
            throw new IllegalStateException("Extraction probe only runs in DBV-Oilfield-Acceptance");
        if (turns < 1 || turns > 20) throw new IllegalArgumentException("turns must be 1..20");
        var pos = new BlockPos(x, 80, -56);
        var hole = (PumpjackHoleBlockEntity) level.getBlockEntity(pos);
        if (hole == null) throw new IllegalStateException("Missing test well");
        var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, (Direction)null);
        if (handler == null) throw new IllegalStateException("Missing native fluid capability");
        hole.headPos = 4;
        hole.bearingPos = 4;
        // Discard pre-existing buffered oil so the probe measures new production, not inventory.
        handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        int total = 0;
        for (int i = 0; i < turns; i++) {
            hole.pumpjackRotation(false);
            var extracted = handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            total += extracted.getAmount();
        }
        return total;
    }
}
