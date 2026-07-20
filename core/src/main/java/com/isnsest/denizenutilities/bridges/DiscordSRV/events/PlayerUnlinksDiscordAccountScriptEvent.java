package com.isnsest.denizenutilities.bridges.DiscordSRV.events;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import org.bukkit.event.Listener;

public class PlayerUnlinksDiscordAccountScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player unlinks discord account
    //
    // @Group denizen-utilities
    //
    // @Triggers when a Minecraft player unlinks their Discord account.
    //
    // @Context
    // <context.discord_id> returns the Discord ID of the account that was unlinked.
    //
    // @Player Always.
    //
    // @Plugin denizen-utilities, DiscordSRV
    //
    // -->

    public static PlayerUnlinksDiscordAccountScriptEvent instance;

    public AccountUnlinkedEvent event;

    public PlayerUnlinksDiscordAccountScriptEvent() {
        instance = this;
        registerCouldMatcher("player unlinks discord account");
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("discord_id")) {
            return new ElementTag(event.getDiscordId());
        }
        return super.getContext(name);
    }

    public void fire(AccountUnlinkedEvent event) {
        instance.event = event;
        super.fire();
    }
}
