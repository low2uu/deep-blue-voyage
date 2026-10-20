package io.github.low2uu.dbv.fleet.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.low2uu.dbv.world.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
public abstract class SubmergedVillagePlacementMixin {
    @WrapMethod(method="placeInChunk")
    private void dbv$groundPaths(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
            RandomSource random, BoundingBox box, ChunkPos pos, Operation<Void> original) {
        boolean before=SubmergedVillages.enter(generator instanceof FloodedWorldGenerator
                && SubmergedVillages.isVillage(level.registryAccess(),((StructureStart)(Object)this).getStructure()));
        try { original.call(level,structures,generator,random,box,pos); }
        finally { SubmergedVillages.restore(before); }
    }
}
