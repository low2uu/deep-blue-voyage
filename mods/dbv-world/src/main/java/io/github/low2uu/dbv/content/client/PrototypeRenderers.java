package io.github.low2uu.dbv.content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.low2uu.dbv.content.PrototypeContent;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Guardian;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Temporary vanilla art. Dedicated servers never load this class. */
@EventBusSubscriber(modid="dbv_world", bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class PrototypeRenderers {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        for (var e : PrototypeContent.SQUIDS) event.registerEntityRenderer(e.type().get(), ctx -> new SquidRenderer<Squid>(ctx, new SquidModel<>(ctx.bakeLayer(ModelLayers.SQUID))) {
            @Override protected void scale(Squid entity, PoseStack pose, float partial) { float s=e.spec().scale(); pose.scale(s,s,s); }
        });
        for (var e : PrototypeContent.GUARDIANS) event.registerEntityRenderer(e.type().get(), ctx -> new GuardianRenderer(ctx) {
            @Override protected void scale(Guardian entity, PoseStack pose, float partial) { float s=e.spec().scale(); pose.scale(s,s,s); }
        });
        for (var e : PrototypeContent.DROWNED) event.registerEntityRenderer(e.type().get(), DrownedRenderer::new);
    }
    private PrototypeRenderers() {}
}
