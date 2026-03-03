package ru.motionreblur.shader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.motionreblur.MotionReBlur;
import ru.motionreblur.compat.IrisCompat;
import ru.motionreblur.config.Module;
import ru.motionreblur.util.MonitorInfoProvider;

public class Shader {
    private final Module config;
    private final PostEffectShader motionBlurShader;

    private long lastNano = System.nanoTime();
    private float currentBlur = 0.0f;
    private float currentFPS = 0.0f;

    private final Matrix4f tempPrevModelView = new Matrix4f();
    private final Matrix4f tempPrevProjection = new Matrix4f();
    private final Matrix4f tempProjInverse = new Matrix4f();
    private final Matrix4f tempMvInverse = new Matrix4f();

    public Shader(Module config) {
        this.config = config;
        motionBlurShader = new PostEffectShader(
                Identifier.of(MotionReBlur.MOD_ID, "motion_blur"),
                shader -> shader.setBlendFactor(config.getStrength())
        );
    }

    public void applyMotionBlurBeforeHands() {
        long now = System.nanoTime();
        float deltaTime = (now - lastNano) / 1_000_000_000.0f;
        lastNano = now;

        if (deltaTime > 0 && deltaTime < 1.0f) {
            currentFPS = 1.0f / deltaTime;
        } else {
            currentFPS = 0.0f;
        }

        if (shouldRenderMotionBlur()) {
            applyMotionBlur();
        }
    }

    private boolean shouldRenderMotionBlur() {
        if (config.getStrength() == 0 || !config.isEnabled()) {
            return false;
        }

        if (IrisCompat.isIrisActive()) {
            String shaderPack = IrisCompat.getCurrentShaderPackName();
            if (shaderPack != null) {
                MotionReBlur.LOGGER.debug("Motion Blur working alongside Iris shader pack: " + shaderPack);
            }
        }

        return true;
    }

    private void applyMotionBlur() {
        MinecraftClient client = MinecraftClient.getInstance();

        MonitorInfoProvider.updateDisplayInfo();
        int displayRefreshRate = MonitorInfoProvider.getRefreshRate();

        float baseStrength = config.getStrength();
        float scaledStrength = baseStrength;
        if (config.isUseRRC()) {
            float fpsOverRefresh = (displayRefreshRate > 0) ? currentFPS / displayRefreshRate : 1.0f;
            if (fpsOverRefresh < 1.0f) fpsOverRefresh = 1.0f;
            scaledStrength = baseStrength * fpsOverRefresh;
        }

        motionBlurShader.setBlendFactor(scaledStrength);
        currentBlur = scaledStrength;

        int sampleAmount = getSampleAmountForFPS(currentFPS);

        motionBlurShader.setViewRes((float) client.getFramebuffer().textureWidth, (float) client.getFramebuffer().textureHeight);
        motionBlurShader.setMotionBlurSamples(sampleAmount);
        motionBlurShader.setHalfSamples(sampleAmount / 2);
        motionBlurShader.setInverseSamples(1.0f / sampleAmount);
        motionBlurShader.setBlurAlgorithm(1);
        motionBlurShader.setHandDepthThreshold(config.getHandDepthThreshold());

        motionBlurShader.render(0.0f);
    }

    private int getSampleAmountForFPS(float fps) {
        int quality = config.getQuality();

        int baseSamples = switch (quality) {
            case 0 -> 8;
            case 1 -> 12;
            case 2 -> 16;
            case 3 -> 24;
            default -> 12;
        };

        if (fps < 30) {
            return Math.max(6, baseSamples / 2);
        } else if (fps < 60) {
            return Math.max(8, (int) (baseSamples * 0.75f));
        } else if (fps > 144) {
            return (int) (baseSamples * 1.25f);
        }

        return baseSamples;
    }

    public void setFrameMotionBlur(Matrix4f modelView, Matrix4f prevModelView,
                                   Matrix4f projection, Matrix4f prevProjection,
                                   Vector3f cameraPos, Vector3f prevCameraPos) {
        motionBlurShader.setMvInverse(tempMvInverse.set(modelView).invert());
        motionBlurShader.setProjInverse(tempProjInverse.set(projection).invert());
        motionBlurShader.setPrevModelView(tempPrevModelView.set(prevModelView));
        motionBlurShader.setPrevProjection(tempPrevProjection.set(prevProjection));
        motionBlurShader.setCameraPos(cameraPos.x, cameraPos.y, cameraPos.z);
        motionBlurShader.setPrevCameraPos(prevCameraPos.x, prevCameraPos.y, prevCameraPos.z);
    }

    public void updateBlurStrength(float strength) {
        motionBlurShader.setBlendFactor(strength);
        currentBlur = strength;
    }
}
