package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Attunement Altar.
 * Attunes players and crystals to specific constellations.
 * Requires a valid multiblock structure with spectral relays.
 *
 * <p>Uses a held crystal ItemStack field rather than TileInventory,
 * since only a single crystal can be placed on the altar at a time.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityAttunementAltar extends BlockEntityTick {

    /** Total ticks required to complete attunement. */
    private static final int ATTUNEMENT_DURATION = 200; // 10 seconds

    /** How often to re-check multiblock structure validity. */
    private static final int STRUCTURE_CHECK_INTERVAL = 40; // Every 2 seconds

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    private int attunementTick = 0;
    private int ticksExisted = 0;
    private boolean structureValid = false;
    private boolean isAttuning = false;

    @Nullable
    private ResourceLocation attunedConstellation = null;

    public BlockEntityAttunementAltar(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ATTUNEMENT_ALTAR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Periodically re-validate multiblock structure
        if (ticksExisted % STRUCTURE_CHECK_INTERVAL == 0) {
            structureValid = validateStructure();
        }

        // Attunement requires: valid structure, crystal present, target constellation set
        if (!structureValid || heldCrystal.isEmpty() || attunedConstellation == null) {
            if (isAttuning) {
                abortAttunement();
            }
            return;
        }

        // Must be nighttime and the target constellation must be visible
        if (!DayTimeHelper.isNight(level)) {
            if (isAttuning) {
                abortAttunement();
            }
            return;
        }

        // Check if target constellation is currently visible in sky
        if (!isConstellationVisible(level, attunedConstellation)) {
            if (isAttuning) {
                abortAttunement();
            }
            return;
        }

        // Must be able to see the sky
        if (!level.canSeeSky(worldPosition.above())) {
            if (isAttuning) {
                abortAttunement();
            }
            return;
        }

        // Begin or continue attunement
        if (!isAttuning) {
            isAttuning = true;
            markForUpdate();
        }

        attunementTick++;

        if (attunementTick >= ATTUNEMENT_DURATION) {
            completeAttunement();
        }
    }

    /**
     * Complete the attunement process — mark the crystal with the constellation.
     */
    @SuppressWarnings("null")
    private void completeAttunement() {
        if (heldCrystal.isEmpty() || attunedConstellation == null) return;

        // Write the constellation attunement to the crystal's NBT
        CompoundTag itemTag = heldCrystal.getOrCreateTag();
        itemTag.putString("attunedConstellation", attunedConstellation.toString());
        heldCrystal.setTag(itemTag);

        // Reset state
        isAttuning = false;
        attunementTick = 0;
        markForUpdate();

        Level level = getLevel();
        if (level != null) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.ATTUNEMENT_BEAM, getBlockPos()),
                    (net.minecraft.server.level.ServerLevel) level, getBlockPos());
            level.playSound(null, getBlockPos(), SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    /**
     * Abort an in-progress attunement (conditions no longer met).
     */
    private void abortAttunement() {
        isAttuning = false;
        attunementTick = 0;
        markForUpdate();
    }

    /**
     * Validates the multiblock structure around the altar.
     * The attunement altar requires spectral relays at specific positions.
     *
     * <p>Structure: The altar at center with 8 sooty marble pillars
     * in a ring (at ±3, ±3 offsets) each topped with a spectral relay.</p>
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        // Check for open sky above
        if (!level.canSeeSky(worldPosition.above(2))) {
            return false;
        }

        // Simplified structure check: verify key positions have solid blocks
        // Full multiblock validation will use PatternBlockArray when available
        BlockPos[] pillarPositions = {
                worldPosition.offset(3, 0, 0),
                worldPosition.offset(-3, 0, 0),
                worldPosition.offset(0, 0, 3),
                worldPosition.offset(0, 0, -3),
                worldPosition.offset(2, 0, 2),
                worldPosition.offset(-2, 0, 2),
                worldPosition.offset(2, 0, -2),
                worldPosition.offset(-2, 0, -2)
        };

        for (BlockPos pillar : pillarPositions) {
            if (level.getBlockState(pillar).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the target constellation is currently visible in the sky.
     */
    private boolean isConstellationVisible(@Nonnull Level level,
                                            @Nonnull ResourceLocation constellationKey) {
        for (IConstellation visible : CelestialHandler.getVisibleConstellations(level)) {
            if (visible.getRegistryName().equals(constellationKey)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack stack) {
        this.heldCrystal = stack;
        markForUpdate();
    }

    public int getAttunementTick() {
        return attunementTick;
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isAttuning() {
        return isAttuning;
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
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
        this.attunementTick = compound.getInt("attunementTick");
        this.structureValid = compound.getBoolean("structureValid");
        this.isAttuning = compound.getBoolean("isAttuning");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putInt("attunementTick", attunementTick);
        compound.putBoolean("structureValid", structureValid);
        compound.putBoolean("isAttuning", isAttuning);
    }
}
