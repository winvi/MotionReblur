package ru.motionreblur.compat;

import net.fabricmc.loader.api.FabricLoader;
import ru.motionreblur.MotionReBlur;

public class IrisCompat {
    private static Boolean irisLoaded = null;

    public static boolean isIrisLoaded() {
        if (irisLoaded == null) {
            irisLoaded = FabricLoader.getInstance().isModLoaded("iris");
        }
        return irisLoaded;
    }

    public static boolean areShadersEnabled() {
        if (!isIrisLoaded()) return false;
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object irisApiInstance = irisApiClass.getMethod("getInstance").invoke(null);
            Object config = irisApiClass.getMethod("getConfig").invoke(irisApiInstance);
            Class<?> configClass = Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig");
            return (boolean) configClass.getMethod("areShadersEnabled").invoke(config);
        } catch (Exception e) {
            MotionReBlur.LOGGER.debug("Could not check Iris shader status: " + e.getMessage());
            return false;
        }
    }

    public static String getCurrentShaderPackName() {
        if (!isIrisLoaded() || !areShadersEnabled()) return null;
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object irisApiInstance = irisApiClass.getMethod("getInstance").invoke(null);
            Object config = irisApiClass.getMethod("getConfig").invoke(irisApiInstance);
            Class<?> configClass = Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig");
            Object shaderPackName = configClass.getMethod("getShaderPackName").invoke(config);
            return shaderPackName != null ? shaderPackName.toString() : null;
        } catch (Exception e) {
            MotionReBlur.LOGGER.debug("Could not get shader pack name: " + e.getMessage());
            return null;
        }
    }

    public static boolean isIrisActive() {
        return isIrisLoaded() && areShadersEnabled();
    }
}
