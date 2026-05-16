package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntitySynchronized;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Block entity for the Telescope.
 * Stores which constellations the player has discovered at this telescope.
 * Does not tick — constellation tracking is done server-side on player interaction.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, TileEntityType -> BlockEntityType,
 * pos/state constructor required.</p>
 */
public class BlockEntityTelescope extends BlockEntitySynchronized {

    private boolean hasBeenUsed = false;

    public BlockEntityTelescope(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.TELESCOPE.get(), pos, state);
    }

    /**
     * Get a tick count for renderer animations.
     * Since the telescope does not tick, uses the level game time.
     */
    public long getTicksExisted() {
        return getLevel() != null ? getLevel().getGameTime() : 0L;
    }

    public boolean hasBeenUsed() {
        return hasBeenUsed;
    }

    public void setUsed() {
        this.hasBeenUsed = true;
        markForUpdate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.hasBeenUsed = compound.getBoolean("hasBeenUsed");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putBoolean("hasBeenUsed", this.hasBeenUsed);
    }
}
