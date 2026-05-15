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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Liquid Interaction recipe: two fluids meeting produce an item or block change.
 * Checked programmatically when fluids touch, not via recipe manager matching.
 *
 * <p>1.16 -> 1.20 changes: same as other recipes.</p>
 */
public class LiquidInteraction implements Recipe<Container> {

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final FluidStack inputFluid1;
    @Nonnull
    private final FluidStack inputFluid2;
    private final float weight;
    @Nonnull
    private final ItemStack outputItem;

    public LiquidInteraction(@Nonnull ResourceLocation id, @Nonnull FluidStack inputFluid1,
                             @Nonnull FluidStack inputFluid2, float weight,
                             @Nonnull ItemStack outputItem) {
        this.id = id;
        this.inputFluid1 = inputFluid1;
        this.inputFluid2 = inputFluid2;
        this.weight = weight;
        this.outputItem = outputItem;
    }

    @Nonnull
    public FluidStack getInputFluid1() {
        return inputFluid1.copy();
    }

    @Nonnull
    public FluidStack getInputFluid2() {
        return inputFluid2.copy();
    }

    public float getWeight() {
        return weight;
    }

    @Nonnull
    public ItemStack getOutputItem() {
        return outputItem.copy();
    }

    /**
     * Check if the given fluid pair matches this interaction (order-independent).
     */
    public boolean matchesFluids(@Nonnull FluidStack fluid1, @Nonnull FluidStack fluid2) {
        boolean direct = fluid1.getFluid() == inputFluid1.getFluid()
                && fluid2.getFluid() == inputFluid2.getFluid();
        boolean reverse = fluid1.getFluid() == inputFluid2.getFluid()
                && fluid2.getFluid() == inputFluid1.getFluid();
        return direct || reverse;
    }

    @Override
    public boolean matches(@Nonnull Container inv, @Nonnull Level level) {
        return false; // Not matched via recipe manager
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv, @Nonnull RegistryAccess access) {
        return outputItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess access) {
        return outputItem.copy();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.LIQUID_INTERACTION.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.LIQUID_INTERACTION.get();
    }

    public static class Serializer implements RecipeSerializer<LiquidInteraction> {

        @Nonnull
        @Override
        public LiquidInteraction fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            FluidStack fluid1 = RecipeHelper.readFluidStack(GsonHelper.getAsJsonObject(json, "fluid1"));
            FluidStack fluid2 = RecipeHelper.readFluidStack(GsonHelper.getAsJsonObject(json, "fluid2"));
            float weight = GsonHelper.getAsFloat(json, "weight", 1.0F);
            ItemStack output = json.has("output")
                    ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"))
                    : ItemStack.EMPTY;
            return new LiquidInteraction(id, fluid1, fluid2, weight, output);
        }

        @Nullable
        @Override
        public LiquidInteraction fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            FluidStack fluid1 = RecipeHelper.readFluidStackFromNetwork(buf);
            FluidStack fluid2 = RecipeHelper.readFluidStackFromNetwork(buf);
            float weight = buf.readFloat();
            ItemStack output = buf.readItem();
            return new LiquidInteraction(id, fluid1, fluid2, weight, output);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull LiquidInteraction recipe) {
            RecipeHelper.writeFluidStack(buf, recipe.inputFluid1);
            RecipeHelper.writeFluidStack(buf, recipe.inputFluid2);
            buf.writeFloat(recipe.weight);
            buf.writeItem(recipe.outputItem);
        }
    }
}
