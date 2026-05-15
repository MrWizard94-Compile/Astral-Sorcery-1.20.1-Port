package hellfirepvp.astralsorcery.common.crafting.recipe;

import com.google.gson.JsonObject;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Liquid Infusion recipe: item + liquid starlight in the Infuser -> output item.
 *
 * <p>1.16 -> 1.20 changes: same as WellLiquefaction.</p>
 */
public class LiquidInfusion implements Recipe<Container> {

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final Ingredient inputItem;
    @Nonnull
    private final ItemStack output;
    private final int fluidMbRequired;
    private final int craftDuration;
    @Nullable
    private final ResourceLocation requiredConstellation;
    private final boolean consumeMultiple;

    public LiquidInfusion(@Nonnull ResourceLocation id, @Nonnull Ingredient inputItem,
                          @Nonnull ItemStack output, int fluidMbRequired, int craftDuration,
                          @Nullable ResourceLocation requiredConstellation, boolean consumeMultiple) {
        this.id = id;
        this.inputItem = inputItem;
        this.output = output;
        this.fluidMbRequired = fluidMbRequired;
        this.craftDuration = craftDuration;
        this.requiredConstellation = requiredConstellation;
        this.consumeMultiple = consumeMultiple;
    }

    @Nonnull
    public Ingredient getInputItem() {
        return inputItem;
    }

    @Nonnull
    public ItemStack getOutput() {
        return output.copy();
    }

    public int getFluidMbRequired() {
        return fluidMbRequired;
    }

    public int getCraftDuration() {
        return craftDuration;
    }

    @Nullable
    public ResourceLocation getRequiredConstellation() {
        return requiredConstellation;
    }

    public boolean doesConsumeMultiple() {
        return consumeMultiple;
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
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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
        return RecipeSerializersAS.LIQUID_INFUSION.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.LIQUID_INFUSION.get();
    }

    public static class Serializer implements RecipeSerializer<LiquidInfusion> {

        @Nonnull
        @Override
        public LiquidInfusion fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int fluidMb = GsonHelper.getAsInt(json, "fluidMbRequired", 500);
            int duration = GsonHelper.getAsInt(json, "craftDuration", 200);
            ResourceLocation constellation = json.has("requiredConstellation")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "requiredConstellation"))
                    : null;
            boolean multi = GsonHelper.getAsBoolean(json, "consumeMultiple", false);
            return new LiquidInfusion(id, input, output, fluidMb, duration, constellation, multi);
        }

        @Nullable
        @Override
        public LiquidInfusion fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            int fluidMb = buf.readVarInt();
            int duration = buf.readVarInt();
            ResourceLocation constellation = buf.readBoolean() ? buf.readResourceLocation() : null;
            boolean multi = buf.readBoolean();
            return new LiquidInfusion(id, input, output, fluidMb, duration, constellation, multi);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull LiquidInfusion recipe) {
            recipe.inputItem.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeVarInt(recipe.fluidMbRequired);
            buf.writeVarInt(recipe.craftDuration);
            if (recipe.requiredConstellation != null) {
                buf.writeBoolean(true);
                buf.writeResourceLocation(recipe.requiredConstellation);
            } else {
                buf.writeBoolean(false);
            }
            buf.writeBoolean(recipe.consumeMultiple);
        }
    }
}
