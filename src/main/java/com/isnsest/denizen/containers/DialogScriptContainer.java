package com.isnsest.denizen.containers;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.isnsest.denizen.objects.ConnectionTag;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.*;
import io.papermc.paper.registry.data.dialog.type.*;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class DialogScriptContainer extends ScriptContainer {

    private static final ClickCallback.Options options = ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build();

    private final Map<String, InputType> keys = new HashMap<>();

    private enum InputType {
        TEXT, SINGLE, BOOLEAN, NUMBER;
    }

    // <--[language]
    // @name Dialog Script Containers
    // @group Script Container System
    // @description
    // Dialog script containers define custom Paper UI windows.
    //
    // <code>
    // Dialog_Script_Name:
    //     type: dialog
    //
    //     # General Window Settings
    //     base:
    //         title: <yellow>Main Menu # (Required)
    //         type: multi # (Required) confirm, list, notice, multi
    //         external title: App Title
    //         can close with escape: true
    //         columns: 3 # Layout for 'list' or 'multi'
    //         button width: 100
    //         exit button: # ActionButton format
    //             label: Exit
    //
    //     # For 'list' type only (Root level!)
    //     dialogs:
    //     - other_dialog_script
    //
    //     # Static content (Root level!)
    //     bodies:
    //         my_text:
    //             type: message
    //             message: Hello!
    //
    //     # Data collection (Root level!)
    //     inputs:
    //         my_input:
    //             type: text
    //             key: custom_context_name
    //             label: Name
    //
    //     # Interaction (Root level!)
    //     # 'multi' uses 'buttons:', 'confirm' uses 'yes:'/'no:', 'notice' uses 'button:'
    //     buttons:
    //         action_key:
    //             label: Submit
    //             script:
    //             - narrate "Context is <context.custom_context_name>"
    // </code>
    // -->

    // <--[language]
    // @name Dialog Inputs
    // @group Script Container System
    // @description
    // Collects player data. Values are retrieved via <context.[key_name]>.
    //
    // TYPES:
    //
    // TEXT:
    // - type: text
    // - label: <text> (Required)
    // - initial: <text>
    // - width: <number>
    // - max length: <number>
    // - label visible: true/false
    // - multiline options:
    //     max lines: <number>
    //     height: <number>
    //
    // BOOLEAN:
    // - type: boolean
    // - label: <text> (Required)
    // - initial: true/false
    // - on true: <label>
    // - on false: <label>
    //
    // NUMBER:
    // - type: number
    // - label: <text> (Required)
    // - start: <float> (Required)
    // - end: <float> (Required)
    // - initial: <float>
    // - step: <float>
    // - width: <number>
    // - label format: <text> (e.g. "Value: %s")
    //
    // SINGLE:
    // - type: single
    // - label: <text> (Required)
    // - label visible: true/false
    // - width: <number>
    // - options:
    //     opt_1:
    //         id: unique_id
    //         display: <text>
    //         initial: true/false
    // -->

    // <--[language]
    // @name Dialog Buttons
    // @group Script Container System
    // @description
    // Buttons trigger actions. Available as 'yes'/'no' (confirm), 'button' (notice), or 'bффффффффффффuttons' (multi).
    // Fields:
    // - label: <text> (Required)
    // - tooltip: <text>
    // - width: <number>
    // - type: SCRIPT, RUN_COMMAND, OPEN_URL, COPY_TO_CLIPBOARD
    //
    // ACTION TYPES:
    // - SCRIPT: Runs 'script:' block. Has <context.connection> and <context.[input_keys]>.
    // - RUN_COMMAND: Runs 'command:'.
    // - OPEN_URL: Opens 'url:'.
    // - COPY_TO_CLIPBOARD: Copies 'text:'.
    // -->

    // <--[language]
    // @name Dialog Bodies
    // @group Script Container System
    // @description
    // Static content displayed in the dialog window.
    //
    // MESSAGE:
    // - type: message
    // - message: <text> (Required)
    // - width: <number>
    //
    // ITEM:
    // - type: item
    // - item: <ItemTag> (Required)
    // - width/height: <number>
    // - show tooltip: true/false
    // - show decorations: true/false
    // - description: (A nested 'message' type body)
    // -->

    public DialogScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        parseInputs(configurationSection.getConfigurationSection("inputs"));
    }

    @Override
    public void postCheck() {
        super.postCheck();
        // Trigger load + cache
        if (shouldEnable() && contains("script")) {
            getBaseEntries(DenizenCore.implementation.getEmptyScriptEntryData());
        }
    }

    private void parseInputs(YamlConfiguration inputs) {
        if (inputs == null) return;
        for (StringHolder sh : inputs.getKeys(false)) {
            parseInput(inputs.getConfigurationSection(sh.str), sh.str);
        }
    }

    private void parseInput(YamlConfiguration section, String keyName) {
        if (section == null) return;
        String key = section.getString("key", keyName);
        String typeStr = section.getString("type", null);
        if (typeStr == null) return;

        InputType inputType = switch (CoreUtilities.toLowerCase(typeStr)) {
            case "text" -> InputType.TEXT;
            case "single" -> InputType.SINGLE;
            case "boolean", "bool" -> InputType.BOOLEAN;
            case "number" -> InputType.NUMBER;
            default -> {
                Debug.echoError("Dialog script '" + getName() + "' has unknown input type: " + typeStr);
                yield null;
            }
        };

        if (inputType != null) {
            keys.put(key, inputType);
        }
    }

    public Dialog getDialogFrom(TagContext context) {
        if (context == null) {
            context = new BukkitTagContext(null, null, new ScriptTag(this));
        }

        DialogBase dialogBase = getDialogBase(context);
        if (dialogBase == null) {
            return null;
        }

        String type = getString("base.type");
        if (type == null) {
            Debug.echoError("Dialog script '" + getName() + "' is missing a required 'base.type'!");
            return null;
        }

        switch (type) {
            case "confirm" -> {
                ActionButton yes = createActionButton("yes", context);
                ActionButton no = createActionButton("no", context);
                if (yes == null || no == null) {
                    return null;
                }
                ConfirmationType confirmationType = DialogType.confirmation(yes, no);
                return Dialog.create(builder -> builder.empty()
                        .base(dialogBase)
                        .type(confirmationType));
            }
            case "list" -> {
                if (!containsScriptSection("dialogs")) {
                    Debug.echoError("Dialog script '" + getName() + "'  is missing a required 'dialogs'");
                    return null;
                }
                YamlConfiguration config = getConfigurationSection("base");
                Integer columns = getInt(config, "columns", context);
                Integer buttonWidth = getInt(config, "button width", context);


                List<Dialog> dialogs = new ArrayList<>();
                for (String id : getStringList("dialogs")) {
                    DialogScriptContainer container = ScriptRegistry.getScriptContainer(id);
                    if (container == null) {
                        Debug.echoError("Invalid dialog script: '" + id + "'");
                        continue;
                    }
                    Dialog dialog = container.getDialogFrom(context);
                    if (dialog == null) {
                        Debug.echoError("Failed to construct dialog script '" + id + "' inside list dialog '" + getName() + "'");
                        continue;
                    }
                    dialogs.add(dialog);
                }
                RegistrySet<@NotNull Dialog> registrySet = RegistrySet.valueSet(RegistryKey.DIALOG, dialogs);
                DialogListType.Builder dialogList = DialogType.dialogList(registrySet);
                if (columns != null) {
                    dialogList.columns(columns);
                }
                if (buttonWidth != null) {
                    dialogList.buttonWidth(buttonWidth);
                }
                return Dialog.create(builder -> builder.empty()
                        .base(dialogBase)
                        .type(dialogList.build()));

            }
            case "notice" -> {
                ActionButton actionButton = createActionButton("button", context);
                NoticeType noticeType;
                if (actionButton == null) {
                    noticeType = DialogType.notice();
                } else {
                    noticeType = DialogType.notice(actionButton);
                }
                return Dialog.create(builder -> builder.empty()
                        .base(dialogBase)
                        .type(noticeType));
            }
            case "multi" -> {
                if (!contains("buttons")) {
                    Debug.echoError("Dialog script '" + getName() + "' is missing a required 'buttons'");
                    return null;
                }
                Integer columns = getInt(getContents(), "base.columns", context);
                ActionButton exitButton = createActionButton("base.exit button", context);
                List<ActionButton> actionButtons = createActionButtons(context);
                MultiActionType.Builder multiActionType = DialogType.multiAction(actionButtons);
                if (columns != null) {
                    multiActionType.columns(columns);
                }
                if (exitButton != null) {
                    multiActionType.exitAction(exitButton);
                }
                return Dialog.create(builder -> builder.empty()
                        .base(dialogBase)
                        .type(multiActionType.build()));
            }
            default -> {
                Debug.echoError("Unknown input type: " + type);
                return null;
            }
        }
    }

    public void showTo(Player player, TagContext context) {
        Dialog dialog = getDialogFrom(context);
        if (dialog == null) {
            Debug.echoError("Failed to show dialog.");
            return;
        }
        player.showDialog(dialog);
    }

    private DialogBase getDialogBase(TagContext context) {
        Component title = getComponent(getContents(), "base.title", context);
        if (title == null) {
            Debug.echoError("Dialog script '" + getName() + "' is missing a required 'base.title'!");
            return null;
        }
        DialogBase.Builder baseBuilder = DialogBase.builder(title);

        Component externalTitle = getComponent(getContents(), "base.external title", context);
        Boolean canCloseWithEscape = getBool(getContents(), "base.can close with escape", context, true);

        baseBuilder.canCloseWithEscape(canCloseWithEscape);
        baseBuilder.externalTitle(externalTitle);

        inputs(baseBuilder, context);
        bodies(baseBuilder, context);

        return baseBuilder.build();
    }

    private void bodies(DialogBase.Builder baseBuilder, TagContext context) {
        YamlConfiguration bodiesSection = getConfigurationSection("bodies");
        if (bodiesSection == null) return;

        List<DialogBody> bodies = new ArrayList<>();
        for (StringHolder sh : bodiesSection.getKeys(false)) {
            YamlConfiguration objectSection = bodiesSection.getConfigurationSection(sh.str);
            if (objectSection == null) continue;

            if (!objectSection.contains("type")) {
                Debug.echoError("Dialog script '" + getName() + "' has an object without a specified type!");
                continue;
            }

            String type = CoreUtilities.toLowerCase(objectSection.getString("type"));
            switch (type) {
                case "message" -> {
                    PlainMessageDialogBody plainMessageDialogBody = createPlainMessageDialogBody(objectSection, context);
                    if (plainMessageDialogBody != null) {
                        bodies.add(plainMessageDialogBody);
                    }
                }
                case "item" -> {
                    if (!objectSection.contains("item")) {
                        Debug.echoError("Dialog script '" + getName() + "' has an object without a specified item!");
                        continue;
                    }
                    String raw = getString(objectSection, "item", context);
                    ItemTag itemTag = ItemTag.valueOf(raw, context);
                    if (itemTag == null) {
                        Debug.echoError(raw + " is not valid ItemTag.");
                        continue;
                    }
                    ItemStack itemStack = itemTag.getItemStack();
                    Integer width = getInt(objectSection, "width", context);
                    Integer height = getInt(objectSection, "height", context);
                    Boolean showTooltip = getBool(objectSection, "show tooltip", context, null);
                    Boolean showDecorations = getBool(objectSection, "show decorations", context, null);
                    YamlConfiguration description = objectSection.getConfigurationSection("description");

                    ItemDialogBody.Builder builder = DialogBody.item(itemStack);
                    if (width != null) {
                        builder.width(width);
                    }
                    if (height != null) {
                        builder.height(height);
                    }
                    if (showTooltip != null) {
                        builder.showTooltip(showTooltip);
                    }
                    if (showDecorations != null) {
                        builder.showDecorations(showDecorations);
                    }
                    if (description != null) {
                        PlainMessageDialogBody plainMessageDialogBody = createPlainMessageDialogBody(description, context);
                        if (plainMessageDialogBody != null) {
                            builder.description(plainMessageDialogBody);
                        }
                    }
                    bodies.add(builder.build());
                }
            }
        }
        baseBuilder.body(bodies);
    }

    private void inputs(DialogBase.Builder baseBuilder, TagContext context) {
        YamlConfiguration inputsSection = getConfigurationSection("inputs");
        if (inputsSection == null) return;

        List<DialogInput> inputs = new ArrayList<>();
        for (StringHolder sh : inputsSection.getKeys(false)) {
            YamlConfiguration objectSection = inputsSection.getConfigurationSection(sh.str);
            if (objectSection == null) continue;
            if (!objectSection.contains("type")) {
                Debug.echoError("Dialog script '" + getName() + "' has an object without a specified type!");
                continue;
            }
            if (!objectSection.contains("label")) {
                Debug.echoError("Dialog script '" + getName() + "' is missing a required 'label' for input '" + sh.str + "'.");
                continue;
            }

            String key = objectSection.getString("key", sh.str);
            InputType type = keys.get(key);
            Component label = getComponent(objectSection, "label", context);

            DialogInput input = switch (type) {
                case TEXT -> createTextInput(key, label, objectSection, context);
                case BOOLEAN -> createBooleanInput(key, label, objectSection);
                case NUMBER -> createNumberRangeInput(key, label, objectSection, context);
                case SINGLE -> createSingleOptionInput(key, label, objectSection, context);
            };

            if (input != null) {
                inputs.add(input);
            }
        }
        baseBuilder.inputs(inputs);
    }

    private PlainMessageDialogBody createPlainMessageDialogBody(YamlConfiguration section, TagContext context) {
        Component message = getComponent(section, "message", context);
        if (message == null) {
            Debug.echoError("Dialog script '" + getName() + "' has an object without a specified message!");
            return null;
        }
        Integer width = getInt(section, "width", context);
        if (width != null) {
            return DialogBody.plainMessage(message, width);
        }
        return DialogBody.plainMessage(message);
    }

    private List<ActionButton> createActionButtons(TagContext context) {
        YamlConfiguration section = getConfigurationSection("buttons");
        List<ActionButton> actionButtons = new ArrayList<>();
        for (StringHolder objectKey : section.getKeys(false)) {
            ActionButton actionButton = createActionButton("buttons" + "." + objectKey.str, context);
            if (actionButton != null) {
                actionButtons.add(actionButton);
            }
        }
        return actionButtons;
    }

    private ActionButton createActionButton(String path, TagContext context) {
        YamlConfiguration section = getConfigurationSection(path);
        if (section == null) {
            return null;
        }

        Component label = getComponent(section, "label", context);
        if (label == null) {
            Debug.echoError("Dialog script '" + getName() + "' is missing a required 'label' in '" + path + "'");
            return null;
        }

        Component tooltip = getComponent(section, "tooltip", context);
        Integer width = getInt(section, "width", context);
        ActionButton.Builder actionButton = ActionButton.builder(label);

        if (tooltip != null) {
            actionButton.tooltip(tooltip);
        }
        if (width != null) {
            actionButton.width(width);
        }

        DialogAction action = null;
        String type = getString(section, "type", context);
        if (type == null) {
            type = "SCRIPT";
        }
        switch (type) {
            case "SCRIPT" -> {
                List<ScriptEntry> entries = getEntries(context.getScriptEntryData(), path + ".script");
                if (entries != null) {
                    action = DialogAction.customClick((responseView, audience) -> {
                        if (!entries.isEmpty()) {
                            InstantQueue queue = new InstantQueue(getName());
                            queue.addEntries(entries);
                            queue.setContextSource(name -> {
                                if (name.equals("connection")) {
                                    UUID uuid = audience.get(Identity.UUID).orElse(null);
                                    if (uuid != null) {
                                        return new ConnectionTag(ConnectionTag.activeConnections.get(uuid));
                                    }
                                }
                                InputType inputType = keys.get(name);
                                if (inputType == null) return null;
                                return switch (inputType) {
                                    case TEXT, SINGLE -> new ElementTag(responseView.getText(name));
                                    case BOOLEAN -> new ElementTag(Boolean.TRUE.equals(responseView.getBoolean(name)));
                                    case NUMBER -> {
                                        Float value = responseView.getFloat(name);
                                        yield value != null ? new ElementTag(value) : null;
                                    }
                                };
                            });

                            queue.start(true);
                        }
                    }, options);
                }
            }
            case "RUN_COMMAND" -> {
                String command = getString(section, "command", context);
                if (command != null) {
                    action = DialogAction.staticAction(ClickEvent.runCommand(command));
                }
            }
            case "OPEN_URL" -> {
                String url = getString(section, "url", context);
                if (url != null) {
                    action = DialogAction.staticAction(ClickEvent.openUrl(url));
                }
            }
            case "COPY_TO_CLIPBOARD" -> {
                String text = getString(section, "text", context);
                if (text != null) {
                    action = DialogAction.staticAction(ClickEvent.copyToClipboard(text));
                }
            }
        }

        actionButton.action(action);
        return actionButton.build();
    }

    private DialogInput createTextInput(String key, Component label, YamlConfiguration config, TagContext context) {
        TextDialogInput.Builder builder = DialogInput.text(key, label);
        Integer width = getInt(config, "width", context);
        Integer maxLength = getInt(config, "max length", context);
        String initial = config.getString("initial", null);
        Boolean labelVisible = getBool(config, "label visible", context, null);


        if (width != null) {
            builder.width(width);
        }
        if (maxLength != null) {
            builder.maxLength(maxLength);
        }
        if (labelVisible != null) {
            builder.labelVisible(labelVisible);
        }
        if (initial != null) {
            builder.initial(initial);
        }

        if (config.contains("multiline options")) {
            Integer maxLines = getInt(config, "multiline options.max lines", context);
            Integer height = getInt(config, "multiline options.height", context);
            builder.multiline(TextDialogInput.MultilineOptions.create(maxLines, height));
        }

        return builder.build();
    }

    private DialogInput createBooleanInput(String key, Component label, YamlConfiguration config) {
        boolean initial = CoreUtilities.equalsIgnoreCase(config.getString("initial", "false"), "true");

        String onTrue = config.getString("on true", "true");
        String onFalse = config.getString("on false", "false");

        return DialogInput.bool(key, label, initial, onTrue, onFalse);
    }

    private DialogInput createNumberRangeInput(String key, Component label, YamlConfiguration config, TagContext context) {
        Float start = getFloat(config, "start", context);
        Float end = getFloat(config, "end", context);
        if (start == null || end == null) {
            Debug.echoError("Dialog script '" + getName() + "' input '" + key + "' is missing required 'start' and 'end' values.");
            return null;
        }

        Float step = getFloat(config, "step", context);
        Float initial = getFloat(config, "initial", context);
        Integer width = getInt(config, "width", context);
        String labelFormat = getString(config, "label format", context);
        NumberRangeDialogInput.Builder builder = DialogInput.numberRange(key, label, start, end);

        builder.step(step);

        if (initial != null && initial >= start && initial <= end) {
            builder.initial(initial);
        }
        if (width != null) {
            builder.width(width);
        }
        if (labelFormat != null) {
            builder.labelFormat(labelFormat);
        }

        return builder.build();
    }

    private DialogInput createSingleOptionInput(String key, Component label, YamlConfiguration config, TagContext context) {
        if (!config.contains("options")) {
            Debug.echoError("Dialog script '" + getName() + "' input '" + key + "' is missing required 'options' list.");
            return null;
        }

        YamlConfiguration optionsSection = config.getConfigurationSection("options");

        Set<StringHolder> objectKeys = optionsSection.getKeys(false);
        List<SingleOptionDialogInput.OptionEntry> entries = new ArrayList<>(objectKeys.size());

        for (StringHolder optionKey : objectKeys) {
            YamlConfiguration optionSection = optionsSection.getConfigurationSection(optionKey.str);
            if (optionSection == null) continue;

            Component display = null;
            if (optionSection.contains("display")) {
                display = getComponent(optionSection, "display", context);
            }
            String id = getString(optionSection, "id", context, optionKey.str);
            boolean initial = getBool(optionSection, "initial", context, false);
            entries.add(SingleOptionDialogInput.OptionEntry.create(id, display, initial));
        }

        SingleOptionDialogInput.Builder builder = DialogInput.singleOption(key, label, entries);

        Integer width = getInt(config, "width", context);
        Boolean visible = getBool(config, "label visible", context, null);

        if (width != null) {
            builder.width(width);
        }
        if (visible != null) {
            builder.labelVisible(visible);
        }

        return builder.build();
    }

    private Component getComponent(YamlConfiguration config, String path, TagContext context) {
        if (!config.contains(path)) {
            return null;
        }
        String text = config.getString(path);
        return PaperModule.parseFormattedText(TagManager.tag(text, context), ChatColor.WHITE);
    }

    private Float getFloat(YamlConfiguration config, String path, TagContext context) {
        if (!config.contains(path)) {
            return null;
        }
        String text = TagManager.tag(config.getString(path), context);
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException ignored) {}
        Debug.echoError(text + " is not a valid float.");
        return null;
    }


    private Integer getInt(YamlConfiguration config, String path, TagContext context) {
        if (!config.contains(path)) {
            return null;
        }
        String text = TagManager.tag(config.getString(path), context);
        if (ArgumentHelper.matchesInteger(text)) {
            return Integer.parseInt(text);
        }
        Debug.echoError("Dialog script '" + getName() + "' path '" + path + "' has invalid integer: " + text);
        return null;
    }

    private Boolean getBool(YamlConfiguration config, String path, TagContext context, Boolean def) {
        if (!config.contains(path)) {
            return def;
        }
        String text = TagManager.tag(config.getString(path), context);
        return CoreUtilities.equalsIgnoreCase(text, "true");
    }

    private String getString(YamlConfiguration config, String path, TagContext context, String def) {
        if (!config.contains(path)) {
            return def;
        }
        return TagManager.tag(config.getString(path), context);
    }

    private String getString(YamlConfiguration config, String path, TagContext context) {
        return getString(config, path, context, null);
    }
}
