/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * Marker interface for perks that have a tick-based cooldown managed by
 * {@link PerkCooldownHelper}. Called back when the cooldown for a player expires.
 */
public interface CooldownPerk {

    void onCooldownTimeout(@Nonnull Player player);
}
