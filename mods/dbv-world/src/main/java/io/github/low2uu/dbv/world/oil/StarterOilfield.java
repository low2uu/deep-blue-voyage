package io.github.low2uu.dbv.world.oil;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.mojang.logging.LogUtils;
import io.github.low2uu.dbv.world.DeepBlueVoyageWorld;
import io.github.low2uu.dbv.world.IslandTerrain;
import io.github.low2uu.dbv.world.OceanWorlds;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Native CDG reserves, seeded once per starter-island save. No tick refill or global config changes. */
@EventBusSubscriber(modid = DeepBlueVoyageWorld.MOD_ID)
public final class StarterOilfield {
    public static final int MIN_X = 176, MAX_X = 207, MIN_Z = -64, MAX_Z = -33;
    public static final int FIRST_CHUNK_X = 11, LAST_CHUNK_X = 12;
    public static final int FIRST_CHUNK_Z = -4, LAST_CHUNK_Z = -3;
    private static final String KEY = "dbv_starter_oilfield";

    private StarterOilfield() {}

    public static boolean contains(int x, int z) {
        return x >= MIN_X && x <= MAX_X && z >= MIN_Z && z <= MAX_Z;
    }

    private static boolean eligible(ServerLevel level) {
        return level.dimension() == Level.OVERWORLD
                && OceanWorlds.isProjectWorld(level.getChunkSource().getGenerator());
    }

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        if (initialize(level)) mark(level);
    }

    /** Public for integration acceptance; idempotent and dimension/preset restricted. */
    public static boolean initialize(ServerLevel level) {
        if (!eligible(level)) return false;
        var state = state(level);
        if (state.initialized) return false;
        if (CDGConfig.OIL_CHUNK_INFINITE_THRESHOLD.get() >= Integer.MAX_VALUE)
            throw new IllegalStateException("CDG infinite oil threshold must be below Integer.MAX_VALUE");
        var oil = OilChunksSavedData.load(level);
        // Preserve the native reserve table before touching any of its four entries.
        state.originalOil = oil.save(new CompoundTag(), level.registryAccess()).copy();
        for (int cx = FIRST_CHUNK_X; cx <= LAST_CHUNK_X; cx++)
            for (int cz = FIRST_CHUNK_Z; cz <= LAST_CHUNK_Z; cz++)
                oil.setChunkAmount(new ChunkPos(cx, cz), Integer.MAX_VALUE);
        state.initialized = true;
        state.setDirty();
        LogUtils.getLogger().info("DBV_OIL initialized four native infinite reserves: X176..207 Z-64..-33; no forced chunk tickets.");
        return true;
    }

    /** Four small, non-destructive boundary signs; no foundations, machinery or terrain flattening. */
    public static int mark(ServerLevel level) {
        if (!eligible(level)) throw new IllegalArgumentException("油田标识仅用于主世界的深蓝远航起始岛预设");
        initialize(level);
        var state = state(level);
        int added = 0, index = 0;
        for (int x : new int[]{MIN_X, MAX_X}) for (int z : new int[]{MIN_Z, MAX_Z}) {
            int bit = 1 << index++;
            if ((state.marked & bit) != 0) continue;
            int y = IslandTerrain.groundHeight(x, z);
            var base = new BlockPos(x, y, z);
            boolean free = level.getBlockState(base).isSolidRender(level, base);
            for (int dy = 1; dy <= 3; dy++) {
                var block = level.getBlockState(base.above(dy));
                free &= (block.isAir() || block.canBeReplaced()) && block.getFluidState().isEmpty()
                        && level.getBlockEntity(base.above(dy)) == null;
            }
            if (!free) continue; // Never erase an existing player building or tree to place a marker.
            level.setBlock(base.above(), Blocks.WEATHERED_COPPER.defaultBlockState(), 3);
            level.setBlock(base.above(2), Blocks.EXPOSED_COPPER.defaultBlockState(), 3);
            level.setBlock(base.above(3), Blocks.OAK_SIGN.defaultBlockState(), 3);
            if (level.getBlockEntity(base.above(3)) instanceof SignBlockEntity sign) {
                var text = new SignText().setMessage(0, Component.literal("深蓝远航 无限油田"))
                        .setMessage(1, Component.literal("X 176 ～ 207"))
                        .setMessage(2, Component.literal("Z -64 ～ -33"))
                        .setMessage(3, Component.literal("原油无限 / 需抽油机")).setHasGlowingText(true);
                sign.setText(text, true);
                sign.setText(text, false);
                sign.setChanged();
                level.sendBlockUpdated(sign.getBlockPos(), sign.getBlockState(), sign.getBlockState(), 3);
            }
            state.marked |= bit;
            state.setDirty();
            added++;
        }
        return added;
    }

    public static String status(ServerLevel level) {
        if (!eligible(level)) return "当前不是主世界的起始岛预设；不会设置无限油田。";
        var s = state(level);
        int infinite = 0;
        // These entries are seeded before querying: CDG does not need to scan/generate distant chunks.
        if (s.initialized) for (int x = FIRST_CHUNK_X; x <= LAST_CHUNK_X; x++)
            for (int z = FIRST_CHUNK_Z; z <= LAST_CHUNK_Z; z++)
                if (OilChunksSavedData.getChunkOilAmount(level, new ChunkPos(x,z)) == Integer.MAX_VALUE) infinite++;
        return "起始岛油田：X176～207 / Z-64～-33；无限区块 " + infinite + "/4；标识 "
                + Integer.bitCount(s.marked) + "/4。井口下方铺直管至基岩，动力和炼油设施由玩家建设。";
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dbv").then(Commands.literal("oilfield")
                .executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal(status(c.getSource().getLevel())), false);
                    return 1;
                }).then(Commands.literal("mark").requires(s -> s.hasPermission(2)).executes(c -> {
                    try {
                        int n = mark(c.getSource().getLevel());
                        c.getSource().sendSuccess(() -> Component.literal("新增油田边界标识 " + n + " 个。" + status(c.getSource().getLevel())), true);
                        return n;
                    } catch (IllegalArgumentException error) {
                        c.getSource().sendFailure(Component.literal(error.getMessage()));
                        return 0;
                    }
                }))));
    }

    private static State state(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(State::new, State::load), KEY);
    }

    private static final class State extends SavedData {
        boolean initialized;
        int marked;
        CompoundTag originalOil = new CompoundTag();
        static State load(CompoundTag tag, HolderLookup.Provider registries) {
            var s = new State();
            s.initialized = tag.getBoolean("Initialized");
            s.marked = tag.getInt("Marked");
            s.originalOil = tag.getCompound("OriginalNativeOil").copy();
            return s;
        }
        @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("Version", 1);
            tag.putBoolean("Initialized", initialized);
            tag.putInt("Marked", marked);
            tag.put("OriginalNativeOil", originalOil.copy());
            return tag;
        }
    }
}
