package com.isnsest.denizenutilities.bridges.litebans;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import litebans.api.Entry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LiteBansUtils {

    private LiteBansUtils() {
    }

    public static PlayerTag getPlayerTag(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(uuid);
            Player online = Bukkit.getPlayer(id);
            if (online != null) {
                return new PlayerTag(online);
            }
            OfflinePlayer offline = Bukkit.getOfflinePlayer(id);
            return new PlayerTag(offline);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static ScriptEntryData getScriptEntryData(Entry entry) {
        PlayerTag player = getPlayerTag(entry.getUuid());
        return new BukkitScriptEntryData(player, null);
    }

    public static ObjectTag getEntryContext(String name, Entry entry) {
        return switch (name) {
            case "id" -> new ElementTag(entry.getId());
            case "reason" -> new ElementTag(entry.getReason());
            case "uuid" -> entry.getUuid() != null ? new ElementTag(entry.getUuid()) : null;
            case "ip" -> entry.getIp() != null ? new ElementTag(entry.getIp()) : null;
            case "executor" -> getPlayerTag(entry.getExecutorUUID());
            case "executor_uuid" -> entry.getExecutorUUID() != null ? new ElementTag(entry.getExecutorUUID()) : null;
            case "executor_name" -> entry.getExecutorName() != null ? new ElementTag(entry.getExecutorName()) : null;
            case "removed_by" -> getPlayerTag(entry.getRemovedByUUID());
            case "removed_by_uuid" -> entry.getRemovedByUUID() != null ? new ElementTag(entry.getRemovedByUUID()) : null;
            case "removed_by_name" -> entry.getRemovedByName() != null ? new ElementTag(entry.getRemovedByName()) : null;
            case "removal_reason" -> entry.getRemovalReason() != null ? new ElementTag(entry.getRemovalReason()) : null;
            case "server_scope" -> new ElementTag(entry.getServerScope());
            case "server_origin" -> new ElementTag(entry.getServerOrigin());
            case "duration" -> entry.getDuration() >= 0 ? new DurationTag(entry.getDuration()) : null;
            case "duration_string" -> new ElementTag(entry.getDurationString());
            case "remaining_duration" -> {
                long remaining = entry.getRemainingDuration(System.currentTimeMillis());
                yield remaining >= 0 ? new DurationTag(remaining) : null;
            }
            case "remaining_duration_string" -> new ElementTag(entry.getRemainingDurationString(System.currentTimeMillis()));
            case "random_id" -> new ElementTag(entry.getRandomID());
            case "template_name" -> new ElementTag(entry.getTemplateName());
            case "date_start" -> new ElementTag(entry.getDateStart());
            case "date_end" -> new ElementTag(entry.getDateEnd());
            case "is_permanent" -> new ElementTag(entry.isPermanent());
            case "is_silent" -> new ElementTag(entry.isSilent());
            case "is_ipban" -> new ElementTag(entry.isIpban());
            case "is_active" -> new ElementTag(entry.isActive());
            default -> null;
        };
    }
}
