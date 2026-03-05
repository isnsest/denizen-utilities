package com.isnsest.denizen.helpers;

import com.denizenscript.denizen.nms.v1_21.impl.BiomeNMSImpl;
import net.minecraft.world.attribute.EnvironmentAttributes;

public class BiomeHelper {

    public static int getSkyColor(BiomeNMSImpl nms) {
        return nms.getEnvironmentAttribute(EnvironmentAttributes.SKY_COLOR);
    }

    public static void setSkyColor(BiomeNMSImpl nms, int color) {
        nms.setEnvironmentAttribute(EnvironmentAttributes.SKY_COLOR, color);
    }

    public static int getSkyLightColor(BiomeNMSImpl nms) {
        return nms.getEnvironmentAttribute(EnvironmentAttributes.SKY_LIGHT_COLOR);
    }

    public static void setSkyLightColor(BiomeNMSImpl nms, int color) {
        nms.setEnvironmentAttribute(EnvironmentAttributes.SKY_LIGHT_COLOR, color);
    }
}
