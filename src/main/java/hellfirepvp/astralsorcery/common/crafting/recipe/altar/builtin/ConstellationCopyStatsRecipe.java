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
 * Altar recipe that copies CrystalAttributes from a source input crystal to the output.
 * Full logic deferred until CrystalAttributes is ported.
 */
public class ConstellationCopyStatsRecipe extends ConstellationBaseItemRecipe {

    public ConstellationCopyStatsRecipe(@Nonnull ResourceLocation id,
                                        @Nonnull BlockAltar.AltarType altarType,
                                        int craftDuration, double starlightRequired,
                                        @Nonnull ItemStack output,
                                        @Nonnull NonNullList<Ingredient> inputs,
                                        @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static ConstellationCopyStatsRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new ConstellationCopyStatsRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    // TODO: override getOutputs(BlockEntityAltar) to copy CrystalAttributes once ported
}
