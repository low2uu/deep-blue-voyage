package io.github.low2uu.dbv.fleet.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import io.github.low2uu.dbv.fleet.compat.ControllerDepth;
import org.joml.Vector3dc;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.Font;

/** Deep Seas 3.3.0 has one unlocalized navigation prefix; do not replace its renderer or controls. */
@Mixin(targets="com.maxenonyme.createsubmarine.submarine.block.entity.renderer.CommandSubRenderer",remap=false)
public abstract class CommandSubChineseMixin {
    @Shadow private static void text(PoseStack ms, MultiBufferSource buffer, Font font, String s,
            float x, float y, float scale, int color, int light, boolean centered) { throw new AssertionError(); }

    @Redirect(method = "render(Lcom/maxenonyme/createsubmarine/submarine/block/entity/CommandSubBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/companion/math/Pose3dc;position()Lorg/joml/Vector3dc;"), remap = false)
    private Vector3dc dbv$renderControllerPosition(Pose3dc pose, CommandSubBlockEntity be, float partialTicks,
            PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        return ControllerDepth.worldPosition(pose, be.getBlockPos());
    }

    @Inject(method = "drawTape", at = @At("HEAD"), cancellable = true, remap = false)
    private void dbv$unknownDepth(CommandSubBlockEntity be, float depth, PoseStack ms,
            MultiBufferSource buffer, Font font, int light, CallbackInfo ci) {
        if (be.surfaceY != Integer.MIN_VALUE) return;
        text(ms, buffer, font, "深度未知", 85, 48, 0.65f, 0xFF8B3026, light, true);
        text(ms, buffer, font, "自动升降暂停", 85, 60, 0.5f, 0xFF8B3026, light, true);
        ci.cancel();
    }

    @ModifyVariable(method="text(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFFIIZZ)V",
            at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false)
    private static String dbv$translateAutomaticDepth(String value){
        return value.startsWith("AUTO ")?"自动 "+value.substring(5):value;
    }
}
