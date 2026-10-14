package io.github.low2uu.dbv.fleet.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Deep Seas 3.3.0 has one unlocalized navigation prefix; do not replace its renderer or controls. */
@Mixin(targets="com.maxenonyme.createsubmarine.submarine.block.entity.renderer.CommandSubRenderer",remap=false)
public abstract class CommandSubChineseMixin {
    @ModifyVariable(method="text(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFFIIZZ)V",
            at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false)
    private static String dbv$translateAutomaticDepth(String value){
        return value.startsWith("AUTO ")?"自动 "+value.substring(5):value;
    }
}
