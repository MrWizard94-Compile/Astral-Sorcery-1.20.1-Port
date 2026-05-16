/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.integration.jei.category;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * JEI recipe category for block transmutation recipes.
 * Shows input block + starlight -> output block.
 */
public class TransmutationRecipeCategory implements IRecipeCategory<BlockTransmutation> {

    public static final RecipeType<BlockTransmutation> RECIPE_TYPE =
            RecipeType.create(AstralSorcery.MODID, "transmutation", BlockTransmutation.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/jei/transmutation.png");

    private final IDrawable background;
    private final IDrawable icon;

    public TransmutationRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.LENS.get()));
    }

    @Override
    @Nonnull
    public RecipeType<BlockTransmutation> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.translatable("jei.astralsorcery.transmutation");
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Override
    @Nonnull
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder,
                           @Nonnull BlockTransmutation recipe,
                           @Nonnull IFocusGroup focuses) {
        // Input block shown as its item form
        ItemStack inputDisplay = new ItemStack(recipe.getInputState().getBlock().asItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStack(inputDisplay);

        // Output block shown as display item
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(recipe.getOutputDisplay());
    }

    public static void registerCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlocksAS.LENS.get()), RECIPE_TYPE);
    }
}
