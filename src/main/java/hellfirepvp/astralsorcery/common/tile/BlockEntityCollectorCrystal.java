package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.ConstellationTile;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkHelper;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block entity for Collector Crystals.
 * Collects starlight from the sky and transmits it to linked receivers via the starlight network.
 * Crystal quality is stored as {@link CrystalAttributes} and applied via the property modifier chain.
 *
 * <p>1.16 → 1.20: TileEntity → BlockEntity, tick via BlockEntityTicker pattern,
 * ResourceLocation for constellation keying, world.canSeeSky → level.canSeeSky.</p>
 */
public class BlockEntityCollectorCrystal extends BlockEntityTick
        implements IIndependentStarlightSource, LinkableTileEntity,
                   CrystalAttributeTile, ConstellationTile {

    /** Base starlight multiplier per tick (scaled by attribute modifiers). */
    private static final double BASE_COLLECTION_RATE = 200.0;

    /** Celestial collector crystals collect 50% more starlight. */
    private static final double CELESTIAL_MULTIPLIER = 1.5;

    /** Maximum outgoing links for a collector crystal. */
    private static final int MAX_OUTGOING_LINKS = 4;

    /** Maximum link range (blocks). */
    private static final double MAX_LINK_RANGE = 64.0;

    @Nonnull
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    @Nullable
    private CrystalAttributes crystalAttributes = null;

    @Nullable
    private IWeakConstellation constellationType = null;

    @Nullable
    private IMinorConstellation constellationTrait = null;

    private double starlightCollected = 0;
    private double cachedProduction = 0;
    private int ticksExisted = 0;
    private boolean celestial = false;
    private boolean registeredInNetwork = false;

    public BlockEntityCollectorCrystal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), pos, state);
    }

    protected BlockEntityCollectorCrystal(@Nonnull BlockEntityType<?> type,
                                          @Nonnull BlockPos pos,
                                          @Nonnull BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        if (!isClientSide() && !registeredInNetwork) {
            StarlightNetworkHelper.registerSource(getLevel(), getBlockPos(), this);
            registeredInNetwork = true;
            hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler handler =
                    StarlightNetworkHelper.getHandler(getLevel());
            if (handler != null) handler.attemptAutoLinkFrom(getBlockPos());
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;

        if (isClientSide()) {
            return;
        }

        if (hasSkyAccess()) {
            double production = calculateProduction();
            starlightCollected = production;
            cachedProduction = production;
        } else {
            starlightCollected = 0;
        }
    }

    private double calculateProduction() {
        Level level = getLevel();
        if (level == null) return 0;

        double crystalMultiplier = crystalAttributes != null
                ? CrystalCalculations.getCollectorCrystalCollectionRate(crystalAttributes)
                : 0.1;

        float distributionFactor = CelestialHandler.getStarlightDistributionFactor(level);
        float attunementBonus = CelestialHandler.getAttunementBonus(level, getAttunedConstellationKey());
        double celestialMult = celestial ? CELESTIAL_MULTIPLIER : 1.0;

        return BASE_COLLECTION_RATE * crystalMultiplier * distributionFactor * attunementBonus * celestialMult;
    }

    // ========================================================================
    // CrystalAttributeTile
    // ========================================================================

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return crystalAttributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        this.crystalAttributes = attributes;
        markForUpdate();
    }

    // ========================================================================
    // ConstellationTile
    // ========================================================================

    @Nullable
    @Override
    public IWeakConstellation getAttunedConstellation() {
        return constellationType;
    }

    @Override
    public boolean setAttunedConstellation(@Nullable IWeakConstellation cst) {
        this.constellationType = cst;
        if (!isClientSide()) {
            StarlightNetworkHelper.registerSource(getLevel(), getBlockPos(), this);
        }
        markForUpdate();
        return true;
    }

    @Nullable
    @Override
    public IMinorConstellation getTraitConstellation() {
        return constellationTrait;
    }

    @Override
    public boolean setTraitConstellation(@Nullable IMinorConstellation cst) {
        this.constellationTrait = cst;
        markForUpdate();
        return true;
    }

    // ========================================================================
    // IIndependentStarlightSource (includes IStarlightSource)
    // ========================================================================

    @Override
    public double getStarlightProduction() {
        return starlightCollected;
    }

    @Nullable
    @Override
    public ResourceLocation getAttunedConstellationKey() {
        return constellationType != null ? constellationType.getRegistryName() : null;
    }

    @Nullable
    @Override
    public Level getSourceLevel() {
        return getLevel();
    }

    @Override
    public boolean hasSkyAccess() {
        Level level = getLevel();
        if (level == null) return false;
        BlockPos above = getBlockPos().above();
        return level.canSeeSky(above)
                && level.getBrightness(LightLayer.SKY, above) > 0;
    }

    @Nonnull
    @Override
    public BlockPos getLocationPos() {
        return getBlockPos();
    }

    @Override
    public boolean providesAutoLink() {
        return true;
    }

    @Nonnull
    @Override
    public CompoundTag serializeSourceNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("cachedProduction", cachedProduction);
        tag.putBoolean("celestial", celestial);
        if (constellationType != null) {
            tag.putString("constellation", constellationType.getRegistryName().toString());
        }
        if (crystalAttributes != null) {
            crystalAttributes.store(tag);
        }
        return tag;
    }

    @Override
    public void deserializeSourceNBT(@Nonnull CompoundTag tag) {
        this.cachedProduction = tag.getDouble("cachedProduction");
        this.celestial = tag.getBoolean("celestial");
        if (tag.contains("constellation")) {
            IConstellation cst = ConstellationRegistry.getConstellation(
                    new ResourceLocation(tag.getString("constellation")));
            this.constellationType = cst instanceof IWeakConstellation wc ? wc : null;
        }
        this.crystalAttributes = CrystalAttributes.getCrystalAttributes(tag);
    }

    // ========================================================================
    // Public API
    // ========================================================================

    public void setCelestial(boolean celestial) {
        this.celestial = celestial;
        setChanged();
    }

    public double getStarlightCollected() {
        return starlightCollected;
    }

    public int getConstellationColor() {
        if (constellationType != null) {
            return constellationType.getConstellationColor().getRGB();
        }
        return celestial ? 0x4488DD : 0xAAAAFF;
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isCelestial() {
        return celestial;
    }

    public boolean isCollecting() {
        return starlightCollected > 0;
    }

    // ========================================================================
    // LinkableTileEntity
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
        return Component.translatable("block.astralsorcery.collector_crystal");
    }

    @Override
    public boolean doesAcceptLinks() {
        return true;
    }

    @Override
    public void onBlockLinkCreate(@Nonnull Player player, @Nonnull BlockPos other) {}

    @Override
    public void onEntityLinkCreate(@Nonnull Player player, @Nonnull Entity entity) {}

    @Override
    public boolean onSelect(@Nonnull Player player) {
        player.displayClientMessage(
                Component.translatable("astralsorcery.link.selected.collector"), true);
        return true;
    }

    @Override
    public boolean tryLinkBlock(@Nonnull Player player, @Nonnull BlockPos other) {
        if (linkedTargets.size() >= MAX_OUTGOING_LINKS) {
            return false;
        }
        if (getBlockPos().distSqr(other) > MAX_LINK_RANGE * MAX_LINK_RANGE) {
            return false;
        }
        if (!linkedTargets.contains(other)) {
            linkedTargets.add(other.immutable());
            if (!isClientSide()) {
                StarlightNetworkHelper.addLink(getLevel(), getBlockPos(), other);
            }
            markForUpdate();
        }
        return true;
    }

    @Override
    public boolean tryLinkEntity(@Nonnull Player player, @Nonnull Entity entity) {
        return false;
    }

    @Override
    public boolean tryUnlink(@Nonnull Player player, @Nonnull BlockPos other) {
        if (linkedTargets.remove(other)) {
            if (!isClientSide()) {
                StarlightNetworkHelper.removeLink(getLevel(), getBlockPos(), other);
            }
            markForUpdate();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public List<BlockPos> getLinkedPositions() {
        return Collections.unmodifiableList(linkedTargets);
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

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);

        this.crystalAttributes = CrystalAttributes.getCrystalAttributes(compound);

        this.constellationType = null;
        this.constellationTrait = null;
        if (compound.contains("constellation")) {
            IConstellation cst = ConstellationRegistry.getConstellation(
                    new ResourceLocation(compound.getString("constellation")));
            this.constellationType = cst instanceof IWeakConstellation wc ? wc : null;
        }
        if (compound.contains("trait")) {
            IConstellation cst = ConstellationRegistry.getConstellation(
                    new ResourceLocation(compound.getString("trait")));
            this.constellationTrait = cst instanceof IMinorConstellation mc ? mc : null;
        }

        this.celestial = compound.getBoolean("celestial");

        this.linkedTargets.clear();
        if (compound.contains("linkedTargets")) {
            ListTag list = compound.getList("linkedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                linkedTargets.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);

        if (crystalAttributes != null) {
            crystalAttributes.store(compound);
        }
        if (constellationType != null) {
            compound.putString("constellation", constellationType.getRegistryName().toString());
        }
        if (constellationTrait != null) {
            compound.putString("trait", constellationTrait.getRegistryName().toString());
        }
        compound.putBoolean("celestial", celestial);

        ListTag list = new ListTag();
        for (BlockPos pos : linkedTargets) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        compound.put("linkedTargets", list);
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.starlightCollected = compound.getDouble("starlightCollected");
        this.cachedProduction = compound.getDouble("cachedProduction");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putDouble("starlightCollected", starlightCollected);
        compound.putDouble("cachedProduction", cachedProduction);
    }
}
