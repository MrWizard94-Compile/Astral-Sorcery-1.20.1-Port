package hellfirepvp.astralsorcery.common.util.block;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Serialization/deserialization utilities for Block and BlockState.
 * Supports both string ("modid:block[prop=val,...]") and JSON formats.
 *
 * <p>1.16 → 1.20 changes:
 * block.getRegistryName() → ForgeRegistries.BLOCKS.getKey(block),
 * block.getDefaultState() → defaultBlockState(),
 * state.get() → getValue(), state.with() → setValue(),
 * property.parseValue() → getValue(),
 * JSONUtils → GsonHelper,
 * MiscUtils.iterativeSearch() → inline property lookup</p>
 */
public class BlockStateHelper {

    private static final Splitter PROP_SPLITTER = Splitter.on(',');
    private static final Splitter PROP_ELEMENT_SPLITTER = Splitter.on('=');

    @Nonnull
    public static String serialize(@Nonnull Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key != null ? key.toString() : "minecraft:air";
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <V extends Comparable<V>> String serialize(@Nonnull BlockState state) {
        ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        StringBuilder name = new StringBuilder(blockKey != null ? blockKey.toString() : "minecraft:air");
        List<Property<?>> props = new ArrayList<>(state.getProperties());
        if (!props.isEmpty()) {
            name.append('[');
            for (int i = 0; i < props.size(); i++) {
                Property<V> prop = (Property<V>) props.get(i);
                if (i > 0) {
                    name.append(',');
                }
                name.append(prop.getName());
                name.append('=');
                name.append(prop.getName(state.getValue(prop)));
            }
            name.append(']');
        }
        return name.toString();
    }

    @Nonnull
    public static JsonObject serializeObject(@Nonnull BlockState state, boolean serializeProperties) {
        JsonObject object = new JsonObject();
        serializeObject(object, state, serializeProperties);
        return object;
    }

    @SuppressWarnings("unchecked")
    public static <V extends Comparable<V>> void serializeObject(
            @Nonnull JsonObject out, @Nonnull BlockState state, boolean serializeProperties) {
        ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        out.addProperty("block", blockKey != null ? blockKey.toString() : "minecraft:air");
        if (serializeProperties && !state.getProperties().isEmpty()) {
            JsonArray properties = new JsonArray();
            for (Property<?> property : state.getProperties()) {
                Property<V> prop = (Property<V>) property;
                JsonObject objProperty = new JsonObject();
                objProperty.addProperty("name", prop.getName());
                objProperty.addProperty("value", prop.getName(state.getValue(prop)));
                properties.add(objProperty);
            }
            out.add("properties", properties);
        }
    }

    @Nonnull
    public static Block deserializeBlock(@Nonnull String serialized) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(serialized));
        return block == null ? Blocks.AIR : block;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState deserialize(@Nonnull String serialized) {
        int propIndex = serialized.indexOf('[');
        boolean hasProperties = !isMissingStateInformation(serialized);
        ResourceLocation key;
        if (hasProperties) {
            key = new ResourceLocation(serialized.substring(0, propIndex).toLowerCase(Locale.ROOT));
        } else {
            key = new ResourceLocation(serialized.toLowerCase(Locale.ROOT));
        }
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = block.defaultBlockState();
        if (!block.equals(Blocks.AIR) && hasProperties) {
            List<String> strProps = PROP_SPLITTER.splitToList(
                    serialized.substring(propIndex + 1, serialized.length() - 1));
            for (String serializedProperty : strProps) {
                List<String> propertyValues = PROP_ELEMENT_SPLITTER.splitToList(serializedProperty);
                if (propertyValues.size() < 2) continue;
                String name = propertyValues.get(0);
                String strValue = propertyValues.get(1);
                state = applyProperty(state, name, strValue);
            }
        }
        return state;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState deserializeObject(@Nonnull JsonObject object) {
        String key = GsonHelper.getAsString(object, "block");
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key));
        if (b == null || b instanceof AirBlock) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = b.defaultBlockState();
        if (isMissingStateInformation(object)) {
            return state;
        }
        if (object.has("properties")) {
            JsonArray properties = GsonHelper.getAsJsonArray(object, "properties");
            for (JsonElement elemProperty : properties) {
                JsonObject objProperty = GsonHelper.convertToJsonObject(elemProperty, "properties[?]");
                String propName = GsonHelper.getAsString(objProperty, "name");
                String propValue = GsonHelper.getAsString(objProperty, "value");
                state = applyProperty(state, propName, propValue);
            }
        }
        return state;
    }

    /**
     * Returns true if the JSON object does NOT contain block state property information.
     * (Note: Fixed inverted logic from original 1.16 source.)
     */
    public static boolean isMissingStateInformation(@Nonnull JsonObject serialized) {
        return !serialized.has("properties");
    }

    public static boolean isMissingStateInformation(@Nonnull String serialized) {
        return serialized.indexOf('[') == -1;
    }

    /**
     * Apply a named property value to a block state.
     * Replaces the MiscUtils.iterativeSearch call from 1.16 with an inline loop.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState applyProperty(
            @Nonnull BlockState state, @Nonnull String propName, @Nonnull String valueStr) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(propName)) {
                Property<T> typedProp = (Property<T>) prop;
                Optional<T> value = typedProp.getValue(valueStr);
                if (value.isPresent()) {
                    return state.setValue(typedProp, value.get());
                }
                break;
            }
        }
        return state;
    }
}
