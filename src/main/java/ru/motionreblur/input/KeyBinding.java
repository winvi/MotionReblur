package ru.motionreblur.input;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.motionreblur.config.ConfigScreen;

public class KeyBinding {
    private static net.minecraft.client.option.KeyBinding openGuiKey;

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new net.minecraft.client.option.KeyBinding(
                "key.motion_re_blur.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.motion_re_blur"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen, client.options));
            }
        });
    }
}
