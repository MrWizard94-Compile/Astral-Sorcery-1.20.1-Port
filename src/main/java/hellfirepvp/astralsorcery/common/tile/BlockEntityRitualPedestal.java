package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Ritual Pedestal.
 * Holds an attuned crystal to produce constellation-based area effects.
 * The active effect depends on the crystal's attuned constellation.
 *
 * <p>Effects are provided by the {@link ConstellationEffectRegistry} /
 * {@link ConstellationEffectProvider} system — no effect logic lives here.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityRitualPedestal extends BlockEntityTick implements IStarlightReceiver {

    private static final int DEFAULT_EFFECT_RANGE = 16;

    /** Maximum starlight the pedestal can store for ritual operation. */
    private static final double STARLIGHT_CAPACITY = 5000.0;

    /** Starlight consumed per application tick. */
    private static final double STARLIGHT_DRAIN_PER_TICK = 2.0;

    /** How often to check multiblock validity (ticks). */
    private static final int STRUCTURE_CHECK_INTERVAL = 60;

    /** How often to apply ritual effects (ticks). */
    private static final int EFFECT_INTERVAL = 40;

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    @Nullable
    private ResourceLocation attunedConstellation = null;

    private int ticksExisted = 0;
    private boolean ritualActive = false;
    private int effectRange = DEFAULT_EFFECT_RANGE;
    private boolean hasMultiblock = false;
    private double storedStarlight = 0;
    private boolean registeredInNetwork = false;

    public BlockEntityRitualPedestal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.RITUAL_PEDESTAL.get(), pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide() && registeredInNetwork) {
            hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler handler =
                    StarlightNetworkHelper.getHandler(getLevel());
            if (handler != null) handler.removeAutoLinkTo(getBlockPos());
            StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
            registeredInNetwork = false;
        }
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerReceiver(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
            hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler handler =
                    StarlightNetworkHelper.getHandler(getLevel());
            if (handler != null) handler.attemptAutoLinkTo(getBlockPos());
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // Client-side: ritual effect particles handled by renderer
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Periodically check multiblock structure
        if (ticksExisted % STRUCTURE_CHECK_INTERVAL == 0) {
            hasMultiblock = validateStructure();
        }

        // Activation conditions: valid structure, crystal present, constellation attuned
        boolean shouldBeActive = hasMultiblock
                && !heldCrystal.isEmpty()
                && attunedConstellation != null
                && storedStarlight > STARLIGHT_DRAIN_PER_TICK;

        if (shouldBeActive != ritualActive) {
            ritualActive = shouldBeActive;
            markForUpdate();
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(
                            ritualActive ? PktPlayEffect.EffectType.RITUAL_ACTIVATE
                                         : PktPlayEffect.EffectType.RITUAL_DEACTIVATE,
                            worldPosition),
                    (ServerLevel) level, worldPosition);
        }

        if (!ritualActive) {
            return;
        }

        // Drain starlight for operation
        storedStarlight -= STARLIGHT_DRAIN_PER_TICK;

        // Apply ritual effects at interval
        if (ticksExisted % EFFECT_INTERVAL == 0) {
            applyConstellationEffect((ServerLevel) level);
        }
    }

    /**
     * Delegates to the {@link ConstellationEffectRegistry} to apply the effect
     * registered for this pedestal's attuned constellation.
     */
    private void applyConstellationEffect(@Nonnull ServerLevel level) {
        if (attunedConstellation == null) return;

        IConstellation cst = ConstellationRegistry.getConstellation(attunedConstellation);
        if (!(cst instanceof IWeakConstellation weak)) {
            AstralSorcery.log.debug("Ritual pedestal: no weak constellation for {}", attunedConstellation);
            return;
        }

        ConstellationEffectProvider provider = ConstellationEffectRegistry.getProvider(weak);
        if (provider == null) {
            AstralSorcery.log.debug("Ritual pedestal: no effect provider for {}", attunedConstellation);
            return;
        }

        ConstellationEffectProperties properties = provider.provideProperties().setSize(effectRange);
        provider.tick(level, worldPosition, properties);
    }

    /**
     * Validates the ritual pedestal multiblock structure.
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        // Must have sky access
        if (!level.canSeeSky(worldPosition.above(2))) {
            return false;
        }

        // Check for support pillars at ±2 positions
        BlockPos[] pillarPositions = {
                worldPosition.offset(2, 0, 0),
                worldPosition.offset(-2, 0, 0),
                worldPosition.offset(0, 0, 2),
                worldPosition.offset(0, 0, -2)
        };

        for (BlockPos pillar : pillarPositions) {
            if (level.getBlockState(pillar).isAir()) {
                return false;
            }
        }
        return true;
    }

    // ========================================================================
    // IStarlightReceiver implementation
    // ========================================================================

    @Override
    public void receiveStarlight(double amount, @Nullable ResourceLocation constellation) {
        double space = STARLIGHT_CAPACITY - storedStarlight;
        if (space > 0) {
            storedStarlight += Math.min(amount, space);
            setChanged();
        }
    }

    @Override
    public double getMaxStarlightInput() {
        return STARLIGHT_CAPACITY * 0.1;
    }

    @Nullable
    @Override
    public Level getReceiverLevel() {
        return getLevel();
    }

    @Nonnull
    @Override
    public BlockPos getLocationPos() {
        return getBlockPos();
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack stack) {
        this.heldCrystal = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        // Extract attuned constellation from the crystal when placed
        if (!stack.isEmpty() && stack.getItem() instanceof ConstellationItem cItem) {
            IWeakConstellation cst = cItem.getAttunedConstellation(stack);
            this.attunedConstellation = cst != null ? cst.getRegistryName() : null;
        } else {
            this.attunedConstellation = null;
        }
        markForUpdate();
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isRitualActive() {
        return ritualActive;
    }

    public int getEffectRange() {
        return effectRange;
    }

    public void setEffectRange(int range) {
        this.effectRange = Math.max(1, range);
    }

    public boolean hasMultiblock() {
        return hasMultiblock;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.heldCrystal = compound.contains("heldCrystal")
                ? ItemStack.of(compound.getCompound("heldCrystal"))
                : ItemStack.EMPTY;
        if (compound.contains("attunedConstellation")) {
            this.attunedConstellation = new ResourceLocation(compound.getString("attunedConstellation"));
        } else {
            this.attunedConstellation = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (!heldCrystal.isEmpty()) {
            CompoundTag crystalTag = new CompoundTag();
            heldCrystal.save(crystalTag);
            compound.put("heldCrystal", crystalTag);
        }
        if (attunedConstellation != null) {
            compound.putString("attunedConstellation", attunedConstellation.toString());
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.ritualActive = compound.getBoolean("ritualActive");
        this.effectRange = compound.contains("effectRange")
                ? compound.getInt("effectRange") : DEFAULT_EFFECT_RANGE;
        this.hasMultiblock = compound.getBoolean("hasMultiblock");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putBoolean("ritualActive", ritualActive);
        compound.putInt("effectRange", effectRange);
        compound.putBoolean("hasMultiblock", hasMultiblock);
    }
}
