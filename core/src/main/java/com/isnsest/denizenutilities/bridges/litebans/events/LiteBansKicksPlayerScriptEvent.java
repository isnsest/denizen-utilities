package com.isnsest.denizenutilities.bridges.litebans.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.bridges.litebans.LiteBansUtils;
import litebans.api.Entry;
import org.bukkit.event.Listener;

public class LiteBansKicksPlayerScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // litebans kicks player
    //
    // @Group denizen-utilities
    //
    // @Triggers when LiteBans adds a kick entry to the database.
    //
    // @Context
    // <context.id> returns the database ID of the kick entry.
    // <context.reason> returns the kick reason.
    // <context.uuid> returns the UUID of the kicked player, if applicable.
    // <context.ip> returns the IP address of the kicked player, if applicable.
    // <context.executor> returns the staff PlayerTag who issued the kick, if online.
    // <context.executor_uuid> returns the UUID of the staff member who issued the kick.
    // <context.executor_name> returns the name of the staff member who issued the kick.
    // <context.server_scope> returns the server scope of the kick.
    // <context.server_origin> returns the server where the kick was created.
    // <context.random_id> returns the random ID of the kick entry.
    // <context.template_name> returns the template name used, if any.
    // <context.date_start> returns the kick time in milliseconds (unix epoch).
    // <context.is_silent> returns whether the kick was issued silently.
    // <context.is_active> returns whether the kick entry is active.
    //
    // @Player When the kicked player is online.
    //
    // @Plugin denizen-utilities, LiteBans
    //
    // -->

    public static LiteBansKicksPlayerScriptEvent instance;

    public Entry entry;

    public LiteBansKicksPlayerScriptEvent() {
        instance = this;
        registerCouldMatcher("litebans kicks player");
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
