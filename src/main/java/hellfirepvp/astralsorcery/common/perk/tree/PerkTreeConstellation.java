/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Client-side renderer for constellation-type perk nodes.
 * Draws the constellation star pattern inside the node circle, but only if
 * the player has discovered the constellation.
 *
 * <p>1.16 → 1.20: MatrixStack + RenderingConstellationUtils.renderConstellationIntoGUI
 * → GuiGraphics + RenderingConstellationUtils.renderConstellationSmall.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class PerkTreeConstellation {

    public static final int ROOT_SPRITE_SIZE = 50;
    public static final int MINOR_SPRITE_SIZE = 40;

    private PerkTreeConstellation() {}

    /**
     * Renders the constellation overlay on a root perk node.
     *
     * @param graphics  the GUI graphics context
     * @param rootPerk  the root perk whose constellation to render
     * @param cx        center X of the node in screen space
     * @param cy        center Y of the node in screen space
     * @param nodeSize  diameter of the node (pixels)
     */
    @OnlyIn(Dist.CLIENT)
    public static void render(@Nonnull GuiGraphics graphics, @Nonnull RootPerk rootPerk,
                               float cx, float cy, float nodeSize) {
        ResourceLocation cstKey = rootPerk.getConstellationKey();
        IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
        if (cst == null) return;

        PlayerProgress prog = ResearchHelper.getClientProgress();
        if (!prog.hasDiscovered(cstKey)) return;

        int x = (int) (cx - nodeSize / 2f);
        int y = (int) (cy - nodeSize / 2f);
        int size = Math.max(1, (int) nodeSize);

        RenderingConstellationUtils.renderConstellationSmall(graphics, cst, x, y, size, size, 0.85f);
    }
}
