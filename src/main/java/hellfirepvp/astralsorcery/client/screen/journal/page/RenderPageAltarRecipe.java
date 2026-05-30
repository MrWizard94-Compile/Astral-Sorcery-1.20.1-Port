package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.core.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Journal page renderer for altar crafting recipes.
 * Shows the grid texture, all input ingredients, the output item,
 * and optionally the required focus constellation.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; getFocusConstellation now returns
 * ResourceLocation; relay inputs removed (not in port's recipe model).</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderPageAltarRecipe extends RenderPageRecipeTemplate {

    private static final int GRID_SIZE = 3;

    private final SimpleAltarRecipe recipe;
    private final AbstractRenderableTexture gridTexture;

    public RenderPageAltarRecipe(@Nullable ResearchNode node, int nodePage, SimpleAltarRecipe recipe) {
        super(node, nodePage);
        this.recipe = recipe;
        this.gridTexture = gridTextureFor(recipe.getAltarType());
    }

    private static AbstractRenderableTexture gridTextureFor(BlockAltar.AltarType type) {
        return switch (type) {
            case DISCOVERY     -> TexturesAS.TEX_GUI_BOOK_GRID_T1;
            case ATTUNEMENT    -> TexturesAS.TEX_GUI_BOOK_GRID_T2;
            case CONSTELLATION -> TexturesAS.TEX_GUI_BOOK_GRID_T3;
            case RADIANCE      -> TexturesAS.TEX_GUI_BOOK_GRID_T4;
        };
    }

    @Override
    public void render(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        clearFrameRectangles();

        renderRecipeGrid(graphics, ox, oy, gridTexture);

        // Output item — centered above the grid
        renderOutput(graphics, ox + 78, oy + 25, 1.4f, recipe.getOutput());

        // Required focus constellation overlay
        ResourceLocation focusKey = recipe.getFocusConstellation();
        if (focusKey != null) {
            IConstellation cst = ConstellationRegistry.getConstellation(focusKey);
            if (cst != null) {
                long tick = ClientScheduler.getClientTick();
                float brightness = 0.4f + 0.1f * RenderingConstellationUtils.getTwinkleBrightness(0, (int) tick, partialTick);
                RenderingConstellationUtils.renderConstellationSmall(graphics, cst,
                        ox + 30, oy + 78, 125, 125, brightness);
            }
        }

        // Input ingredients in 3×3 grid
        NonNullList<Ingredient> inputs = recipe.getIngredients();
        int expectedSlots = recipe.getExpectedSlotCount();
        int cols = Math.min(GRID_SIZE, expectedSlots);
        int rows = (expectedSlots + cols - 1) / cols;

        float startX = ox + 30;
        float startY = oy + 78;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = col + row * cols;
                if (idx >= inputs.size()) break;
                Ingredient ing = inputs.get(idx);
                if (ing.isEmpty()) continue;
                renderIngredientAt(graphics, startX + col * 25, startY + row * 25, 1.1f, idx * 20L, ing);
            }
        }
    }

    @Override
    public void postRender(GuiGraphics graphics, int ox, int oy, float partialTick, int mouseX, int mouseY) {
        renderHoverTooltips(graphics, mouseX, mouseY, recipe.getId());

        // Info-star tooltip showing starlight requirement
        if (thisFrameOutputStack != null && thisFrameOutputStack.getA().contains(mouseX, mouseY)) {
            List<Component> tip = new java.util.ArrayList<>();
            addAltarRecipeTooltip(recipe, tip);
            ResourceLocation focusKey = recipe.getFocusConstellation();
            if (focusKey != null) {
                IConstellation cst = ConstellationRegistry.getConstellation(focusKey);
                if (cst != null) {
                    tip.add(Component.translatable("astralsorcery.journal.recipe.constellation",
                            cst.getConstellationName()));
                }
            }
            if (!tip.isEmpty()) {
                graphics.renderComponentTooltip(net.minecraft.client.Minecraft.getInstance().font, tip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseY) {
        return handleRecipeNameCopyClick(mouseX, mouseY, recipe) || handleBookLookupClick(mouseX, mouseY);
    }
}
