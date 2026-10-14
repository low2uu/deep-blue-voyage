package io.github.low2uu.dbv.fleet.command;

import com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.compat.VesselInspection;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Sneak-right-click opens the server-backed panel; ordinary computer use stays upstream. */
public final class ControllerInteraction {
    private static final Map<Player, Long> LAST_USE = new WeakHashMap<>();
    private ControllerInteraction() {}
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof CommandSubBlockEntity)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel().isClientSide()) return;
        var player = event.getEntity();
        long now = event.getLevel().getGameTime();
        Long previous = LAST_USE.get(player);
        if (previous != null && now >= previous && now - previous < 20) return;
        LAST_USE.put(player, now);
        var owner = SableCompanion.INSTANCE.getContaining(event.getLevel(), event.getPos());
        if (!(owner instanceof ServerSubLevel body)) {
            if(player instanceof net.minecraft.server.level.ServerPlayer p)try{
                var m=io.github.low2uu.dbv.fleet.FleetModule.get(p.getServer());
                if(m.refit().contains(event.getPos())){io.github.low2uu.dbv.fleet.ui.FleetPanel.openMain(p);return;}
            }catch(Exception ignored){}
            if(player instanceof net.minecraft.server.level.ServerPlayer p) io.github.low2uu.dbv.fleet.ui.FleetPanel.unavailable(p,"中央控制器尚未安装在已装配船体上。"); return;
        }
        try {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                io.github.low2uu.dbv.fleet.ui.FleetPanel.open(serverPlayer,body,event.getPos());
        }
        catch (RuntimeException exception) { if(player instanceof net.minecraft.server.level.ServerPlayer p) io.github.low2uu.dbv.fleet.ui.FleetPanel.unavailable(p,"设备读取失败，请稍后重试。"); }
    }
    public static int display(VesselInspection.Report report, java.util.function.Consumer<Component> output, int page) {
        int pages = Math.max(1, (report.devices().size() + 11) / 12);
        if (page < 1 || page > pages) throw new IllegalArgumentException("页码应为1～" + pages);
        var q = report.qualification();
        output.accept(Component.literal("主潜艇资格：" + (q.eligible() ? "符合（仍需唯一登记）" : String.join("；", q.reasons()))));
        output.accept(Component.literal("设备 " + report.devices().size() + " 个 | 第 " + page + "/" + pages + " 页 | tick=" + report.tick()));
        report.devices().stream().skip((long) (page - 1) * 12).limit(12).forEach(d -> output.accept(Component.literal(
                d.type() + " | " + d.id() + " | 艇内相对位置 " + d.relativePosition().toShortString() + " | " + d.status())));
        return q.eligible() ? 1 : 0;
    }
}
