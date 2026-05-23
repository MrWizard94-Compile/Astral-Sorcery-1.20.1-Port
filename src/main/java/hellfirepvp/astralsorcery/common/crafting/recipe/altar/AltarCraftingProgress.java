/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar;

import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.nbt.CompoundTag;

/**
 * Per-tick crafting progress callback invoked by the altar's active recipe tick loop.
 *
 * <p>1.16 → 1.20: TileAltar → BlockEntityAltar, CompoundNBT → CompoundTag</p>
 */
public interface AltarCraftingProgress {

    boolean tryProcess(BlockEntityAltar altar, ActiveSimpleAltarRecipe currentRecipe,
                       CompoundTag data, int ticksCrafting, int totalCraftingTime);
}
