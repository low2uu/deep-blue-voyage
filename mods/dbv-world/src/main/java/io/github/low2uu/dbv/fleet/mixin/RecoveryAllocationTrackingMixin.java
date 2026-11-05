package io.github.low2uu.dbv.fleet.mixin;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Record only newly allocated carriers. Native loading also fires onSubLevelAdded, but it calls
 * allocateSubLevel(savedUUID, ...) directly and must never make an existing nearby ship a rollback target.
 */
@Mixin(value = SubLevelContainer.class, remap = false)
public abstract class RecoveryAllocationTrackingMixin {
    @Inject(method = "allocateNewSubLevel", at = @At("RETURN"), remap = false)
    private void dbv$recordNewRecoveryCarrier(Pose3d pose, CallbackInfoReturnable<SubLevel> cir) {
        if (!(cir.getReturnValue() instanceof ServerSubLevel body)) return;
        var module = FleetModule.existing(body.getLevel().getServer());
        if (module != null) module.recovery().recordAllocated(body);
    }
}
