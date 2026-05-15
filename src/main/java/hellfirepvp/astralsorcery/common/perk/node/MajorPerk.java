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
 * A major stat perk in the perk tree. Major perks provide significant
 * attribute bonuses — larger than small perks but less impactful
 * than key perks. They often specialize in a particular stat area
 * (offensive, defensive, utility).
 */
public class MajorPerk extends AbstractPerk {

    /**
     * @param key unique perk key
     * @param x   horizontal position in the tree
     * @param y   vertical position in the tree
     */
    public MajorPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y, AbstractPerk.PerkCategory.MAJOR);
    }
}
