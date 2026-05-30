package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Journal page renderer for vanilla crafting recipes (3×3 grid).
 *
 * <p>1.16 → 1.20: IRecipe → Recipe; getRecipeOutput → getResultItem with RegistryAccess.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderPageRecipe extends RenderPageRecipeTemplate {

    private final Map<Integer, Ingredient> inputs;
    private final ItemStack output;
    private final ResourceLocation recipeId;

    private RenderPageRecipe(@Nullable ResearchNode node, int nodePage,
                              Map<Integer, Ingredient> inputs, ItemStack output, ResourceLocation recipeId) {
        super(node, nodePage);
        this.inputs = inputs;
        this.output = output;
        this.recipeId = recipeId;
    }

    @SuppressWarnings("null")
    public static RenderPageRecipe fromRecipe(@Nullable ResearchNode node, int nodePage, Recipe<?> recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        Map<Integer, Ingredient> inputs = new HashMap<>();
        for (int i = 0; i < 9; i++) inputs.put(i, Ingredient.EMPTY);

        boolean single = ingredients.size() == 1;
        for (int xx = 0; xx < 3; xx++) {
            for (int zz = 0; zz < 3; zz++) {
                int slot = xx + zz * 3;
                int idx = single ? slot - 4 : slot;
                if (idx >= 0 && idx < ingredients.size()) {
                    inputs.put(slot, ingredients.get(idx));
                }
            }
        }
        // Use getResultItem with empty RegistryAccess for display purposes
        ItemStack output = recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY);
        return new RenderPageRecipe(node, nodePage, inputs, output, recipe.getId());
    }

    @Override
    public void render(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        clearFrameRectangles();
        renderRecipeGrid(graphics, ox, oy, TexturesAS.TEX_GUI_BOOK_GRID_T1);
        renderOutput(graphics, ox + 78, oy + 25, 1.4f, output);

        float recipeX = ox + 55;
        float recipeY = oy + 103;
        for (int xx = 0; xx < 3; xx++) {
            for (int yy = 0; yy < 3; yy++) {
                int slot = xx + yy * 3;
                Ingredient ing = inputs.get(slot);
                if (ing != null && !ing.isEmpty()) {
                    renderIngredientAt(graphics, recipeX + 25 * xx, recipeY + 25 * yy, 1.1f, slot * 20L, ing);
                }
            }
        }
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseY) {
        return handleBookLookupClick(mouseX, mouseY);
    }

    @Override
    public void postRender(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        renderHoverTooltips(graphics, mouseX, mouseY, recipeId);
    }
}
