package com.isnsest.denizenutilities.bridges;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;

import java.util.List;

public class BridgeLoader {

    private record BridgeEntry(String pluginName, String className) {}

    private static final List<BridgeEntry> MODULES = List.of(
            new BridgeEntry("SkinsRestorer", "com.isnsest.denizenutilities.bridges.skinsrestorer.SkinsRestorerModule"),
            new BridgeEntry("BetterModel", "com.isnsest.denizenutilities.bridges.bettermodel.BetterModelModule"),
            new BridgeEntry("DiscordSRV", "com.isnsest.denizenutilities.bridges.discordsrv.DiscordSRVModule"),
            new BridgeEntry("LiteBans", "com.isnsest.denizenutilities.bridges.litebans.LiteBansModule")
    );

    public static int loadAll() {
        int loadedCount = 0;
        for (BridgeEntry bridge : MODULES) {
            if (!Bukkit.getPluginManager().isPluginEnabled(bridge.pluginName())) {
                continue;
            }

            try {
                Class<?> clazz = Class.forName(bridge.className());
                BridgeModule module = (BridgeModule) clazz.getDeclaredConstructor().newInstance();
                module.register();
                loadedCount++;

            } catch (Throwable t) {
                Debug.echoError("Failed to register bridge module: " + bridge.pluginName());
                Debug.echoError(t);
            }
        }
        return loadedCount;
    }

    public static int getTotalAvailable() {
        return MODULES.size();
    }
}