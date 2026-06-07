/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.item.ItemGlassLens;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Block entity for the Spectral Relay â€” a starlight booster for altars.
 * When placed on its required marble platform (see structure pattern below),
 * loaded with a Glass Lens, and with sky access, it provides starlight
 * to the nearest Starlight Crafting Altar within 16 blocks each tick.
 *
 * <p>Required structure (at y-1 relative to the relay block):
 * <pre>
 *   [M] [A] [M]
 *   [A] [S] [A]
 *   [M] [A] [M]
 * </pre>
 * M = Marble Chiseled, A = Marble Arch, S = Black Marble Raw (center below relay)</p>
 *
 * <p>Proximity penalty: if another relay is within 8 blocks, the starlight
 * contribution scales down linearly with distance to the neighbour.</p>
 *
 * <p>1.16 â†’ 1.20: TileEntityTick â†’ BlockEntityTick, CompoundNBT â†’ CompoundTag,
 * TileAltar â†’ BlockEntityAltar, AltarCollectionCategory â†’ altar.receiveStarlight(),
 * DayTimeHelper.getCurrentDaytimeDistribution â†’ CelestialHandler.getStarlightDistributionFactor(),
 * observerlib PatternBlockArray multiblock â†’ inline block-position check.</p>
 */
public class BlockEntitySpectralRelay extends BlockEntityTick {

    /** Search radius for nearby altars (blocks). */
    private static final int ALTAR_SEARCH_RADIUS = 16;

    /** Search radius for nearby relays (for proximity penalty). */
    private static final int RELAY_SEARCH_RADIUS = 8;

    /** How often to re-validate structure and re-search for altar/relays (ticks). */
    private static final int STRUCTURE_CHECK_INTERVAL = 40;

    /** Starlight provided per tick when active (matches 1.16 heightAmount * 45). */
    private static final float BASE_STARLIGHT_RATE = 45.0F;

    private final TileInventory inventory;

    @Nullable
    private BlockPos altarPos = null;

    @Nullable
    private BlockPos closestRelayPos = null;

    private float proximityMultiplier = 1.0F;
    private boolean hasMultiblock = false;

