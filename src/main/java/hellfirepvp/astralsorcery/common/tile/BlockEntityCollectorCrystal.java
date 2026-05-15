package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for Collector Crystals.
 * A starlight source that collects starlight from the sky and transmits
 * it to connected receivers (altars, infusers, etc.) via the starlight network.
 *
 * <p>Stores:
 * - Attuned constellation (ResourceLocation key)
 * - Crystal properties (size, purity, cutting)
 * - Transmission links
 * - Starlight collection rate</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity,
 * tick() via BlockEntityTicker pattern,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityCollectorCrystal extends BlockEntityTick {

    @Nullable
    private ResourceLocation attunedConstellation = null;

    private double starlightCollected = 0;
    private int crystalSize = 400;
    private int crystalPurity = 100;
    private int crystalCutting = 100;

    public BlockEntityCollectorCrystal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side particle effects
            return;
        }
        // TODO: Server-side starlight collection logic
        // - Check sky access
        // - Calculate collection rate from crystal properties + constellation
        // - Feed into starlight transmission network
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
    }

    public double getStarlightCollected() {
        return starlightCollected;
    }

    public int getCrystalSize() {
        return crystalSize;
    }

    public int getCrystalPurity() {
        return crystalPurity;
    }

    public int getCrystalCutting() {
        return crystalCutting;
    }

    /**
     * Get the tint color for rendering based on attuned constellation.
     */
    public int getConstellationColor() {
        // TODO: Look up constellation color from ConstellationsAS registry
        return attunedConstellation != null ? 0x4488DD : 0xAAAAFF;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        if (compound.contains("constellation")) {
            this.attunedConstellation = new ResourceLocation(compound.getString("constellation"));
        } else {
            this.attunedConstellation = null;
        }
        this.crystalSize = compound.getInt("crystalSize");
        this.crystalPurity = compound.getInt("crystalPurity");
        this.crystalCutting = compound.getInt("crystalCutting");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (attunedConstellation != null) {
            compound.putString("constellation", attunedConstellation.toString());
        }
        compound.putInt("crystalSize", crystalSize);
        compound.putInt("crystalPurity", crystalPurity);
        compound.putInt("crystalCutting", crystalCutting);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.starlightCollected = compound.getDouble("starlightCollected");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("starlightCollected", starlightCollected);
    }
}
