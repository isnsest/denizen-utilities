package com.isnsest.denizenutilities.nms.v26_2.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v26_2.helpers.EntityDataNameMapper;
import com.denizenscript.denizen.nms.v26_2.helpers.EntityHelperImpl;
import com.denizenscript.denizen.nms.v26_2.helpers.PacketHelperImpl;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.commands.core.ReflectionSetCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FixedEntityHelperImpl extends EntityHelperImpl {

    private static MethodHandle CREATE_DATA_HANDLE;
    private static MethodHandle SET_DATA_HANDLE;

    public static void patch() {
        try {
            Field helperField = NMSHandler.class.getField("entityHelper");
            helperField.setAccessible(true);
            helperField.set(null, new FixedEntityHelperImpl());

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            for (Method m : PacketHelperImpl.class.getDeclaredMethods()) {
                if (m.getName().equals("createEntityData") && m.getParameterCount() == 2) {
                    m.setAccessible(true);
                    CREATE_DATA_HANDLE = lookup.unreflect(m);
                    break;
                }
            }

            for (Method m : SynchedEntityData.class.getDeclaredMethods()) {
                if (m.getName().equals("set") && m.getParameterCount() == 2) {
                    m.setAccessible(true);
                    SET_DATA_HANDLE = lookup.unreflect(m);
                    break;
                }
            }
        } catch (Exception ignored) {

        }
    }

    @SuppressWarnings("unchecked")
    public Int2ObjectMap<SynchedEntityData.DataItem<Object>> getFixedDataItems(Entity entity) {
        try {
            Object itemsObj = SynchedEntityData_itemsById.get(((CraftEntity) entity).getHandle().getEntityData());
            if (itemsObj instanceof Int2ObjectMap) {
                return (Int2ObjectMap<SynchedEntityData.DataItem<Object>>) itemsObj;
            } else if (itemsObj instanceof SynchedEntityData.DataItem<?>[] array) {
                Int2ObjectMap<SynchedEntityData.DataItem<Object>> map = new Int2ObjectOpenHashMap<>();
                for (int i = 0; i < array.length; i++) {
                    if (array[i] != null) {
                        map.put(i, (SynchedEntityData.DataItem<Object>) array[i]);
                    }
                }
                return map;
            }
            return new Int2ObjectOpenHashMap<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processConvertedData(Entity entity, MapTag internalData, BiConsumer<SynchedEntityData.DataItem<Object>, Object> processConverted) {
        Int2ObjectMap<SynchedEntityData.DataItem<Object>> dataItemsById = getFixedDataItems(entity);
        for (Map.Entry<StringHolder, ObjectTag> entry : internalData.entrySet()) {
            int id = EntityDataNameMapper.getIdForName(((CraftEntity) entity).getHandle().getClass(), entry.getKey().low);
            if (id == -1) {
                Debug.echoError("Invalid internal data key: " + entry.getKey().low);
                continue;
            }
            SynchedEntityData.DataItem<Object> dataItem = dataItemsById.get(id);
            if (dataItem == null) continue;

            Object converted = ReflectionSetCommand.convertObjectTypeFor(dataItem.getValue().getClass(), entry.getValue());
            if (converted != null) {
                processConverted.accept(dataItem, converted);
            }
        }
    }

    @Override
    public List<Object> convertInternalEntityDataValues(Entity entity, MapTag internalData) {
        List<Object> dataValues = new ArrayList<>(internalData.size());
        processConvertedData(entity, internalData, (dataItem, converted) -> {
            try {
                dataValues.add(CREATE_DATA_HANDLE.invoke(dataItem.getAccessor(), converted));
            } catch (Throwable t) {
                Debug.echoError(t);
            }
        });
        return dataValues;
    }

    @Override
    public void modifyInternalEntityData(Entity entity, MapTag internalData) {
        SynchedEntityData nmsData = ((CraftEntity) entity).getHandle().getEntityData();
        processConvertedData(entity, internalData, (dataItem, converted) -> {
            try {
                SET_DATA_HANDLE.invoke(nmsData, dataItem.getAccessor(), converted);
            } catch (Throwable t) {
                Debug.echoError(t);
            }
        });
    }
}