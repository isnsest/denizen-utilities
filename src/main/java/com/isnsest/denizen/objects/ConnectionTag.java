package com.isnsest.denizen.objects;

import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.isnsest.denizen.containers.DialogScriptContainer;
import com.isnsest.denizen.events.PlayerConnectionConfigureEvent;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionTag implements ObjectTag, Adjustable {

    private final PlayerConfigurationConnection connection;

    public static Map<UUID, PlayerConfigurationConnection> activeConnections = new ConcurrentHashMap<>();

    public ConnectionTag(PlayerConfigurationConnection connection) {
        this.connection = connection;
    }

    @Fetchable("connection")
    public static ConnectionTag valueOf(String string, TagContext context) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        if (string.startsWith("connection@")) {
            string = string.substring("connection@".length());
        }

        try {
            UUID uuid = UUID.fromString(string);
            PlayerConfigurationConnection conn = activeConnections.get(uuid);
            if (conn != null) {
                return new ConnectionTag(conn);
            }
        }
        catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("connection@")) {
            return true;
        }
        return true;
    }

    @Override
    public String identify() {
        return "connection@" + connection.getProfile().getId();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    private String prefix = "Connection";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static ObjectTagProcessor<ConnectionTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {

        // <--[tag]
        // @attribute <ConnectionTag.uuid>
        // @returns ElementTag
        // @description
        // Returns the UUID of the player profile associated with this connection.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "uuid", ((attribute, object) -> {
            UUID uuid = object.connection.getProfile().getId();
            if (uuid != null) {
                return new ElementTag(uuid.toString());
            }
            return null;
        }));

        // <--[tag]
        // @attribute <ConnectionTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the player profile associated with this connection.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "name", ((attribute, object) -> {
            return new ElementTag(object.connection.getProfile().getName());
        }));

        // <--[mechanism]
        // @object ConnectionTag
        // @name disconnect
        // @input ElementTag
        // @description
        // Disconnects the connection with a specified reason. Supports Paper-formatted text (MiniMessage/Legacy).
        // -->
        tagProcessor.registerMechanism("disconnect", false, ElementTag.class, (object, mechanism, input) -> {
            object.connection.disconnect(PaperModule.parseFormattedText(input.toString(), ChatColor.WHITE));
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name connect
        // @input None
        // @description
        // Confirms the connection and allows the player to continue the login process.
        // Use this to finish the configuration stage once your requirements are met.
        // -->
        tagProcessor.registerMechanism("connect", false, (object, mechanism) -> {
            UUID uuid = object.connection.getProfile().getId();
            Map<UUID, CompletableFuture<Boolean>> list = PlayerConnectionConfigureEvent.instance.awaitingResponse;
            if (list.containsKey(uuid)) {
                list.get(uuid).complete(true);
            }
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name close_dialog
        // @input None
        // @description
        // Closes any currently open dialog for this connection.
        // -->
        tagProcessor.registerMechanism("close_dialog", false, (object, mechanism) -> {
            object.connection.getAudience().closeDialog();
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name show_dialog
        // @input ElementTag
        // @description
        // Shows a specific dialog to the connection using the name of a Dialog script container.
        // -->
        tagProcessor.registerMechanism("show_dialog", false, ElementTag.class, (object, mechanism, input) -> {
            TagContext context = new BukkitTagContext(null, null, null);
            DialogScriptContainer container = ScriptRegistry.getScriptContainer(input.toString());
            Dialog dialog = container.getDialogFrom(context);
            if (dialog == null) {
                Debug.log("Dialog is null");
                return;
            }
            object.connection.getAudience().showDialog(dialog);
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

}