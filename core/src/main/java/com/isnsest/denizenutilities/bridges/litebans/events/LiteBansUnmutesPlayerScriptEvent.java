package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.bridges.litebans.LiteBansUtils;
import litebans.api.Entry;
import org.bukkit.event.Listener;

public class LiteBansUnmutesPlayerScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans unmutes player
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans removes a mute entry from the database.
    //
    // @Context
    // <context.id> returns the database ID of the mute entry.
    // <context.reason> returns the original mute reason.
    // <context.uuid> returns the UUID of the unmuted player, if applicable.
    // <context.ip> returns the IP address that was affected by the mute, if applicable.
    // <context.executor> returns the staff PlayerTag who originally issued the mute, if online.
    // <context.executor_uuid> returns the UUID of the staff member who originally issued the mute.
    // <context.executor_name> returns the name of the staff member who originally issued the mute.
    // <context.removed_by> returns the staff PlayerTag who removed the mute, if online.
    // <context.removed_by_uuid> returns the UUID of the staff member who removed the mute.
    // <context.removed_by_name> returns the name of the staff member who removed the mute.
    // <context.removal_reason> returns the reason the mute was removed.
    // <context.server_scope> returns the server scope of the mute.
    // <context.server_origin> returns the server where the mute was created.
    // <context.duration> returns a DurationTag of the original mute length, if temporary.
    // <context.duration_string> returns a human-readable duration string.
    // <context.random_id> returns the random ID of the mute entry.
    // <context.template_name> returns the template name used, if any.
    // <context.date_start> returns the mute start time in milliseconds (unix epoch).
    // <context.date_end> returns the mute end time in milliseconds (unix epoch).
    // <context.is_permanent> returns whether the mute was permanent.
    // <context.is_silent> returns whether the mute was issued silently.
    // <context.is_ipban> returns whether the mute affected an IP address.
    // <context.is_active> returns whether the mute entry is active.
    //
    // @Player When the unmuted player is online.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansUnmutesPlayerScriptEvent instance;

    public Entry entry;

    public LiteBansUnmutesPlayerScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans unmutes player");
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return LiteBansUtils.getScriptEntryData(entry);
    }

    @Override
    public ObjectTag getContext(String name) {
        ObjectTag context = LiteBansUtils.getEntryContext(name, entry);
        return context != null ? context : super.getContext(name);
    }

    public void fire(Entry entry) {
        this.entry = entry;
        super.fire();
    }
}
