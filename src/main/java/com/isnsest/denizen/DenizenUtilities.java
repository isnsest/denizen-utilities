package com.isnsest.denizen;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.isnsest.denizen.containers.DialogScriptContainer;
import com.isnsest.denizen.events.PlayerConnectionConfigureEvent;
import com.isnsest.denizen.objects.ConnectionTag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizen.properties.BiomeExtensions;
import com.isnsest.denizen.properties.PlayerExtensions;

public class DenizenUtilities extends JavaPlugin {

    public static DenizenUtilities instance;
    Metrics metrics = new Metrics(this, 29915);;

    private void register() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            ScriptRegistry._registerType("dialog", DialogScriptContainer.class);
        }

        // Events
        ScriptEvent.registerScriptEvent(PlayerConnectionConfigureEvent.class);
        //

        ObjectFetcher.registerWithObjectFetcher(ConnectionTag.class, ConnectionTag.tagProcessor);

        PlayerExtensions.register();
        BiomeExtensions.register();
    }

    @Override
    public void onEnable() {
        Debug.log("denizen-utilities", "loading...");
        instance = this;
        saveDefaultConfig();
        register();

        metrics.addCustomChart(
            new Metrics.SimplePie("Denizen", () -> Bukkit.getPluginManager().getPlugin("Denizen").getDescription().getVersion())
        );
        metrics.addCustomChart(
            new Metrics.SimplePie("dDiscordBot", () -> Bukkit.getPluginManager().getPlugin("dDiscordBot").getDescription().getVersion())
        );

        Debug.log("denizen-utilities", "loaded!");
    }

    @Override
    public void onDisable() {
    }
}