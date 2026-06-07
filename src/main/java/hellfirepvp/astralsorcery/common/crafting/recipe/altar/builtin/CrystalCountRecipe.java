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
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Altar recipe that produces multiple output crystals, averaging input crystal stats
 * and setting output count via {@link hellfirepvp.astralsorcery.common.crystal.CrystalCalculations#getSizeCraftingAmount}.
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

    @Nonnull
    @Override
    public List<ItemStack> getOutputs(@Nonnull BlockEntityAltar altar) {
        List<ItemStack> out = super.getOutputs(altar); // calls ConstellationBaseAverageStatsRecipe first
        out.forEach(this::setAmount);
        return out;
    }

    private void setAmount(@Nonnull ItemStack out) {
        CrystalProperties props = CrystalProperties.getFromStack(out);
        if (props != null) {
            out.setCount(CrystalCalculations.getSizeCraftingAmount(props));
        }
    }
}
