package com.isnsest.denizenutilities.bridges.bettermodel;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.isnsest.denizenutilities.DenizenUtilities;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.bone.BoneRenderContext;
import kr.toxicity.model.api.bone.RenderedBone;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.RenderSource;
import kr.toxicity.model.api.profile.ModelProfile;
import kr.toxicity.model.api.skin.SkinData;
import kr.toxicity.model.api.tracker.Tracker;
import kr.toxicity.model.api.util.TransformedItemStack;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.isnsest.denizenutilities.bridges.bettermodel.objects.BMActiveModelTag.updateBone;

public class BetterModelUtils {

    // Statically cached with setAccessible(true) for near-zero overhead, equivalent to direct field access
    private static final Field ITEM_STACK_FIELD;

    static {
        Field f = null;
        try {
            f = RenderedBone.class.getDeclaredField("itemStack");
            f.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
        ITEM_STACK_FIELD = f;
    }

    public static TransformedItemStack getTransform(RenderedBone bone) {
        try {
            if (ITEM_STACK_FIELD != null) {
                return (TransformedItemStack) ITEM_STACK_FIELD.get(bone);
            }
        } catch (IllegalAccessException ignored) {}
        return TransformedItemStack.empty();
    }

    public static AnimationIterator.Type parseLoop(String mode) {
        return switch (mode.toUpperCase()) {
            case "LOOP" -> AnimationIterator.Type.LOOP;
            case "HOLD", "HOLD_ON_LAST" -> AnimationIterator.Type.HOLD_ON_LAST;
            default -> AnimationIterator.Type.PLAY_ONCE;
        };
    }

    //
     //
    //

    public static void changeSkin(@NotNull Tracker tracker, @NotNull ObjectTag object) {
        changeSkin(tracker, object, null);
    }

    public static void changeSkin(@NotNull Tracker tracker, @NotNull ObjectTag object, @Nullable RenderedBone bone) {
        ModelProfile.Uncompleted uncompleted = null;

        if (object instanceof PlayerTag player) {
            uncompleted = ModelProfile.of(BukkitAdapter.adapt(player.getPlayerEntity())).asUncompleted();
        } else {
            try {
                UUID uuid = UUID.fromString(object.toString());
                uncompleted = ModelProfile.of(uuid);
            } catch (Exception ignored) {
                // Ignored.
            }
        }

        if (uncompleted != null) {
            CompletableFuture<? extends SkinData> future = BetterModel.platform().skinManager().complete(uncompleted);
            if (future.isDone()) {
                changeSkinWithProfile(tracker, future.join(), bone);
            } else {
                future.thenAccept(skin -> {
                    Bukkit.getScheduler().runTask(DenizenUtilities.getInstance(), () -> changeSkinWithProfile(tracker, skin, bone));
                });
            }
        }
    }

    private static void changeSkinWithProfile(@NotNull Tracker tracker, @NotNull SkinData skinData, RenderedBone bone) {
        if (tracker.isClosed()) {
            return;
        }

        RenderSource<?> source = tracker.getPipeline().getSource();
        BoneRenderContext boneRenderContext = new BoneRenderContext(source, skinData);

        Iterable<RenderedBone> bonesToUpdate = (bone != null) ? bone.flattenBones() : tracker.bones();

        for (RenderedBone targetBone : bonesToUpdate) {
            updateBoneSkinData(targetBone, boneRenderContext);
        }

        tracker.forceUpdate(true);
    }

    private static void updateBoneSkinData(RenderedBone bone, BoneRenderContext context) {
        TransformedItemStack current = BetterModelUtils.getTransform(bone);
        Vector3f position = current.position();
        Vector3f offset = current.offset();
        Vector3f scale = current.scale();

        bone.updateItem(context);

        TransformedItemStack updated = BetterModelUtils.getTransform(bone);
        updateBone(bone, _ -> new TransformedItemStack(position, offset, scale, updated.itemStack()));
    }

}
