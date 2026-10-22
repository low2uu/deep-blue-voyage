package io.github.low2uu.dbv.content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.low2uu.dbv.content.PrototypeContent;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Guardian;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** All model/renderer classes are reachable only on the client. */
@EventBusSubscriber(modid="dbv_world", bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class PrototypeRenderers {
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for(var e:PrototypeContent.SQUIDS) layer(event,e.spec());
        for(var e:PrototypeContent.GUARDIANS) layer(event,e.spec());
        for(var e:PrototypeContent.DROWNED) layer(event,e.spec());
    }
    private static void layer(EntityRenderersEvent.RegisterLayerDefinitions event,PrototypeContent.MobSpec spec) {
        event.registerLayerDefinition(CreatureGeometry.location(spec.id()),()->CreatureGeometry.load(spec.id()).layer());
    }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        for(var e:PrototypeContent.SQUIDS) event.registerEntityRenderer(e.type().get(),ctx->new CreatureRenderer<>(ctx,e.spec(),false));
        for(var e:PrototypeContent.DROWNED) event.registerEntityRenderer(e.type().get(),ctx->new CreatureRenderer<>(ctx,e.spec(),true));
        for(var e:PrototypeContent.GUARDIANS) event.registerEntityRenderer(e.type().get(),ctx->new GuardianCreatureRenderer(ctx,e.spec()));
    }
    private static boolean extendedVisible(Mob mob,Frustum frustum,double x,double y,double z,double radius) {
        return mob.shouldRender(x,y,z) && frustum.isVisible(mob.getBoundingBoxForCulling().inflate(radius));
    }
    private static final class CreatureRenderer<T extends Mob> extends MobRenderer<T,OceanCreatureModel<T>> {
        private final PrototypeContent.MobSpec spec;
        private final CreatureGeometry.Rig rig;
        CreatureRenderer(EntityRendererProvider.Context ctx,PrototypeContent.MobSpec spec,boolean hands) {
            super(ctx,new OceanCreatureModel<>(ctx.bakeLayer(CreatureGeometry.location(spec.id())),CreatureGeometry.load(spec.id())),spec.width()*.45F);
            this.spec=spec;this.rig=CreatureGeometry.load(spec.id());
            if(hands)addLayer(new ItemInHandLayer<>(this,ctx.getItemInHandRenderer()));
        }
        @Override public ResourceLocation getTextureLocation(T entity){return ResourceLocation.parse(rig.texture());}
        @Override protected void scale(T entity,PoseStack pose,float partial){float s=spec.scale();pose.scale(s,s,s);}
        @Override public boolean shouldRender(T entity,Frustum frustum,double x,double y,double z){
            return super.shouldRender(entity,frustum,x,y,z)||extendedVisible(entity,frustum,x,y,z,rig.cullingRadius()*spec.scale());
        }
        @Override protected void setupRotations(T entity,PoseStack pose,float age,float bodyYaw,float partial,float scale){
            // Squid AI supplies velocity, but not a fish-style facing. Presentation only.
            if(entity instanceof Squid && entity.getDeltaMovement().horizontalDistanceSqr()>.0001)
                bodyYaw=(float)(Mth.atan2(entity.getDeltaMovement().z,entity.getDeltaMovement().x)*Mth.RAD_TO_DEG)-90;
            super.setupRotations(entity,pose,age,bodyYaw,partial,scale);
        }
    }
    /** Retains vanilla beam telegraph/rendering; replacing the model must not hide an active attack. */
    private static final class GuardianCreatureRenderer extends GuardianRenderer {
        private final PrototypeContent.MobSpec spec;
        private final CreatureGeometry.Rig rig;
        GuardianCreatureRenderer(EntityRendererProvider.Context ctx,PrototypeContent.MobSpec spec){
            super(ctx);this.spec=spec;this.rig=CreatureGeometry.load(spec.id());
            this.model=new GuardianRigModel(ctx.bakeLayer(CreatureGeometry.location(spec.id())),rig);
            this.shadowRadius=spec.width()*.45F;
        }
        @Override public ResourceLocation getTextureLocation(Guardian entity){return ResourceLocation.parse(rig.texture());}
        @Override protected void scale(Guardian entity,PoseStack pose,float partial){float s=spec.scale();pose.scale(s,s,s);}
        @Override public boolean shouldRender(Guardian entity,Frustum frustum,double x,double y,double z){
            return super.shouldRender(entity,frustum,x,y,z)||extendedVisible(entity,frustum,x,y,z,rig.cullingRadius()*spec.scale());
        }
    }
    private static final class GuardianRigModel extends GuardianModel {
        private final OceanCreatureModel<Guardian> delegate;
        GuardianRigModel(ModelPart root,CreatureGeometry.Rig rig){
            // GuardianRenderer is typed to GuardianModel; its vanilla rig is never rendered.
            super(GuardianModel.createBodyLayer().bakeRoot());delegate=new OceanCreatureModel<>(root,rig);
        }
        @Override public ModelPart root(){return delegate.root();}
        @Override public void setupAnim(Guardian entity,float swing,float amount,float age,float yaw,float pitch){
            delegate.attackTime=attackTime;delegate.young=young;delegate.riding=riding;
            delegate.setupAnim(entity,swing,amount,age,yaw,pitch);
        }
    }
    private PrototypeRenderers() {}
}
