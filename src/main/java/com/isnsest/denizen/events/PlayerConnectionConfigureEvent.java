package com.isnsest.denizen.events;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.isnsest.denizen.DenizenUtilities;
import com.isnsest.denizen.objects.ConnectionTag;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerConnectionConfigureEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player connection configure
    //
    // @Group Player
    //
    // @Triggers when a player's connection is being configured (Paper specific).
    //
    // @Context
    // <context.connection> returns the ConnectionTag.
    //
    // @Determine
    // "WAIT" to delay the configuration process for up to 1 minute.
    //
    // -->

    public static PlayerConnectionConfigureEvent instance;

    public final Map<UUID, CompletableFuture<Boolean>> awaitingResponse = new ConcurrentHashMap<>();

    public String determination = null;
    public ConnectionTag connection;

    public PlayerConnectionConfigureEvent() {
        instance = this;
        registerCouldMatcher("player connection configure");
        this.<PlayerConnectionConfigureEvent, ObjectTag>registerDetermination(null, ObjectTag.class, (evt, context, output) -> {
            determination = output.toString();
        });
    }

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, DenizenUtilities.instance);
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public ObjectTag getContext(String n) {
        if (n.equals("connection")) {
            return connection;
        }
        return super.getContext(n);
    }

    @EventHandler
    public void onPlayerConfigure(AsyncPlayerConnectionConfigureEvent event) {
        PlayerConfigurationConnection connection = event.getConnection();
        UUID uniqueId = connection.getProfile().getId();

        ConnectionTag.activeConnections.put(event.getConnection().getProfile().getId(), event.getConnection());
        instance.connection = new ConnectionTag(event.getConnection());
        instance.fire();

        if (instance.determination != null && CoreUtilities.toLowerCase(instance.determination).equals("wait")) {
            CompletableFuture<Boolean> response = new CompletableFuture<>();
            response.completeOnTimeout(false, 1, TimeUnit.MINUTES);

            awaitingResponse.put(uniqueId, response);

            Audience audience = connection.getAudience();

            if (!response.join()) {
                audience.closeDialog();
                connection.disconnect(Component.empty());
            }

            awaitingResponse.remove(uniqueId);
        }
    }
}
