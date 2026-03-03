package ru.motionreblur.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.motionreblur.config.Module;

@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
    @Unique
    private Matrix4f prevModelView = new Matrix4f();
    @Unique
    private Matrix4f prevProjection = new Matrix4f();
    @Unique
    private Vector3f prevCameraPos = new Vector3f();

    @Inject(method = "render", at = @At("HEAD"))
    private void setMatrices(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f viewMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickProgress(true);
        GameRenderer gameRenderer = net.minecraft.client.MinecraftClient.getInstance().gameRenderer;
        float fov = ((GameRendererAccessor) gameRenderer).invokeGetFov(camera, tickDelta, true);
        Module.getInstance().shader.setFrameMotionBlur(positionMatrix, prevModelView,
                gameRenderer.getBasicProjectionMatrix(fov),
                prevProjection,
                new Vector3f(
                        (float) (camera.getCameraPos().x % 30000f),
                        (float) (camera.getCameraPos().y % 30000f),
                        (float) (camera.getCameraPos().z % 30000f)
                ),
                prevCameraPos
        );
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void setOldMatrices(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f viewMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        prevModelView = new Matrix4f(positionMatrix);
        GameRenderer gameRenderer = net.minecraft.client.MinecraftClient.getInstance().gameRenderer;
        float tickDelta = tickCounter.getTickProgress(true);
        float fov = ((GameRendererAccessor) gameRenderer).invokeGetFov(camera, tickDelta, true);
        prevProjection = new Matrix4f(gameRenderer.getBasicProjectionMatrix(fov));
        prevCameraPos = new Vector3f(
                (float) (camera.getCameraPos().x % 30000f),
                (float) (camera.getCameraPos().y % 30000f),
                (float) (camera.getCameraPos().z % 30000f)
        );
    }
}
