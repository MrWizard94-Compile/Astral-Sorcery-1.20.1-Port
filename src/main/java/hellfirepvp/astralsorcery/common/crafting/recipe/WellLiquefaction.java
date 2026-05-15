package hellfirepvp.astralsorcery.common.crafting.recipe;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Well Liquefaction recipe: an item dropped into a Lightwell produces fluid over time.
 *
 * <p>1.16 -> 1.20 changes:
 * IRecipe -> Recipe, IRecipeSerializer -> RecipeSerializer,
 * IRecipeType -> RecipeType, RegistryAccess param on assemble/getResultItem</p>
 */
public class WellLiquefaction implements Recipe<Container> {

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final Ingredient inputItem;
    @Nonnull
    private final FluidStack output;
    private final float productionMultiplier;
    private final int color;

    public WellLiquefaction(@Nonnull ResourceLocation id, @Nonnull Ingredient inputItem,
                            @Nonnull FluidStack output, float productionMultiplier, int color) {
        this.id = id;
        this.inputItem = inputItem;
        this.output = output;
        this.productionMultiplier = productionMultiplier;
        this.color = color;
    }

    @Nonnull
    public Ingredient getInputItem() {
        return inputItem;
    }

    @Nonnull
    public FluidStack getOutputFluid() {
        return output.copy();
    }

    public float getProductionMultiplier() {
        return productionMultiplier;
    }

    public int getColor() {
        return color;
    }

    public boolean matches(@Nonnull ItemStack stack) {
        return inputItem.test(stack);
    }

    @Override
    public boolean matches(@Nonnull Container inv, @Nonnull Level level) {
        return inputItem.test(inv.getItem(0));
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv, @Nonnull RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.WELL_LIQUEFACTION.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.WELL_LIQUEFACTION.get();
    }

    public static class Serializer implements RecipeSerializer<WellLiquefaction> {

        @Nonnull
        @Override
        public WellLiquefaction fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            FluidStack output = RecipeHelper.readFluidStack(GsonHelper.getAsJsonObject(json, "output"));
            float multiplier = GsonHelper.getAsFloat(json, "productionMultiplier", 1.0F);
            int color = GsonHelper.getAsInt(json, "color", 0x4466FF);
            return new WellLiquefaction(id, input, output, multiplier, color);
        }

        @Nullable
        @Override
        public WellLiquefaction fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            FluidStack output = RecipeHelper.readFluidStackFromNetwork(buf);
            float multiplier = buf.readFloat();
            int color = buf.readInt();
            return new WellLiquefaction(id, input, output, multiplier, color);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull WellLiquefaction recipe) {
            recipe.inputItem.toNetwork(buf);
            RecipeHelper.writeFluidStack(buf, recipe.output);
            buf.writeFloat(recipe.productionMultiplier);
            buf.writeInt(recipe.color);
        }
    }
}
