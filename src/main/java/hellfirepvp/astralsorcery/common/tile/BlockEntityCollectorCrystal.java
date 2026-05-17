package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
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
 * A starlight source that collects starlight from the sky and transmits
 * it to connected receivers (altars, infusers, etc.) via the starlight network.
 *
 * <p>Collection formula:
 * base = (crystalSize / 900) * BASE_COLLECTION_RATE
 * efficiency = (crystalPurity / 100) * 0.7 + (crystalCutting / 100) * 0.3
 * nightMultiplier = isNight ? 1.0 : 0.2 (reduced during day)
 * skyMultiplier = hasSkyAccess ? 1.0 : 0.0
 * collected = base * efficiency * nightMultiplier * skyMultiplier</p>
 *
 * <p>Implements {@link IStarlightSource} and {@link IIndependentStarlightSource}
 * so the starlight network can query its production even when the chunk is unloaded.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity,
 * tick() via BlockEntityTicker pattern,
 * ResourceLocation for constellation keying,
 * world.canSeeSky -> level.canSeeSky,
 * world.isNight -> level.isNight()</p>
 */
public class BlockEntityCollectorCrystal extends BlockEntityTick
        implements IStarlightSource, IIndependentStarlightSource, LinkableTileEntity {

    /** Base starlight units collected per tick at max size with perfect sky. */
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
    private ResourceLocation attunedConstellation = null;

    private double starlightCollected = 0;
    private double cachedProduction = 0;
    private int crystalSize = 400;
    private int crystalPurity = 100;
    private int crystalCutting = 100;
    private int ticksExisted = 0;
    private boolean celestial = false;
    private boolean registeredInNetwork = false;

    public BlockEntityCollectorCrystal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), pos, state);
    }

    /**
     * Protected constructor for subclasses (e.g., celestial variant) with custom type.
     */
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
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;

        if (isClientSide()) {
            // Client-side: particle effects based on collection state
            return;
        }

        // Server-side: calculate and cache starlight production
        if (hasSkyAccess()) {
            double production = calculateProduction();
            starlightCollected = production;
            cachedProduction = production;
        } else {
            starlightCollected = 0;
        }
    }

    /**
     * Calculates the starlight production rate for this tick.
     * Uses {@link CelestialHandler} for time/weather/moon/attunement factors.
     *
     * @return starlight units produced this tick
     */
    private double calculateProduction() {
        Level level = getLevel();
        if (level == null) return 0;

        // Base from crystal size
        double base = (crystalSize / 900.0) * BASE_COLLECTION_RATE;

        // Efficiency from purity (70% weight) and cutting (30% weight)
        double efficiency = (crystalPurity / 100.0) * 0.7 + (crystalCutting / 100.0) * 0.3;

        // Celestial distribution factor (time of day + weather + moon phase)
        float distributionFactor = CelestialHandler.getStarlightDistributionFactor(level);

        // Attunement bonus: 1.5x when attuned constellation is visible
        float attunementBonus = CelestialHandler.getAttunementBonus(level, attunedConstellation);

        // Celestial variant bonus
        double celestialMult = celestial ? CELESTIAL_MULTIPLIER : 1.0;

        return base * efficiency * distributionFactor * attunementBonus * celestialMult;
    }

    // ========================================================================
    // IStarlightSource implementation
    // ========================================================================

    @Override
    public double getStarlightProduction() {
        return starlightCollected;
    }

    @Nullable
    @Override
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
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

    // ========================================================================
    // IIndependentStarlightSource implementation
    // ========================================================================

    @Override
    public boolean providesAutoLink() {
        return true; // Collector crystals persist their production when chunk unloads
    }

    @Nonnull
    @Override
    public CompoundTag serializeSourceNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("cachedProduction", cachedProduction);
        tag.putInt("crystalSize", crystalSize);
        tag.putInt("crystalPurity", crystalPurity);
        tag.putInt("crystalCutting", crystalCutting);
        tag.putBoolean("celestial", celestial);
        if (attunedConstellation != null) {
            tag.putString("constellation", attunedConstellation.toString());
        }
        return tag;
    }

    @Override
    public void deserializeSourceNBT(@Nonnull CompoundTag tag) {
        this.cachedProduction = tag.getDouble("cachedProduction");
        this.crystalSize = tag.getInt("crystalSize");
        this.crystalPurity = tag.getInt("crystalPurity");
        this.crystalCutting = tag.getInt("crystalCutting");
        this.celestial = tag.getBoolean("celestial");
        if (tag.contains("constellation")) {
            this.attunedConstellation = new ResourceLocation(tag.getString("constellation"));
        }
    }

    // ========================================================================
    // Public API
    // ========================================================================

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        if (!isClientSide()) {
            StarlightNetworkHelper.registerSource(getLevel(), getBlockPos(), this);
        }
        markForUpdate();
    }

    public void setCrystalProperties(int size, int purity, int cutting) {
        this.crystalSize = Math.max(1, Math.min(900, size));
        this.crystalPurity = Math.max(0, Math.min(100, purity));
        this.crystalCutting = Math.max(0, Math.min(100, cutting));
        setChanged();
    }

    public void setCelestial(boolean celestial) {
        this.celestial = celestial;
        setChanged();
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

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    /**
     * Whether this is a celestial collector crystal (as opposed to a regular one).
     * Celestial crystals have enhanced collection rates.
     */
    public boolean isCelestial() {
        return celestial;
    }

    /**
     * Whether this crystal is actively collecting starlight.
     * Used by the renderer to show collection particle effects.
     */
    public boolean isCollecting() {
        return starlightCollected > 0;
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
        return Component.translatable("block.astralsorcery.collector_crystal");
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
        // Collectors don't link to entities
    }

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
        // Independent source: data stays in WorldNetworkHandler
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

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
        if (attunedConstellation != null) {
            compound.putString("constellation", attunedConstellation.toString());
        }
        compound.putInt("crystalSize", crystalSize);
        compound.putInt("crystalPurity", crystalPurity);
        compound.putInt("crystalCutting", crystalCutting);
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
