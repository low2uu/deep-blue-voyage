package io.github.low2uu.dbv.fleet.mixin;

import io.github.low2uu.dbv.world.WorldHeights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Level's vanilla deprecated API still returns 63 independently of the generator. */
@Mixin(Level.class)
public abstract class OceanSeaLevelMixin {
    private static final ResourceLocation DBV_OCEAN = ResourceLocation.fromNamespaceAndPath("dbv_world", "ocean");

    @Inject(method="getSeaLevel", at=@At("HEAD"), cancellable=true)
    private void dbv$oceanSeaLevel(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level)(Object)this;
        if (level.dimensionTypeRegistration().unwrapKey().map(key -> key.location().equals(DBV_OCEAN)).orElse(false)) {
            cir.setReturnValue(WorldHeights.SEA_LEVEL);
        }
    }
}
