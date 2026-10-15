package io.github.low2uu.dbv.fleet.compat;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.tags.FluidTags;
import io.github.low2uu.dbv.fleet.telemetry.DepthReference;

/** Transform the controller's plot-space block centre, including ship rotation. */
public final class ControllerDepth {
    private ControllerDepth() {}

    public static Vector3dc worldPosition(Pose3dc pose, BlockPos controller) {
        return pose.transformPosition(new Vector3d(controller.getX() + 0.5,
                controller.getY() + 0.5, controller.getZ() + 0.5));
    }

    public static int surface(Level level, Vector3dc reference) {
        int x = (int)Math.floor(reference.x()), z = (int)Math.floor(reference.z());
        var chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
        if (chunk == null) return Integer.MIN_VALUE;
        var pos = new BlockPos.MutableBlockPos();
        return DepthReference.surface((int)Math.floor(reference.y()), level.getMinBuildHeight(),
                level.getMaxBuildHeight(), y -> {
                    pos.set(x, y, z);
                    if (CompartmentTracker.realFluidState(chunk, pos).is(FluidTags.WATER)) return 1;
                    return CompartmentTracker.realBlockState(chunk, pos).isAir() ? 0 : 2;
                });
    }
}
