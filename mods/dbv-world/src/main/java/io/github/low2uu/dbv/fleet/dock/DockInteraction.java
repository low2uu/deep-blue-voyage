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
        report.devices().stream().filter(d->(d.id().equals("create_submarine:command_sub") || d.id().equals("dbv_world:jiuhu_console"))).findFirst().ifPresent(d->
                io.github.low2uu.dbv.fleet.ui.FleetPanel.open(player,body,d.position()));
    }
    private static void commands(RegisterCommandsEvent event) {
        var root=Commands.literal("refit").executes(c->{
            try { io.github.low2uu.dbv.fleet.ui.FleetPanel.openMain(c.getSource().getPlayerOrException());return 1; }
            catch(Exception e) { c.getSource().sendFailure(Component.literal(e.getMessage()));return 0; }
        });
        for(String action:new String[]{"begin","check","commit","cancel","angler"}) root.then(Commands.literal(action).executes(c->{
            try {
                var m=FleetModule.get(c.getSource().getServer());var p=c.getSource().getPlayerOrException();
                String result=m.refit().execute(action,p,m);c.getSource().sendSuccess(()->Component.literal(result),false);return 1;
            } catch(Exception e) { c.getSource().sendFailure(Component.literal("改装未完成："+e.getMessage()));return 0; }
        }));
        event.getDispatcher().register(Commands.literal("dbv").requires(s->s.hasPermission(2)).then(root));
    }
    private static boolean deny(ServerPlayer player,BlockPos pos,BlockState state) {
        try{
            var m=FleetModule.get(player.getServer());
            if(player.serverLevel()==player.getServer().overworld()&&m.refit().contains(pos)){
                if(m.refit().mayEdit(player,pos,state))return false;
                player.sendSystemMessage(Component.literal("船坞改装区域已锁定：仅当前改装者可在施工阶段编辑，请检查改装状态与权限。"));return true;
            }
        }catch(Exception e){return true;}
        var owner=SableCompanion.INSTANCE.getContaining(player.level(),pos);
        if(!(owner instanceof ServerSubLevel body)) return false;
        try {
            var m=FleetModule.get(player.getServer());
            if(m.registry().flagship().isEmpty() || !m.registry().flagship().get().bodyId().equals(body.getUniqueId())) return false;
            if(!m.refit().active())return false; // Normal physical ships retain Sable's ordinary building interactions.
            if(m.refit().mayEdit(player,pos,state)) return false;
            player.sendSystemMessage(Component.literal("主艇正在转换、验收或还原，暂时无法编辑；请等待改装流程结束。"));
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
