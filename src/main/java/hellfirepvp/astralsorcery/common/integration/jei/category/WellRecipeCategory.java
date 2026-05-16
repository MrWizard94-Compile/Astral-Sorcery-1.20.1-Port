/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.integration.jei.category;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
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
 * JEI recipe category for Lightwell liquefaction recipes.
 * Shows catalyst item → liquid output.
 */
public class WellRecipeCategory implements IRecipeCategory<WellLiquefaction> {

    public static final RecipeType<WellLiquefaction> RECIPE_TYPE =
            RecipeType.create(AstralSorcery.MODID, "well", WellLiquefaction.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/jei/well.png");

    private final IDrawable background;
    private final IDrawable icon;

    public WellRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.WELL.get()));
    }

    @Override
    @Nonnull
    public RecipeType<WellLiquefaction> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.translatable("block.astralsorcery.well");
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
                           @Nonnull WellLiquefaction recipe,
                           @Nonnull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addIngredients(recipe.getInputItem());

        // Output is fluid — display as a fluid stack
        // The well converts catalyst items to liquid starlight over time
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addFluidStack(recipe.getOutputFluid().getFluid(), recipe.getOutputFluid().getAmount());
    }

    public static void registerCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlocksAS.WELL.get()), RECIPE_TYPE);
    }
}
