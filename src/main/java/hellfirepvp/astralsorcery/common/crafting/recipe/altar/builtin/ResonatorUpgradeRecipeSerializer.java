package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Serializer for resonator upgrade recipes.
 * JSON format: altar_type, target_upgrade ("fluid_fields" or "area_size"),
 * craft_duration, starlight_required, inputs (flat array). No output field.
 */
public class ResonatorUpgradeRecipeSerializer implements RecipeSerializer<SimpleAltarRecipe> {

    @Nonnull
    @Override
    public SimpleAltarRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
        BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(
                GsonHelper.getAsString(json, "altar_type").toUpperCase(Locale.ROOT));
        ItemResonator.ResonatorUpgrade targetUpgrade = ItemResonator.ResonatorUpgrade.valueOf(
                GsonHelper.getAsString(json, "target_upgrade").toUpperCase(Locale.ROOT));
        int duration = GsonHelper.getAsInt(json, "craft_duration", 200);
        double starlight = GsonHelper.getAsFloat(json, "starlight_required", 1000.0F);

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

        return new ResonatorUpgradeRecipe(id, altarType, duration, starlight, inputs, targetUpgrade);
    }

    @Nullable
    @Override
    public SimpleAltarRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
        BlockAltar.AltarType altarType = buf.readEnum(BlockAltar.AltarType.class);
        ItemResonator.ResonatorUpgrade targetUpgrade = buf.readEnum(ItemResonator.ResonatorUpgrade.class);
        int duration = buf.readVarInt();
        double starlight = buf.readDouble();
        int inputCount = buf.readVarInt();
        NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);
        for (int i = 0; i < inputCount; i++) {
            inputs.set(i, Ingredient.fromNetwork(buf));
        }
        return new ResonatorUpgradeRecipe(id, altarType, duration, starlight, inputs, targetUpgrade);
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull SimpleAltarRecipe recipe) {
        buf.writeEnum(recipe.getAltarType());
        ItemResonator.ResonatorUpgrade target = recipe instanceof ResonatorUpgradeRecipe r
                ? r.getTargetUpgrade()
                : ItemResonator.ResonatorUpgrade.FLUID_FIELDS;
        buf.writeEnum(target);
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
