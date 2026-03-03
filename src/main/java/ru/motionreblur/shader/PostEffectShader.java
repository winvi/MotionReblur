package ru.motionreblur.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import ru.motionreblur.MotionReBlur;
import ru.motionreblur.mixin.PostEffectPassAccessor;
import ru.motionreblur.mixin.PostEffectProcessorAccessor;
import ru.motionreblur.mixin.ShaderLoaderAccessor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Обёртка над PostEffectProcessor для управления пост-эффектами.
 * Адаптирована для 1.21.11 с UBO-based uniform системой.
 */
public class PostEffectShader {
    private final Identifier location;
    private final Consumer<PostEffectShader> initCallback;
    private PostEffectProcessor processor;
    private boolean initialized = false;
    private boolean errored = false;

    // Cached uniform values
    private final Matrix4f mvInverse = new Matrix4f();
    private final Matrix4f projInverse = new Matrix4f();
    private final Matrix4f prevModelView = new Matrix4f();
    private final Matrix4f prevProjection = new Matrix4f();
    private float cameraPosX, cameraPosY, cameraPosZ;
    private float prevCameraPosX, prevCameraPosY, prevCameraPosZ;
    private float viewResX = 1.0f, viewResY = 1.0f;
    private float blendFactor = 0.5f;
    private float inverseSamples = 1.0f;
    private float handDepthThreshold = 0.56f;
    private int motionBlurSamples = 0;
    private int halfSamples = 0;
    private int blurAlgorithm = 0;

    public PostEffectShader(Identifier location, Consumer<PostEffectShader> initCallback) {
        this.location = location;
        this.initCallback = initCallback;
    }

    public PostEffectShader(Identifier location) {
        this(location, s -> {});
    }

    private void ensureInitialized() {
        if (initialized || errored) return;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ShaderLoader shaderLoader = client.getShaderLoader();
            ShaderLoader.Cache cache = ((ShaderLoaderAccessor) shaderLoader).getCache();
            this.processor = cache.getOrLoadProcessor(location, DefaultFramebufferSet.MAIN_ONLY);
            this.initialized = true;
            this.initCallback.accept(this);
            MotionReBlur.LOGGER.info("Shader loaded: {}", location);
        } catch (Exception e) {
            this.errored = true;
            MotionReBlur.LOGGER.error("Failed to load shader {}", location, e);
        }
    }

    /**
     * Рендерит пост-эффект, создавая новый GpuBuffer с uniform данными каждый кадр.
     */
    public void render(float tickDelta) {
        ensureInitialized();
        if (processor == null) return;

        replaceUniformBuffer();

        MinecraftClient client = MinecraftClient.getInstance();
        processor.render(client.getFramebuffer(), net.minecraft.client.util.ObjectAllocator.TRIVIAL);
    }

    /**
     * Создаёт новый immutable GpuBuffer с текущими uniform данными
     * и подменяет его в uniformBuffers map PostEffectPass.
     * Minecraft сам привяжет буфер к шейдеру при рендере через RenderPass.setUniform().
     */
    private void replaceUniformBuffer() {
        if (processor == null) return;

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor) processor).getPasses();
        if (passes.isEmpty()) return;

        PostEffectPass motionBlurPass = passes.get(0);
        Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) motionBlurPass).getUniformBuffers();

        if (!uniformBuffers.containsKey("MotionBlurParams")) return;

        // Build uniform data using std140 layout on stack
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Calculate size: 4 mat4 (4*64=256) + 2 vec3 (2*16=32) + vec2 (8) + 3 float (12) + 3 int (12) = 320
            Std140Builder builder = Std140Builder.onStack(stack, 320);

            // mat4 mvInverse
            putMatrix4f(builder, mvInverse);
            // mat4 projInverse
            putMatrix4f(builder, projInverse);
            // mat4 prevModelView
            putMatrix4f(builder, prevModelView);
            // mat4 prevProjection
            putMatrix4f(builder, prevProjection);
            // vec3 cameraPos
            builder.putVec3(cameraPosX, cameraPosY, cameraPosZ);
            // vec3 prevCameraPos
            builder.putVec3(prevCameraPosX, prevCameraPosY, prevCameraPosZ);
            // vec2 view_res
            builder.putVec2(viewResX, viewResY);
            // float BlendFactor
            builder.putFloat(blendFactor);
            // float inverseSamples
            builder.putFloat(inverseSamples);
            // float handDepthThreshold
            builder.putFloat(handDepthThreshold);
            // int motionBlurSamples
            builder.putInt(motionBlurSamples);
            // int halfSamples
            builder.putInt(halfSamples);
            // int blurAlgorithm
            builder.putInt(blurAlgorithm);

            ByteBuffer data = builder.get();

            // Close old buffer
            GpuBuffer oldBuffer = uniformBuffers.get("MotionBlurParams");
            if (oldBuffer != null) {
                oldBuffer.close();
            }

            // Create new immutable buffer with our data via GpuDevice
            GpuBuffer newBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "motion_re_blur MotionBlurParams UBO",
                    GpuBuffer.USAGE_UNIFORM,
                    data
            );

            uniformBuffers.put("MotionBlurParams", newBuffer);
        }
    }

    private void putMatrix4f(Std140Builder builder, Matrix4f mat) {
        builder.putMat4f(mat);
    }

    // Setters

    public void setBlendFactor(float value) { this.blendFactor = value; }
    public void setViewRes(float x, float y) { this.viewResX = x; this.viewResY = y; }
    public void setMotionBlurSamples(int value) { this.motionBlurSamples = value; }
    public void setHalfSamples(int value) { this.halfSamples = value; }
    public void setInverseSamples(float value) { this.inverseSamples = value; }
    public void setBlurAlgorithm(int value) { this.blurAlgorithm = value; }
    public void setHandDepthThreshold(float value) { this.handDepthThreshold = value; }
    public void setMvInverse(Matrix4f mat) { this.mvInverse.set(mat); }
    public void setProjInverse(Matrix4f mat) { this.projInverse.set(mat); }
    public void setPrevModelView(Matrix4f mat) { this.prevModelView.set(mat); }
    public void setPrevProjection(Matrix4f mat) { this.prevProjection.set(mat); }
    public void setCameraPos(float x, float y, float z) { this.cameraPosX = x; this.cameraPosY = y; this.cameraPosZ = z; }
    public void setPrevCameraPos(float x, float y, float z) { this.prevCameraPosX = x; this.prevCameraPosY = y; this.prevCameraPosZ = z; }

    public void reload() {
        this.processor = null;
        this.initialized = false;
        this.errored = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
