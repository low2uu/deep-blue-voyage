package io.github.low2uu.dbv.content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Guardian;

/** Visual-only joint animation; never changes targets, damage, movement or hitboxes. */
public final class OceanCreatureModel<T extends Mob> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final CreatureGeometry.Rig rig;
    private final Map<String,ModelPart> nodes = new LinkedHashMap<>();
    public OceanCreatureModel(ModelPart root, CreatureGeometry.Rig rig) {
        this.root=root; this.rig=rig;
        nodes.put("root",root.getChild("root"));
        for (var p:rig.parts()) nodes.put(p.name(),nodes.get(p.parent()).getChild(p.name()));
    }
    @Override public ModelPart root() { return root; }
    @Override public void setupAnim(T mob,float limbSwing,float limbAmount,float age,float headYaw,float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float activity=Mth.clamp((float)mob.getDeltaMovement().length()*8,0,1);
        for(var p:rig.parts()) {
            var node=nodes.get(p.name());
            float angle=Mth.sin(age*p.rate()+p.phase())*p.amplitude()*(.35F+.65F*activity);
            switch(p.motion()) {
                case "look" -> {
                    node.yRot=Mth.clamp(headYaw,-55,55)*Mth.DEG_TO_RAD*p.amplitude();
                    node.xRot=Mth.clamp(headPitch,-30,30)*Mth.DEG_TO_RAD*p.amplitude();
                    continue;
                }
                case "pulse" -> {
                    float pulse=Mth.sin(age*p.rate())*p.amplitude();
                    node.xScale=1+pulse;node.zScale=1+pulse;node.yScale=1-pulse;
                    continue;
                }
                case "arm", "leg" -> {
                    angle=mob.isInWater()?angle:Mth.cos(limbSwing*.6662F+p.phase())*limbAmount*p.amplitude()*2;
                    if (p.motion().equals("arm") && mob.isAggressive()) angle-=.7F;
                    if (p.motion().equals("arm")) angle-=Mth.sin(attackTime*Mth.PI)*.8F;
                }
                case "jaw" -> angle+=mob instanceof Guardian g && g.hasActiveAttackTarget()?.25F:attackTime*.3F;
                default -> { }
            }
            switch(p.axis()) { case "x" -> node.xRot+=angle;case "y" -> node.yRot+=angle;case "z" -> node.zRot+=angle; }
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color) {
        boolean small=young && nodes.containsKey("right_arm");
        if(small){pose.pushPose();pose.translate(0,.75,0);pose.scale(.5F,.5F,.5F);}
        super.renderToBuffer(pose,vertices,light,overlay,color);
        if(small)pose.popPose();
    }
    @Override public void translateToHand(HumanoidArm arm,PoseStack pose) {
        // Same shoulder-space convention as vanilla ItemInHandLayer, for the diver only.
        nodes.get("root").translateAndRotate(pose);
        nodes.get("body").translateAndRotate(pose);
        nodes.get(arm==HumanoidArm.RIGHT?"right_arm":"left_arm").translateAndRotate(pose);
    }
}
