package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageBlockTransmutation;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
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
 * Journal page that looks up a {@link BlockTransmutation} recipe by output predicate.
 *
 * <p>1.16 → 1.20: RecipeManager.getRecipes → getAllRecipesFor; RecipeTypesAS API updated.</p>
 */
public class JournalPageBlockTransmutation implements JournalPage {

    private final Supplier<BlockTransmutation> recipeProvider;

    private JournalPageBlockTransmutation(Supplier<BlockTransmutation> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    @SuppressWarnings("null")
    public static JournalPageBlockTransmutation fromOutput(Predicate<ItemStack> outputTest) {
        return new JournalPageBlockTransmutation(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) return null;
            return mgr.getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get())
                    .stream()
                    .filter(r -> outputTest.test(r.getOutputDisplay()))
                    .findFirst()
                    .orElse(null);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        BlockTransmutation recipe = recipeProvider.get();
        if (recipe != null) {
            return new RenderPageBlockTransmutation(node, nodePage, recipe);
        }
        return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
    }
}
