package com.isnsest.denizenutilities.nms;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizenutilities.nms.helpers.BiomeHelper;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;

public abstract class NMSHandler {

    public static NMSHandler instance;

    public static Object entityHelper;
    public static BiomeHelper biomeHelper;

    private static final List<String> SUPPORTED_VERSIONS = List.of("1.21", "26.1", "26.2", "26.3");

    private static Optional<String> getNMSPackageVersion(String bukkitVersion) {
        return SUPPORTED_VERSIONS.stream()
                .filter(bukkitVersion::contains)
                .findFirst()
                .map(version -> "v" + version.replace('.', '_'));
    }

    public static boolean initialize() {
        String bukkitVersion = Bukkit.getBukkitVersion();

        String packageVersion = getNMSPackageVersion(bukkitVersion).orElse(null);
        if (packageVersion == null) {
            Debug.log("denizen-utilities", "This server version is not supported!");
            return false;
        }

        try {
            Class<?> clazz = Class.forName("com.isnsest.denizenutilities.nms." + packageVersion + ".Handler");

            if (NMSHandler.class.isAssignableFrom(clazz)) {
                instance = (NMSHandler) clazz.getDeclaredConstructor().newInstance();
                return true;
            }
        } catch (Exception e) {
            Debug.echoError(e);
        }

        return false;
    }

    public abstract void patchEntityHelper();
}