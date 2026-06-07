/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Client-side size helper for major perk nodes.
 * Major perks render 40% larger than standard nodes to indicate their importance.
 *
 * <p>1.16 → 1.20: same multiplier (1.4x), adapted for the GuiGraphics rendering path.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class PerkTreeMajor {

    /** Size multiplier applied to the base node diameter for major perk nodes. */
    public static final float SIZE_MULTIPLIER = 1.4f;

    private PerkTreeMajor() {}

    /**
     * Returns the adjusted node diameter for a major perk.
     *
     * @param perk     the major perk (unused today, reserved for per-perk overrides)
     * @param baseSize base node diameter in pixels at current zoom
     * @return scaled node diameter
     */
    @OnlyIn(Dist.CLIENT)
    public static float getNodeSize(@Nonnull MajorPerk perk, float baseSize) {
        return baseSize * SIZE_MULTIPLIER;
    }
}
