package hellfirepvp.astralsorcery.common.util.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * JSON parsing utilities for recipe and data deserialization.
 * Handles ItemStack, FluidStack, and Color deserialization from JSON.
 *
 * <p>1.16 → 1.20 changes:
 * JSONUtils → GsonHelper, CompoundNBT → CompoundTag,
 * JsonToNBT → TagParser, getTagFromJson → parseTag,
 * FluidAttributes.BUCKET_VOLUME → FluidType.BUCKET_VOLUME,
 * item.getRegistryName() → ForgeRegistries.ITEMS.getKey(item),
 * Fluid/Fluids import paths updated</p>
 */
public class JsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void parseMultipleStrings(@Nonnull JsonObject root,
                                            @Nonnull String key,
                                            @Nonnull Consumer<String> consumer) {
        consumeJsonListConfiguration(root, key, "String", "Strings",
                JsonElement::isJsonPrimitive, JsonElement::getAsString, consumer);
    }

    public static void parseMultipleJsonPrimitives(@Nonnull JsonObject root,
                                                   @Nonnull String key,
                                                   @Nonnull String singular,
                                                   @Nonnull String plural,
                                                   @Nonnull Consumer<JsonPrimitive> consumer) {
        consumeJsonListConfiguration(root, key, singular, plural,
                JsonElement::isJsonPrimitive, JsonElement::getAsJsonPrimitive, consumer);
    }

    public static void parseMultipleJsonObjects(@Nonnull JsonObject root,
                                                @Nonnull String key,
                                                @Nonnull Consumer<JsonObject> consumer) {
        consumeJsonListConfiguration(root, key, "JsonObject", "JsonObjects",
                JsonElement::isJsonObject, JsonElement::getAsJsonObject, consumer);
    }

    private static <T> void consumeJsonListConfiguration(
            @Nonnull JsonObject root,
            @Nonnull String key,
            @Nonnull String singular,
            @Nonnull String plural,
            @Nonnull Predicate<JsonElement> verifier,
            @Nonnull Function<JsonElement, T> consumerTransformer,
            @Nonnull Consumer<T> consumer) {
        if (!root.has(key)) {
            throw new JsonSyntaxException(
                    String.format("Expected '%s' to be a %s or an array of %s!", key, singular, plural));
        }
        JsonElement el = root.get(key);
        if (verifier.test(el)) {
            consumer.accept(consumerTransformer.apply(el));
        } else if (el.isJsonArray()) {
            JsonArray objectArray = el.getAsJsonArray();
            for (JsonElement arrayEl : objectArray) {
                if (!verifier.test(arrayEl)) {
                    throw new JsonSyntaxException(
                            String.format("Expected '%s' to be an array of %s!", key, plural));
                }
                consumer.accept(consumerTransformer.apply(arrayEl));
            }
        } else {
            throw new JsonSyntaxException(
                    String.format("Expected '%s' to be a %s or an array of %s!", key, singular, plural));
        }
    }

    @Nonnull
    public static FluidStack getFluidStack(@Nonnull JsonElement fluidElement,
                                           @Nonnull String infoKey) {
        FluidStack fluidStack;
        if (fluidElement.isJsonPrimitive() && ((JsonPrimitive) fluidElement).isString()) {
            String strKey = fluidElement.getAsString();
            ResourceLocation fluidKey = new ResourceLocation(strKey);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidKey);
            fluidStack = fluid != null ? new FluidStack(fluid, FluidType.BUCKET_VOLUME) : FluidStack.EMPTY;
        } else if (fluidElement.isJsonObject()) {
            fluidStack = getFluidStack(fluidElement.getAsJsonObject(), true);
        } else {
            throw new JsonSyntaxException(
                    "Missing " + infoKey + ", expected to find a string or object");
        }
        return fluidStack;
    }

    @Nonnull
    public static FluidStack getFluidStack(@Nonnull JsonObject json, boolean readNBT) {
        String fluidName = GsonHelper.getAsString(json, "fluid");
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        if (readNBT && json.has("nbt")) {
            try {
                JsonElement element = json.get("nbt");
                CompoundTag nbt;
                if (element.isJsonObject()) {
                    nbt = TagParser.parseTag(GSON.toJson(element));
                } else {
                    nbt = TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
                }

                CompoundTag tempRead = new CompoundTag();
                tempRead.put("Tag", nbt);
                tempRead.putString("FluidName", fluidName);
                tempRead.putInt("Amount",
                        GsonHelper.getAsInt(json, "amount", FluidType.BUCKET_VOLUME));

                return FluidStack.loadFluidStackFromNBT(tempRead);
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + e);
            }
        }
        return new FluidStack(fluid, GsonHelper.getAsInt(json, "amount", FluidType.BUCKET_VOLUME));
    }

    @Nonnull
    public static ItemStack getItemStack(@Nonnull JsonElement itemElement,
                                         @Nonnull String infoKey) {
        ItemStack itemstack;
        if (itemElement.isJsonPrimitive() && ((JsonPrimitive) itemElement).isString()) {
            String strKey = itemElement.getAsString();
            ResourceLocation itemKey = new ResourceLocation(strKey);
            net.minecraft.world.item.Item resolved = ForgeRegistries.ITEMS.getValue(itemKey);
            if (resolved == null) {
                throw new JsonSyntaxException("Unknown item '" + strKey + "'");
            }
            itemstack = new ItemStack(resolved);
        } else if (itemElement.isJsonObject()) {
            itemstack = CraftingHelper.getItemStack(itemElement.getAsJsonObject(), true);
        } else {
            throw new JsonSyntaxException(
                    "Missing " + infoKey + ", expected to find a string or object");
        }
        return itemstack;
    }

    @Nonnull
    public static ItemStack getItemStack(@Nonnull JsonObject root, @Nonnull String key) {
        if (!root.has(key)) {
            throw new JsonSyntaxException(
                    "Missing " + key + ", expected to find a string or object");
        }
        ItemStack itemstack;
        if (root.get(key).isJsonObject()) {
            itemstack = CraftingHelper.getItemStack(
                    GsonHelper.getAsJsonObject(root, key), true);
        } else {
            String strKey = GsonHelper.getAsString(root, key);
            ResourceLocation itemKey = new ResourceLocation(strKey);
            net.minecraft.world.item.Item resolved = ForgeRegistries.ITEMS.getValue(itemKey);
            if (resolved == null) {
                throw new JsonSyntaxException("Unknown item '" + strKey + "'");
            }
            itemstack = new ItemStack(resolved);
        }
        return itemstack;
    }

    @Nonnull
    public static JsonObject serializeItemStack(@Nonnull ItemStack stack) {
        JsonObject object = new JsonObject();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        object.addProperty("item", itemKey != null ? itemKey.toString() : "minecraft:air");
        object.addProperty("count", stack.getCount());
        net.minecraft.nbt.CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            object.addProperty("nbt", nbt.toString());
        }
        return object;
    }

    @Nonnull
    public static Color getColor(@Nonnull JsonObject object, @Nonnull String key) {
        String value = GsonHelper.getAsString(object, key);
        if (value.startsWith("0x")) {
            String hexNbr = value.substring(2);
            try {
                return new Color(Integer.parseInt(hexNbr, 16), true);
            } catch (NumberFormatException exc) {
                throw new JsonParseException(
                        "Expected " + hexNbr + " to be a hexadecimal string!", exc);
            }
        } else {
            try {
                return new Color(Integer.parseInt(value), true);
            } catch (NumberFormatException exc) {
                try {
                    return new Color(Integer.parseInt(value, 16), true);
                } catch (NumberFormatException e) {
                    throw new JsonParseException(
                            "Expected " + value + " to be a int or hexadecimal-number!", e);
                }
            }
        }
    }
}
