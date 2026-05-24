package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Tree Beacon.
 * Collects starlight passively and stores it to boost tree growth
 * in a surrounding area. Emits a visible upward light beam that
 * scales with stored starlight intensity.
 *
 * <p>The tree beacon:
 * <ul>
 *   <li>Passively collects ambient starlight at night</li>
 *   <li>Stores starlight up to a capacity, consumed by growth ticks</li>
 *   <li>Accelerates nearby sapling and crop growth when charged</li>
 *   <li>Emits a vertical light beam visible from long range</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20: TileEntity → BlockEntity.
 * Tick via BlockEntityTicker.</p>
 */
public class BlockEntityTreeBeacon extends BlockEntityTick implements IStarlightReceiver {

    /** Maximum starlight the beacon can store. */
    private static final float MAX_STARLIGHT = 500.0f;

    /** How many blocks in each horizontal direction the effect reaches. */
    private static final int EFFECT_RANGE = 16;

    /** Starlight consumed per growth tick. */
    private static final float STARLIGHT_PER_TICK = 0.5f;

    /** Ticks between growth boost attempts. */
    private static final int GROWTH_INTERVAL = 40;

    private float storedStarlight = 0;
    private int ticksExisted = 0;
    private boolean active = false;

    public BlockEntityTreeBeacon(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.TREE_BEACON.get(), pos, state);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide()) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) return;

        // Passive starlight collection at night
        if (level != null && level.isNight() && canSeeSky()) {
            float ambient = 0.1f;
            storedStarlight = Math.min(MAX_STARLIGHT, storedStarlight + ambient);
        }

        // Update active state
        boolean wasActive = active;
        active = storedStarlight > STARLIGHT_PER_TICK;
        if (wasActive != active) {
            markForUpdate();
        }

        // Growth boosting
        if (active && ticksExisted % GROWTH_INTERVAL == 0) {
            applyGrowthBoost();
        }
    }

    /**
     * Applies random-tick growth to a random crop/sapling within range.
     */
    private void applyGrowthBoost() {
        if (level == null || storedStarlight < STARLIGHT_PER_TICK) return;

        // Pick a random position in range
        int dx = level.getRandom().nextInt(EFFECT_RANGE * 2 + 1) - EFFECT_RANGE;
        int dz = level.getRandom().nextInt(EFFECT_RANGE * 2 + 1) - EFFECT_RANGE;

        // Check a column of blocks for growable plants
        for (int dy = -2; dy <= 4; dy++) {
            BlockPos target = worldPosition.offset(dx, dy, dz);
            BlockState targetState = level.getBlockState(target);

            if (targetState.isRandomlyTicking()) {
                targetState.randomTick(
                        (net.minecraft.server.level.ServerLevel) level,
                        target,
                        level.getRandom());
                storedStarlight -= STARLIGHT_PER_TICK;
                setChanged();
                return;
            }
        }
    }

    private boolean canSeeSky() {
        return level != null && level.canSeeSky(worldPosition.above());
    }

    // IStarlightReceiver implementation
    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        storedStarlight = Math.min(MAX_STARLIGHT, storedStarlight + (float) amount);
        setChanged();
    }

    @Override
    public double getMaxStarlightInput() {
        return 10.0;
    }

    @Nullable
    @Override
    public Level getReceiverLevel() {
        return this.level;
    }

    public float getStoredStarlight() {
        return storedStarlight;
    }

    public float getStarlightRatio() {
        return storedStarlight / MAX_STARLIGHT;
    }

    public boolean isActive() {
        return active;
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.storedStarlight = compound.getFloat("storedStarlight");
        this.active = compound.getBoolean("active");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putFloat("storedStarlight", storedStarlight);
        compound.putBoolean("active", active);
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
