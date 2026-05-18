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
 * Altar recipe that produces multiple output crystals by setting their stack count
 * based on the number of crystal inputs. Full logic deferred until CrystalAttributes
 * is ported.
 */
public class CrystalCountRecipe extends ConstellationBaseAverageStatsRecipe {

    public CrystalCountRecipe(@Nonnull ResourceLocation id,
                               @Nonnull BlockAltar.AltarType altarType,
                               int craftDuration, double starlightRequired,
                               @Nonnull ItemStack output,
                               @Nonnull NonNullList<Ingredient> inputs,
                               @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static CrystalCountRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new CrystalCountRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    // TODO: override getOutputs(BlockEntityAltar) to set output count from CrystalAttributes once ported
}
