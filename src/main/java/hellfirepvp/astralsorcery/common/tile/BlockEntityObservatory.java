package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.entity.EntityObservatoryHelper;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for the Observatory.
 * An advanced telescope that allows constellation observation with
 * full sky camera control. When a player right-clicks, it spawns an
 * {@link EntityObservatoryHelper} and mounts the player on it.
 *
 * <p>The observatory tracks which entity helper it has spawned to
 * prevent duplicates. When the player dismounts or the helper is
 * discarded, the state resets.</p>
 *
 * <p>1.16 â†’ 1.20: TileEntity â†’ BlockEntity.
 * Tick via BlockEntityTicker in the block class.</p>
 */
public class BlockEntityObservatory extends BlockEntityTick {

    @Nullable
    private UUID activeHelperUUID = null;

    public BlockEntityObservatory(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.OBSERVATORY.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) return;

        // Periodically check if our helper entity is still alive
        UUID helperUUID = activeHelperUUID;
        if (helperUUID != null && getTicksExisted() % 20 == 0) {
            if (level instanceof ServerLevel serverLevel) {
                Entity helper = serverLevel.getEntity(helperUUID);
                if (helper == null || helper.isRemoved()) {
                    activeHelperUUID = null;
                    markForUpdate();
                }
            }
        }
    }

    /**
     * Attempt to start observatory usage by the given player.
     * Spawns a helper entity and mounts the player on it.
     *
     * @param player the server player trying to use the observatory
     * @return true if the player was mounted, false if already in use
     */
    public boolean tryStartUsing(@Nonnull ServerPlayer player) {
        UUID helperUUID = activeHelperUUID;
        if (helperUUID != null) {
            // Check if the existing helper is actually alive
            if (level instanceof ServerLevel serverLevel) {
                Entity existing = serverLevel.getEntity(helperUUID);
                if (existing != null && !existing.isRemoved()) {
                    return false; // Observatory already in use
                }
            }
            activeHelperUUID = null;
        }

        Level currentLevel = this.level;
        if (currentLevel == null) return false;

        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + 0.5;
        double cz = worldPosition.getZ() + 0.5;

        EntityObservatoryHelper helper = new EntityObservatoryHelper(currentLevel, cx, cy, cz, player);
        if (currentLevel.addFreshEntity(helper)) {
            player.startRiding(helper);
            activeHelperUUID = helper.getUUID();
            markForUpdate();
            return true;
        }
        return false;
    }

    public boolean isInUse() {
        return activeHelperUUID != null;
    }


    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        if (compound.hasUUID("activeHelper")) {
            this.activeHelperUUID = compound.getUUID("activeHelper");
        } else {
            this.activeHelperUUID = null;
        }
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        if (activeHelperUUID != null) {
            compound.putUUID("activeHelper", activeHelperUUID);
        }
    }
}
