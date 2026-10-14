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

/** Sneak-right-click adds an inspection entry; ordinary computer use stays upstream. */
public final class ControllerInteraction {
    private static final Map<Player, Long> LAST_USE = new WeakHashMap<>();
    private static final Map<Player, Integer> LAST_PAGE = new WeakHashMap<>();
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
            player.sendSystemMessage(Component.literal("中央控制器尚未安装在已装配船体上。")); return;
        }
        try {
            var report = new VesselInspection().inspect(body);
            int pages = Math.max(1, (report.devices().size() + 11) / 12);
            int page = previous == null || now - previous > 200 ? 1 : LAST_PAGE.getOrDefault(player, 0) % pages + 1;
            LAST_PAGE.put(player, page);
            display(report, player::sendSystemMessage, page);
            if (pages > 1) player.sendSystemMessage(Component.literal("再次潜行右键翻页；停止操作10秒后回到第一页。"));
        }
        catch (RuntimeException exception) { player.sendSystemMessage(Component.literal("设备检测失败，未更改主艇身份：" + exception.getClass().getSimpleName())); }
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
