package io.github.low2uu.dbv.fleet.mixin;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentDetector;
import io.github.low2uu.dbv.fleet.door.CabinDoors;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=CompartmentDetector.class,remap=false)
public abstract class CabinDoorPermeabilityMixin {
    @Inject(method="isPermeable",at=@At("HEAD"),cancellable=true,remap=false)
    private static void dbv$lockedDoor(BlockState state,CallbackInfoReturnable<Boolean> result){
        if(CabinDoors.isDoor(state))result.setReturnValue(!CabinDoors.sealed(state));
    }
}
