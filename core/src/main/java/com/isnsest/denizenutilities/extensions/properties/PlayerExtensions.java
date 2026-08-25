package com.isnsest.denizenutilities.extensions.properties;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptContainer;
import com.isnsest.denizenutilities.extensions.objects.ConnectionTag;
import com.isnsest.denizenutilities.packetevents.PacketVersionHelper;

public class PlayerExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.connection>
        // @returns ConnectionTag
        // @plugin denizen-utilities
        // @description
        // Returns the active ConnectionTag associated with this online player.
        // @example
        // # Natively transfers the player to another server
        // - adjust <player.connection> transfer:play.example.com:25565
        // -->
        PlayerTag.registerOnlineOnlyTag(ConnectionTag.class, "connection", (_, object) ->
                new ConnectionTag(object.getPlayerEntity().getConnection()));

        // <--[tag]
        // @attribute <PlayerTag.version>
        // @returns ElementTag
        // @plugin denizen-utilities
        // @description
        // Returns the client release version name (such as "1.21.1" or "26.2") of this online player.
        // Append '.protocol' (as in <PlayerTag.version.protocol>) to return the numeric protocol version ID instead (e.g. 767).
        // @example
        // # Displays the player's client release name
        // - narrate "Your client version is: <player.version>"
        // @example
        // # Checks the numeric protocol version ID
        // - if <player.version.protocol> >= 767:
        //     - narrate "You are using a modern client!"
        // -->
        PlayerTag.tagProcessor.registerTag(ElementTag.class, "version", (attribute, object) ->
                PacketVersionHelper.format(attribute, PacketVersionHelper.getByUUID(object.getPlayerEntity().getUniqueId())));

        // <--[mechanism]
        // @object PlayerTag
        // @name show_dialog
        // @input ElementTag
        // @plugin denizen-utilities, Paper
        // @description
        // Opens a dialog UI for the player using the specified dialog script.
        // -->
        PlayerTag.registerOnlineOnlyMechanism("show_dialog", ElementTag.class, (object, mechanism, input) -> {
            BukkitTagContext context = (BukkitTagContext) mechanism.context;
            context.player = new PlayerTag(object.getPlayerEntity());
            DialogScriptContainer container = ScriptRegistry.getScriptContainer(input.asString());
            if (container == null) {
                mechanism.echoError("Invalid dialog script: '" + input.asString() + "'");
                return;
            }
            container.showTo(object.getPlayerEntity().getConnection(), context);
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name close_dialog
        // @input None
        // @plugin denizen-utilities, Paper
        // @description
        // Closes the player's current dialog UI.
        // -->
        PlayerTag.registerOnlineOnlyMechanism("close_dialog", (object, _) -> {
            object.getPlayerEntity().closeDialog();
        });
    }
}
