/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Custom crafting recipe that applies a DyeColor to a dyeable AS item.
 * Used for the Illumination Wand and Celestial Gateway beam color.
 *
 * <p>Pattern: place the target item and a single dye anywhere in the crafting grid.
 * Only these two items may be present.</p>
 *
 * <p>1.16 → 1.20: SpecialRecipe → CustomRecipe, SpecialRecipeSerializer → SimpleCraftingRecipeSerializer,
 * CraftingInventory → CraftingContainer, IRecipeSerializer → RecipeSerializer,
 * SimpleCraftingRecipeSerializer.Factory now takes (ResourceLocation, CraftingBookCategory).</p>
 */
public class RecipeDyeableChangeColor extends CustomRecipe {

    private final Supplier<RecipeSerializer<?>> serializer;
    private final Item targetItem;
    private final BiConsumer<ItemStack, DyeColor> colorFn;

    public RecipeDyeableChangeColor(@Nonnull ResourceLocation id,
                                     @Nonnull CraftingBookCategory category,
                                     @Nonnull Supplier<RecipeSerializer<?>> serializer,
                                     @Nonnull Item targetItem,
                                     @Nonnull BiConsumer<ItemStack, DyeColor> colorFn) {
        super(id, category);
        this.serializer = serializer;
        this.targetItem = targetItem;
        this.colorFn = colorFn;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer input, @Nonnull Level level) {
        return tryFindValidRecipeAndDye(input) != null;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingContainer input, @Nonnull RegistryAccess registryAccess) {
        Result result = tryFindValidRecipeAndDye(input);
        if (result == null) return ItemStack.EMPTY;
        ItemStack out = result.item.copy();
        out.setCount(1);
        colorFn.accept(out, result.color);
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }

    @Nullable
    private Result tryFindValidRecipeAndDye(@Nonnull CraftingContainer input) {
        ItemStack itemFound = ItemStack.EMPTY;
        DyeColor dyeColorFound = null;
        int nonEmptyCount = 0;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack in = input.getItem(i);
            if (in.isEmpty()) continue;
            nonEmptyCount++;

            if (in.getItem() == targetItem) {
                itemFound = in;
            } else {
                DyeColor color = DyeColor.getColor(in);
                if (color != null) {
                    dyeColorFound = color;
                }
            }
        }

        if (itemFound.isEmpty() || dyeColorFound == null || nonEmptyCount != 2) {
            return null;
        }
        return new Result(itemFound, dyeColorFound);
    }

    private record Result(@Nonnull ItemStack item, @Nonnull DyeColor color) {}

    // =========================================================================
    // Serializer sub-classes (one per dyeable item)
    // =========================================================================

    public static class IlluminationWandColorSerializer
            extends SimpleCraftingRecipeSerializer<RecipeDyeableChangeColor> {

        public IlluminationWandColorSerializer() {
            super((id, category) -> new RecipeDyeableChangeColor(
                    id, category,
                    () -> RecipeSerializersAS.CHANGE_WAND_COLOR.get(),
                    ItemsAS.ILLUMINATION_WAND.get(),
                    ItemIlluminationWand::setConfiguredColor));
        }
    }

    public static class GatewayColorSerializer
            extends SimpleCraftingRecipeSerializer<RecipeDyeableChangeColor> {

        public GatewayColorSerializer() {
            super((id, category) -> new RecipeDyeableChangeColor(
                    id, category,
                    () -> RecipeSerializersAS.CHANGE_GATEWAY_COLOR.get(),
                    BlocksAS.GATEWAY.get().asItem(),
                    BlockEntityGateway::setItemColor));
        }
    }
}
