package io.github.low2uu.dbv.world;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Per-thread, exception-safe scope: only drowned-world villages ignore water when projecting. */
public final class SubmergedVillages {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);
    private SubmergedVillages() {}
    public static boolean isVillage(RegistryAccess access, Structure structure) {
        var registry = access.registryOrThrow(Registries.STRUCTURE);
        return registry.getHolder(registry.getId(structure)).map(h -> h.is(StructureTags.VILLAGE)).orElse(false);
    }
    public static boolean enter(boolean active) { boolean before=ACTIVE.get(); ACTIVE.set(active); return before; }
    public static void restore(boolean before) { if(before) ACTIVE.set(true); else ACTIVE.remove(); }
    public static Heightmap.Types projection(Heightmap.Types type) {
        if(!ACTIVE.get()) return type;
        return switch(type) {
            case WORLD_SURFACE_WG -> Heightmap.Types.OCEAN_FLOOR_WG;
            case WORLD_SURFACE -> Heightmap.Types.OCEAN_FLOOR;
            default -> type;
        };
    }
}
