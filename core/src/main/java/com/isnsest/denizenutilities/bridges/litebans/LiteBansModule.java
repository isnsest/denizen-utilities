package com.isnsest.denizenutilities.bridges.litebans;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizenutilities.bridges.BridgeModule;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansBansPlayerScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansKicksPlayerScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansMutesPlayerScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansSendsBroadcastScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansUnbansPlayerScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansUnmutesPlayerScriptEvent;
import com.isnsest.denizenutilities.bridges.litebans.events.LiteBansWarnsPlayerScriptEvent;
import litebans.api.Events;

public class LiteBansModule implements BridgeModule {

    private static LiteBansListener listener;

    @Override
    public String getPluginName() {
        return "LiteBans";
    }

    @Override
    public void register() {
        listener = new LiteBansListener();
        Events.get().register(listener);

        // Events
        ScriptEvent.registerScriptEvent(LiteBansBansPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansUnbansPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansMutesPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansUnmutesPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansKicksPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansWarnsPlayerScriptEvent.class);
        ScriptEvent.registerScriptEvent(LiteBansSendsBroadcastScriptEvent.class);
        //

        Debug.log("denizen-utilities", "LiteBans bridge initialized.");
    }
}
