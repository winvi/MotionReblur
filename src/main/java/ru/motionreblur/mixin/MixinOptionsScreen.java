package ru.motionreblur.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.motionreblur.config.ConfigScreen;

@Mixin(OptionsScreen.class)
public class MixinOptionsScreen {

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;",
            ordinal = 9
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void addMotionReBlurButton(CallbackInfo ci, DirectionalLayoutWidget directionalLayoutWidget, DirectionalLayoutWidget directionalLayoutWidget2, GridWidget gridWidget, GridWidget.Adder adder) {
        MinecraftClient client = MinecraftClient.getInstance();

        adder.add(
            ButtonWidget.builder(
                Text.translatable("gui.motion_re_blur.button"),
                button -> client.setScreen(new ConfigScreen((OptionsScreen) (Object) this, client.options))
            )
            .width(150)
            .build()
        );
    }
}
