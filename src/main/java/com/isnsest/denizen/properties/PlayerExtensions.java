package com.isnsest.denizen.properties;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.isnsest.denizen.containers.DialogScriptContainer;

public class PlayerExtensions {

    public static void register() {
        // <--[mechanism]
        // @object PlayerTag
        // @name show_dialog
        // @input ElementTag
        // @Plugin Paper
        // @group paper
        // @description
        // ...
        // -->
        PlayerTag.registerOnlineOnlyMechanism("show_dialog", ElementTag.class, (object, mechanism, input) -> {
            DialogScriptContainer container = ScriptRegistry.getScriptContainer(input.asString());
            if (container == null) {
                mechanism.echoError("Invalid dialog script: '" + input.asString() + "'");
                return;
            }
            container.showTo(object.getPlayerEntity(), mechanism.context);
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name close_dialog
        // @input None
        // @Plugin Paper
        // @group paper
        // @description
        // ...
        // -->
        PlayerTag.registerOnlineOnlyMechanism("close_dialog", (object, mechanism) -> {
            object.getPlayerEntity().closeDialog();
        });
    }
}
