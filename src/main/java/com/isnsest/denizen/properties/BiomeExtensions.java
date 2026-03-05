package com.isnsest.denizen.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.v1_21.impl.BiomeNMSImpl;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.isnsest.denizen.helpers.BiomeHelper;

import static com.denizenscript.denizen.objects.BiomeTag.tagProcessor;

public class BiomeExtensions {

    public static void register() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {

            // <--[tag]
            // @attribute <BiomeTag.sky_color>
            // @returns ColorTag
            // @mechanism BiomeTag.sky_color
            // @description
            // Returns the biome's sky color.
            // @example
            // # Sends the player a message in their current biome's sky color.
            // - narrate "You are currently seeing sky that looks like <&color[<player.location.biome.sky_color>]>this!"
            // -->
            tagProcessor.registerTag(ColorTag.class, "sky_color", (attribute, object) -> {
                BiomeNMSImpl biome = (BiomeNMSImpl) object.getBiome();
                return ColorTag.fromRGB(BiomeHelper.getSkyColor(biome));
            });

            // <--[tag]
            // @attribute <BiomeTag.sky_light_color>
            // @returns ColorTag
            // @mechanism BiomeTag.sky_light_color
            // @description
            // Returns the biome's skylight color.
            // @example
            // # Sends the player a message in their current biome's skylight color.
            // - narrate "You are currently seeing skylight that looks like <&color[<player.location.biome.sky_light_color>]>this!"
            // -->
            tagProcessor.registerTag(ColorTag.class, "sky_light_color", (attribute, object) -> {
                BiomeNMSImpl biome = (BiomeNMSImpl) object.getBiome();
                return ColorTag.fromRGB(BiomeHelper.getSkyLightColor(biome));
            });

            // <--[mechanism]
            // @object BiomeTag
            // @name sky_color
            // @input ColorTag
            // @description
            // Sets the biome's sky color.
            // @tags
            // <BiomeTag.sky_color>
            // @example
            // # Makes the plains biome's sky color red permanently, using a server start event to keep it applied.
            // on server start:
            // - adjust <biome[plains]> sky_color:red
            // -->
            tagProcessor.registerMechanism("sky_color", false, ColorTag.class, (object, mechanism, input) -> {
                BiomeNMSImpl biome = (BiomeNMSImpl) object.getBiome();
                BiomeHelper.setSkyColor(biome, input.asRGB());
            });

            // <--[mechanism]
            // @object BiomeTag
            // @name sky_light_color
            // @input ColorTag
            // @description
            // Sets the biome's skylight color.
            // @tags
            // <BiomeTag.sky_light_color>
            // @example
            // # Makes the plains biome's skylight color red permanently, using a server start event to keep it applied.
            // on server start:
            // - adjust <biome[plains]> sky_light_color:red
            // -->
            tagProcessor.registerMechanism("sky_light_color", false, ColorTag.class, (object, mechanism, input) -> {
                BiomeNMSImpl biome = (BiomeNMSImpl) object.getBiome();
                BiomeHelper.setSkyLightColor(biome, input.asRGB());
            });
        }
    }
}
