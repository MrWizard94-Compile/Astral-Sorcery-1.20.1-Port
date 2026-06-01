/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.integration.jei.category;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
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
 * JEI recipe category for liquid interaction recipes.
 * Shows two input fluids -> output item.
 */
public class LiquidInteractionRecipeCategory implements IRecipeCategory<LiquidInteraction> {

    public static final RecipeType<LiquidInteraction> RECIPE_TYPE =
            RecipeType.create(AstralSorcery.MODID, "liquid_interaction", LiquidInteraction.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/jei/interaction.png");

    private final IDrawable background;
    private final IDrawable icon;

    public LiquidInteractionRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.CHALICE.get()));
    }

    @Override
    @Nonnull
    public RecipeType<LiquidInteraction> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.translatable("jei.astralsorcery.liquid_interaction");
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
                           @Nonnull LiquidInteraction recipe,
                           @Nonnull IFocusGroup focuses) {
        // Two fluid inputs
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addFluidStack(recipe.getInputFluid1().getFluid(), recipe.getInputFluid1().getAmount());

        builder.addSlot(RecipeIngredientRole.INPUT, 40, 19)
                .addFluidStack(recipe.getInputFluid2().getFluid(), recipe.getInputFluid2().getAmount());

        // Item output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(recipe.getOutputItem());
    }

    public static void registerCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlocksAS.CHALICE.get()), RECIPE_TYPE);
    }
}
