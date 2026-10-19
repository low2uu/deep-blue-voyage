package io.github.low2uu.dbv.fleet.mixin;

import io.github.low2uu.dbv.world.FloodedWorldGenerator;
import io.github.low2uu.dbv.world.OceanBounds;
import io.github.low2uu.dbv.world.IslandTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevent cross-chunk features from decorating the exterior or retained starter buildings. */
@Mixin(WorldGenRegion.class)
public abstract class OceanGenerationBoundsMixin {
    @Inject(method="setBlock",at=@At("HEAD"),cancellable=true)
    private void dbv$boundFeatures(BlockPos pos,BlockState state,int flags,int depth,CallbackInfoReturnable<Boolean> ci){
        var region=(WorldGenRegion)(Object)this;
        if(region.getLevel().getChunkSource().getGenerator() instanceof FloodedWorldGenerator
                &&(!OceanBounds.contains(pos.getX(),pos.getZ())||(pos.getY()>=IslandTerrain.groundHeight(pos.getX(),pos.getZ())-4&&OceanBounds.protectedSurface(pos.getX(),pos.getZ()))))ci.setReturnValue(false);
    }
}
