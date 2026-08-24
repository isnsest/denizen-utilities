package com.isnsest.denizenutilities.bridges.bettermodel;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizenutilities.DenizenUtilities;
import com.isnsest.denizenutilities.bridges.bettermodel.commands.BMLimbCommand;
import com.isnsest.denizenutilities.bridges.bettermodel.commands.BMModelCommand;
import com.isnsest.denizenutilities.bridges.bettermodel.commands.BMPartCommand;
import com.isnsest.denizenutilities.bridges.bettermodel.commands.BMStateCommand;
import com.isnsest.denizenutilities.bridges.bettermodel.events.*;
import com.isnsest.denizenutilities.bridges.bettermodel.objects.BMBoneTag;
import com.isnsest.denizenutilities.bridges.bettermodel.objects.BMActiveModelTag;
import com.isnsest.denizenutilities.bridges.bettermodel.objects.BMModelTag;
import com.isnsest.denizenutilities.bridges.bettermodel.properties.BetterModelExtensions;
import com.isnsest.denizenutilities.bridges.BridgeModule;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.profile.ModelProfile;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BetterModelModule implements BridgeModule {

    @Override
    public String getPluginName() {
        return "BetterModel";
    }

    @Override
    public void register() {

        // Events
        ScriptEvent.registerScriptEvent(BMModelInteractScriptEvent.class);
        ScriptEvent.registerScriptEvent(BMPlayerAnimationSignalScriptEvent.class);
        ScriptEvent.registerScriptEvent(BMAnimationSignalScriptEvent.class);
        ScriptEvent.registerScriptEvent(BMStartReloadScriptEvent.class);
        ScriptEvent.registerScriptEvent(BMEndReloadScriptEvent.class);
        //

        // Commands
        DenizenCore.commandRegistry.registerCommand(BMModelCommand.class);
        DenizenCore.commandRegistry.registerCommand(BMLimbCommand.class);
        DenizenCore.commandRegistry.registerCommand(BMStateCommand.class);
        DenizenCore.commandRegistry.registerCommand(BMPartCommand.class);
        //

        // Objects
        ObjectFetcher.registerWithObjectFetcher(BMActiveModelTag.class, BMActiveModelTag.tagProcessor).setAsNOtherCode();
        ObjectFetcher.registerWithObjectFetcher(BMModelTag.class, BMModelTag.tagProcessor, "model", "BMModelTag").setAsNOtherCode().generateBaseTag();
        ObjectFetcher.registerWithObjectFetcher(BMBoneTag.class, BMBoneTag.tagProcessor).setAsNOtherCode();
        //

        BMScriptManager.register();
        BetterModelExtensions.register();

        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            SkinsRestorerHook.init();
        }

        Debug.log("denizen-utilities", "BetterModel bridge initialized.");
    }

    private static class SkinsRestorerHook {
        public static void init() {
            SkinsRestorerProvider.get().getEventBus().subscribe(DenizenUtilities.getInstance(), SkinApplyEvent.class, event -> {
                Bukkit.getScheduler().runTaskLater(DenizenUtilities.getInstance(), () -> {
                    var player = event.getPlayer(Player.class);
                    BetterModel.platform().skinManager().complete(ModelProfile.of(BukkitAdapter.adapt(player)).asUncompleted());
                }, 2L);
            });
        }
    }

}