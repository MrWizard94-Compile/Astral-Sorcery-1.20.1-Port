package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageLiquidInfusion;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Journal page that looks up a {@link LiquidInfusion} recipe by output predicate.
 *
 * <p>1.16 → 1.20: RecipeManager.getRecipes → getAllRecipesFor; LiquidInfusion.getOutput()
 * replaces getOutput(ItemStack.EMPTY).</p>
 */
public class JournalPageLiquidInfusion implements JournalPage {

    private final Supplier<LiquidInfusion> recipeProvider;

    private JournalPageLiquidInfusion(Supplier<LiquidInfusion> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    @SuppressWarnings("null")
    public static JournalPageLiquidInfusion fromOutput(Predicate<ItemStack> outputTest) {
        return new JournalPageLiquidInfusion(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) return null;
            return mgr.getAllRecipesFor(RecipeTypesAS.LIQUID_INFUSION.get())
                    .stream()
                    .filter(r -> outputTest.test(r.getOutput()))
                    .findFirst()
                    .orElse(null);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        LiquidInfusion recipe = recipeProvider.get();
        if (recipe != null) {
            return new RenderPageLiquidInfusion(node, nodePage, recipe);
        }
        return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
    }
}
