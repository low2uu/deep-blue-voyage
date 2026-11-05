package io.github.low2uu.dbv.fleet.mixin;

import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** DeepSeas reads saved energy through rate-limited transfer calls. Loading is not an energy
 * transfer: retain the saved FE exactly for disk loads and synchronization, with no rate/capacity changes.
 */
@Mixin(value = ElectrolyzerBlockEntity.class, remap = false)
public abstract class ElectrolyzerPersistenceMixin {
    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private void dbv$readExactStoredEnergy(CompoundTag tag, HolderLookup.Provider registries,
                                         boolean clientPacket, CallbackInfo ci) {
        if (!tag.contains("Energy", Tag.TAG_ANY_NUMERIC)) return;
        var device = (ElectrolyzerBlockEntity) (Object) this;
        device.energyStorage.deserializeNBT(registries, IntTag.valueOf(tag.getInt("Energy")));
    }
}
