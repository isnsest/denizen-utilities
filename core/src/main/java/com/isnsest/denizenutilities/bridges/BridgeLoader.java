package com.isnsest.denizenutilities.bridges;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizenutilities.bridges.bettermodel.BetterModelModule;
import com.isnsest.denizenutilities.bridges.discordsrv.DiscordSRVModule;
import com.isnsest.denizenutilities.bridges.skinsrestorer.SkinsRestorerModule;
import org.bukkit.Bukkit;

import java.util.List;

public class BridgeLoader {

    private static final List<Class<? extends BridgeModule>> MODULES = List.of(
            SkinsRestorerModule.class,
            BetterModelModule.class,
            DiscordSRVModule.class
    );

    public static int loadAll() {
        int loadedCount = 0;
        for (Class<? extends BridgeModule> clazz : MODULES) {
            try {
                BridgeModule module = clazz.getDeclaredConstructor().newInstance();
                String pluginName = module.getPluginName();

                if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                    continue;
                }

                module.register();
                loadedCount++;

            } catch (Exception e) {
                Debug.echoError("Failed to register bridge module: " + clazz.getSimpleName());
                Debug.echoError(e);
            }
        }
        return loadedCount;
    }

    public static int getTotalAvailable() {
        return MODULES.size();
    }
}