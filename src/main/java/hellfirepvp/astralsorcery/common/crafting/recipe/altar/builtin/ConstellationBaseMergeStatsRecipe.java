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
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Altar recipe that merges the CrystalAttributes of multiple input crystals
 * into a single output crystal using a best-of strategy (Math.max per stat).
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

    @Nonnull
    @Override
    public List<ItemStack> getOutputs(@Nonnull BlockEntityAltar altar) {
        List<ItemStack> out = new ArrayList<>(super.getOutputs(altar));
        TileInventory inv = altar.getInventory();
        out.forEach(stack -> applyMergedProps(stack, inv));
        return out;
    }

    private void applyMergedProps(@Nonnull ItemStack out, @Nonnull TileInventory inv) {
        int bestSize = 0, bestPurity = 0, bestCutting = 0;
        boolean found = false;
        for (int i = 0; i < getExpectedSlotCount(); i++) {
            CrystalProperties props = CrystalProperties.getFromStack(inv.getStackInSlot(i));
            if (props != null) {
                bestSize = Math.max(bestSize, props.getSize());
                bestPurity = Math.max(bestPurity, props.getPurity());
                bestCutting = Math.max(bestCutting, props.getCutting());
                found = true;
            }
        }
        if (found) {
            CrystalProperties.setOnStack(out, new CrystalProperties(bestSize, bestPurity, bestCutting));
        }
    }
}
