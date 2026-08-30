package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.bridges.litebans.LiteBansUtils;
import litebans.api.Entry;
import org.bukkit.event.Listener;

public class LiteBansWarnsPlayerScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans warns player
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans adds a warning entry to the database.
    //
    // @Context
    // <context.id> returns the database ID of the warning entry.
    // <context.reason> returns the warning reason.
    // <context.uuid> returns the UUID of the warned player, if applicable.
    // <context.ip> returns the IP address of the warned player, if applicable.
    // <context.executor> returns the staff PlayerTag who issued the warning, if online.
    // <context.executor_uuid> returns the UUID of the staff member who issued the warning.
    // <context.executor_name> returns the name of the staff member who issued the warning.
    // <context.server_scope> returns the server scope of the warning.
    // <context.server_origin> returns the server where the warning was created.
    // <context.duration> returns a DurationTag of the warning length, if temporary.
    // <context.duration_string> returns a human-readable duration string.
    // <context.remaining_duration> returns a DurationTag of the remaining warning time, if temporary.
    // <context.remaining_duration_string> returns a human-readable remaining duration string.
    // <context.random_id> returns the random ID of the warning entry.
    // <context.template_name> returns the template name used, if any.
    // <context.date_start> returns the warning start time in milliseconds (unix epoch).
    // <context.date_end> returns the warning end time in milliseconds (unix epoch).
    // <context.is_permanent> returns whether the warning is permanent.
    // <context.is_silent> returns whether the warning was issued silently.
    // <context.is_active> returns whether the warning entry is active.
    //
    // @Player When the warned player is online.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansWarnsPlayerScriptEvent instance;

    public Entry entry;

    public LiteBansWarnsPlayerScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans warns player");
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
