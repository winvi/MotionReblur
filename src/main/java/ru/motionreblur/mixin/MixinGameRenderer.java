package ru.motionreblur.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.motionreblur.config.Module;

@Mixin(value = GameRenderer.class, priority = 1100)
public class MixinGameRenderer {

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;renderHand(FZLorg/joml/Matrix4f;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeRenderHand(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!Module.getInstance().isEnabled()) return;
        Module.getInstance().shader.applyMotionBlurBeforeHands();
    }
}
