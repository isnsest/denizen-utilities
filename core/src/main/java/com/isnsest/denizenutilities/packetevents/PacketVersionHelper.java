package com.isnsest.denizenutilities.packetevents;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketVersionHelper {

    private static final Map<String, ClientVersion> addressCache = new ConcurrentHashMap<>();

    public static void cacheAddress(SocketAddress address, ClientVersion version) {
        if (address == null || version == null) return;

        addressCache.put(cleanAddress(address.toString()), version);

        if (address instanceof InetSocketAddress inet) {
            if (inet.getAddress() != null) {
                addressCache.put(inet.getAddress().getHostAddress(), version);
            }
            addressCache.put(inet.getHostString(), version);
        }
    }

    public static void uncacheAddress(SocketAddress address) {
        if (address == null) return;

        addressCache.remove(cleanAddress(address.toString()));

        if (address instanceof InetSocketAddress inet) {
            if (inet.getAddress() != null) {
                addressCache.remove(inet.getAddress().getHostAddress());
            }
            addressCache.remove(inet.getHostString());
        }
    }

    public static ClientVersion getByAddress(String rawInput) {
        if (rawInput == null || rawInput.isEmpty()) return null;

        String clean = cleanAddress(rawInput);

        ClientVersion version = addressCache.get(clean);
        if (version != null) return version;

        int colon = clean.lastIndexOf(':');
        if (colon > 0) {
            return addressCache.get(clean.substring(0, colon));
        }

        return null;
    }

    public static ClientVersion getByUUID(UUID uuid) {
        if (uuid == null) return null;
        ProtocolManager pm = PacketEvents.getAPI().getProtocolManager();
        Object channel = pm.getChannel(uuid);
        User user = channel != null ? pm.getUser(channel) : null;
        return user != null ? user.getClientVersion() : null;
    }

    public static ElementTag format(Attribute attribute, ClientVersion version) {
        if (version == null) return null;
        if (attribute.startsWith("protocol", 2)) {
            attribute.fulfill(1);
            return new ElementTag(version.getProtocolVersion());
        }
        return new ElementTag(version.getReleaseName());
    }

    private static String cleanAddress(String str) {
        return str.startsWith("/") ? str.substring(1) : str;
    }
}