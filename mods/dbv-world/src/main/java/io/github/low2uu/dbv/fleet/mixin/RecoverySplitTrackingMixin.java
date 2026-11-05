package io.github.low2uu.dbv.fleet.mixin;

import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep a transaction receipt for native fragments, without changing native splitting behavior.
 * onSubLevelAdded precedes setSplitFrom; by the normal post-tick hook networking clears the source.
 */
@Mixin(value = ServerSubLevel.class, remap = false)
public abstract class RecoverySplitTrackingMixin {
    @Inject(method = "setSplitFrom", at = @At("TAIL"), remap = false)
    private void dbv$recordRecoveryFragment(ServerSubLevel source, Pose3d sourcePose, CallbackInfo ci) {
        var child = (ServerSubLevel) (Object) this;
        var module = FleetModule.existing(source.getLevel().getServer());
        // The service records the child before writing and retains the transaction on any write failure.
        if (module != null) module.recovery().recordSplit(source, child);
    }
}
