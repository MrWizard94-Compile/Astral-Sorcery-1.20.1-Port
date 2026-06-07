/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.perk.node.GemSocketPerk;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Client-side renderer for gem socket perk nodes.
 * Draws the socketed gem item centered in the node using {@link GuiGraphics#renderItem}.
 *
 * <p>1.16 → 1.20: RenderingUtils.renderItemStackGUI + MatrixStack.push/scale
 * → GuiGraphics.renderItem (built-in).</p>
 */
@OnlyIn(Dist.CLIENT)
public final class PerkTreeGem {

    private PerkTreeGem() {}

    /**
     * Renders the gem item overlay on a gem socket perk node.
     *
     * @param graphics  the GUI graphics context
     * @param gemPerk   the gem socket perk whose item to render
     * @param cx        center X of the node in screen space
     * @param cy        center Y of the node in screen space
     * @param scale     the current perk tree zoom scale (used to offset item)
     */
    @OnlyIn(Dist.CLIENT)
    public static void render(@Nonnull GuiGraphics graphics, @Nonnull GemSocketPerk gemPerk,
                               float cx, float cy, float scale) {
        if (!gemPerk.hasGem()) return;
        ItemStack gem = gemPerk.getSocketedGem();
        if (gem == null || gem.isEmpty()) return;

        // renderItem draws a 16x16 item at the given screen coordinates.
        // Offset by 8 so the item is centered on (cx, cy).
        int ix = (int) (cx - 8 * scale);
        int iy = (int) (cy - 8 * scale);

        graphics.pose().pushPose();
        graphics.pose().translate(ix, iy, 50f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.renderItem(gem, 0, 0);
        graphics.pose().popPose();
    }
}
