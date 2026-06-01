/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.integration.jei.category;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI recipe category for Astral Sorcery altar crafting.
 * Supports all 4 altar tiers (Discovery, Attunement, Constellation, Radiance).
 *
 * <p>1.16 → 1.20: Uses {@link RecipeType} instead of string UID.
 * Layout built via {@link IRecipeLayoutBuilder} instead of the old
 * init/set pattern. {@code IRecipeCategoryRegistration} builder pattern.</p>
 */
public class AltarRecipeCategory implements IRecipeCategory<SimpleAltarRecipe> {

    public static final RecipeType<SimpleAltarRecipe> RECIPE_TYPE =
            RecipeType.create(AstralSorcery.MODID, "altar", SimpleAltarRecipe.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/jei/altar_discovery.png");

    private static final int BG_WIDTH = 116;
    private static final int BG_HEIGHT = 54;

    private final IDrawable background;
    private final IDrawable icon;

    public AltarRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, BG_WIDTH, BG_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(BlocksAS.ALTAR.get()));
    }

    @Override
    @Nonnull
    public RecipeType<SimpleAltarRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.translatable("container.astralsorcery.altar_discovery");
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
                           @Nonnull SimpleAltarRecipe recipe,
                           @Nonnull IFocusGroup focuses) {
        List<Ingredient> inputs = recipe.getIngredients();

        // 3x3 grid layout
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                if (index < inputs.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                            .addIngredients(inputs.get(index));
                }
            }
        }

        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(recipe.getOutput());
    }

    public static void registerCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlocksAS.ALTAR.get()), RECIPE_TYPE);
    }
}
