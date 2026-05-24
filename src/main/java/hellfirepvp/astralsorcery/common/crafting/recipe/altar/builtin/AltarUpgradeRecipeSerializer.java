package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Serializer for altar upgrade recipes (attunement, constellation, radiance).
 * Reads `altar_type` (current tier), `target_altar_type` (target tier),
 * and `inputs`; no output item (the upgrade is in-place block state change).
 */
public class AltarUpgradeRecipeSerializer implements RecipeSerializer<SimpleAltarRecipe> {

    @Nonnull
    @Override
    public SimpleAltarRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
        BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(
                GsonHelper.getAsString(json, "altar_type").toUpperCase(Locale.ROOT));
        BlockAltar.AltarType targetType = BlockAltar.AltarType.valueOf(
                GsonHelper.getAsString(json, "target_altar_type").toUpperCase(Locale.ROOT));
        int duration = GsonHelper.getAsInt(json, "craft_duration", 100);
        double starlight = GsonHelper.getAsFloat(json, "starlight_required", 200.0F);

        JsonArray inputsArr = GsonHelper.getAsJsonArray(json, "inputs");
        int slotCount = slotCountFor(altarType);
        NonNullList<Ingredient> inputs = NonNullList.withSize(slotCount, Ingredient.EMPTY);
        for (int i = 0; i < Math.min(inputsArr.size(), slotCount); i++) {
            JsonElement el = inputsArr.get(i);
            if (!el.isJsonObject() || el.getAsJsonObject().entrySet().isEmpty()) {
                inputs.set(i, Ingredient.EMPTY);
            } else {
                inputs.set(i, Ingredient.fromJson(el));
            }
        }

        SimpleAltarRecipe base = new SimpleAltarRecipe(id, altarType, duration, starlight,
                ItemStack.EMPTY, inputs, null);
        return switch (targetType) {
            case ATTUNEMENT -> AttunementUpgradeRecipe.convertToThis(base);
            case CONSTELLATION -> ConstellationUpgradeRecipe.convertToThis(base);
            case RADIANCE -> TraitUpgradeRecipe.convertToThis(base);
            default -> base;
        };
    }

    @Nullable
    @Override
    public SimpleAltarRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
        BlockAltar.AltarType altarType = buf.readEnum(BlockAltar.AltarType.class);
        BlockAltar.AltarType targetType = buf.readEnum(BlockAltar.AltarType.class);
        int duration = buf.readVarInt();
        double starlight = buf.readDouble();
        int inputCount = buf.readVarInt();
        NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);
        for (int i = 0; i < inputCount; i++) {
            inputs.set(i, Ingredient.fromNetwork(buf));
        }
        SimpleAltarRecipe base = new SimpleAltarRecipe(id, altarType, duration, starlight,
                ItemStack.EMPTY, inputs, null);
        return switch (targetType) {
            case ATTUNEMENT -> AttunementUpgradeRecipe.convertToThis(base);
            case CONSTELLATION -> ConstellationUpgradeRecipe.convertToThis(base);
            case RADIANCE -> TraitUpgradeRecipe.convertToThis(base);
            default -> base;
        };
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull SimpleAltarRecipe recipe) {
        buf.writeEnum(recipe.getAltarType());
        BlockAltar.AltarType targetType;
        if (recipe instanceof AttunementUpgradeRecipe) {
            targetType = BlockAltar.AltarType.ATTUNEMENT;
        } else if (recipe instanceof ConstellationUpgradeRecipe) {
            targetType = BlockAltar.AltarType.CONSTELLATION;
        } else {
            targetType = BlockAltar.AltarType.RADIANCE;
        }
        buf.writeEnum(targetType);
        buf.writeVarInt(recipe.getCraftDuration());
        buf.writeDouble(recipe.getStarlightRequired());
        buf.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ing : recipe.getIngredients()) {
            ing.toNetwork(buf);
        }
    }

    private static int slotCountFor(BlockAltar.AltarType type) {
        return switch (type) {
            case DISCOVERY -> 9;
            case ATTUNEMENT -> 13;
            case CONSTELLATION -> 21;
            case RADIANCE -> 25;
        };
    }
}
