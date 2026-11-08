package com.isnsest.denizenutilities.bridges.bettermodel;

import com.isnsest.denizenutilities.DenizenUtilities;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.armor.PlayerArmor;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.manager.SkinManager;
import kr.toxicity.model.api.profile.ModelProfile;
import kr.toxicity.model.api.skin.SkinData;
import kr.toxicity.model.api.util.TransformedItemStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class SkinPartsManager implements Listener {

    private static final long TTL_MS = 30_000L;
    private static final long CLEANUP_PERIOD_TICKS = 20L * 60 * 5;

    private static final PlayerArmor NO_ARMOR = PlayerArmor.EMPTY;

    private static final Executor MAIN = runnable -> {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(DenizenUtilities.getInstance(), runnable);
        }
    };

    private record Entry(CompletableFuture<Map<String, TransformedItemStack>> future, long time) {
        boolean expired(long now) {
            return now - time >= TTL_MS;
        }
    }

    private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();

    private SkinPartsManager() {}

    public static void register(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new SkinPartsManager(), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, SkinPartsManager::cleanup, CLEANUP_PERIOD_TICKS, CLEANUP_PERIOD_TICKS);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        skinParts(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CACHE.remove(event.getPlayer().getUniqueId());
    }

    public static @NotNull CompletableFuture<Map<String, TransformedItemStack>> skinParts(@NotNull UUID uuid) {
        long now = System.currentTimeMillis();
        boolean[] created = {false};
        Entry entry = CACHE.compute(uuid, (_, existing) -> {
            if (existing != null && !existing.expired(now)) return existing;
            created[0] = true;
            return new Entry(new CompletableFuture<>(), now);
        });
        if (created[0]) {
            load(uuid).whenComplete((parts, error) -> {
                if (error != null || parts == null) {
                    CACHE.remove(uuid, entry);
                    entry.future().completeExceptionally(error != null ? error : new IllegalStateException("no parts"));
                    if (error != null) error.printStackTrace();
                } else {
                    entry.future().complete(parts);
                }
            });
        }
        return entry.future();
    }
    public static Map<String, TransformedItemStack> skinPartsNow(@NotNull UUID uuid) {
        CompletableFuture<Map<String, TransformedItemStack>> future = skinParts(uuid);
        return future.isDone() && !future.isCompletedExceptionally() ? future.getNow(null) : null;
    }

    public static TransformedItemStack skinPartNow(@NotNull UUID uuid, @NotNull String name) {
        Map<String, TransformedItemStack> parts = skinPartsNow(uuid);
        return parts == null ? null : parts.get(name);
    }

    private static void cleanup() {
        long now = System.currentTimeMillis();
        CACHE.values().removeIf(entry -> entry.expired(now) && entry.future().isDone());
    }

    private static CompletableFuture<Map<String, TransformedItemStack>> load(@NotNull UUID uuid) {
        SkinManager manager = BetterModel.platform().skinManager();

        Player online = Bukkit.getPlayer(uuid);
        ModelProfile.Uncompleted profile = online != null
                ? ModelProfile.of(BukkitAdapter.adapt(online)).asUncompleted()
                : ModelProfile.of(uuid);

        return manager.complete(profile).handleAsync((skin, error) -> {
            if (error != null) error.printStackTrace();
            return buildParts(skin != null ? skin : manager.fallback());
        }, MAIN);
    }

    private static Map<String, TransformedItemStack> buildParts(@NotNull SkinData skin) {
        Map<String, TransformedItemStack> m = new LinkedHashMap<>();
        m.put("head", skin.head(NO_ARMOR));
        m.put("chest", skin.chest(NO_ARMOR));
        m.put("waist", skin.waist(NO_ARMOR));
        m.put("hip", skin.hip(NO_ARMOR));
        m.put("left_arm", skin.leftArm(NO_ARMOR));
        m.put("left_forearm", skin.leftForeArm());
        m.put("right_arm", skin.rightArm(NO_ARMOR));
        m.put("right_forearm", skin.rightForeArm());
        m.put("left_leg", skin.leftLeg(NO_ARMOR));
        m.put("left_foreleg", skin.leftForeLeg(NO_ARMOR));
        m.put("right_leg", skin.rightLeg(NO_ARMOR));
        m.put("right_foreleg", skin.rightForeLeg(NO_ARMOR));
        TransformedItemStack cape = skin.cape(NO_ARMOR);
        if (cape != null) m.put("cape", cape);
        return Collections.unmodifiableMap(m);
    }
}
