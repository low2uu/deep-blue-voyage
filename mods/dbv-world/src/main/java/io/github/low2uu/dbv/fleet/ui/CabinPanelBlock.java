package io.github.low2uu.dbv.fleet.ui;

import io.github.low2uu.dbv.fleet.FleetModule;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Stateless terminal: breaking it never deletes vessel data. */
public final class CabinPanelBlock {
    private static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"dbv_world");
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(Registries.ITEM,"dbv_world");
    public static final java.util.function.Supplier<Block> PANEL=BLOCKS.register("cabin_panel",()->new CabinMonitorBlock(BlockBehaviour.Properties.of().strength(2.5f).lightLevel(s->5)));
    public static final java.util.function.Supplier<Item> ITEM=ITEMS.register("cabin_panel",()->new BlockItem(PANEL.get(),new Item.Properties()));
    public static final java.util.function.Supplier<Block> CONSOLE=BLOCKS.register("jiuhu_console",()->
            new com.maxenonyme.createsubmarine.submarine.block.CommandSubBlock(BlockBehaviour.Properties.of().strength(3.5f).noOcclusion()));
    public static final java.util.function.Supplier<Item> CONSOLE_ITEM=ITEMS.register("jiuhu_console",()->new BlockItem(CONSOLE.get(),new Item.Properties()));
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);DbvCreativeTabs.register(bus);bus.addListener(CabinPanelBlock::consoleEntity);NeoForge.EVENT_BUS.addListener(CabinPanelBlock::interact);}
    private static void consoleEntity(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event){
        event.modify(com.maxenonyme.createsubmarine.CreateSubmarine.COMMAND_SUB_BE.get(),CONSOLE.get());
    }
    private static void interact(PlayerInteractEvent.RightClickBlock e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||!e.getLevel().getBlockState(e.getPos()).is(PANEL.get()))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        try{
            var m=FleetModule.get(p.getServer());var owner=SableCompanion.INSTANCE.getContaining(e.getLevel(),e.getPos());
            boolean onboard=owner instanceof ServerSubLevel body&&m.registry().flagship().map(i->i.bodyId().equals(body.getUniqueId())).orElse(false);
            if(!onboard&&!(p.serverLevel()==p.getServer().overworld()&&m.refit().contains(e.getPos())))throw new IllegalStateException("请将信息面板安装在主潜艇内");
            FleetPanel.openMain(p);
        }catch(Exception error){FleetPanel.unavailable(p,error.getMessage()==null?"面板暂时无法连接主艇":error.getMessage());}
    }
}
