package ru.motionreblur;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.motionreblur.command.Command;
import ru.motionreblur.config.Module;
import ru.motionreblur.input.KeyBinding;

public class MotionReBlur implements ClientModInitializer {
    public static final String MOD_ID = "motion_re_blur";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftClient mc;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();

        LOGGER.info("Motion ReBlur initialized!");

        Module.getInstance();

        Command.register();

        KeyBinding.register();
    }
}
