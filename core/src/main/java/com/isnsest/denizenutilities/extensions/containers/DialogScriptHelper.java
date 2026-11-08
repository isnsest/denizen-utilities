package com.isnsest.denizenutilities.extensions.containers;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import io.papermc.paper.connection.PlayerCommonConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DialogScriptHelper implements Listener {

    public static final Map<UUID, DialogData> dialogDataMap = new ConcurrentHashMap<>();

    public static UUID keyOf(PlayerCommonConnection connection) {
        if (connection instanceof PlayerGameConnection game) {
            return game.getPlayer().getUniqueId();
        }
        if (connection instanceof PlayerConfigurationConnection config) {
            return config.getProfile().getId();
        }
        return null;
    }

    public static void put(PlayerCommonConnection connection, DialogData data) {
        UUID key = keyOf(connection);
        if (key != null) {
            dialogDataMap.put(key, data);
        }
    }

    public static DialogData get(PlayerCommonConnection connection) {
        UUID key = keyOf(connection);
        return key == null ? null : dialogDataMap.get(key);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dialogDataMap.remove(event.getPlayer().getUniqueId());
    }

    public DialogScriptHelper() {
    }

    public enum InputType {
        TEXT, SINGLE, BOOLEAN, NUMBER
    }

    public static class DialogData {
        public PlayerCommonConnection connection;
        public Map<String, InputType> inputs;
        public Map<String, YamlConfiguration> configurationMap;

        public DialogData() {
        }

        public Map<String, InputType> getInputs() {
            return inputs;
        }

        public Map<String, YamlConfiguration> getConfigurationMap() {
            return configurationMap;
        }
    }


    public static YamlConfiguration mapToConfig(MapTag map) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
            ObjectTag val = entry.getValue();
            if (val instanceof MapTag mapTag) {
                config.contents.put(entry.getKey(), mapToConfig(mapTag).contents);
            } else {
                config.contents.put(entry.getKey(), val.getJavaObject());
            }
        }
        return config;
    }

    public static Object deeplyEscapeTags(Object obj, String... prefixes) {
        return switch (obj) {
            case String str -> autoEscapeTags(str, prefixes);
            case StringHolder sh -> new StringHolder(autoEscapeTags(sh.str, prefixes));
            case List<?> list -> list.stream().map(item -> deeplyEscapeTags(item, prefixes)).toList();
            case Map<?, ?> map -> {
                Map<Object, Object> newMap = new LinkedHashMap<>();
                map.forEach((k, v) -> {
                    String keyStr = k instanceof StringHolder sh ? sh.str : String.valueOf(k);
                    if (keyStr.equalsIgnoreCase("script")) {
                        newMap.put(k, escapeAllTags(v));
                    } else {
                        newMap.put(deeplyEscapeTags(k, prefixes), deeplyEscapeTags(v, prefixes));
                    }
                });
                yield newMap;
            }
            case null, default -> obj;
        };
    }

    private static Object escapeAllTags(Object obj) {
        return switch (obj) {
            case String str -> autoEscapeTags(str, "<");
            case StringHolder sh -> new StringHolder(autoEscapeTags(sh.str, "<"));
            case List<?> list -> list.stream().map(DialogScriptHelper::escapeAllTags).toList();
            case Map<?, ?> map -> {
                Map<Object, Object> newMap = new LinkedHashMap<>();
                map.forEach((k, v) -> newMap.put(escapeAllTags(k), escapeAllTags(v)));
                yield newMap;
            }
            case null, default -> obj;
        };
    }

    public static String autoEscapeTags(String input, String... prefixes) {
        if (input == null || prefixes == null || prefixes.length == 0 || !input.contains("<")) {
            return input;
        }

        StringBuilder result = new StringBuilder(input.length() + 32);
        int length = input.length();

        for (int i = 0; i < length; i++) {
            if (input.charAt(i) != '<') {
                result.append(input.charAt(i));
                continue;
            }

            boolean matched = false;
            for (String prefix : prefixes) {
                if (input.startsWith(prefix, i)) {
                    int depth = 1;
                    int closeIndex = -1;
                    for (int j = i + prefix.length(); j < length; j++) {
                        if (input.charAt(j) == '<') {
                            depth++;
                        } else if (input.charAt(j) == '>') {
                            depth--;
                            if (depth == 0) {
                                closeIndex = j;
                                break;
                            }
                        }
                    }
                    if (closeIndex != -1) {
                        result.append("<&lt>");
                        result.append(input, i + 1, closeIndex);
                        result.append("<&gt>");
                        i = closeIndex;
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }
}