package ru.motionreblur.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

public class ConfigScreen extends GameOptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("gui.motion_re_blur.title");

    public ConfigScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, TITLE_TEXT);
    }

    @Override
    protected void addOptions() {
        Module module = Module.getInstance();

        SimpleOption<?>[] options = new SimpleOption[]{
                createToggleOption(module),
                createStrengthOption(module),
                createQualityOption(module),
                createRRCOption(module),
                createHandThresholdOption(module)
        };

        this.body.addAll(options);
    }

    private SimpleOption<Boolean> createToggleOption(Module module) {
        return SimpleOption.ofBoolean(
                "gui.motion_re_blur.toggle",
                module.isEnabled(),
                module::setEnabled
        );
    }

    private SimpleOption<Integer> createStrengthOption(Module module) {
        return new SimpleOption<>(
                "gui.motion_re_blur.strength",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.translatable("gui.motion_re_blur.strength", String.format("%.2f", value / 100.0)),
                new SimpleOption.ValidatingIntSliderCallbacks(-200, 200),
                (int) (module.getStrength() * 100),
                (value) -> module.setStrength(value / 100.0f)
        );
    }

    private SimpleOption<Integer> createQualityOption(Module module) {
        return new SimpleOption<>(
                "gui.motion_re_blur.quality",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> {
                    String qualityKey = switch (value) {
                        case 0 -> "gui.motion_re_blur.quality.low";
                        case 1 -> "gui.motion_re_blur.quality.medium";
                        case 2 -> "gui.motion_re_blur.quality.high";
                        case 3 -> "gui.motion_re_blur.quality.ultra";
                        default -> "gui.motion_re_blur.quality.medium";
                    };
                    return Text.translatable("gui.motion_re_blur.quality", Text.translatable(qualityKey).getString());
                },
                new SimpleOption.PotentialValuesBasedCallbacks<>(
                        java.util.List.of(0, 1, 2, 3),
                        com.mojang.serialization.Codec.INT
                ),
                module.getQuality(),
                module::setQuality
        );
    }

    private SimpleOption<Boolean> createRRCOption(Module module) {
        return SimpleOption.ofBoolean(
                "gui.motion_re_blur.rrc",
                module.isUseRRC(),
                module::setUseRRC
        );
    }

    private SimpleOption<Integer> createHandThresholdOption(Module module) {
        return new SimpleOption<>(
                "gui.motion_re_blur.hand_threshold",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.translatable("gui.motion_re_blur.hand_threshold", String.format("%.2f", value / 100.0)),
                new SimpleOption.ValidatingIntSliderCallbacks(0, 100),
                (int) (module.getHandDepthThreshold() * 100),
                (value) -> module.setHandDepthThreshold(value / 100.0f)
        );
    }
}
