package io.github.low2uu.dbv.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Hull construction materials, independent of the submarine's control system. */
public final class HullBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, DeepBlueVoyageWorld.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, DeepBlueVoyageWorld.MOD_ID);
    public static final java.util.function.Supplier<Block> IRON_SUBMARINE_HULL =
            BLOCKS.register("iron_submarine_hull", () ->
                    new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final java.util.function.Supplier<Item> IRON_SUBMARINE_HULL_ITEM =
            ITEMS.register("iron_submarine_hull", () ->
                    new BlockItem(IRON_SUBMARINE_HULL.get(), new Item.Properties()));

    private HullBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
