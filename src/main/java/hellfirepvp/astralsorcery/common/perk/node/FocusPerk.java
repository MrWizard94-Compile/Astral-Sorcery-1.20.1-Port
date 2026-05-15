/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * A constellation focus perk. Focus perks provide constellation-specific
 * bonuses and require the player to be attuned to the target constellation.
 *
 * <p>Each focus perk is bound to a specific constellation and gates
 * powerful, thematic effects behind constellation attunement.</p>
 */
public class FocusPerk extends AbstractPerk {

    @Nonnull
    private final ResourceLocation focusConstellation;

    /**
     * @param key                unique perk key
     * @param x                  horizontal position in the tree
     * @param y                  vertical position in the tree
     * @param focusConstellation the constellation this focus perk is tied to
     */
    public FocusPerk(@Nonnull ResourceLocation key, int x, int y,
                     @Nonnull ResourceLocation focusConstellation) {
        super(key, x, y, AbstractPerk.PerkCategory.FOCUS);
        this.focusConstellation = focusConstellation;
        setRequiredConstellation(focusConstellation);
    }

    @Nonnull
    public ResourceLocation getFocusConstellation() {
        return focusConstellation;
    }
}
