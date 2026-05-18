/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Upgrade recipe that promotes a Constellation altar to a Radiance (Trait) altar.
 *
 * <p>1.16 → 1.20: TileAltar → BlockEntityAltar;
 * ALTAR_RADIANCE block and ResearchManager.informCraftedAltar deferred.</p>
 */
public class TraitUpgradeRecipe extends SimpleAltarRecipe {

    public TraitUpgradeRecipe(@Nonnull ResourceLocation id,
                               @Nonnull BlockAltar.AltarType altarType,
                               int craftDuration, double starlightRequired,
                               @Nonnull ItemStack output,
                               @Nonnull NonNullList<Ingredient> inputs,
                               @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static TraitUpgradeRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new TraitUpgradeRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    // TODO: set block to RADIANCE_ALTAR and call ResearchManager.informCraftedAltar once ported
    public void onRecipeCompletion(@Nonnull BlockEntityAltar altar,
                                   @Nonnull ActiveSimpleAltarRecipe activeRecipe) {}
}
