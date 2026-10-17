package io.github.low2uu.dbv.fleet.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.low2uu.dbv.fleet.FleetModule;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FleetCommands {
    private FleetCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dbv").requires(source -> source.hasPermission(2))
                .then(Commands.literal("flagship")
                        .then(Commands.literal("inspect").then(Commands.argument("body", UuidArgument.uuid())
                                .executes(context -> inspect(context.getSource(), UuidArgument.getUuid(context, "body"), 1))
                                .then(Commands.argument("page", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                        .executes(context -> inspect(context.getSource(), UuidArgument.getUuid(context, "body"),
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "page"))))))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("candidates").executes(context -> candidates(context.getSource())))
                        .then(Commands.literal("register")
                                .then(Commands.argument("body", UuidArgument.uuid())
                                        .then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
                                            try {
                                                var source = context.getSource();
                                                var module = FleetModule.get(source.getServer());
                                                var body = module.adapter().find(source.getLevel(), UuidArgument.getUuid(context, "body"));
                                                if (body == null) { throw new IllegalArgumentException("当前维度中没有这个已加载载体"); }
                                                module.bind(source.getServer(), body, StringArgumentType.getString(context, "name"));
                                                source.sendSuccess(() -> Component.literal("主潜艇登记成功；该世界不再接受第二艘主艇登记。"), true);
                                                return status(source);
                                            } catch (Exception exception) { return fail(context.getSource(), exception); }
                                        }))))));
    }

    private static int candidates(CommandSourceStack source) {
        try {
            var bodies = FleetModule.get(source.getServer()).adapter().loaded(source.getLevel());
            source.sendSuccess(() -> Component.literal("当前维度已加载载体：" + bodies.size() + "（仅列出，不自动选择）"), false);
            bodies.stream().limit(30).forEach(body -> source.sendSuccess(() -> Component.literal(
                    body.getUniqueId() + " | " + (body.getName() == null ? "未命名" : body.getName())), false));
            return bodies.size();
        } catch (Exception exception) { return fail(source, exception); }
    }

    private static int inspect(CommandSourceStack source, java.util.UUID id, int page) {
        try {
            var module = FleetModule.get(source.getServer());
            var body = module.adapter().find(source.getLevel(), id);
            if (body == null) throw new IllegalArgumentException("当前维度中没有这个已加载载体");
            return ControllerInteraction.display(module.inspection().inspect(body),
                    message -> source.sendSuccess(() -> message, false), page);
        } catch (Exception exception) { return fail(source, exception); }
    }

    private static int status(CommandSourceStack source) {
        try {
            var module = FleetModule.get(source.getServer());
            var sample = module.sample(source.getServer());
            module.registry().flagship().ifPresent(identity -> source.sendSuccess(() -> Component.literal(
                    identity.name() + " | 主艇编号=" + identity.vesselId() + " | 代次=" + identity.generation()), false));
            source.sendSuccess(() -> Component.literal(sample.state().displayName() + " | " + sample.detail()), false);
            if (Double.isFinite(sample.mass()) && Double.isFinite(sample.speed())) {
                source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                        "物理质量=%.2f（物理引擎单位） | 物理速度=%.3f | 深度=%s | 裂纹=%s | 破舱=%s | 观测游戏刻=%d",
                        sample.mass(), sample.speed(), value(sample.depth()), value(sample.cracks()), value(sample.breached()), sample.observedTick())), false);
            }
            source.sendSuccess(() -> Component.literal("建设船坞：/dbv refit；仅支持初始小艇有限方块快照，尚非完整灾难恢复。"), false);
            return 1;
        } catch (Exception exception) { return fail(source, exception); }
    }

    private static String value(Object value) { return value == null ? "未知/未初始化" : value instanceof Boolean flag ? (flag ? "是" : "否") : value.toString(); }

    private static int fail(CommandSourceStack source, Exception exception) {
        source.sendFailure(Component.literal("深蓝远航操作未完成：" + exception.getMessage()));
        return 0;
    }
}
