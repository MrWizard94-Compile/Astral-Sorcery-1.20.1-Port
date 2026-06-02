package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLens;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block entity for the Lens.
 * Part of the starlight transmission network. Redirects a starlight
 * beam from one source to one target. Supports a color overlay
 * from tinted lens items.
 *
 * <p>Maximum 1 linked target. Lenses do not split beams — for that,
 * use a Prism ({@link BlockEntityPrism}).</p>
 *
 * <p>Transmission efficiency is 95% by default. Color overlays
 * can modify the starlight type passing through (e.g., damage lens
 * converts starlight to focused damage beam).</p>
 *
 * <p>Implements {@link IStarlightTransmission} so it participates
 * in the starlight network's graph traversal.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for color overlay keying,
 * NBTUtil -> NbtUtils</p>
 */
public class BlockEntityLens extends BlockEntityTick implements IStarlightTransmission, LinkableTileEntity {

    /** Maximum number of outgoing links for a basic lens. */
    private static final int MAX_LINKS = 1;

    /** Default efficiency: 95% starlight passes through. */
    private static final double DEFAULT_EFFICIENCY = 0.95;

    /** Maximum allowed link distance specific to lenses (shorter than network global). */
    private static final double MAX_LENS_RANGE = 48.0;

    @Nullable
    private ResourceLocation colorOverlay = null;

    @Nonnull
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;
    private double transmissionEfficiency = DEFAULT_EFFICIENCY;
    private int ticksExisted = 0;
    private boolean registeredInNetwork = false;

    public BlockEntityLens(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.LENS.get(), pos, state);
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
            return;
        }
        if (colorOverlay != null && !linkedTargets.isEmpty()) {
            applyBeamEffects();
        }
    }

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

    private void collectBlocksAlongBeam(Level level, Vec3 from, Vec3 to,
                                         PartialEffectExecutor exec, ItemColoredLens.LensColor color) {
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 0.5) return;
        Vec3 step = dir.normalize().scale(0.5);
        int steps = (int) (length / 0.5) + 1;
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
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

    private void collectEntitiesAlongBeam(Level level, Vec3 from, Vec3 to,
                                            PartialEffectExecutor exec, ItemColoredLens.LensColor color) {
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

    @Nullable
    private ItemColoredLens.LensColor resolveLensColor() {
        return ItemColoredLens.fromOverlay(colorOverlay);
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
        return distSq <= MAX_LENS_RANGE * MAX_LENS_RANGE;
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

    @Nullable
    public ResourceLocation getColorOverlay() {
        return colorOverlay;
    }

    /**
     * Returns the visual tint color for the transmitted starlight beam, or
     * null if no colored lens is installed (use default starlight blue-white).
     */
    @Nullable
    public Color getBeamColor() {
        ItemColoredLens.LensColor color = ItemColoredLens.fromOverlay(colorOverlay);
        return color != null ? color.getBeamColor() : null;
    }

    public void setColorOverlay(@Nullable ResourceLocation overlay) {
        this.colorOverlay = overlay;
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

    @Nonnull
    public List<BlockPos> getLinkedTargets() {
        return Collections.unmodifiableList(linkedTargets);
    }

    /**
     * Attempts to add a linked target. Returns false if the max link count is reached
     * or if the target is out of range.
     */
    public boolean addLinkedTarget(@Nonnull BlockPos target) {
        if (linkedTargets.size() >= MAX_LINKS) {
            return false;
        }
        if (getBlockPos().distSqr(target) > MAX_LENS_RANGE * MAX_LENS_RANGE) {
            return false;
        }
        if (!linkedTargets.contains(target)) {
            linkedTargets.add(target.immutable());
            // Register the link in the network
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

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    /**
     * Whether the lens is actively transmitting starlight.
     * Used by the renderer to show beam effects.
     */
    public boolean isTransmitting() {
        return !linkedTargets.isEmpty();
    }

    /**
     * Whether this lens has a color overlay applied.
     */
    public boolean hasColorOverlay() {
        return colorOverlay != null;
    }

    // ========================================================================
    // LinkableTileEntity implementation
    // ========================================================================

    @Nullable
    @Override
    public Level getLinkWorld() {
        return getLevel();
    }

    @Nonnull
    @Override
    public BlockPos getLinkPos() {
        return getBlockPos();
    }

    @Nonnull
    @Override
    public Component getUnLocalizedDisplayName() {
        return Component.translatable("block.astralsorcery.lens");
    }

    @Override
    public boolean doesAcceptLinks() {
        return true;
    }

    @Override
    public void onBlockLinkCreate(@Nonnull Player player, @Nonnull BlockPos other) {
        // Feedback handled by the linking tool
    }

    @Override
    public void onEntityLinkCreate(@Nonnull Player player, @Nonnull Entity entity) {
        // Lenses don't link to entities
    }

    @Override
    public boolean onSelect(@Nonnull Player player) {
        player.displayClientMessage(
                Component.translatable("astralsorcery.link.selected.lens"), true);
        return true;
    }

    @Override
    public boolean tryLinkBlock(@Nonnull Player player, @Nonnull BlockPos other) {
        return addLinkedTarget(other);
    }

    @Override
    public boolean tryLinkEntity(@Nonnull Player player, @Nonnull Entity entity) {
        return false; // Lenses cannot link to entities
    }

    @Override
    public boolean tryUnlink(@Nonnull Player player, @Nonnull BlockPos other) {
        if (linkedTargets.contains(other)) {
            removeLinkedTarget(other);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public List<BlockPos> getLinkedPositions() {
        return getLinkedTargets();
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
        if (compound.contains("colorOverlay")) {
            this.colorOverlay = new ResourceLocation(compound.getString("colorOverlay"));
        } else {
            this.colorOverlay = null;
        }

        this.linkedTargets.clear();
        if (compound.contains("linkedTargets")) {
            ListTag list = compound.getList("linkedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedTargets.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }

        if (compound.contains("heldCrystal")) {
            this.heldCrystal = ItemStack.of(compound.getCompound("heldCrystal"));
        } else {
            this.heldCrystal = ItemStack.EMPTY;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (colorOverlay != null) {
            compound.putString("colorOverlay", colorOverlay.toString());
        }

        ListTag list = new ListTag();
        for (BlockPos pos : linkedTargets) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedTargets", list);

        if (!heldCrystal.isEmpty()) {
            compound.put("heldCrystal", heldCrystal.save(new CompoundTag()));
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        if (compound.contains("heldCrystal")) {
            this.heldCrystal = ItemStack.of(compound.getCompound("heldCrystal"));
        } else {
            this.heldCrystal = ItemStack.EMPTY;
        }
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
