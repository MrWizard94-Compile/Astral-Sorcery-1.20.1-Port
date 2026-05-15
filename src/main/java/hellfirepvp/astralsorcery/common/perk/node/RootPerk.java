/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * A root perk — the entry point into a constellation's perk branch.
 * Each constellation has exactly one root perk, and allocating it
 * is the first step for that constellation's tree path.
 *
 * <p>Root perks typically grant a small initial bonus and unlock
 * the constellation's branch for further allocation.</p>
 */
public class RootPerk extends AbstractPerk {

    @Nonnull
    private final ResourceLocation constellationKey;

    /**
     * @param key              unique perk key
     * @param x                horizontal position in the tree
     * @param y                vertical position in the tree
     * @param constellationKey the constellation this root belongs to
     */
    public RootPerk(@Nonnull ResourceLocation key, int x, int y,
                    @Nonnull ResourceLocation constellationKey) {
        super(key, x, y, AbstractPerk.PerkCategory.ROOT);
        this.constellationKey = constellationKey;
        setRequiredConstellation(constellationKey);
    }

    @Nonnull
    public ResourceLocation getConstellationKey() {
        return constellationKey;
    }

    @Override
    public void onAllocate(@Nonnull Player player) {
        super.onAllocate(player);
        AstralSorcery.log.debug("Player {} allocated root perk for constellation {}",
                player.getName().getString(), constellationKey);
    }
}
