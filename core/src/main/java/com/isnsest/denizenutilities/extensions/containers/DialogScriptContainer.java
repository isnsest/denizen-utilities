package com.isnsest.denizenutilities.extensions.containers;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.*;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.isnsest.denizenutilities.Compatibility;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptHelper.DialogData;
import com.isnsest.denizenutilities.extensions.containers.DialogScriptHelper.InputType;
import io.papermc.paper.connection.PlayerCommonConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerGameConnection;
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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.isnsest.denizenutilities.extensions.containers.DialogScriptHelper.mapToConfig;

@SuppressWarnings("UnstableApiUsage")
public class DialogScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Dialog Script Containers
    // @group Script Container System
    // @description
    // Dialog script containers define custom UI dialogs using the Paper dialog system.
    //
    // Each dialog requires a 'base' section that defines general properties and the dialog type.
    //
    // TYPES:
    // - confirm : Two-button confirmation dialog (yes/no)
    // - notice  : Simple dialog with a single button
    // - list    : Shows a list of other dialog scripts
    // - multi   : Grid layout with multiple action buttons
    //
    // DYNAMIC GENERATION (Procedural):
    // You can define a 'procedural' script section at the root of the container.
    // This executes a Denizen queue in parallel when opening the dialog, allowing you
    // to dynamically construct and determine maps for 'inputs', 'bodies', or 'buttons'.
    // Any statically defined elements in the dialog container will be cleanly merged
    // with your procedurally generated maps.
    // Note: You can naturally use click-time tags like '<context.connection>' or '<context.button_id>'
    // inside your dynamic scripts; they are automatically escaped at load-time to prevent premature execution.
    //
    // <code>
    // Dialog_Script_Name:
    //     type: dialog
    //
    //     base:
    //         title: <yellow>Main Menu
    //         type: multi
    //
    //         # Optional window settings
    //         external title: App Title
    //         can close with escape: true
    //
    //         # Layout columns (list or multi)
    //         columns: 3
    //
    //         # Button layout option (only for 'list')
    //         button width: 100
    //
    //         # Exit button (list or multi)
    //         exit button:
    //             label: Exit
    //
    //     # Procedural queue to generate elements dynamically
    //     procedural:
    //     - repeat 15 as:x:
    //         - definemap button:
    //             label: <[x]>
    //             width: 15
    //             script:
    //             - announce "Clicked button <context.button_id>"
    //             - adjust <context.connection> connect
    //         - define buttons.<[x]>:<[button]>
    //     - determine buttons:<[buttons]>
    //
    //     # only for 'list'
    //     dialogs:
    //     - other_dialog_script
    //
    //     # Static content displayed in the dialog
    //     bodies:
    //         my_text:
    //             type: message
    //             message: Hello!
    //
    //     # Input fields
    //     inputs:
    //         1:
    //             type: text
    //             key: custom_context_name
    //             label: Name
    //         my_input:
    //             type: text
    //             label: Name2
    //
    //     # Interaction buttons
    //     # 'multi' uses 'buttons:'
    //     # 'confirm' uses 'yes:' and 'no:'
    //     # 'notice' uses 'button:'
    //     buttons:
    //         submit:
    //             label: Submit
    //             script:
    //             - narrate "Context is <context.custom_context_name>, <context.my_input>"
    // </code>
    // -->

    // <--[language]
    // @name Dialog Inputs
    // @group Script Container System
    // @description
    // Dialog inputs collect player data that becomes available in the script context.
    //
    // Input values can be accessed directly using:
    // <context.[key]> (e.g., <context.applicant_name>)
    // Or as a full map using:
    // <context.inputs>
    //
    // TYPES:
    //
    // TEXT
    // A text input field.
    //
    // - type: text
    // - label: <text> (Required)
    // - key: <context_key>
    // - initial: <text>
    // - width: <number>
    // - max length: <number>
    // - label visible: true/false
    //
    // Multiline options:
    // - multiline options:
    //     max lines: <number>
    //     height: <number>
    //
    //
    // BOOLEAN
    // A toggle switch input.
    //
    // - type: boolean
    // - label: <text> (Required)
    // - key: <context_key>
    // - initial: true/false
    // - on true: <label>
    // - on false: <label>
    //
    //
    // NUMBER
    // A numeric range slider.
    //
    // - type: number
    // - label: <text> (Required)
    // - key: <context_key>
    // - start: <float> (Required)
    // - end: <float> (Required)
    // - initial: <float>
    // - step: <float>
    // - width: <number>
    // - label format: <text>
    //   Example: "Value: %s"
    //
    //
    // SINGLE
    // A single-choice selection list.
    //
    // - type: single
    // - label: <text> (Required)
    // - key: <context_key>
    // - label visible: true/false
    // - width: <number>
    //
    // Options:
    //   options:
    //     option_name:
    //         id: unique_id
    //         display: <text>
    //         initial: true/false
    // -->

    // <--[language]
    // @name Dialog Buttons
    // @group Script Container System
    // @description
    // Buttons define interactive actions within dialogs.
    //
    // Button sections depend on dialog type:
    //
    // confirm dialog:
    // - yes:
    // - no:
    //
    // notice dialog:
    // - button:
    //
    // multi dialog:
    // - buttons:
    //
    // Button fields:
    //
    // - label: <text> (Required)
    // - tooltip: <text>
    // - width: <number>
    // - type: SCRIPT, RUN_COMMAND, OPEN_URL, COPY_TO_CLIPBOARD
    //
    // ACTION TYPES:
    //
    // SCRIPT (default)
    // Runs a script block.
    //
    // Example:
    // script:
    // - narrate "Hello"
    //
    // Available context values:
    // <context.connection> returns the ConnectionTag of the player.
    // <context.button_id> returns the ID of the button clicked.
    // <context.namespace> returns the namespace of the dialog (e.g., 'denizen').
    // <context.inputs> returns a MapTag of all active input fields (only for Denizen dialogs).
    // <context.[input_id]> returns the typed value of the specific input field directly.
    //
    // RUN_COMMAND
    // Runs a command when clicked.
    // command: spawn
    //
    // OPEN_URL
    // Opens a web URL.
    // url: https://example.com
    //
    // COPY_TO_CLIPBOARD
    // Copies text to the player's clipboard.
    //
    // text: Hello world
    // -->

    // <--[language]
    // @name Dialog Bodies
    // @group Script Container System
    // @description
    // Bodies define static content displayed inside the dialog.
    //
    // MESSAGE
    // Displays formatted text.
    //
    // - type: message
    // - message: <text> (Required)
    // - width: <number>
    //
    //
    // ITEM
    // Displays an item preview.
    //
    // - type: item
    // - item: <ItemTag> (Required)
    // - width: <number>
    // - height: <number>
    // - show tooltip: true/false
    // - show decorations: true/false
    //
    // Optional description:
    // - description:
    //     type: message
    //     message: <text>
    // -->

    @SuppressWarnings("unchecked")
    public DialogScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        if (configurationSection.contains("procedural")) {
            List<Object> procedural = (List<Object>) DialogScriptHelper.deeplyEscapeTags(
                    configurationSection.getList("procedural"),
                    "<context.connection",
                    "<context.inputs"
            );

            configurationSection.set("procedural", procedural);
        }
        super(configurationSection, scriptContainerName);
    }

    private InputType parseInput(String type) {
        return switch (CoreUtilities.toLowerCase(type)) {
            case "text" -> InputType.TEXT;
            case "single" -> InputType.SINGLE;
            case "boolean", "bool" -> InputType.BOOLEAN;
            case "number" -> InputType.NUMBER;
            default -> {
                Debug.echoError("Dialog script '" + getName() + "' has unknown input type: " + type);
                yield null;
            }
        };
    }

    public Dialog getDialogFrom(TagContext context, PlayerCommonConnection connection) {
        if (context == null) {
            context = new BukkitTagContext(null, null, new ScriptTag(this));
        }

        final Map<String, YamlConfiguration> proceduralData = new HashMap<>();
        final DialogData dialogData = new DialogData();
        dialogData.connection = connection;

        proceduralData.put("inputs", getConfigurationSection("inputs"));
        proceduralData.put("bodies", getConfigurationSection("bodies"));
        proceduralData.put("buttons", getConfigurationSection("buttons"));

        if (containsScriptSection("procedural")) {
            TagContext finalContext = context;
            InstantQueue queue = new InstantQueue(getName());
            List<ScriptEntry> entries = getEntries(context.getScriptEntryData(), "procedural");
            queue.addEntries(entries);
            queue.determinationTarget = (s, object) -> {
                if (!object.canBeType(MapTag.class)) {
                    return;
                }
                if (s.equalsIgnoreCase("inputs"))
                    proceduralData.put("inputs", mapToConfig(object.asType(MapTag.class, finalContext)));
                if (s.equalsIgnoreCase("bodies"))
                    proceduralData.put("bodies", mapToConfig(object.asType(MapTag.class, finalContext)));
                if (s.equalsIgnoreCase("buttons"))
                    proceduralData.put("buttons", mapToConfig(object.asType(MapTag.class, finalContext)));
            };
            queue.start();
        }

        dialogData.configurationMap = proceduralData;

        DialogBase dialogBase = getDialogBase(context, dialogData);
        if (dialogBase == null) {
            return null;
        }

        DialogType dialogType = getDialogType(context, dialogData, connection);
        if (dialogType == null) {
            return null;
        }

        return Dialog.create(builder -> builder.empty()
                .base(dialogBase)
                .type(dialogType));
    }

    public DialogType getDialogType(TagContext context, DialogData dialogData, PlayerCommonConnection connection) {
        String type = getString("base.type");
        if (type == null) {
            Debug.echoError("Dialog script '" + getName() + "' is missing a required 'base.type'!");
            return null;
        }

        return switch (type) {
            case "confirm" -> {
                ActionButton yes = createActionButton("yes", getConfigurationSection("yes"), context);
                ActionButton no = createActionButton("no", getConfigurationSection("no"), context);
                if (yes == null || no == null) {
                    yield null;
                }
                yield DialogType.confirmation(yes, no);
            }
            case "list" -> {
                if (!containsScriptSection("dialogs")) {
                    Debug.echoError("Dialog script '" + getName() + "' is missing a required 'dialogs'");
                    yield null;
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
                    Dialog dialog = container.getDialogFrom(context, connection);
                    if (dialog == null) {
                        Debug.echoError("Failed to construct dialog script '" + id + "' inside list dialog '" + getName() + "'");
                        continue;
                    }
                    dialogs.add(dialog);
                }
                RegistrySet<@NotNull Dialog> registrySet = RegistrySet.valueSet(RegistryKey.DIALOG, dialogs);
                DialogListType.Builder dialogList = DialogType.dialogList(registrySet);
                ActionButton exitButton = createActionButton("exit button", getConfigurationSection("exit button"), context);
                if (columns != null) {
                    dialogList.columns(columns);
                }
                if (buttonWidth != null) {
                    dialogList.buttonWidth(buttonWidth);
                }
                if (exitButton != null) {
                    dialogList.exitAction(exitButton);
                }
                yield dialogList.build();
            }
            case "notice" -> {
                ActionButton actionButton = createActionButton("button", getConfigurationSection("button"), context);
                yield (actionButton == null) ? DialogType.notice() : DialogType.notice(actionButton);
            }
            case "multi" -> {
                Integer columns = getInt(getContents(), "base.columns", context);
                ActionButton exitButton = createActionButton("exit button", getConfigurationSection("exit button"), context);
                List<ActionButton> actionButtons = createActionButtons(context, dialogData.configurationMap.get("buttons"));
                if (actionButtons == null) {
                    Debug.echoError("Dialog script '" + getName() + "' is missing a required 'buttons'");
                    yield null;
                }
                MultiActionType.Builder multiActionType = DialogType.multiAction(actionButtons);
                if (columns != null) {
                    multiActionType.columns(columns);
                }
                if (exitButton != null) {
                    multiActionType.exitAction(exitButton);
                }
                yield multiActionType.build();
            }
            default -> {
                Debug.echoError("Unknown dialog type: " + type);
                yield null;
            }
        };
    }

    public void showTo(PlayerCommonConnection connection, TagContext context) {
        Dialog dialog = getDialogFrom(context, connection);
        if (dialog == null) {
            Debug.echoError("Failed to show dialog.");
            return;
        }
        if (connection instanceof PlayerConfigurationConnection configurationConnection) {
            configurationConnection.getAudience().showDialog(dialog);
        } else if (connection instanceof PlayerGameConnection gameConnection) {
            gameConnection.getPlayer().showDialog(dialog);
        }
    }

    public DialogBase getDialogBase(TagContext context, DialogData dialogData) {
        Component title = getComponent(getContents(), "base.title", context);
        if (title == null) {
            Debug.echoError("Dialog script '" + getName() + "' is missing a required 'base.title'!");
            return null;
        }

        Component externalTitle = getComponent(getContents(), "base.external title", context);
        Boolean canCloseWithEscape = getBool(getContents(), "base.can close with escape", context, true);

        DialogBase.Builder baseBuilder = DialogBase.builder(title)
                .canCloseWithEscape(canCloseWithEscape)
                .externalTitle(externalTitle)
                .pause(false);

        inputs(baseBuilder, dialogData, context);
        bodies(baseBuilder, dialogData.configurationMap.get("bodies"), context);

        if (contains("base.after action")) {
            String action = getString(getContents(), "base.after action", context).toUpperCase();
            DialogBase.DialogAfterAction afterAction = switch (action) {
                case "NONE" -> DialogBase.DialogAfterAction.NONE;
                case "WAIT_FOR_RESPONSE" -> DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE;
                default -> {
                    if (!action.isEmpty() && !action.equals("CLOSE")) {
                        Debug.echoError("Dialog script '" + getName() + "' has an invalid 'base.after action' value: '" + action + "'.");
                    }
                    yield DialogBase.DialogAfterAction.CLOSE;
                }
            };
            baseBuilder.afterAction(afterAction);
        }

        DialogScriptHelper.put(dialogData.connection, dialogData);

        return baseBuilder.build();
    }

    private void bodies(DialogBase.Builder baseBuilder, YamlConfiguration bodiesSection, TagContext context) {
        if (bodiesSection == null) return;

        List<DialogBody> bodies = new ArrayList<>();
        for (StringHolder sh : bodiesSection.getKeys(false)) {
            YamlConfiguration objectSection = bodiesSection.getConfigurationSection(sh.str);
            if (objectSection == null || !checkCondition(objectSection, context)) continue;

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

    private void inputs(DialogBase.Builder baseBuilder, DialogData dialogData, TagContext context) {
        YamlConfiguration inputsSection = dialogData.configurationMap.get("inputs");
        if (inputsSection == null) return;

        Map<String, InputType> inputTypeMap = new HashMap<>();
        List<DialogInput> inputs = new ArrayList<>();
        for (StringHolder sh : inputsSection.getKeys(false)) {
            YamlConfiguration objectSection = inputsSection.getConfigurationSection(sh.str);
            if (objectSection == null || !checkCondition(objectSection, context)) continue;
            if (!objectSection.contains("type")) {
                Debug.echoError("Dialog script '" + getName() + "' has an object without a specified type!");
                continue;
            }
            if (!objectSection.contains("label")) {
                Debug.echoError("Dialog script '" + getName() + "' is missing a required 'label' for input '" + sh.str + "'.");
                continue;
            }

            String key = objectSection.getString("key", sh.str);
            Component label = getComponent(objectSection, "label", context);

            InputType type = parseInput(objectSection.getString("type"));
            if (type == null) {
                continue;
            }

            DialogInput input = switch (type) {
                case TEXT -> createTextInput(key, label, objectSection, context);
                case BOOLEAN -> createBooleanInput(key, label, objectSection, context);
                case NUMBER -> createNumberRangeInput(key, label, objectSection, context);
                case SINGLE -> createSingleOptionInput(key, label, objectSection, context);
            };

            if (input != null) {
                inputTypeMap.put(key, type);
                inputs.add(input);
            }
        }

        dialogData.inputs = inputTypeMap;
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

    @SuppressWarnings("PatternValidation")
    public Key buttonKey(String buttonId) {
        return Key.key("denizen", getName().toLowerCase() + "/" + buttonId.toLowerCase());
    }

    private List<ActionButton> createActionButtons(TagContext context, YamlConfiguration buttonsSection) {
        if (buttonsSection == null) {
            return null;
        }

        List<ActionButton> actionButtons = new ArrayList<>();
        for (StringHolder objectKey : buttonsSection.getKeys(false)) {
            ActionButton actionButton = createActionButton("buttons." + objectKey.str, buttonsSection.getConfigurationSection(objectKey.str), context);
            if (actionButton != null) {
                actionButtons.add(actionButton);
            }
        }
        return actionButtons;
    }

    private ActionButton createActionButton(String path, YamlConfiguration section, TagContext context) {
        if (section == null || !checkCondition(section, context)) {
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
        String type = getString(section, "type", context, "SCRIPT");
        switch (type) {
            case "SCRIPT" -> action = DialogAction.customClick(buttonKey(path), null);
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
        String initial = getString(config, "initial", context);
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

    private DialogInput createBooleanInput(String key, Component label, YamlConfiguration config, TagContext context) {
        boolean initial = CoreUtilities.equalsIgnoreCase(getString(config, "initial", context, "false"), "true");

        String onTrue = getString(config, "on true", context, "true");
        String onFalse = getString(config, "on false", context, "false");

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

    private Boolean checkCondition(YamlConfiguration config, TagContext context) {
        if (!config.contains("condition")) {
            return true;
        }
        String condition = TagManager.tag(config.getString("condition"), context);
        return condition.equalsIgnoreCase("true");
    }

    private Component getComponent(YamlConfiguration config, String path, TagContext context) {
        if (!config.contains(path)) {
            return null;
        }
        String text = TagManager.tag(config.getString(path), context);
        return Compatibility.get().parse(text);
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

    public static List<ScriptEntry> cleanDup(ScriptEntryData data, ScriptEntrySet set) {
        if (set == null) {
            return null;
        }
        set = set.duplicate();
        for (ScriptEntry entry : set.entries) {
            entry.entryData = data.clone();
            entry.updateContext();
            entry.entryData.scriptEntry = entry;
        }
        return set.entries;
    }

    public List<ScriptEntry> getEntries(YamlConfiguration section, ScriptEntryData data, String path) {
        if (path == null) {
            path = "script";
        }
        return cleanDup(data, getSetFor(section, path));
    }

    public ScriptEntrySet getSetFor(YamlConfiguration section, String path) {
        List<Object> stringEntries = section.getList(path);
        if (stringEntries == null || stringEntries.isEmpty()) {
            return null;
        }
        List<ScriptEntry> entries = ScriptBuilder.buildScriptEntries(stringEntries, this, null);
        if (entries == null) {
            return null;
        }
        return new ScriptEntrySet(entries);
    }

}