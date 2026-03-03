package ru.motionreblur.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.motionreblur.config.ConfigScreen;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addMotionReBlurButton(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("gui.motion_re_blur.button"),
                button -> client.setScreen(new ConfigScreen((OptionsScreen) (Object) this, client.options))
            )
            .dimensions(this.width / 2 - 75, this.height - 52, 150, 20)
            .build()
        );
    }
}