    public BlockEntitySpectralRelay(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.SPECTRAL_RELAY.get(), pos, state);
        this.inventory = new TileInventory(this, () -> 1);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide()) {
            updateRelayProximity();
            updateAltarPos();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (isClientSide()) {
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Eject inventory if blocked from above
        if (!level.isEmptyBlock(getBlockPos().above())) {
            ItemStack held = inventory.getStackInSlot(0);
            if (!held.isEmpty()) {
                ItemUtils.dropItem(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), held.copy());
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        }


        // Periodic re-validation
        if (getTicksExisted() % STRUCTURE_CHECK_INTERVAL == 0) {
            hasMultiblock = validateStructure();
            if (hasMultiblock && hasGlassLens()) {
                updateAltarPos();
                updateRelayProximity();
            } else if (!hasGlassLens()) {
                altarPos = null;
            }
        }

        BlockPos ap = altarPos;
        if (hasMultiblock && hasGlassLens() && ap != null) {
            MiscUtils.executeWithChunk(level, ap, () -> {
                BlockEntityAltar altar = MiscUtils.getTileAt(level, ap, BlockEntityAltar.class, true);
                if (altar == null) {
                    altarPos = null;
                } else {
                    provideStarlight(altar);
                }
            });
        }
    }

    private void provideStarlight(@Nonnull BlockEntityAltar altar) {
        Level level = getLevel();
        if (level == null || !doesSeeSky()) return;

        // Height factor: more starlight the higher the relay (matches 1.16 formula)
        float heightAmount = Mth.clamp(
                (float) Math.pow(getBlockPos().getY() / 7.0F, 1.5F) / 60.0F, 0.0F, 1.0F);
        heightAmount = 0.7F + heightAmount * 0.3F;

        // Daytime factor â€” less starlight during day (matches CelestialHandler distribution)
        heightAmount *= CelestialHandler.getStarlightDistributionFactor(level);

        // Proximity penalty
        heightAmount *= proximityMultiplier;

        if (heightAmount > 1E-4F) {
            altar.receiveStarlight(heightAmount * BASE_STARLIGHT_RATE, null);
        }
    }

    // ========================================================================
    // Multiblock validation
    // ========================================================================

    /**
     * Validates the marble platform beneath the relay:
     * Black marble directly below; chiseled marble at diagonals; marble arch at cardinals.
     */
    private boolean validateStructure() {
        Level level = getLevel();
        if (level == null) return false;

        BlockPos center = getBlockPos().below();

        // Black marble directly below
        if (!level.getBlockState(center).is(BlocksAS.BLACK_MARBLE_RAW.get())) {
            return false;
        }
        // Chiseled marble at diagonals
        if (!level.getBlockState(center.offset(-1, 0, -1)).is(BlocksAS.MARBLE_CHISELED.get())) return false;
        if (!level.getBlockState(center.offset( 1, 0, -1)).is(BlocksAS.MARBLE_CHISELED.get())) return false;
        if (!level.getBlockState(center.offset(-1, 0,  1)).is(BlocksAS.MARBLE_CHISELED.get())) return false;
        if (!level.getBlockState(center.offset( 1, 0,  1)).is(BlocksAS.MARBLE_CHISELED.get())) return false;
        // Marble arch at cardinals
        if (!level.getBlockState(center.offset(-1, 0,  0)).is(BlocksAS.MARBLE_ARCH.get())) return false;
        if (!level.getBlockState(center.offset( 1, 0,  0)).is(BlocksAS.MARBLE_ARCH.get())) return false;
        if (!level.getBlockState(center.offset( 0, 0, -1)).is(BlocksAS.MARBLE_ARCH.get())) return false;
        if (!level.getBlockState(center.offset( 0, 0,  1)).is(BlocksAS.MARBLE_ARCH.get())) return false;

        return true;
    }

    // ========================================================================
    // Altar + relay neighbour search
    // ========================================================================

    private void updateAltarPos() {
        Level level = getLevel();
        if (level == null || !hasGlassLens() || !hasMultiblock) {
            altarPos = null;
            markForUpdate();
            return;
        }

        Set<BlockPos> altarPositions = BlockDiscoverer.searchForTileEntitiesAround(
                level, getBlockPos(), ALTAR_SEARCH_RADIUS, tile -> tile instanceof BlockEntityAltar);

        BlockPos thisPos = getBlockPos();
        BlockPos closest = null;
        for (BlockPos other : altarPositions) {
            if (closest == null || thisPos.distSqr(other) < thisPos.distSqr(closest)) {
                closest = other;
            }
        }

        altarPos = closest;
        markForUpdate();
    }

    private void updateRelayProximity() {
        Level level = getLevel();
        if (level == null || !hasGlassLens()) {
            closestRelayPos = null;
            proximityMultiplier = 1.0F;
            return;
        }

        BlockPos thisPos = getBlockPos();
        Set<BlockPos> nearby = BlockDiscoverer.searchForTileEntitiesAround(
                level, thisPos, RELAY_SEARCH_RADIUS, tile ->
                        tile instanceof BlockEntitySpectralRelay relay
                                && !relay.getBlockPos().equals(thisPos)
                                && relay.hasGlassLens()
                                && relay.hasMultiblock);

        BlockPos closest = null;
        for (BlockPos pos : nearby) {
            if (closest == null || thisPos.distSqr(pos) < thisPos.distSqr(closest)) {
                closest = pos;
            }
        }

        closestRelayPos = closest;
        if (closest == null) {
            proximityMultiplier = 1.0F;
        } else {
            double dist = Math.sqrt(thisPos.distSqr(closest));
            proximityMultiplier = Mth.clamp((float) dist / RELAY_SEARCH_RADIUS, 0.0F, 1.0F);
        }
        markForUpdate();
    }

    // ========================================================================
    // Public API
    // ========================================================================

    public boolean hasGlassLens() {
        return inventory.getStackInSlot(0).getItem() instanceof ItemGlassLens;
    }

    public boolean hasMultiblock() {
        return hasMultiblock;
    }

    @Nonnull
    public TileInventory getInventory() {
        return inventory;
    }

    @Nullable
    public BlockPos getAltarPos() {
        return altarPos;
    }

    public float getProximityMultiplier() {
        return proximityMultiplier;
    }

    // ========================================================================
    // Capability
    // ========================================================================

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (inventory.hasCapability(cap, side)) {
            return inventory.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inventory.getCapability().invalidate();
    }

    // ========================================================================
    // NBT
    // ========================================================================

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        inventory.deserialize(compound.getCompound("inventory"));
        altarPos = compound.contains("altarPos") ? NbtUtils.readBlockPos(compound.getCompound("altarPos")) : null;
        closestRelayPos = compound.contains("closestRelayPos") ? NbtUtils.readBlockPos(compound.getCompound("closestRelayPos")) : null;
        hasMultiblock = compound.getBoolean("hasMultiblock");
        proximityMultiplier = compound.getFloat("proximityMultiplier");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.put("inventory", inventory.serialize());
        BlockPos ap2 = altarPos;
        if (ap2 != null) {
            compound.put("altarPos", NbtUtils.writeBlockPos(ap2));
        }
        BlockPos crp = closestRelayPos;
        if (crp != null) {
            compound.put("closestRelayPos", NbtUtils.writeBlockPos(crp));
        }
        compound.putBoolean("hasMultiblock", hasMultiblock);
        compound.putFloat("proximityMultiplier", proximityMultiplier);
    }
}
