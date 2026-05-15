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
 * A key milestone perk in the perk tree. Key perks represent major
 * branching points with significant bonuses and are limited in number.
 *
 * <p>Players typically must allocate several small/major perks to reach
 * a key perk. These often gate access to powerful constellation-specific
 * branches or unlock special abilities.</p>
 */
public class KeyPerk extends AbstractPerk {

    /**
     * @param key unique perk key
     * @param x   horizontal position in the tree
     * @param y   vertical position in the tree
     */
    public KeyPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y, AbstractPerk.PerkCategory.KEY);
    }
}
