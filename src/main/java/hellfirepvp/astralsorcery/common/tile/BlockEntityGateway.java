package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Celestial Gateway.
 * A teleportation node in the gateway network for cross-dimension travel.
 * Simple data holder — stores display name and multiblock validity.
 *
 * <p>On first tick, registers with the GatewayHelper to join the
 * gateway network. No inventory or fluid storage.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker</p>
 */
public class BlockEntityGateway extends BlockEntityTick {

    @Nullable
    private String displayName = null;

    private boolean structureValid = false;

    public BlockEntityGateway(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.GATEWAY.get(), pos, state);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide()) {
            // TODO: Register with GatewayHelper.registerGateway(this)
            // This adds the gateway to the network so it appears in the gateway GUI
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side gateway portal particles
            return;
        }

        // TODO: Server-side multiblock structure validation
        // Gateway is a simple data holder; most logic is in the GatewayHelper
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String name) {
        this.displayName = name;
        markForUpdate();
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public void setStructureValid(boolean valid) {
        this.structureValid = valid;
        markForUpdate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        if (compound.contains("displayName")) {
            this.displayName = compound.getString("displayName");
        } else {
            this.displayName = null;
        }
        this.structureValid = compound.getBoolean("structureValid");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (displayName != null) {
            compound.putString("displayName", displayName);
        }
        compound.putBoolean("structureValid", structureValid);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
    }
}
