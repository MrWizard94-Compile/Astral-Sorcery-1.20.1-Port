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
 * The most common perk type — small stat nodes that provide minor
 * attribute bonuses. Small perks form the connective tissue of the
 * perk tree, linking roots to major perks and key milestones.
 *
 * <p>These are cheap to allocate and numerous. A typical tree path
 * consists largely of small perks with occasional major/key nodes.</p>
 */
public class SmallPerk extends AbstractPerk {

    /**
     * @param key unique perk key
     * @param x   horizontal position in the tree
     * @param y   vertical position in the tree
     */
    public SmallPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y, AbstractPerk.PerkCategory.SMALL);
    }
}
