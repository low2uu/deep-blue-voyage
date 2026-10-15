package io.github.low2uu.dbv.fleet.ui;

import io.github.low2uu.dbv.world.DeepBlueVoyageWorld;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All items registered by this mod automatically appear in its creative tab. */
public final class DbvCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DeepBlueVoyageWorld.MOD_ID);

    static {
        TABS.register("deep_blue_voyage", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.dbv_world"))
                .icon(() -> CabinPanelBlock.ITEM.get().getDefaultInstance())
                .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .displayItems((parameters, output) -> BuiltInRegistries.ITEM.entrySet().stream()
                        .filter(entry -> entry.getKey().location().getNamespace().equals(DeepBlueVoyageWorld.MOD_ID))
                        .sorted(java.util.Comparator.comparing(entry -> entry.getKey().location().toString()))
                        .forEach(entry -> output.accept(entry.getValue())))
                .build());
    }

    private DbvCreativeTabs() {}

    public static void register(IEventBus bus) { TABS.register(bus); }
}
