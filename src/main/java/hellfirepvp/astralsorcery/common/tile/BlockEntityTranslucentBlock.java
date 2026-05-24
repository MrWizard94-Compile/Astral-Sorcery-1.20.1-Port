package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityFakedState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for the translucent block placed by the Evorsio perk.
 * Stores the UUID of the player that placed it so it can be reclaimed.
 *
 * <p>1.16 → 1.20: CompoundNBT.putUniqueId/getUniqueId → NBTHelper.putUUID/getUUID.</p>
 */
public class BlockEntityTranslucentBlock extends BlockEntityFakedState {

    private UUID playerUUID = null;

    public BlockEntityTranslucentBlock(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.TRANSLUCENT_BLOCK.get(), pos, state);
    }

    @Nullable
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(@Nullable UUID playerUUID) {
        this.playerUUID = playerUUID;
        markForUpdate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.playerUUID = compound.hasUUID("playerUUID") ? compound.getUUID("playerUUID") : null;
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (this.playerUUID != null) {
            compound.putUUID("playerUUID", this.playerUUID);
        }
    }
}
