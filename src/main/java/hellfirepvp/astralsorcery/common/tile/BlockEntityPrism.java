package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLens;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (isClientSide()) {
            // Client-side: beam split rendering handled by TESR
            return;
        }
        if (insertedLens != null && !linkedTargets.isEmpty()) {
            applyBeamEffects();
        }
    }

    /**
     * Applies the inserted colored lens effect to every split beam.
     * Mirrors BlockEntityLens.applyBeamEffects() but fans out across all linked targets.
     */
    private void applyBeamEffects() {
        Level level = getLevel();
        if (level == null) return;

        ItemColoredLens.LensColor color = resolveLensColor();
        if (color == null || color.getTargetType() == ItemColoredLens.TargetType.NONE) return;

        Vec3 from = Vec3.atCenterOf(getBlockPos());
        for (BlockPos targetPos : linkedTargets) {
            Vec3 to = Vec3.atCenterOf(targetPos);
            PartialEffectExecutor exec = new PartialEffectExecutor(1.0F);

            if (color.getTargetType().doBlockInteraction()) {
                collectBlocksAlongBeam(level, from, to, exec, color);
            }
            if (color.getTargetType().doEntityInteraction()) {
                collectEntitiesAlongBeam(level, from, to, exec, color);
            }
        }
    }

    @Nullable
    private ItemColoredLens.LensColor resolveLensColor() {
        ItemStack lens = insertedLens;
        if (lens == null || lens.isEmpty()) return null;
        if (!(lens.getItem() instanceof ItemColoredLens coloredLens)) return null;
        return coloredLens.getLensColor();
    }

    private void collectBlocksAlongBeam(@Nonnull Level level, @Nonnull Vec3 from, @Nonnull Vec3 to,
                                         @Nonnull PartialEffectExecutor exec,
                                         @Nonnull ItemColoredLens.LensColor color) {
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 0.5) return;
        Vec3 step = dir.normalize().scale(0.5);
        int steps = (int) (length / 0.5) + 1;
        Set<BlockPos> visited = new HashSet<>();
        Vec3 cur = from;
        for (int i = 0; i < steps; i++) {
            BlockPos pos = BlockPos.containing(cur.x, cur.y, cur.z);
            if (visited.add(pos)) {
                BlockState state = level.getBlockState(pos);
                if (!state.isAir()) {
                    color.blockInBeam(level, pos, state, exec);
                }
            }
            cur = cur.add(step);
        }
    }

    private void collectEntitiesAlongBeam(@Nonnull Level level, @Nonnull Vec3 from, @Nonnull Vec3 to,
                                            @Nonnull PartialEffectExecutor exec,
                                            @Nonnull ItemColoredLens.LensColor color) {
        AABB box = new AABB(from.x, from.y, from.z, to.x, to.y, to.z).inflate(0.75);
        Vec3 dir = to.subtract(from);
        double lenSq = dir.lengthSqr();
        for (Entity entity : level.getEntities((Entity) null, box,
                e -> !(e instanceof Player) || !((Player) e).isSpectator())) {
            if (lenSq > 0) {
                Vec3 toEntity = entity.position().subtract(from);
                double t = Math.max(0, Math.min(1, toEntity.dot(dir) / lenSq));
                Vec3 closest = from.add(dir.scale(t));
                if (entity.position().distanceToSqr(closest) > 0.75 * 0.75) continue;
            }
            color.entityInBeam(level, from, to, entity, exec);
        }
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
            if (!isClientSide()) {
                boolean accepted = StarlightNetworkHelper.addLink(getLevel(), getBlockPos(), target);
                if (!accepted) return false;
            }
            linkedTargets.add(target.immutable());
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
        ItemStack lens = insertedLens;
        return lens != null && !lens.isEmpty();
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
        // heldCrystal is already read in readCustomNBT (sent in the update tag).
        // Trigger recalculation here so disk-load also derives efficiency.
        recalculateEfficiency();
    }
}
