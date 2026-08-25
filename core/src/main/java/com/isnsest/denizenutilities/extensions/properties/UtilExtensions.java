package com.isnsest.denizenutilities.extensions.properties;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.core.UtilTagBase;
import com.isnsest.denizenutilities.packetevents.PacketVersionHelper;

public class UtilExtensions {

    public static void register() {
        // <--[tag]
        // @attribute <util.client_version_by_address[<address>]>
        // @returns ElementTag
        // @plugin denizen-utilities
        // @description
        // Returns the raw client release version name (e.g., "1.21.1" or "26.2") for the specified IP address.
        // Append '.protocol' (as in <util.client_version_by_address[<address>].protocol>) to return the numeric protocol version ID instead.
        //
        // NOTE: This tag is specifically designed for the 'on server list ping' event when using protocol translation plugins like ViaVersion.
        // ViaVersion rewrites the incoming packet version to match the server core version, allowing the server to accept the connection.
        // As a result, '<context.client_protocol_version>' in Paper ends up reporting the server core's version instead of the client's actual version when a newer client pings the server:
        // For example, if the server core version is 1.21.11 and a player pings from version 1.21.7, Paper correctly reports 1.21.7;
        // but if a player pings from a newer version (e.g., 26.2 via ViaVersion), Paper will cap the value and, due to ViaVersion's rewrite, incorrectly report the server's own version (1.21.11).
        // This tag determines the actual client version directly from the connection session before ViaVersion's packet translation occurs.
        // -->
        UtilTagBase.instance.tagProcessor.registerTag(ElementTag.class, ElementTag.class, "client_version_by_address", (attribute, _, input) ->
                PacketVersionHelper.format(attribute, PacketVersionHelper.getByAddress(input.asString())));
    }
}