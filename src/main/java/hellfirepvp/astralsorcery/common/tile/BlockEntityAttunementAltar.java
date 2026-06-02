package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktAttunementActive;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

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

    private static final int CRYSTAL_ATTUNEMENT_DURATION = 200;  // 10 seconds
    private static final int PLAYER_ATTUNEMENT_DURATION  = 800;  // 40 seconds

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
    @Nullable
    private UUID attunedPlayerUUID = null;

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private Object idleSound;
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private Object activeSound;
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private boolean prevAttuning;

    public BlockEntityAttunementAltar(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ATTUNEMENT_ALTAR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            tickIdleSound();
            tickActiveSound();
            return;
        }

        Level level = getLevel();
        if (level == null) return;

        // Periodically re-validate multiblock structure
        if (ticksExisted % STRUCTURE_CHECK_INTERVAL == 0) {
            structureValid = validateStructure();
        }

        // Attunement requires: valid structure and target constellation set
        if (!structureValid || attunedConstellation == null) {
            if (isAttuning) abortAttunement();
            return;
        }

        ResourceLocation cstKey = java.util.Objects.requireNonNull(attunedConstellation);
        // Must be nighttime and the target constellation must be visible
        if (!DayTimeHelper.isNight(level) || !isConstellationVisible(level, cstKey)
                || !level.canSeeSky(worldPosition.above())) {
            if (isAttuning) abortAttunement();
            return;
        }

        if (!heldCrystal.isEmpty()) {
            // ── Crystal attunement path ──
            if (!isAttuning) {
                isAttuning = true;
                markForUpdate();
            }
            attunementTick++;
            if (attunementTick >= CRYSTAL_ATTUNEMENT_DURATION) completeAttunement();
        } else {
            // ── Player attunement path ──
            ServerPlayer target = findEligiblePlayer(level);
            if (target == null) {
                if (isAttuning) abortAttunement();
                return;
            }
            if (!isAttuning || !target.getUUID().equals(attunedPlayerUUID)) {
                isAttuning = true;
                attunedPlayerUUID = target.getUUID();
                attunementTick = 0;
                target.setInvulnerable(true);
                PacketChannel.sendToPlayer(new PktAttunementActive(true, worldPosition), target);
                markForUpdate();
            }
            attunementTick++;
            if (attunementTick >= PLAYER_ATTUNEMENT_DURATION) completePlayerAttunement(level);
        }
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private void tickIdleSound() {
        boolean active = structureValid && !isAttuning;
        if (SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0) {
            idleSound = null;
            return;
        }
        if (active) {
            if (idleSound == null || ((PositionedLoopSound) idleSound).hasStoppedPlaying()) {
                net.minecraft.world.phys.Vec3 center = net.minecraft.world.phys.Vec3.atCenterOf(worldPosition).add(0, 0.5, 0);
                idleSound = SoundHelper.playSoundLoopFadeInClient(
                        SoundsAS.ATTUNEMENT_ALTAR_IDLE.get(), SoundSource.BLOCKS, center, 0.4F, 1F, false,
                        s -> isRemoved() || SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0
                                || !structureValid || isAttuning)
                        .setFadeInTicks(20).setFadeOutTicks(20);
            }
        } else {
            idleSound = null;
        }
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private void tickActiveSound() {
        if (SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0) {
            activeSound = null;
            prevAttuning = isAttuning;
            return;
        }
        if (isAttuning) {
            if (!prevAttuning) {
                // Transition to attuning: fire one-shot start sound
                SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ALTAR_ITEM_START.get(),
                        SoundSource.BLOCKS, worldPosition, 1F, 1F);
                activeSound = null; // force loop to (re)start fresh
            }
            if (activeSound == null || ((FadeLoopSound) activeSound).hasStoppedPlaying()) {
                net.minecraft.world.phys.Vec3 center = net.minecraft.world.phys.Vec3.atCenterOf(worldPosition).add(0, 0.5, 0);
                activeSound = SoundHelper.playSoundLoopFadeInClient(
                        SoundsAS.ATTUNEMENT_ALTAR_ITEM_LOOP.get(), SoundSource.BLOCKS, center, 0.8F, 1F, false,
                        s -> isRemoved() || SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 || !isAttuning)
                        .setFadeInTicks(20).setFadeOutTicks(20);
            }
        } else {
            if (prevAttuning) {
                // Transition out of attuning: fire one-shot finish sound
                SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ALTAR_ITEM_FINISH.get(),
                        SoundSource.BLOCKS, worldPosition, 1F, 1F);
            }
            activeSound = null;
        }
        prevAttuning = isAttuning;
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
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.ATTUNEMENT_COMPLETE, getBlockPos()),
                    (net.minecraft.server.level.ServerLevel) level, getBlockPos());
            level.playSound(null, getBlockPos(), SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    /**
     * Abort an in-progress attunement (conditions no longer met).
     */
    private void abortAttunement() {
        if (attunedPlayerUUID != null) {
            UUID pid = attunedPlayerUUID;
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                ServerPlayer p = srv.getPlayerList().getPlayer(pid);
                if (p != null) {
                    p.setInvulnerable(false);
                    PacketChannel.sendToPlayer(new PktAttunementActive(false, worldPosition), p);
                }
            }
            attunedPlayerUUID = null;
        }
        isAttuning = false;
        attunementTick = 0;
        markForUpdate();
    }

    @Nullable
    private ServerPlayer findEligiblePlayer(@Nonnull Level level) {
        if (attunedConstellation == null) return null;
        IConstellation cst = ConstellationRegistry.getConstellation(attunedConstellation);
        if (!(cst instanceof IMajorConstellation)) return null;
        AABB box = new AABB(worldPosition).inflate(3.0);
        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, box);
        for (ServerPlayer candidate : nearby) {
            PlayerProgress progress = PlayerProgressHelper.getProgress(candidate);
            if (progress != null && progress.isAtLeast(ProgressionTier.ATTUNEMENT)
                    && progress.getAttunedConstellation() == null) {
                return candidate;
            }
        }
        return null;
    }

    private void completePlayerAttunement(@Nonnull Level level) {
        UUID uuid = attunedPlayerUUID;
        ResourceLocation cst = attunedConstellation;
        if (uuid == null || cst == null) { abortAttunement(); return; }
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) {
            ServerPlayer sp = srv.getPlayerList().getPlayer(uuid);
            if (sp != null) {
                sp.setInvulnerable(false);
                PacketChannel.sendToPlayer(new PktAttunementActive(false, worldPosition), sp);
                ResearchManager.attuneTo(sp, cst);
            }
        }
        if (level instanceof ServerLevel sl) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.CONSTELLATION_DISCOVER, worldPosition),
                    sl, worldPosition);
        }
        attunedPlayerUUID = null;
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
        if (compound.hasUUID("attunedPlayerUUID")) {
            this.attunedPlayerUUID = compound.getUUID("attunedPlayerUUID");
        } else {
            this.attunedPlayerUUID = null;
        }
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putInt("attunementTick", attunementTick);
        compound.putBoolean("structureValid", structureValid);
        compound.putBoolean("isAttuning", isAttuning);
        if (attunedPlayerUUID != null) {
            compound.putUUID("attunedPlayerUUID", attunedPlayerUUID);
        }
    }
}
