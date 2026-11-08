package com.isnsest.denizenutilities;

import com.denizenscript.denizen.paper.utilities.FormattedTextHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CompatibilityImpl extends Compatibility {

    public enum DenizenType { DENIZENM }

    private static DenizenType type;

    public static void init() {
        try {
            Class.forName("com.denizenscript.denizen.paper.events.PlayerChangesUncheckedSignScriptEvent");
            type = DenizenType.DENIZENM;
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public Component parse(String text) {
        if (type == DenizenType.DENIZENM) {
            return FormattedTextHelper.parse(text, NamedTextColor.WHITE);
        }

        return defaultParse(text);
    }
}