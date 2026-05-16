/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.integration.jei.category.AltarRecipeCategory;
import hellfirepvp.astralsorcery.common.integration.jei.category.InfuserRecipeCategory;
import hellfirepvp.astralsorcery.common.integration.jei.category.LiquidInteractionRecipeCategory;
import hellfirepvp.astralsorcery.common.integration.jei.category.TransmutationRecipeCategory;
import hellfirepvp.astralsorcery.common.integration.jei.category.WellRecipeCategory;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI plugin for Astral Sorcery. Registers recipe categories for:
 * <ul>
 *   <li>Altar crafting (all 4 tiers)</li>
 *   <li>Starlight infusion</li>
 *   <li>Lightwell liquefaction</li>
 *   <li>Block transmutation</li>
 *   <li>Liquid interaction</li>
 * </ul>
 *
 * <p>1.16 → 1.20: JEI API moved to builder pattern for categories.
 * {@code IRecipeCategoryRegistration} replaces the old registration method.
 * Recipe types use {@code RecipeType<T>} instead of raw UIDs.</p>
 */
@JeiPlugin
public class AstralSorceryJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID =
            new ResourceLocation(AstralSorcery.MODID, "jei_plugin");

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new AltarRecipeCategory(guiHelper),
                new InfuserRecipeCategory(guiHelper),
                new WellRecipeCategory(guiHelper),
                new TransmutationRecipeCategory(guiHelper),
                new LiquidInteractionRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        AltarRecipeCategory.registerCatalysts(registration);
        InfuserRecipeCategory.registerCatalysts(registration);
        WellRecipeCategory.registerCatalysts(registration);
        TransmutationRecipeCategory.registerCatalysts(registration);
        LiquidInteractionRecipeCategory.registerCatalysts(registration);
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<SimpleAltarRecipe> altarRecipes =
                recipeManager.getAllRecipesFor(RecipeTypesAS.ALTAR.get());
        registration.addRecipes(AltarRecipeCategory.RECIPE_TYPE, altarRecipes);

        List<LiquidInfusion> infuserRecipes =
                recipeManager.getAllRecipesFor(RecipeTypesAS.LIQUID_INFUSION.get());
        registration.addRecipes(InfuserRecipeCategory.RECIPE_TYPE, infuserRecipes);

        List<WellLiquefaction> wellRecipes =
                recipeManager.getAllRecipesFor(RecipeTypesAS.WELL_LIQUEFACTION.get());
        registration.addRecipes(WellRecipeCategory.RECIPE_TYPE, wellRecipes);

        List<BlockTransmutation> transmutationRecipes =
                recipeManager.getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get());
        registration.addRecipes(TransmutationRecipeCategory.RECIPE_TYPE, transmutationRecipes);

        List<LiquidInteraction> liquidInteractionRecipes =
                recipeManager.getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get());
        registration.addRecipes(LiquidInteractionRecipeCategory.RECIPE_TYPE, liquidInteractionRecipes);
    }
}
