package ru.motionreblur.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru.motionreblur.MotionReBlur;
import ru.motionreblur.mixin.GameRendererAccessor;
import ru.motionreblur.mixin.PostEffectProcessorAccessor;
import ru.motionreblur.mixin.ShaderLoaderAccessor;

import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * Обёртка над PostEffectProcessor для удобного управления пост-эффектами.
 * Заменяет зависимость от Satin.
 */
public class PostEffectShader {
    private final Identifier location;
    private final Consumer<PostEffectShader> initCallback;
    private PostEffectProcessor processor;
    private boolean initialized = false;
    private boolean errored = false;

    public PostEffectShader(Identifier location, Consumer<PostEffectShader> initCallback) {
        this.location = location;
        this.initCallback = initCallback;
    }

    public PostEffectShader(Identifier location) {
        this(location, s -> {});
    }

    /**
     * Ленивая инициализация — загружает PostEffectProcessor при первом использовании
     */
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
     * Рендерит пост-эффект на текущий framebuffer
     */
    public void render(float tickDelta) {
        ensureInitialized();
        if (processor == null) return;

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();

        MinecraftClient client = MinecraftClient.getInstance();
        processor.render(client.getFramebuffer(), ((GameRendererAccessor) client.gameRenderer).getPool());
        client.getFramebuffer().beginWrite(true);

        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }

    /**
     * Устанавливает float uniform на все passes шейдера
     */
    public void setUniformValue(String name, float value) {
        forEachUniform(name, u -> u.set(value));
    }

    public void setUniformValue(String name, float v0, float v1) {
        forEachUniform(name, u -> u.set(v0, v1));
    }

    public void setUniformValue(String name, float v0, float v1, float v2) {
        forEachUniform(name, u -> u.set(v0, v1, v2));
    }

    public void setUniformValue(String name, float v0, float v1, float v2, float v3) {
        forEachUniform(name, u -> u.set(v0, v1, v2, v3));
    }

    /**
     * Устанавливает int uniform на все passes шейдера
     */
    public void setUniformValue(String name, int value) {
        forEachUniform(name, u -> u.set(value));
    }

    public void setUniformValue(String name, int v0, int v1) {
        forEachUniform(name, u -> u.set(v0, v1));
    }

    /**
     * Устанавливает Matrix4f uniform на все passes шейдера
     */
    public void setUniformValue(String name, Matrix4f value) {
        forEachUniform(name, u -> u.set(value));
    }

    /**
     * Перезагружает шейдер (при смене разрешения и т.д.)
     */
    public void reload() {
        this.processor = null;
        this.initialized = false;
        this.errored = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void forEachUniform(String name, Consumer<GlUniform> action) {
        ensureInitialized();
        if (processor == null) return;

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor) processor).getPasses();
        for (PostEffectPass pass : passes) {
            GlUniform uniform = pass.getProgram().getUniform(name);
            if (uniform != null) {
                action.accept(uniform);
            }
        }
    }
}
