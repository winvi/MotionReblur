package ru.motionreblur.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.motionreblur.MotionReBlur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(MotionReBlur.MOD_ID + ".json");

    public boolean enabled = false;
    public float strength = -0.8f;
    public boolean useRRC = true;
    public int quality = 2;
    public float handDepthThreshold = 0.56f;

    public static Config load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                Config config = GSON.fromJson(json, Config.class);
                MotionReBlur.LOGGER.info("Configuration loaded from: " + CONFIG_PATH);
                return config;
            } catch (IOException e) {
                MotionReBlur.LOGGER.error("Failed to load configuration: " + e.getMessage());
            }
        }
        MotionReBlur.LOGGER.info("Using default configuration");
        return new Config();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            MotionReBlur.LOGGER.info("Configuration saved to: " + CONFIG_PATH);
        } catch (IOException e) {
            MotionReBlur.LOGGER.error("Failed to save configuration: " + e.getMessage());
        }
    }

    public void copyFrom(Module module) {
        this.enabled = module.isEnabled();
        this.strength = module.getStrength();
        this.useRRC = module.isUseRRC();
        this.quality = module.getQuality();
        this.handDepthThreshold = module.getHandDepthThreshold();
    }
}
