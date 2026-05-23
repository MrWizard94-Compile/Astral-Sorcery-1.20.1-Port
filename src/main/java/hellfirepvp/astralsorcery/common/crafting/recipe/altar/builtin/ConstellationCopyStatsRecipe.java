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
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

    @Nonnull
    @Override
    @SuppressWarnings("null")
    public List<ItemStack> getOutputs(@Nonnull BlockEntityAltar altar) {
        List<ItemStack> out = new ArrayList<>(super.getOutputs(altar));
        TileInventory inv = altar.getInventory();
        out.forEach(stack -> copyConstellation(stack, inv));
        return out;
    }

    @SuppressWarnings("null")
    private void copyConstellation(@Nonnull ItemStack out, @Nonnull TileInventory inv) {
        CompoundTag outTag = out.getTag();
        if (outTag != null && outTag.contains("attunedConstellation")) return;
        for (int i = 0; i < getExpectedSlotCount(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("attunedConstellation")) {
                out.getOrCreateTag().putString("attunedConstellation",
                        tag.getString("attunedConstellation"));
                return;
            }
        }
    }
}
