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
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block Transmutation recipe: block -> block conversion via concentrated starlight.
 * Not a standard workbench recipe; checked programmatically against block states.
 *
 * <p>1.16 -> 1.20 changes: same as other recipes.</p>
 */
public class BlockTransmutation implements Recipe<Container> {

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final BlockState inputState;
    @Nonnull
    private final BlockState outputState;
    @Nonnull
    private final ItemStack outputDisplay;
    private final double starlightRequired;

    public BlockTransmutation(@Nonnull ResourceLocation id, @Nonnull BlockState inputState,
                              @Nonnull BlockState outputState, @Nonnull ItemStack outputDisplay,
                              double starlightRequired) {
        this.id = id;
        this.inputState = inputState;
        this.outputState = outputState;
        this.outputDisplay = outputDisplay;
        this.starlightRequired = starlightRequired;
    }

    @Nonnull
    public BlockState getInputState() {
        return inputState;
    }

    @Nonnull
    public BlockState getOutputState() {
        return outputState;
    }

    @Nonnull
    public ItemStack getOutputDisplay() {
        return outputDisplay.copy();
    }

    public double getStarlightRequired() {
        return starlightRequired;
    }

    /**
     * Check if the given block state matches the input of this transmutation.
     */
    public boolean matchesInput(@Nonnull BlockState state) {
        return state.getBlock() == inputState.getBlock();
    }

    @Override
    public boolean matches(@Nonnull Container inv, @Nonnull Level level) {
        return false; // Not matched via recipe manager
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container inv, @Nonnull RegistryAccess access) {
        return outputDisplay.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess access) {
        return outputDisplay.copy();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.BLOCK_TRANSMUTATION.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.BLOCK_TRANSMUTATION.get();
    }

    public static class Serializer implements RecipeSerializer<BlockTransmutation> {

        @Nonnull
        @Override
        public BlockTransmutation fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            BlockState input = RecipeHelper.readBlockState(GsonHelper.getAsString(json, "input"));
            BlockState output = RecipeHelper.readBlockState(GsonHelper.getAsString(json, "output"));
            ItemStack display = json.has("outputDisplay")
                    ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "outputDisplay"))
                    : new ItemStack(output.getBlock());
            double starlight = GsonHelper.getAsFloat(json, "starlightRequired", 200.0F);
            return new BlockTransmutation(id, input, output, display, starlight);
        }

        @Nullable
        @Override
        public BlockTransmutation fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            BlockState input = RecipeHelper.readBlockStateFromNetwork(buf);
            BlockState output = RecipeHelper.readBlockStateFromNetwork(buf);
            ItemStack display = buf.readItem();
            double starlight = buf.readDouble();
            return new BlockTransmutation(id, input, output, display, starlight);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull BlockTransmutation recipe) {
            RecipeHelper.writeBlockState(buf, recipe.inputState);
            RecipeHelper.writeBlockState(buf, recipe.outputState);
            buf.writeItem(recipe.outputDisplay);
            buf.writeDouble(recipe.starlightRequired);
        }
    }
}
