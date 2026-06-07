package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Journal page that lazily looks up a recipe by output predicate.
 * Prefers altar recipes when {@code preferAltar = true}, vanilla crafting otherwise.
 *
 * <p>1.16 → 1.20: IRecipe → Recipe; IRecipeType.CRAFTING → RecipeType.CRAFTING;
 * getRecipeOutput → getResultItem(RegistryAccess.EMPTY);
 * RecipeManager.getRecipes(type) → getAllRecipesFor(type).</p>
 */
public class JournalPageRecipe implements JournalPage {

    private final Supplier<Recipe<?>> recipeProvider;

    private JournalPageRecipe(Supplier<Recipe<?>> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    /**
     * Looks up a recipe by its exact {@link ResourceLocation} name.
     * Used for named altar recipes (e.g. enchantment_amulet_init).
     */
    public static JournalPageRecipe fromName(ResourceLocation recipeName) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) return null;
            return mgr.byKey(recipeName).orElse(null);
        });
    }

    public static JournalPageRecipe fromOutputPreferAltarRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) return null;

            SimpleAltarRecipe altar = mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get())
                    .stream()
                    .filter(r -> outputTest.test(r.getOutput()))
                    .findFirst()
                    .orElse(null);
            if (altar != null) return altar;

            return findVanillaRecipe(mgr, outputTest);
        });
    }

    public static JournalPageRecipe fromOutputPreferVanillaRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) return null;

            Recipe<?> vanilla = findVanillaRecipe(mgr, outputTest);
            if (vanilla != null) return vanilla;

            return mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get())
                    .stream()
                    .filter(r -> outputTest.test(r.getOutput()))
                    .findFirst()
                    .orElse(null);
        });
    }

    @Nullable
    private static Recipe<?> findVanillaRecipe(RecipeManager mgr, Predicate<ItemStack> outputTest) {
        return mgr.getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .filter(r -> outputTest.test(r.getResultItem(RegistryAccess.EMPTY)))
                .findFirst()
                .orElse(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        Recipe<?> recipe = this.recipeProvider.get();
        if (recipe instanceof SimpleAltarRecipe altarRecipe) {
            return new RenderPageAltarRecipe(node, nodePage, altarRecipe);
        } else if (recipe != null) {
            return RenderPageRecipe.fromRecipe(node, nodePage, recipe);
        } else {
            return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
        }
    }
}
