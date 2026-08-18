package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.Listener;

public class LiteBansSendsBroadcastScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans sends broadcast
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans sends a broadcast or notification message to players with permission to see it.
    //
    // @Context
    // <context.message> returns the broadcast message.
    // <context.type> returns the broadcast type, if any. When non-null, viewers need the "litebans.notify.[type]" permission.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansSendsBroadcastScriptEvent instance;

    public String message;
    public String type;

    public LiteBansSendsBroadcastScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans sends broadcast");
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "message" -> new ElementTag(message);
            case "type" -> type != null ? new ElementTag(type) : null;
            default -> super.getContext(name);
        };
    }

    public void fire(String message, String type) {
        this.message = message;
        this.type = type;
        super.fire();
    }
}
