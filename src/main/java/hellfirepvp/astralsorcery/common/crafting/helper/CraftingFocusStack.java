/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.helper;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Binds a crafting ingredient to a specific slot index and world position,
 * used by the altar crafting system to track focus relay inputs.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag,
 * net.minecraft.util.math.BlockPos → net.minecraft.core.BlockPos.</p>
 */
public class CraftingFocusStack {

    private final int stackIndex;
    @Nullable
    private final WrappedIngredient input;
    private final BlockPos at;

    public CraftingFocusStack(int stackIndex, @Nullable WrappedIngredient input, @Nonnull BlockPos at) {
        this.stackIndex = stackIndex;
        this.input = input;
        this.at = at;
    }

    public CraftingFocusStack(@Nonnull CompoundTag nbt) {
        this.stackIndex = nbt.getInt("stackIndex");
        this.input = WrappedIngredient.deserialize(nbt.getCompound("ingredient"));
        this.at = NBTHelper.readBlockPosFromNBT(nbt);
    }

    @Nullable
    public WrappedIngredient getInput() {
        return input;
    }

    @Nonnull
    public BlockPos getRealPosition() {
        return at;
    }

    public int getStackIndex() {
        return stackIndex;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        NBTHelper.writeBlockPosToNBT(this.at, nbt);
        nbt.putInt("stackIndex", this.stackIndex);
        if (this.input != null) {
            nbt.put("ingredient", this.input.serialize());
        }
        return nbt;
    }
}
