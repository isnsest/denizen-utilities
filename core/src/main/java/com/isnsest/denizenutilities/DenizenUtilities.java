package com.isnsest.denizenutilities;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.isnsest.denizenutilities.bridges.BridgeLoader;
import com.isnsest.denizenutilities.extensions.commands.ShowDialogCommand;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptContainer;
import com.isnsest.denizenutilities.extensions.events.PlayerConnectionConfigureEvent;
import com.isnsest.denizenutilities.extensions.events.PlayerCustomClickScriptEvent;
import com.isnsest.denizenutilities.extensions.events.ResourcePackStatusConfigureEvent;
import com.isnsest.denizenutilities.extensions.objects.ConnectionTag;
import com.isnsest.denizenutilities.extensions.properties.UtilExtensions;
import com.isnsest.denizenutilities.packetevents.PacketEventsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.isnsest.denizenutilities.nms.NMSHandler;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizenutilities.extensions.properties.BiomeExtensions;
import com.isnsest.denizenutilities.extensions.properties.PlayerExtensions;

public class DenizenUtilities extends JavaPlugin {

    private static DenizenUtilities instance;

    public static DenizenUtilities getInstance() {
        return instance;
    }

    Metrics metrics;

    private void register() {
        PlayerExtensions.register();
        BiomeExtensions.register();
        UtilExtensions.register();

        ScriptRegistry._registerType("dialog", DialogScriptContainer.class);

        // Commands
        DenizenCore.commandRegistry.registerCommand(ShowDialogCommand.class);
        //

        // Events
        ScriptEvent.registerScriptEvent(PlayerConnectionConfigureEvent.class);
        ScriptEvent.registerScriptEvent(PlayerCustomClickScriptEvent.class);
        ScriptEvent.registerScriptEvent(ResourcePackStatusConfigureEvent.class);
        //

        // Objects
        ObjectFetcher.registerWithObjectFetcher(ConnectionTag.class, ConnectionTag.tagProcessor).setAsNOtherCode();
        //

        Bukkit.getPluginManager().registerEvents(new PlayerCustomClickScriptEvent.DialogEvents(), this);
    }

    private void registerMetrics() {
        metrics = new Metrics(this, 29915);
        metrics.addCustomChart(new Metrics.SimplePie("Denizen", () -> {
            var plugin = Bukkit.getPluginManager().getPlugin("Denizen");
            return plugin != null ? plugin.getPluginMeta().getVersion() : null;
        }));

        metrics.addCustomChart(new Metrics.SimplePie("dDiscordBot", () -> {
            var plugin = Bukkit.getPluginManager().getPlugin("dDiscordBot");
            return plugin != null ? plugin.getPluginMeta().getVersion() : null;
        }));
    }

    @Override
    public void onLoad() {
        PacketEventsManager.load(this);
    }

    @Override
    public void onEnable() {
        instance = this;

        Debug.log("denizen-utilities", "Loading...");
        saveDefaultConfig();

        Compatibility.init();

        register();
        registerMetrics();

        PacketEventsManager.init();

        if (NMSHandler.initialize()) {
            if (getConfig().getBoolean("fixes.fakeinternaldata", false)) {
                NMSHandler.instance.patchEntityHelper();
            }
        }

        int loadedBridges = BridgeLoader.loadAll();

        Debug.log("denizen-utilities", "Loaded successfully! <A>" + loadedBridges
                + "<W> plugin bridge(s) loaded (of <A>" + BridgeLoader.getTotalAvailable() + "<W> available)");
    }

    @Override
    public void onDisable() {
        PacketEventsManager.terminate();
    }
}