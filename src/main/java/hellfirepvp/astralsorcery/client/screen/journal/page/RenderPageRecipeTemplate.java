package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupInfo;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupRegistry;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.journal.RenderablePage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.client.util.IngredientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for journal page renderers that display crafting recipes.
 * Handles ingredient slot drawing, output rendering, tooltips, and book-lookup clicks.
 *
 * <p>1.16 → 1.20: MatrixStack/RenderStack → GuiGraphics; RenderHelper item lighting removed
 * (GuiGraphics.renderItem handles this); ITextProperties → Component.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class RenderPageRecipeTemplate implements RenderablePage {

    @Nullable
    private final ResearchNode node;
    private final int nodePage;

    protected Map<Rectangle, Tuple<ItemStack, Ingredient>> thisFrameInputStacks = new HashMap<>();
    protected Tuple<Rectangle, ItemStack> thisFrameOutputStack = null;

    protected RenderPageRecipeTemplate(@Nullable ResearchNode node, int nodePage) {
        this.node = node;
        this.nodePage = nodePage;
    }

    protected void clearFrameRectangles() {
        thisFrameInputStacks.clear();
        thisFrameOutputStack = null;
    }

    protected void renderRecipeGrid(GuiGraphics graphics, float x, float y, AbstractRenderableTexture tex) {
        RenderingDrawUtils.drawTexturedRect(graphics, tex.getKey(),
                (int) (x + 25), (int) y, 129, 202);
    }

    protected void renderIngredientAt(GuiGraphics graphics, float x, float y, float scale, long tickOffset, Ingredient ingredient) {
        ItemStack stack = IngredientHelper.getRandomVisibleStack(ingredient, ClientScheduler.getClientTick() + tickOffset);
        if (!stack.isEmpty()) {
            renderItemAt(graphics, x, y, scale, stack);
            thisFrameInputStacks.put(
                    new Rectangle((int) x, (int) y, (int) (16 * scale), (int) (16 * scale)),
                    new Tuple<>(stack, ingredient));
        }
    }

    protected void renderOutput(GuiGraphics graphics, float x, float y, float scale, ItemStack stack) {
        if (!stack.isEmpty()) {
            renderItemAt(graphics, x, y, scale, stack);
            thisFrameOutputStack = new Tuple<>(
                    new Rectangle((int) x, (int) y, (int) (16 * scale), (int) (16 * scale)),
                    stack);
        }
    }

    protected void renderItemAt(GuiGraphics graphics, float x, float y, float scale, ItemStack stack) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    protected boolean handleBookLookupClick(double mouseX, double mouseY) {
        ResearchNode currentNode = this.node;
        for (Map.Entry<Rectangle, Tuple<ItemStack, Ingredient>> entry : thisFrameInputStacks.entrySet()) {
            if (entry.getKey().contains(mouseX, mouseY)) {
                ItemStack stack = entry.getValue().getA();
                BookLookupInfo info = BookLookupRegistry.findPage(stack);
                if (info != null && info.canSee(hellfirepvp.astralsorcery.common.data.research.ResearchHelper.getClientProgress())
                        && !info.getResearchNode().equals(currentNode)) {
                    info.openGui();
                    return true;
                }
            }
        }
        if (thisFrameOutputStack != null && thisFrameOutputStack.getA().contains(mouseX, mouseY)) {
            ItemStack stack = thisFrameOutputStack.getB();
            BookLookupInfo info = BookLookupRegistry.findPage(stack);
            if (info != null && info.canSee(hellfirepvp.astralsorcery.common.data.research.ResearchHelper.getClientProgress())
                    && !info.getResearchNode().equals(currentNode)) {
                info.openGui();
                return true;
            }
        }
        return false;
    }

    protected boolean handleRecipeNameCopyClick(double mouseX, double mouseY, SimpleAltarRecipe recipe) {
        if (Minecraft.getInstance().options.renderDebug && Screen.hasControlDown()
                && thisFrameOutputStack != null
                && thisFrameOutputStack.getA().contains(mouseX, mouseY)) {
            String name = recipe.getId().toString();
            Minecraft.getInstance().keyboardHandler.setClipboard(name);
            net.minecraft.client.player.LocalPlayer msgPlayer = Minecraft.getInstance().player;
            if (msgPlayer != null) {
                msgPlayer.displayClientMessage(
                        Component.translatable("astralsorcery.misc.ctrlcopy.copied", name), false);
            }
            return true;
        }
        return false;
    }

    protected void renderHoverTooltips(GuiGraphics graphics, int mouseX, int mouseY, ResourceLocation recipeName) {
        List<Component> tooltip = new ArrayList<>();

        for (Map.Entry<Rectangle, Tuple<ItemStack, Ingredient>> entry : thisFrameInputStacks.entrySet()) {
            if (entry.getKey().contains(mouseX, mouseY)) {
                addItemTooltip(entry.getValue().getA(), tooltip);
                break;
            }
        }
        if (tooltip.isEmpty() && thisFrameOutputStack != null
                && thisFrameOutputStack.getA().contains(mouseX, mouseY)) {
            addItemTooltip(thisFrameOutputStack.getB(), tooltip);
            if (Minecraft.getInstance().options.renderDebug) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("astralsorcery.misc.recipename", recipeName.toString())
                        .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.ITALIC));
            }
        }
        if (!tooltip.isEmpty()) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    private void addItemTooltip(ItemStack stack, List<Component> out) {
        try {
            TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips
                    ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            out.addAll(stack.getTooltipLines(Minecraft.getInstance().player, flag));
        } catch (Exception ignored) {
            out.add(Component.translatable("astralsorcery.misc.tooltipError")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    protected void addAltarRecipeTooltip(SimpleAltarRecipe recipe, List<Component> tooltip) {
        if (recipe.getStarlightRequired() > 0) {
            String key = "astralsorcery.journal.recipe.altar.starlight.";
            float perc = (float)(recipe.getStarlightRequired() / 1000.0);
            if      (perc <= 0.1f)  { key += "lowest"; }
            else if (perc <= 0.25f) { key += "low"; }
            else if (perc <= 0.5f)  { key += "avg"; }
            else if (perc <= 0.75f) { key += "more"; }
            else if (perc <= 0.9f)  { key += "high"; }
            else                    { key += "highest"; }
            tooltip.add(Component.translatable("astralsorcery.journal.recipe.altar.starlight.desc"));
            tooltip.add(Component.translatable(key));
        }
    }

    @Nullable
    protected ResearchNode getResearchNode() { return node; }
    protected int getNodePage()              { return nodePage; }
}
