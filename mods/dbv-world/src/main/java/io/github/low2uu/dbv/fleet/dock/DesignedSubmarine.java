package io.github.low2uu.dbv.fleet.dock;

import com.enxv.aeronauticsstructuretool.PlacementSnapMode;
import com.enxv.aeronauticsstructuretool.blueprint.codec.BlueprintArchiveCodec;
import com.enxv.aeronauticsstructuretool.blueprint.placement.BlueprintVerticalPlacement;
import com.enxv.aeronauticsstructuretool.blueprint.placement.NativeBlueprintPlacementService;
import com.google.gson.JsonParser;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.storage.BlockEntityJson;
import io.github.low2uu.dbv.world.WorldHeights;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/** User's world 5 design. JSON is the canonical template; upstream handles linked device relocation. */
public final class DesignedSubmarine {
    private DesignedSubmarine() {}

    public static CompoundTag read() throws IOException {
        try (var stream = DesignedSubmarine.class.getResourceAsStream("/data/dbv_world/blueprints/world5-starter.json")) {
            if (stream == null) throw new IOException("缺少用户设计的初始艇蓝图");
            var tag = (CompoundTag) BlockEntityJson.decode(JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
            if(tag.getList("sublevels", 10).size()!=1) throw new IOException("初始艇蓝图必须只有一个主船体");
            // Upstream otherwise silently substitutes missing blocks. Fail before allocating anything.
            validateBlocks(tag);
            return tag;
        }
    }

    private static void validateBlocks(net.minecraft.nbt.Tag tag) throws IOException {
        if (tag instanceof CompoundTag c) {
            if (c.contains("Name", 8)) {
                var id = ResourceLocation.tryParse(c.getString("Name"));
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id))
                    throw new IOException("初始艇缺少方块依赖：" + c.getString("Name"));
            }
            for (String key : c.getAllKeys()) validateBlocks(c.get(key));
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (var child : list) validateBlocks(child);
        }
    }

    public static ServerSubLevel create(ServerLevel level) throws IOException {
        var template = read();
        var result = NativeBlueprintPlacementService.place(level,
                new BlockPos(64, WorldHeights.fromLegacy(68), 180), Direction.UP,
                "酒狐号-用户设计初始艇", BlueprintArchiveCodec.encode(template),
                64, WorldHeights.fromLegacy(68), 180, 0, 100, 0, 0, 0,
                PlacementSnapMode.HIT, null,
                BlueprintVerticalPlacement.alignMinimumCenter(WorldHeights.fromLegacy(68) + .5, Double.NaN),
                null, null);
        if (result.placedSubLevelCount() != 1) throw new IOException("用户初始艇必须只有一个主物理船体");
        return result.rootSubLevel();
    }
}
