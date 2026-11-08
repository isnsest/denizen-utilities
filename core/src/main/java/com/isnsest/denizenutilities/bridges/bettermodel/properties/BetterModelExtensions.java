package com.isnsest.denizenutilities.bridges.bettermodel.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.isnsest.denizenutilities.bridges.bettermodel.SkinPartsManager;
import com.isnsest.denizenutilities.bridges.bettermodel.objects.BMActiveModelTag;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitItemStack;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.util.TransformedItemStack;

public class BetterModelExtensions {

    public static void register() {
        // <--[tag]
        // @attribute <EntityTag.model[(<model_name>)]>
        // @returns BMActiveModelTag
        // @plugin denizen-utilities, BetterModel
        // @description
        // Returns the BetterModel tracker with the specified name from the entity.
        // If no name is provided, returns the first active model.
        // -->
        EntityTag.tagProcessor.registerTag(BMActiveModelTag.class, "model", (attribute, object) -> {
            return BetterModel.registry(object.getUUID()).map(registry -> {
                EntityTracker tracker = attribute.hasParam() ? registry.tracker(attribute.getParam()) : registry.first();
                if (tracker == null) return null;
                return new BMActiveModelTag(tracker);
            }).orElse(null);
        });

        // <--[tag]
        // @attribute <EntityTag.models>
        // @returns ListTag
        // @plugin denizen-utilities, BetterModel
        // @description
        // Returns a list of all BetterModel model names currently active on the entity.
        // -->
        EntityTag.tagProcessor.registerTag(ListTag.class, "models", (_, object) -> {
            ListTag list = new ListTag();
            BetterModel.registry(object.getUUID()).ifPresent(registry -> {
                for (EntityTracker t : registry.trackers()) {
                    list.add(t.name());
                }
            });
            return list;
        });

        // <--[tag]
        // @attribute <PlayerTag.limb>
        // @returns BMActiveModelTag
        // @plugin denizen-utilities, BetterModel
        // @description
        // Returns the BetterModel tracker with the specified name from the entity.
        // If no name is provided, returns the first active model.
        // -->
        PlayerTag.tagProcessor.registerTag(BMActiveModelTag.class, "limb", (_, object) -> {
            return BetterModel.registry(object.getUUID())
                    .flatMap(reg -> reg.trackers().stream()
                            .filter(t -> t.getPipeline().getParent().type() == ModelRenderer.Type.PLAYER)
                            .findFirst()
                            .map(BMActiveModelTag::new))
                    .orElse(null);
        });

        // <--[tag]
        // @attribute <PlayerTag.bm_part[<part_name>]>
        // @returns ItemTag
        // @plugin denizen-utilities, BetterModel
        // @description
        // Returns the current item used for the specified BetterModel skin part of the player.
        // Returns null if the skin part does not exist or has no active transformed item.
        // -->
        PlayerTag.tagProcessor.registerTag(ItemTag.class, ElementTag.class, "bm_part", (_, player, name) -> {
            TransformedItemStack t = SkinPartsManager.skinPartNow(player.getUUID(), name.asString());
            return t == null ? null : new ItemTag(((BukkitItemStack) t.itemStack()).source());
        });
    }
}
