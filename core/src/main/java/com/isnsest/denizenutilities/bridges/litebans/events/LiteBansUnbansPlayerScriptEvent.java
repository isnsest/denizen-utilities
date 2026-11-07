package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.bridges.litebans.LiteBansUtils;
import litebans.api.Entry;
import org.bukkit.event.Listener;

public class LiteBansUnbansPlayerScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans unbans player
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans removes a ban entry from the database.
    //
    // @Context
    // <context.id> returns the database ID of the ban entry.
    // <context.reason> returns the original ban reason.
    // <context.uuid> returns the UUID of the unbanned player, if applicable.
    // <context.ip> returns the IP address that was affected by the ban, if applicable.
    // <context.executor> returns the staff PlayerTag who originally issued the ban, if online.
    // <context.executor_uuid> returns the UUID of the staff member who originally issued the ban.
    // <context.executor_name> returns the name of the staff member who originally issued the ban.
    // <context.removed_by> returns the staff PlayerTag who removed the ban, if online.
    // <context.removed_by_uuid> returns the UUID of the staff member who removed the ban.
    // <context.removed_by_name> returns the name of the staff member who removed the ban.
    // <context.removal_reason> returns the reason the ban was removed.
    // <context.server_scope> returns the server scope of the ban.
    // <context.server_origin> returns the server where the ban was created.
    // <context.duration> returns a DurationTag of the original ban length, if temporary.
    // <context.duration_string> returns a human-readable duration string.
    // <context.random_id> returns the random ID of the ban entry.
    // <context.template_name> returns the template name used, if any.
    // <context.date_start> returns the ban start time in milliseconds (unix epoch).
    // <context.date_end> returns the ban end time in milliseconds (unix epoch).
    // <context.is_permanent> returns whether the ban was permanent.
    // <context.is_silent> returns whether the ban was issued silently.
    // <context.is_ipban> returns whether the ban affected an IP address.
    // <context.is_active> returns whether the ban entry is active.
    //
    // @Player When the unbanned player is online.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansUnbansPlayerScriptEvent instance;

    public Entry entry;

    public LiteBansUnbansPlayerScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans unbans player");
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
