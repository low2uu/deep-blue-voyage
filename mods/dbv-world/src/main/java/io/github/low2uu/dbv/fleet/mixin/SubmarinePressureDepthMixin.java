package io.github.low2uu.dbv.fleet.mixin;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import io.github.low2uu.dbv.fleet.compat.ControllerDepth;
import java.util.UUID;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.Level;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import io.github.low2uu.dbv.fleet.telemetry.DepthReference;

@Mixin(value = SubmarinePressureSystem.class, remap = false)
public abstract class SubmarinePressureDepthMixin {
    @Redirect(method = "processSubmarine", at = @At(value = "INVOKE",
            target = "Ldev/ryanhcode/sable/companion/math/Pose3dc;position()Lorg/joml/Vector3dc;"), remap = false)
    private static Vector3dc dbv$controllerPosition(Pose3dc pose, UUID id, SubLevelAccess sub) {
        var controller = CommandSubBlockEntity.onSub(id);
        // Ships without a registered controller retain upstream behaviour.
        return controller == null ? pose.position() : ControllerDepth.worldPosition(pose, controller.getBlockPos());
    }

    @Redirect(method = "processSubmarine", at = @At(value = "INVOKE", target = "Lcom/maxenonyme/createsubmarine/submarine/system/SubmarinePressureSystem;measureSurfaceY(Lnet/minecraft/world/level/Level;Lorg/joml/Vector3dc;)I"), remap = false)
    private static int dbv$surface(Level level, Vector3dc reference, UUID id, SubLevelAccess sub) {
        return CommandSubBlockEntity.onSub(id) == null ? SubmarinePressureSystem.measureSurfaceY(level, reference)
                : ControllerDepth.surface(level, reference);
    }

    @Inject(method = "getCachedDepth", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dbv$readDepth(UUID id, CallbackInfoReturnable<Integer> ci) {
        var controller = CommandSubBlockEntity.onSub(id);
        var sub = CompartmentTracker.getSub(id);
        if (controller != null && sub != null) ci.setReturnValue(DepthReference.displayed(controller.surfaceY,
                ControllerDepth.worldPosition(sub.logicalPose(), controller.getBlockPos()).y()));
    }
}
