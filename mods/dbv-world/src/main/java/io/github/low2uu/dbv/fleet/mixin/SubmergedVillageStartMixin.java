package io.github.low2uu.dbv.fleet.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.low2uu.dbv.world.*;
import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Structure.class)
public abstract class SubmergedVillageStartMixin {
    @WrapMethod(method="generate")
    private StructureStart dbv$groundVillage(RegistryAccess access, ChunkGenerator generator, BiomeSource biomes,
            RandomState random, StructureTemplateManager templates, long seed, ChunkPos pos, int references,
            LevelHeightAccessor height, Predicate<Holder<Biome>> validBiome, Operation<StructureStart> original) {
        boolean before=SubmergedVillages.enter(generator instanceof FloodedWorldGenerator
                && SubmergedVillages.isVillage(access,(Structure)(Object)this));
        try { return original.call(access,generator,biomes,random,templates,seed,pos,references,height,validBiome); }
        finally { SubmergedVillages.restore(before); }
    }
}
