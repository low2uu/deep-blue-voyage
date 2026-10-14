package io.github.low2uu.dbv.fleet.dock;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class DockInteraction {
    private DockInteraction() {}
    public static void initialize() {
        NeoForge.EVENT_BUS.addListener(DockInteraction::commands);
        NeoForge.EVENT_BUS.addListener(DockInteraction::breakBlock);
        NeoForge.EVENT_BUS.addListener(DockInteraction::placeBlock);
    }
    public static void menu(ServerPlayer player,ServerSubLevel body) {
        var report=new io.github.low2uu.dbv.fleet.compat.VesselInspection().inspect(body);
        report.devices().stream().filter(d->d.id().equals("create_submarine:command_sub")).findFirst().ifPresent(d->
                io.github.low2uu.dbv.fleet.ui.FleetPanel.open(player,body,d.position()));
    }
    private static void commands(RegisterCommandsEvent event) {
        var root=Commands.literal("refit").executes(c->{
            try { var m=FleetModule.get(c.getSource().getServer()); c.getSource().sendSuccess(()->Component.literal(m.refit().status()+"；操作：begin/check/commit/cancel"),false);return 1; }
            catch(Exception e) { c.getSource().sendFailure(Component.literal(e.getMessage()));return 0; }
        });
        for(String action:new String[]{"begin","check","commit","cancel"}) root.then(Commands.literal(action).executes(c->{
            try {
                var m=FleetModule.get(c.getSource().getServer());var p=c.getSource().getPlayerOrException();
                String result=m.refit().execute(action,p,m);c.getSource().sendSuccess(()->Component.literal(result),false);return 1;
            } catch(Exception e) { c.getSource().sendFailure(Component.literal("改装未完成："+e.getMessage()));return 0; }
        }));
        event.getDispatcher().register(Commands.literal("dbv").requires(s->s.hasPermission(2)).then(root));
    }
    private static boolean deny(ServerPlayer player,BlockPos pos,BlockState state) {
        var owner=SableCompanion.INSTANCE.getContaining(player.level(),pos);
        if(!(owner instanceof ServerSubLevel body)) return false;
        try {
            var m=FleetModule.get(player.getServer());
            if(m.registry().flagship().isEmpty() || !m.registry().flagship().get().bodyId().equals(body.getUniqueId())) return false;
            if(m.refit().mayEdit(player,pos,state)) return false;
            player.sendSystemMessage(Component.literal("主艇编辑已锁定：请先在船载电脑开始改装，并使用支持方块；不得越界或拆除系泊角块。"));
            return true;
        } catch(Exception e) { return true; }
    }
    private static void breakBlock(BlockEvent.BreakEvent event) {
        if(event.getPlayer() instanceof ServerPlayer p && deny(p,event.getPos(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState())) event.setCanceled(true);
    }
    private static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof ServerPlayer p && deny(p,event.getPos(),event.getPlacedBlock())) event.setCanceled(true);
    }
}
