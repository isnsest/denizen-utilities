package com.isnsest.denizenutilities.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("UnstableApiUsage")
public class PacketEventsManager {

    public static void load(Plugin plugin) {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    public static void init() {
        PacketEvents.getAPI().init();

        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListener() {
                    @Override
                    public void onPacketReceive(PacketReceiveEvent event) {
                        if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
                            PacketVersionHelper.cacheAddress(event.getSocketAddress(), event.getUser().getClientVersion());
                        }
                    }

                    @Override
                    public void onUserDisconnect(UserDisconnectEvent event) {
                        PacketVersionHelper.uncacheAddress(event.getUser().getAddress());
                    }
                },
                PacketListenerPriority.NORMAL
        );
    }

    public static void terminate() {
        PacketEvents.getAPI().terminate();
    }
}