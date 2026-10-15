package io.github.low2uu.dbv.fleet.mixin;

import io.github.low2uu.dbv.world.IslandProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class IslandFireMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void dbv$stopSpread(BlockState state,ServerLevel level,BlockPos pos,RandomSource random,CallbackInfo ci) {
        // Include the few blocks from which fire can spread across the boundary.
        for(int x:new int[]{-5,0,5})for(int z:new int[]{-5,0,5})
            if(IslandProtection.protectedAt(level,pos.offset(x,0,z))){ci.cancel();return;}
    }
    @Inject(method="checkBurnOut",at=@At("HEAD"),cancellable=true)
    private void dbv$stopBurn(Level level,BlockPos pos,int chance,RandomSource random,int age,Direction face,CallbackInfo ci) {
        if(IslandProtection.protectedAt(level,pos))ci.cancel();
    }
}
