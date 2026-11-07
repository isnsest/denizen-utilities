package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.bridges.litebans.LiteBansUtils;
import litebans.api.Entry;
import org.bukkit.event.Listener;

public class LiteBansBansPlayerScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans bans player
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans adds a ban entry to the database.
    //
    // @Context
    // <context.id> returns the database ID of the ban entry.
    // <context.reason> returns the ban reason.
    // <context.uuid> returns the UUID of the banned player, if applicable.
    // <context.ip> returns the IP address affected by the ban, if applicable.
    // <context.executor> returns the staff PlayerTag who issued the ban, if online.
    // <context.executor_uuid> returns the UUID of the staff member who issued the ban.
    // <context.executor_name> returns the name of the staff member who issued the ban.
    // <context.server_scope> returns the server scope of the ban.
    // <context.server_origin> returns the server where the ban was created.
    // <context.duration> returns a DurationTag of the ban length, if temporary.
    // <context.duration_string> returns a human-readable duration string.
    // <context.remaining_duration> returns a DurationTag of the remaining ban time, if temporary.
    // <context.remaining_duration_string> returns a human-readable remaining duration string.
    // <context.random_id> returns the random ID of the ban entry.
    // <context.template_name> returns the template name used, if any.
    // <context.date_start> returns the ban start time in milliseconds (unix epoch).
    // <context.date_end> returns the ban end time in milliseconds (unix epoch).
    // <context.is_permanent> returns whether the ban is permanent.
    // <context.is_silent> returns whether the ban was issued silently.
    // <context.is_ipban> returns whether the ban affects an IP address.
    // <context.is_active> returns whether the ban entry is active.
    //
    // @Player When the banned player is online.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansBansPlayerScriptEvent instance;

    public Entry entry;

    public LiteBansBansPlayerScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans bans player");
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
