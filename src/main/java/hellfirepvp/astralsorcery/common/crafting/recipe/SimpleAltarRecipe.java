package hellfirepvp.astralsorcery.common.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Altar Crafting recipe — the primary crafting mechanism of Astral Sorcery.
 * Supports all four altar tiers with varying grid sizes:
 * <ul>
 *   <li>Discovery: 3x3 grid (9 slots)</li>
 *   <li>Attunement: 5x5 cross (13 slots)</li>
 *   <li>Constellation: 5x5 full (21 slots)</li>
 *   <li>Radiance: 5x5 + outer ring (25 slots)</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes:
 * IRecipe -> Recipe, IRecipeSerializer -> RecipeSerializer,
 * IRecipeType -> RecipeType, RegistryAccess on assemble/getResultItem,
 * NonNullList for ingredients, IStringSerializable -> StringRepresentable</p>
 */
public class SimpleAltarRecipe implements Recipe<Container> {

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final BlockAltar.AltarType altarType;
    private final int craftDuration;
    private final double starlightRequired;
    @Nonnull
    private final ItemStack output;
    @Nonnull
    private final NonNullList<Ingredient> inputs;
    @Nullable
    private final ResourceLocation focusConstellation;

    public SimpleAltarRecipe(@Nonnull ResourceLocation id,
                             @Nonnull BlockAltar.AltarType altarType,
                             int craftDuration, double starlightRequired,
                             @Nonnull ItemStack output,
                             @Nonnull NonNullList<Ingredient> inputs,
                             @Nullable ResourceLocation focusConstellation) {
        this.id = id;
        this.altarType = altarType;
        this.craftDuration = craftDuration;
        this.starlightRequired = starlightRequired;
        this.output = output;
        this.inputs = inputs;
        this.focusConstellation = focusConstellation;
    }

    @Nonnull
    public BlockAltar.AltarType getAltarType() {
        return altarType;
    }

    public int getCraftDuration() {
        return craftDuration;
    }

    public double getStarlightRequired() {
        return starlightRequired;
    }

    @Nonnull
    public ItemStack getOutput() {
        return output.copy();
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Nullable
    public ResourceLocation getFocusConstellation() {
        return focusConstellation;
    }

    /**
     * Get the expected slot count for this recipe's altar tier.
     */
    public int getExpectedSlotCount() {
        return switch (altarType) {
            case DISCOVERY -> 9;
            case ATTUNEMENT -> 13;
            case CONSTELLATION -> 21;
            case RADIANCE -> 25;
        };
    }

    @Override
    public boolean matches(@Nonnull Container inv, @Nonnull Level level) {
        int slotCount = getExpectedSlotCount();
        if (inv.getContainerSize() < slotCount) {
            return false;
        }
        for (int i = 0; i < slotCount; i++) {
            Ingredient required = i < inputs.size() ? inputs.get(i) : Ingredient.EMPTY;
            if (!required.test(inv.getItem(i))) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv, @Nonnull RegistryAccess access) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= getExpectedSlotCount();
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess access) {
        return output.copy();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.ALTAR.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.ALTAR.get();
    }

    public static class Serializer implements RecipeSerializer<SimpleAltarRecipe> {

        @Nonnull
        @Override
        public SimpleAltarRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            String typeStr = GsonHelper.getAsString(json, "altar_type");
            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));

            int duration = GsonHelper.getAsInt(json, "craft_duration", 100);
            double starlight = GsonHelper.getAsFloat(json, "starlight_required", 200.0F);

            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            JsonArray inputsArr = GsonHelper.getAsJsonArray(json, "inputs");
            int expectedSlots = slotCountForType(altarType);
            NonNullList<Ingredient> inputs = NonNullList.withSize(expectedSlots, Ingredient.EMPTY);
            for (int i = 0; i < Math.min(inputsArr.size(), expectedSlots); i++) {
                if (!inputsArr.get(i).isJsonObject()
                        || inputsArr.get(i).getAsJsonObject().entrySet().isEmpty()) {
                    inputs.set(i, Ingredient.EMPTY);
                } else {
                    inputs.set(i, Ingredient.fromJson(inputsArr.get(i)));
                }
            }

            ResourceLocation constellation = json.has("focus_constellation")
                    && !json.get("focus_constellation").isJsonNull()
                    ? new ResourceLocation(GsonHelper.getAsString(json, "focus_constellation"))
                    : null;

            return new SimpleAltarRecipe(id, altarType, duration, starlight,
                    output, inputs, constellation);
        }

        @Nullable
        @Override
        public SimpleAltarRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            BlockAltar.AltarType altarType = buf.readEnum(BlockAltar.AltarType.class);
            int duration = buf.readVarInt();
            double starlight = buf.readDouble();
            ItemStack output = buf.readItem();

            int inputCount = buf.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);
            for (int i = 0; i < inputCount; i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ResourceLocation constellation = buf.readBoolean() ? buf.readResourceLocation() : null;
            return new SimpleAltarRecipe(id, altarType, duration, starlight,
                    output, inputs, constellation);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull SimpleAltarRecipe recipe) {
            buf.writeEnum(recipe.altarType);
            buf.writeVarInt(recipe.craftDuration);
            buf.writeDouble(recipe.starlightRequired);
            buf.writeItem(recipe.output);

            buf.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.toNetwork(buf);
            }

            if (recipe.focusConstellation != null) {
                buf.writeBoolean(true);
                buf.writeResourceLocation(recipe.focusConstellation);
            } else {
                buf.writeBoolean(false);
            }
        }

        private static int slotCountForType(@Nonnull BlockAltar.AltarType type) {
            return switch (type) {
                case DISCOVERY -> 9;
                case ATTUNEMENT -> 13;
                case CONSTELLATION -> 21;
                case RADIANCE -> 25;
            };
        }
    }
}
