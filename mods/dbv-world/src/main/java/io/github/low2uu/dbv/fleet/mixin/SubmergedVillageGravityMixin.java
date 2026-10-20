package io.github.low2uu.dbv.fleet.mixin;

import io.github.low2uu.dbv.world.SubmergedVillages;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GravityProcessor.class)
public abstract class SubmergedVillageGravityMixin {
    @Redirect(method="processBlock",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/LevelReader;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    private int dbv$groundPath(LevelReader level, Heightmap.Types type, int x, int z) {
        return level.getHeight(SubmergedVillages.projection(type),x,z);
    }
}
