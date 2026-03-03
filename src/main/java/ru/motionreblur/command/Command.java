package ru.motionreblur.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import ru.motionreblur.config.ConfigScreen;
import ru.motionreblur.config.Module;
import ru.motionreblur.util.MonitorInfoProvider;

import static ru.motionreblur.MotionReBlur.mc;

public class Command {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> registerCommand(dispatcher)
        );
    }

    private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("motionreblur")
                .executes(Command::openGui)
                .then(ClientCommandManager.literal("on")
                        .executes(Command::enable))
                .then(ClientCommandManager.literal("off")
                        .executes(Command::disable))
                .then(ClientCommandManager.literal("toggle")
                        .executes(Command::toggle))
                .then(ClientCommandManager.literal("strength")
                        .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(-2.0f, 2.0f))
                                .executes(ctx -> setStrength(ctx, FloatArgumentType.getFloat(ctx, "value")))))
                .then(ClientCommandManager.literal("rrc")
                        .executes(Command::toggleRRC))
                .then(ClientCommandManager.literal("status")
                        .executes(Command::showStatus))
                .then(ClientCommandManager.literal("help")
                        .executes(Command::showHelp))
        );
    }

    private static int openGui(CommandContext<FabricClientCommandSource> ctx) {
        mc.execute(() -> mc.setScreen(new ConfigScreen(null, mc.options)));
        ctx.getSource().sendFeedback(Text.literal("§aОткрываю меню настроек..."));
        return 1;
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> ctx) {
        Module mb = Module.getInstance();
        ctx.getSource().sendFeedback(Text.literal("§7§m                    "));
        ctx.getSource().sendFeedback(Text.literal("§6Motion ReBlur"));
        ctx.getSource().sendFeedback(Text.literal("§7Состояние: " + (mb.isEnabled() ? "§aВКЛ" : "§cВЫКЛ")));
        ctx.getSource().sendFeedback(Text.literal("§7Сила: §f" + String.format("%.1f", mb.getStrength())));
        ctx.getSource().sendFeedback(Text.literal("§7RRC: " + (mb.isUseRRC() ? "§aВКЛ" : "§cВЫКЛ")));
        ctx.getSource().sendFeedback(Text.literal("§7Refresh Rate: §f" + MonitorInfoProvider.getRefreshRate() + " Hz"));
        ctx.getSource().sendFeedback(Text.literal("§7§m                    "));
        return 1;
    }

    private static int enable(CommandContext<FabricClientCommandSource> ctx) {
        Module.getInstance().setEnabled(true);
        ctx.getSource().sendFeedback(Text.literal("§aMotion Blur включен"));
        return 1;
    }

    private static int disable(CommandContext<FabricClientCommandSource> ctx) {
        Module.getInstance().setEnabled(false);
        ctx.getSource().sendFeedback(Text.literal("§cMotion Blur выключен"));
        return 1;
    }

    private static int toggle(CommandContext<FabricClientCommandSource> ctx) {
        Module mb = Module.getInstance();
        boolean newState = !mb.isEnabled();
        mb.setEnabled(newState);
        ctx.getSource().sendFeedback(Text.literal("§7Motion Blur: " + (newState ? "§aВКЛ" : "§cВЫКЛ")));
        return 1;
    }

    private static int setStrength(CommandContext<FabricClientCommandSource> ctx, float value) {
        Module.getInstance().setStrength(value);
        ctx.getSource().sendFeedback(Text.literal("§aСила установлена на §f" + String.format("%.1f", value)));
        return 1;
    }

    private static int toggleRRC(CommandContext<FabricClientCommandSource> ctx) {
        Module mb = Module.getInstance();
        boolean newState = !mb.isUseRRC();
        mb.setUseRRC(newState);
        ctx.getSource().sendFeedback(Text.literal("§7Адаптация к частоте монитора: " + (newState ? "§aВКЛ" : "§cВЫКЛ")));
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.literal("§7§m                              "));
        ctx.getSource().sendFeedback(Text.literal("§6Motion ReBlur - Команды"));
        ctx.getSource().sendFeedback(Text.literal("§7§m                              "));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur §7- открыть GUI"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur on §7- включить"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur off §7- выключить"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur toggle §7- переключить"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur strength <значение> §7- установить силу"));
        ctx.getSource().sendFeedback(Text.literal("§7  Диапазон: от -2.0 до 2.0"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur rrc §7- переключить адаптацию к частоте"));
        ctx.getSource().sendFeedback(Text.literal("§e/motionreblur status §7- показать статус"));
        ctx.getSource().sendFeedback(Text.literal("§7§m                              "));
        return 1;
    }
}
