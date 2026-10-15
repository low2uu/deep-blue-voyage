package io.github.low2uu.dbv.fleet.mixin;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import io.github.low2uu.dbv.fleet.compat.ControllerDepth;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.level.Level;
import io.github.low2uu.dbv.fleet.telemetry.DepthReference;

/** Keep display, depth holding and seabed-following targets on the same reference point. */
@Mixin(value = CommandSubBlockEntity.class, remap = false)
public abstract class CommandSubDepthMixin {
    @Shadow private int command;

    @Redirect(method = {"measure", "followFloor"}, at = @At(value = "INVOKE",
            target = "Ldev/ryanhcode/sable/companion/math/Pose3dc;position()Lorg/joml/Vector3dc;"), remap = false)
    private Vector3dc dbv$controllerPosition(Pose3dc pose) {
        return ControllerDepth.worldPosition(pose, ((CommandSubBlockEntity)(Object)this).getBlockPos());
    }

    @Redirect(method = "measure", at = @At(value = "INVOKE", target = "Lcom/maxenonyme/createsubmarine/submarine/system/SubmarinePressureSystem;measureSurfaceY(Lnet/minecraft/world/level/Level;Lorg/joml/Vector3dc;)I"), remap = false)
    private int dbv$surface(Level level, Vector3dc reference) { return ControllerDepth.surface(level, reference); }

    @Inject(method = "depth", at = @At("HEAD"), cancellable = true, remap = false)
    private void dbv$depth(SubLevelAccess sub, CallbackInfoReturnable<Double> ci) {
        var self = (CommandSubBlockEntity)(Object)this;
        ci.setReturnValue(DepthReference.depth(self.surfaceY, ControllerDepth.worldPosition(sub.logicalPose(), self.getBlockPos()).y()));
    }

    @Inject(method = "measure", at = @At("RETURN"), remap = false)
    private void dbv$display(SubLevelAccess sub, CallbackInfo ci) {
        var self = (CommandSubBlockEntity)(Object)this;
        int value = sub == null ? -1 : DepthReference.displayed(self.surfaceY, ControllerDepth.worldPosition(sub.logicalPose(), self.getBlockPos()).y());
        if (self.syncedDepth != value) {
            self.syncedDepth = value;
            self.getLevel().sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
        }
    }

    @Inject(method = "steer", at = @At("HEAD"), cancellable = true, remap = false)
    private void dbv$holdWithoutSurface(SubLevelAccess sub, CallbackInfo ci) {
        var self = (CommandSubBlockEntity)(Object)this;
        if (sub != null && self.surfaceY != Integer.MIN_VALUE) return;
        command = com.maxenonyme.createsubmarine.submarine.block.entity.PumpControllerBlockEntity.HOLD;
        self.syncedStatus = command + 1;
        self.getLevel().sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
        ci.cancel();
    }

    @Inject(method = "trim", at = @At("HEAD"), cancellable = true, remap = false)
    private void dbv$noBlindImpulse(SubLevelAccess sub, CallbackInfo ci) {
        if (((CommandSubBlockEntity)(Object)this).surfaceY == Integer.MIN_VALUE) ci.cancel();
    }
}
