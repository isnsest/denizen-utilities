package com.isnsest.denizenutilities.extensions.events;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.JavaReflectedObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptContainer;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptHelper;
import com.isnsest.denizenutilities.extensions.objects.ConnectionTag;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

import java.util.List;


@SuppressWarnings("UnstableApiUsage")
public class PlayerCustomClickScriptEvent extends ScriptEvent implements Listener {

    public static PlayerCustomClickScriptEvent instance;

    public PlayerCustomClickEvent event;
    public DialogScriptHelper.DialogData dialogData;
    public String buttonId;

    private record ButtonPathInfo(String scriptName, String buttonPath, String buttonId) {}

    public PlayerCustomClickScriptEvent() {
        instance = this;
        registerCouldMatcher("player custom click");
        registerSwitches("button_id", "namespace");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "button_id", buttonId)) {
            return false;
        }
        if (event == null || !runGenericSwitchCheck(path, "namespace", event.getIdentifier().namespace())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "connection" -> new ConnectionTag(event.getCommonConnection());
            case "inputs" -> getInputs(dialogData, event.getDialogResponseView());
            case "reflect_event" -> new JavaReflectedObjectTag(event);
            case "namespace" -> new ElementTag(event.getIdentifier().namespace());
            case "button_id" -> new ElementTag(buttonId);
            default -> {
                ObjectTag value = getResponseValue(dialogData, event.getDialogResponseView(), name);
                if (value != null) {
                    yield value;
                }

                yield super.getContext(name);
            }
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        if (event != null && event.getCommonConnection() instanceof PlayerGameConnection gameConnection) {
            return new BukkitScriptEntryData(gameConnection.getPlayer());
        }
        return super.getScriptEntryData();
    }

    public static class DialogEvents implements Listener {
        @EventHandler
        public void onDialogClick(PlayerCustomClickEvent event) {
            ButtonPathInfo pathInfo = parseButtonKey(event.getIdentifier());

            var dialogData = DialogScriptHelper.get(event.getCommonConnection());

            instance.event = event;
            instance.dialogData = dialogData;
            instance.buttonId = pathInfo.buttonId();
            instance.fire();

            if (pathInfo.scriptName() == null) {
                return;
            }

            DialogScriptContainer container = ScriptRegistry.getScriptContainerAs(pathInfo.scriptName(), DialogScriptContainer.class);
            YamlConfiguration buttonSection = getButtonSection(container, dialogData, pathInfo.buttonPath());
            if (buttonSection == null) {
                return;
            }

            BukkitScriptEntryData entryData;
            if (event.getCommonConnection() instanceof PlayerGameConnection gameConnection) {
                entryData = new BukkitScriptEntryData(gameConnection.getPlayer());
            } else {
                entryData = new BukkitScriptEntryData(null, null);
            }

            List<ScriptEntry> entries = container.getEntries(buttonSection, entryData, "script");
            if (entries != null && !entries.isEmpty()) {
                InstantQueue queue = new InstantQueue(instance.getName());
                queue.addEntries(entries);
                queue.setContextSource(name -> switch (name) {
                    case "connection" -> new ConnectionTag(event.getCommonConnection());
                    case "button_id" -> new ElementTag(pathInfo.buttonId());
                    case "inputs" -> getInputs(dialogData, event.getDialogResponseView());
                    case "namespace" -> new ElementTag(event.getIdentifier().namespace());
                    default -> {
                        ObjectTag value = getResponseValue(dialogData, event.getDialogResponseView(), name);
                        if (value != null) {
                            yield value;
                        }
                        yield instance.getContext(name);
                    }
                });
                queue.start(true);
            }
        }
    }

    private static ObjectTag getResponseValue(DialogScriptHelper.DialogData dialogData, DialogResponseView responseView, String name) {
        if (responseView == null) return null;

        if (dialogData != null && dialogData.getInputs() != null) {
            DialogScriptHelper.InputType type = dialogData.getInputs().get(name);
            if (type != null) {
                return switch (type) {
                    case TEXT, SINGLE -> new ElementTag(responseView.getText(name));
                    case BOOLEAN -> new ElementTag(Boolean.TRUE.equals(responseView.getBoolean(name)));
                    case NUMBER -> {
                        Float v = responseView.getFloat(name);
                        yield v != null ? new ElementTag(v) : null;
                    }
                };
            }
        }

        Float f = responseView.getFloat(name);
        if (f != null) return new ElementTag(f);

        Boolean bool = responseView.getBoolean(name);
        if (bool != null) return new ElementTag(bool);

        String string = responseView.getText(name);
        if (string != null) return new ElementTag(string);

        return null;
    }

    private static ButtonPathInfo parseButtonKey(Key key) {
        String value = key.value();

        if (!key.namespace().equals("denizen") || !value.contains("/")) {
            int lastDot = value.lastIndexOf('.');
            String buttonId = lastDot >= 0 ? value.substring(lastDot + 1) : value;
            return new ButtonPathInfo(null, value, buttonId);
        }

        int slashIdx = value.indexOf('/');
        String scriptName = value.substring(0, slashIdx);
        String buttonPath = value.substring(slashIdx + 1);

        int lastDot = buttonPath.lastIndexOf('.');
        String buttonId = lastDot >= 0 ? buttonPath.substring(lastDot + 1) : buttonPath;

        return new ButtonPathInfo(scriptName, buttonPath, buttonId);
    }

    private static YamlConfiguration getButtonSection(DialogScriptContainer container, DialogScriptHelper.DialogData dialogData, String buttonPath) {
        if (dialogData == null) {
            return container == null ? null : container.getConfigurationSection(buttonPath);
        }

        int firstDot = buttonPath.indexOf('.');
        if (firstDot >= 0) {
            String sectionName = buttonPath.substring(0, firstDot);
            String subPath = buttonPath.substring(firstDot + 1);
            YamlConfiguration parentSection = dialogData.getConfigurationMap().get(sectionName);
            if (parentSection != null) {
                return parentSection.getConfigurationSection(subPath);
            }
        } else {
            YamlConfiguration section = dialogData.getConfigurationMap().get(buttonPath);
            if (section != null) {
                return section;
            }
            return container.getConfigurationSection(buttonPath);
        }
        return null;
    }

    private static MapTag getInputs(DialogScriptHelper.DialogData dialogData, DialogResponseView responseView) {
        MapTag inputs = new MapTag();
        if (dialogData == null || dialogData.getInputs() == null || responseView == null) {
            return inputs;
        }
        for (String key : dialogData.getInputs().keySet()) {
            ObjectTag val = getResponseValue(dialogData, responseView, key);
            if (val != null) {
                inputs.putObject(key, val);
            }
        }
        return inputs;
    }
}