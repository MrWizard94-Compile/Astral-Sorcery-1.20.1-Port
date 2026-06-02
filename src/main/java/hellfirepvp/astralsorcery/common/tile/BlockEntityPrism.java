package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block entity for the Prism.
 * Part of the starlight transmission network. Unlike a Lens which has
 * a single output, a Prism supports up to 4 output targets, splitting
 * the incoming starlight evenly among them.
 *
 * <p>A Prism also accepts an inserted colored lens item that filters
 * the transmitted starlight (e.g., fire, growth, damage). The lens
 * effect is applied to all outputs.</p>
 *
 * <p>Efficiency is 85% by default (lower than a Lens's 95% because
 * of the splitting mechanism). Each output receives:
 * (incoming * 0.85) / numTargets</p>
 *
 * <p>Implements {@link IStarlightTransmission} for network participation.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * NBTUtil -> NbtUtils</p>
 */
public class BlockEntityPrism extends BlockEntityTick implements IStarlightTransmission {

    /** Maximum number of outgoing links. */
    private static final int MAX_LINKS = 4;

    /** Default transmission efficiency for a prism. */
    private static final double DEFAULT_EFFICIENCY = 0.85;

    /** Maximum link range for prism connections. */
    private static final double MAX_PRISM_RANGE = 48.0;

    @Nonnull
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    @Nullable
    private ItemStack insertedLens = null;
    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    private int ticksExisted = 0;
    private double transmissionEfficiency = DEFAULT_EFFICIENCY;
    private boolean registeredInNetwork = false;

    public BlockEntityPrism(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.PRISM.get(), pos, state);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerTransmission(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // Client-side: beam split rendering handled by TESR
            return;
        }
        // Server-side: transmission handled by WorldNetworkHandler graph traversal
    }

    // ========================================================================
    // IStarlightTransmission implementation
    // ========================================================================

    @Override
    public double getTransmissionEfficiency() {
        return transmissionEfficiency;
    }

    @Nonnull
    @Override
    public List<BlockPos> getTransmissionTargets() {
        return Collections.unmodifiableList(linkedTargets);
    }

    @Override
    public boolean canAcceptLink(@Nonnull BlockPos from) {
        double distSq = getBlockPos().distSqr(from);
        return distSq <= MAX_PRISM_RANGE * MAX_PRISM_RANGE;
    }

    @Nullable
    @Override
    public Level getTransmissionLevel() {
        return getLevel();
    }

    @Nonnull
    @Override
    public BlockPos getLocationPos() {
        return getBlockPos();
    }

    // ========================================================================
    // Public API
    // ========================================================================

    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isTransmitting() {
        return !linkedTargets.isEmpty();
    }

    /**
     * Number of active output links.
     */
    public int getLinkCount() {
        return linkedTargets.size();
    }

    /**
     * Maximum links this prism supports.
     */
    public int getMaxLinks() {
        return MAX_LINKS;
    }

    @Nonnull
    public List<BlockPos> getLinkedTargets() {
        return Collections.unmodifiableList(linkedTargets);
    }

    /**
     * Attempts to add a linked target. Returns false if the max link count is reached
     * or target is out of range.
     */
    public boolean addLinkedTarget(@Nonnull BlockPos target) {
        if (linkedTargets.size() >= MAX_LINKS) {
            return false;
        }
        if (getBlockPos().distSqr(target) > MAX_PRISM_RANGE * MAX_PRISM_RANGE) {
            return false;
        }
        if (!linkedTargets.contains(target)) {
            linkedTargets.add(target.immutable());
            if (!isClientSide()) {
                StarlightNetworkHelper.addLink(getLevel(), getBlockPos(), target);
            }
            markForUpdate();
        }
        return true;
    }

    public void removeLinkedTarget(@Nonnull BlockPos target) {
        if (linkedTargets.remove(target)) {
            if (!isClientSide()) {
                StarlightNetworkHelper.removeLink(getLevel(), getBlockPos(), target);
            }
            markForUpdate();
        }
    }

    public void clearLinkedTargets() {
        for (BlockPos target : new ArrayList<>(linkedTargets)) {
            if (!isClientSide()) {
                StarlightNetworkHelper.removeLink(getLevel(), getBlockPos(), target);
            }
        }
        linkedTargets.clear();
        markForUpdate();
    }

    @Nullable
    public ItemStack getInsertedLens() {
        return insertedLens;
    }

    /**
     * Sets the inserted colored lens item. Null or empty removes the lens.
     */
    public void setInsertedLens(@Nullable ItemStack lens) {
        this.insertedLens = (lens != null && !lens.isEmpty()) ? lens.copy() : null;
        markForUpdate();
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack crystal) {
        this.heldCrystal = crystal.isEmpty() ? ItemStack.EMPTY : crystal.copy();
        recalculateEfficiency();
        markForUpdate();
    }

    private void recalculateEfficiency() {
        if (heldCrystal.isEmpty() || !(heldCrystal.getItem() instanceof ItemCrystalBase)) {
            this.transmissionEfficiency = DEFAULT_EFFICIENCY;
            return;
        }
        CrystalProperties props = CrystalProperties.getFromStack(heldCrystal);
        this.transmissionEfficiency = props != null
                ? CrystalCalculations.getTransmissionEfficiency(props)
                : DEFAULT_EFFICIENCY;
    }

    /**
     * Whether this prism has a colored lens inserted.
     */
    public boolean hasInsertedLens() {
        return insertedLens != null && !insertedLens.isEmpty();
    }

    // ========================================================================
    // Lifecycle
    // ========================================================================

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isClientSide()) {
            StarlightNetworkHelper.removeNode(getLevel(), getBlockPos());
        }
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);

        this.linkedTargets.clear();
        if (compound.contains("linkedTargets")) {
            ListTag list = compound.getList("linkedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedTargets.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }

        this.insertedLens = compound.contains("insertedLens")
                ? ItemStack.of(compound.getCompound("insertedLens")) : null;

        this.heldCrystal = compound.contains("heldCrystal")
                ? ItemStack.of(compound.getCompound("heldCrystal")) : ItemStack.EMPTY;
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);

        ListTag list = new ListTag();
        for (BlockPos pos : linkedTargets) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedTargets", list);

        ItemStack lens = insertedLens;
        if (lens != null && !lens.isEmpty()) {
            compound.put("insertedLens", lens.save(new CompoundTag()));
        }
        if (!heldCrystal.isEmpty()) {
            compound.put("heldCrystal", heldCrystal.save(new CompoundTag()));
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.heldCrystal = compound.contains("heldCrystal")
                ? ItemStack.of(compound.getCompound("heldCrystal")) : ItemStack.EMPTY;
        recalculateEfficiency();
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        if (!heldCrystal.isEmpty()) {
            compound.put("heldCrystal", heldCrystal.save(new CompoundTag()));
        }
    }
}
