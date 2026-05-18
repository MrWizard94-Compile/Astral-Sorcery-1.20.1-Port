/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Altar recipe that merges the CrystalAttributes of multiple input crystals
 * into a single output crystal. Full logic deferred until CrystalAttributes is ported.
 */
public class ConstellationBaseMergeStatsRecipe extends ConstellationBaseItemRecipe {

    public ConstellationBaseMergeStatsRecipe(@Nonnull ResourceLocation id,
                                             @Nonnull BlockAltar.AltarType altarType,
                                             int craftDuration, double starlightRequired,
                                             @Nonnull ItemStack output,
                                             @Nonnull NonNullList<Ingredient> inputs,
                                             @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static ConstellationBaseMergeStatsRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new ConstellationBaseMergeStatsRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    // TODO: override getOutputs(BlockEntityAltar) to merge CrystalAttributes once ported
}
