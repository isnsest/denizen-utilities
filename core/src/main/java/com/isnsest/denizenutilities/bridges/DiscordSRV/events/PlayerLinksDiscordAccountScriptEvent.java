package com.isnsest.denizenutilities.bridges.DiscordSRV.events;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import org.bukkit.event.Listener;

public class PlayerLinksDiscordAccountScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player links discord account
    //
    // @Group denizen-utilities
    //
    // @Triggers when a Minecraft player links their Discord account.
    //
    // @Context
    // <context.discord_id> returns the Discord ID of the account that was linked.
    //
    // @Player Always.
    //
    // @Plugin denizen-utilities, DiscordSRV
    //
    // -->

    public static PlayerLinksDiscordAccountScriptEvent instance;

    public AccountLinkedEvent event;

    public PlayerLinksDiscordAccountScriptEvent() {
        instance = this;
        registerCouldMatcher("player links discord account");
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("discord_id")) {
            return new ElementTag(event.getUser().getId());
        }
        return super.getContext(name);
    }

    public void fire(AccountLinkedEvent event) {
        instance.event = event;
        super.fire();
    }
}
