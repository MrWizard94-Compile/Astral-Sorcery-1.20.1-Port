/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar;

import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;

import javax.annotation.Nonnull;

/**
 * Interface for altar recipes that, on completion, upgrade the altar block to the next tier.
 * {@link hellfirepvp.astralsorcery.common.tile.BlockEntityAltar#completeCrafting} calls
 * {@link #onRecipeCompletion} after producing outputs and consuming inputs.
 */
public interface AltarUpgradeRecipe {

    void onRecipeCompletion(@Nonnull BlockEntityAltar altar);
}
