package com.isnsest.denizenutilities.extensions.objects;

import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.google.common.net.HostAndPort;
import com.isnsest.denizenutilities.Compatibility;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptContainer;
import com.isnsest.denizenutilities.extensions.events.PlayerConnectionConfigureEvent;
import io.papermc.paper.connection.PlayerCommonConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class ConnectionTag implements ObjectTag, Adjustable {

    private final PlayerCommonConnection connection;
    private UUID uuid;

    public static Map<UUID, PlayerCommonConnection> activeConnections = new ConcurrentHashMap<>();

    public ConnectionTag(PlayerCommonConnection connection) {
        this.connection = connection;

        if (connection instanceof PlayerGameConnection gameConn) {
            this.uuid = gameConn.getPlayer().getUniqueId();
        } else if (connection instanceof PlayerConfigurationConnection configConn) {
            this.uuid = configConn.getProfile().getId();
        }
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
            var conn = activeConnections.get(uuid);
            if (conn != null) {
                return new ConnectionTag(conn);
            } else {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) return new ConnectionTag(player.getConnection());
            }
        }
        catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    public static boolean matches(String input) {
        return input != null && input.startsWith("connection@");
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String identify() {
        return "connection@" + uuid;
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
        return true;
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public static ObjectTagProcessor<ConnectionTag> tagProcessor = new ObjectTagProcessor<>();

    public static void register() {

        // <--[tag]
        // @attribute <ConnectionTag.is_connected>
        // @returns ElementTag(Boolean)
        // @plugin denizen-utilities
        // @description
        // Returns whether this connection is currently open and active.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_connected", (_, object) -> new ElementTag(object.connection.isConnected()));

        // <--[tag]
        // @attribute <ConnectionTag.uuid>
        // @returns ElementTag
        // @plugin denizen-utilities
        // @description
        // Returns the UUID of the player profile associated with this connection.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "uuid", (_, object) ->
                new ElementTag(object.uuid.toString()));

        // <--[tag]
        // @attribute <ConnectionTag.name>
        // @returns ElementTag
        // @plugin denizen-utilities
        // @description
        // Returns the name of the player profile associated with this connection.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "name", (_, object) -> {
            if (object.connection instanceof PlayerConfigurationConnection connection) {
                return new ElementTag(connection.getProfile().getName());
            } else if (object.connection instanceof PlayerGameConnection connection) {
                return new ElementTag(connection.getPlayer().getName());
            }
            return null;
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name disconnect
        // @plugin denizen-utilities
        // @input ElementTag
        // @description
        // Disconnects the connection with a specified reason. Supports Paper-formatted text (MiniMessage/Legacy).
        // -->
        tagProcessor.registerMechanism("disconnect", false, ElementTag.class, (object, _, input) ->
                object.connection.disconnect(Compatibility.get().parse(input.toString())));

        // <--[mechanism]
        // @object ConnectionTag
        // @name connect
        // @plugin denizen-utilities
        // @input None
        // @description
        // Confirms the connection and allows the player to continue the login process.
        // Use this to finish the configuration stage once your requirements are met.
        // -->
        tagProcessor.registerMechanism("connect", false, (object, _) -> {
            Map<UUID, CompletableFuture<Boolean>> list = PlayerConnectionConfigureEvent.awaitingResponse;
            if (list.containsKey(object.uuid)) {
                list.get(object.uuid).complete(true);
            }
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name reconfigure
        // @plugin denizen-utilities
        // @input None
        // @description
        // Completes the configuration for this player, which will cause this player to reenter the game.
        // Note: this should only be called if you are reconfiguring the player.
        // -->
        tagProcessor.registerMechanism("reconfigure", false, (object, _) -> {
            if (object.connection instanceof PlayerConfigurationConnection connection) {
                connection.completeReconfiguration();
            } else if (object.connection instanceof PlayerGameConnection connection) {
                connection.reenterConfiguration();
            }
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name close_dialog
        // @plugin denizen-utilities
        // @input None
        // @description
        // Closes any currently open dialog for this connection.
        // -->
        tagProcessor.registerMechanism("close_dialog", false, (object, _) -> {
            if (object.connection instanceof PlayerConfigurationConnection connection) {
                connection.getAudience().closeDialog();
            } else if (object.connection instanceof PlayerGameConnection connection) {
                connection.getPlayer().closeDialog();
            }
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name show_dialog
        // @plugin denizen-utilities
        // @input ElementTag
        // @description
        // Shows a specific dialog to the connection using the name of a Dialog script container.
        // -->
        tagProcessor.registerMechanism("show_dialog", false, ScriptTag.class, (object, mechanism, input) -> {
            if (input.getContainer() instanceof DialogScriptContainer container) {
                container.showTo(object.connection, mechanism.context);
                return;
            }
            mechanism.echoError("Invalid script '" + input.getName()
                    + "' for mechanism 'show_dialog': must be a dialog script container.");
        });

        // <--[mechanism]
        // @object ConnectionTag
        // @name transfer
        // @plugin denizen-utilities
        // @input ElementTag
        // @description
        // Natively transfers the client to another Minecraft server (host or host:port).
        // Examples:
        // - adjust <connection> transfer:play.example.com
        // - adjust <connection> transfer:play.example.com:25570
        // -->
        tagProcessor.registerMechanism("transfer", false, ElementTag.class, (object, _, input) -> {
            HostAndPort target = HostAndPort
                    .fromString(input.asString().trim())
                    .withDefaultPort(25565);
            object.connection.transfer(target.getHost(), target.getPort());
        });
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a connection!");
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

}