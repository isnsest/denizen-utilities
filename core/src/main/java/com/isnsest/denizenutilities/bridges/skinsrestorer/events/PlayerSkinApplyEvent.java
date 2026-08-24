package com.isnsest.denizenutilities.bridges.skinsrestorer.events;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.isnsest.denizenutilities.DenizenUtilities;
import com.isnsest.denizenutilities.bridges.skinsrestorer.SkinsRestorerModule;
import com.isnsest.denizenutilities.bridges.skinsrestorer.SkinsRestorerUtils;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.skinsrestorer.api.event.SkinApplyEvent;

public class PlayerSkinApplyEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player skin apply
    //
    // @Group denizen-utilities
    //
    // @Triggers when a player's skin is being applied.
    //
    // @Context
    // <context.value> returns the Base64 texture value of the skin.
    // <context.signature> returns the signature of the skin.
    //
    // @Determine
    // "TEXTURE:<value>;<signature>" to set a custom skin by raw data.
    // "NAME:<skin_name>" to set a skin by its name.
    // "URL:<url>" to set a skin from a URL.
    //
    // @Player Always.
    //
    // @Plugin denizen-utilities, SkinsRestorer
    //
    // -->

    public static PlayerSkinApplyEvent instance;

    public SkinApplyEvent event;

    public PlayerSkinApplyEvent() {
        instance = this;
        registerCouldMatcher("player skin apply");

        this.<PlayerSkinApplyEvent, ObjectTag>registerDetermination("texture", ObjectTag.class, (evt, context, output) -> {
            String[] data = output.toString().split(";");
            SkinProperty property = SkinProperty.of(data[0], data[1]);
            event.setProperty(property);
        });
        this.<PlayerSkinApplyEvent, ObjectTag>registerDetermination("name", ObjectTag.class, (evt, context, output) -> {
            SkinProperty property = SkinsRestorerUtils.getSkinByName(output.toString()).getProperty();
            event.setProperty(property);
        });
        this.<PlayerSkinApplyEvent, ObjectTag>registerDetermination("url", ObjectTag.class, (evt, context, output) -> {
            SkinProperty property = SkinsRestorerUtils.getSkinFromUrl(output.toString());
            event.setProperty(property);
        });

        SkinsRestorerModule.getAPI()
                .getEventBus()
                .subscribe(DenizenUtilities.getInstance(), SkinApplyEvent.class, event -> {
                    instance.event = event;
                    instance.fire();
                });
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer(Player.class)), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        SkinProperty property = event.getProperty();
        return switch (name) {
            case "value" -> new ElementTag(property.getValue());
            case "signature" -> new ElementTag(property.getSignature());
            default -> super.getContext(name);
        };
    }
}
