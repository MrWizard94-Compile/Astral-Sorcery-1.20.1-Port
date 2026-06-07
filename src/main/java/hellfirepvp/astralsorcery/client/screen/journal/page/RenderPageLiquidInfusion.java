package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Journal page renderer for liquid infusion recipes.
 * Shows the input item, required fluid amount, and output.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics;
 * FluidAttributes → FluidType (fluid rendering skipped — uses text description instead).</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderPageLiquidInfusion extends RenderPageRecipeTemplate {

    private final LiquidInfusion recipe;

    public RenderPageLiquidInfusion(@Nullable ResearchNode node, int nodePage, LiquidInfusion recipe) {
        super(node, nodePage);
        this.recipe = recipe;
    }

    @Override
    public void render(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        clearFrameRectangles();

        renderRecipeGrid(graphics, ox, oy, TexturesAS.TEX_GUI_BOOK_GRID_INFUSION);
        renderOutput(graphics, ox + 78, oy + 25, 1.4f, recipe.getOutput());

        float renderX = ox + 80;
        float renderY = oy + 128;

        // Infuser block
        renderItemAt(graphics, renderX, renderY + 15, 1.2f, new ItemStack(BlocksAS.INFUSER.get()));
        // Input ingredient
        renderIngredientAt(graphics, renderX, renderY, 1.2f, 0L, recipe.getInputItem());

        // Required constellation (if any)
        ResourceLocation cstKey = recipe.getRequiredConstellation();
        if (cstKey != null) {
            IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
            if (cst != null) {
                hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils
                        .renderConstellationSmall(graphics, cst, ox + 30, oy + 78, 125, 125, 0.4f);
            }
        }
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseY) {
        return handleBookLookupClick(mouseX, mouseY);
    }

    @Override
    public void postRender(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        renderHoverTooltips(graphics, mouseX, mouseY, recipe.getId());

        // Fluid amount tooltip on output hover
        if (thisFrameOutputStack != null && thisFrameOutputStack.getA().contains(mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("astralsorcery.journal.recipe.infusion.fluid",
                    recipe.getFluidMbRequired()));
            graphics.renderComponentTooltip(net.minecraft.client.Minecraft.getInstance().font, tip, mouseX, mouseY);
        }
    }
}
