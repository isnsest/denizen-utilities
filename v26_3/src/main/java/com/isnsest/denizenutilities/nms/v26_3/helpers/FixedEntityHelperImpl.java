package com.isnsest.denizenutilities.nms.v26_3.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v26_3.helpers.EntityDataNameMapper;
import com.denizenscript.denizen.nms.v26_3.helpers.EntityHelperImpl;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.commands.core.ReflectionSetCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FixedEntityHelperImpl extends EntityHelperImpl {

    public static void patch() {
        try {
            Field helperField = NMSHandler.class.getField("entityHelper");
            helperField.setAccessible(true);
            helperField.set(null, new FixedEntityHelperImpl());
        } catch (Throwable t) {
            Debug.echoError("Failed to patch EntityHelper: " + t.getMessage());
            Debug.echoError(t);
        }
    }

    @SuppressWarnings("unchecked")
    private SynchedEntityData.DataItem<Object> getDataItem(Object itemsObj, int id) {
        if (itemsObj instanceof Int2ObjectMap<?> map) {
            return (SynchedEntityData.DataItem<Object>) map.get(id);
        } else if (itemsObj instanceof SynchedEntityData.DataItem<?>[] array) {
            if (id >= 0 && id < array.length) {
                return (SynchedEntityData.DataItem<Object>) array[id];
            }
        }
        return null;
    }

    public void processConvertedData(Entity entity, MapTag internalData, BiConsumer<SynchedEntityData.DataItem<Object>, Object> processConverted) {
        Object itemsObj;
        try {
            itemsObj = SynchedEntityData_itemsById.get(((CraftEntity) entity).getHandle().getEntityData());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<StringHolder, ObjectTag> entry : internalData.entrySet()) {
            int id = EntityDataNameMapper.getIdForName(((CraftEntity) entity).getHandle().getClass(), entry.getKey().low);
            if (id == -1) {
                Debug.echoError("Invalid internal data key: " + entry.getKey());
                continue;
            }

            SynchedEntityData.DataItem<Object> dataItem = getDataItem(itemsObj, id);
            if (dataItem == null) {
                Debug.echoError("Invalid internal data id '" + id + "': couldn't be matched to any internal data for entity of type '" + entity.getType() + "'.");
                continue;
            }

            Object converted = ReflectionSetCommand.convertObjectTypeFor(dataItem.getValue().getClass(), entry.getValue());
            if (converted != null) {
                processConverted.accept(dataItem, converted);
            }
        }
    }

    @Override
    public List<Object> convertInternalEntityDataValues(Entity entity, MapTag internalData) {
        List<Object> dataValues = new ArrayList<>(internalData.size());
        processConvertedData(entity, internalData, (dataItem, converted) -> dataValues.add(SynchedEntityData.DataValue.create(dataItem.getAccessor(), converted)));
        return dataValues;
    }

    @Override
    public void modifyInternalEntityData(Entity entity, MapTag internalData) {
        SynchedEntityData nmsData = ((CraftEntity) entity).getHandle().getEntityData();
        processConvertedData(entity, internalData, (dataItem, converted) -> nmsData.set(dataItem.getAccessor(), converted));
    }
}