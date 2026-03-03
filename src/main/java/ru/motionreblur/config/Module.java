package ru.motionreblur.config;

import ru.motionreblur.MotionReBlur;
import ru.motionreblur.shader.Shader;

public class Module {
    private static final Module instance = new Module();
    public final Shader shader;
    private final Config config;

    private boolean enabled;
    private float strength;
    private boolean useRRC;
    private int quality;
    private float handDepthThreshold;

    private Module() {
        config = Config.load();

        this.enabled = config.enabled;
        this.strength = config.strength;
        this.useRRC = config.useRRC;
        this.quality = config.quality;
        this.handDepthThreshold = config.handDepthThreshold;

        shader = new Shader(this);
    }

    public static Module getInstance() {
        return instance;
    }

    public void saveConfig() {
        config.copyFrom(this);
        config.save();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        MotionReBlur.LOGGER.info("Motion Blur " + (enabled ? "enabled" : "disabled"));
        saveConfig();
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = Math.max(-2.0f, Math.min(2.0f, strength));
        shader.updateBlurStrength(this.strength);
        MotionReBlur.LOGGER.info("Motion Blur strength set to " + this.strength);
        saveConfig();
    }

    public boolean isUseRRC() {
        return useRRC;
    }

    public void setUseRRC(boolean useRRC) {
        this.useRRC = useRRC;
        MotionReBlur.LOGGER.info("Refresh Rate Scaling " + (useRRC ? "enabled" : "disabled"));
        saveConfig();
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = Math.max(0, Math.min(3, quality));
        MotionReBlur.LOGGER.info("Motion Blur quality set to " + getQualityName());
        saveConfig();
    }

    public String getQualityName() {
        return switch (quality) {
            case 0 -> "gui.motion_re_blur.quality.low";
            case 1 -> "gui.motion_re_blur.quality.medium";
            case 2 -> "gui.motion_re_blur.quality.high";
            case 3 -> "gui.motion_re_blur.quality.ultra";
            default -> "gui.motion_re_blur.quality.medium";
        };
    }

    public float getHandDepthThreshold() {
        return handDepthThreshold;
    }

    public void setHandDepthThreshold(float threshold) {
        this.handDepthThreshold = Math.max(0.0f, Math.min(1.0f, threshold));
        MotionReBlur.LOGGER.info("Hand depth threshold set to " + this.handDepthThreshold);
        saveConfig();
    }
}
