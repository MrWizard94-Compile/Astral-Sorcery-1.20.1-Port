package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Journal page renderer for block transmutation recipes.
 * Shows the input block, output block, and any required constellation.
 *
 * <p>1.16 → 1.20: Light-beam VFX and SpritesAS.SPR_LIGHTBEAM omitted —
 * SpritesAS is not yet ported. Input is taken from getInputState().getBlock()
 * rather than the 1.16 BlockMatchInformation list.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderPageBlockTransmutation extends RenderPageRecipeTemplate {

    private final BlockTransmutation recipe;

    public RenderPageBlockTransmutation(@Nullable ResearchNode node, int nodePage, BlockTransmutation recipe) {
        super(node, nodePage);
        this.recipe = recipe;
    }

    @SuppressWarnings("null")
    @Override
    public void render(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        clearFrameRectangles();
        renderRecipeGrid(graphics, ox, oy, TexturesAS.TEX_GUI_BOOK_GRID_TRANSMUTATION);

        renderOutput(graphics, ox + 78, oy + 25, 1.4f, recipe.getOutputDisplay());

        // Input block as item stack
        ItemStack inputStack = new ItemStack(recipe.getInputState().getBlock());
        if (!inputStack.isEmpty()) {
            renderItemAt(graphics, ox + 80, oy + 128, 1.2f, inputStack);
            thisFrameInputStacks.put(
                    new java.awt.Rectangle(ox + 80, oy + 128, 20, 20),
                    new net.minecraft.util.Tuple<>(inputStack, null));
        }
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseY) {
        return handleBookLookupClick(mouseX, mouseY);
    }

    @Override
    public void postRender(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        renderHoverTooltips(graphics, mouseX, mouseY, recipe.getId());
    }
}
