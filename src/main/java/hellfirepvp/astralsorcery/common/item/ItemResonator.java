package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.capability.ChunkFluidCapabilityProvider;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Random;

/**
 * Resonator — reveals starlight sources and area of influence.
 * Upgradeable via altar: STARLIGHT (base) → FLUID_FIELDS (domic) → AREA_SIZE (ichosic).
 * Upgrades are stored as an int[] in NBT; shift-right-click cycles modes.
 *
 * <p>1.16 → 1.20 changes: ActionResult → InteractionResultHolder, World → Level.</p>
 */
public class ItemResonator extends ItemAS {

    private static final String TAG_UPGRADES = "upgrades";
    private static final String TAG_SELECTED = "selected_upgrade";
    private static final Random RAND = new Random();

    public ItemResonator() {
        super(new Properties().stacksTo(1));
    }

    // -------------------------------------------------------------------------
    // Upgrade storage helpers
    // -------------------------------------------------------------------------

    /** Returns true if the given upgrade is stored in the stack's NBT. */
    public static boolean hasUpgrade(@Nonnull ItemStack stack, @Nonnull ResonatorUpgrade upgrade) {
        if (upgrade == ResonatorUpgrade.STARLIGHT) return true; // always available
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_UPGRADES)) return false;
        int[] stored = tag.getIntArray(TAG_UPGRADES);
        for (int v : stored) {
            if (v == upgrade.ordinal()) return true;
        }
        return false;
    }

    /** Adds the upgrade to the stack and sets it as the selected mode. */
    public static void applyUpgrade(@Nonnull ItemStack stack, @Nonnull ResonatorUpgrade upgrade) {
        CompoundTag tag = stack.getOrCreateTag();
        int[] existing = tag.contains(TAG_UPGRADES) ? tag.getIntArray(TAG_UPGRADES) : new int[0];
        for (int v : existing) {
            if (v == upgrade.ordinal()) {
                tag.putInt(TAG_SELECTED, upgrade.ordinal());
                return;
            }
        }
        int[] updated = Arrays.copyOf(existing, existing.length + 1);
        updated[updated.length - 1] = upgrade.ordinal();
        tag.putIntArray(TAG_UPGRADES, updated);
        tag.putInt(TAG_SELECTED, upgrade.ordinal());
    }

    /** Returns the currently selected upgrade, defaulting to STARLIGHT. */
    @Nonnull
    public static ResonatorUpgrade getSelectedUpgrade(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SELECTED)) return ResonatorUpgrade.STARLIGHT;
        int idx = tag.getInt(TAG_SELECTED);
        ResonatorUpgrade[] values = ResonatorUpgrade.values();
        return (idx >= 0 && idx < values.length) ? values[idx] : ResonatorUpgrade.STARLIGHT;
    }

    /**
     * Cycles to the next unlocked upgrade. Wraps around. Called on shift-right-click.
     * Returns true if the mode changed.
     */
    public static boolean cycleUpgrade(@Nonnull ItemStack stack) {
        ResonatorUpgrade current = getSelectedUpgrade(stack);
        ResonatorUpgrade[] values = ResonatorUpgrade.values();
        for (int i = 1; i <= values.length; i++) {
            ResonatorUpgrade next = values[(current.ordinal() + i) % values.length];
            if (hasUpgrade(stack, next)) {
                stack.getOrCreateTag().putInt(TAG_SELECTED, next.ordinal());
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Item behaviour
    // -------------------------------------------------------------------------

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            cycleUpgrade(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // -------------------------------------------------------------------------
    // Inventory tick — visualization per held upgrade
    // -------------------------------------------------------------------------

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level,
                              @Nonnull Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player)) return;
        switch (getSelectedUpgrade(stack)) {
            case STARLIGHT    -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                                    () -> () -> tickStarlightClient(level, player));
            case FLUID_FIELDS -> tickFluidFieldsServer(level, player);
            case AREA_SIZE    -> {} // no AOI interface in this port
        }
    }

    // Blue starfield color matching 1.16 ColorsAS.RESONATOR_STARFIELD (~0x607AFE)
    private static final Vector3f RESONATOR_BLUE = new Vector3f(0.376f, 0.478f, 0.996f);

    @OnlyIn(Dist.CLIENT)
    private static void tickStarlightClient(@Nonnull Level level, @Nonnull Player player) {
        float nightProgress = DayTimeHelper.getNightProgress(level);
        if (nightProgress <= 0f) return;

        ResourceKey<Level> dim = level.dimension();
        BlockPos origin = player.blockPosition();

        for (int i = 0; i < 24; i++) {
            int dx = RAND.nextInt(193) - 96;
            int dz = RAND.nextInt(193) - 96;
            BlockPos samplePos = origin.offset(dx, 0, dz);

            SkyCollectionHelper.getSkyNoiseDistributionClient(dim, samplePos).ifPresent(noise -> {
                float fPerc = (float) Math.pow(Math.max(0f, (noise - 0.4f) * 1.65f), 2);
                if (noise >= 0.4f && RAND.nextFloat() <= fPerc) {
                    if (RAND.nextInt(6) == 0) {
                        int dy = RAND.nextInt(64) - 32;
                        Vec3 pos = Vec3.atCenterOf(samplePos.offset(0, dy, 0));

                        // Large blue dust particle — the "fog" visual
                        float size = 1.5f + fPerc * 3.0f;
                        level.addParticle(new DustParticleOptions(RESONATOR_BLUE, size),
                                pos.x + RAND.nextFloat(),
                                pos.y + 0.15,
                                pos.z + RAND.nextFloat(),
                                0, 0.001 * nightProgress, 0);

                        // Tiny white rising sparkle at very high noise
                        if (noise >= 0.8f && RAND.nextInt(3) == 0) {
                            level.addParticle(ParticleTypes.END_ROD,
                                    pos.x, pos.y, pos.z,
                                    (RAND.nextDouble() - 0.5) * 0.01,
                                    RAND.nextDouble() * 0.005 * nightProgress,
                                    (RAND.nextDouble() - 0.5) * 0.01);
                        }
                    }
                }
            });
        }
    }

    private static void tickFluidFieldsServer(@Nonnull Level level, @Nonnull Player player) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
        if (level.getGameTime() % 4 != 0) return;

        BlockPos origin = player.blockPosition();
        for (int i = 0; i < 8; i++) {
            int dx = RAND.nextInt(151) - 75;
            int dz = RAND.nextInt(151) - 75;
            BlockPos checkPos = origin.offset(dx, 0, dz);
            if (!level.isLoaded(checkPos)) continue;

            level.getChunkAt(checkPos)
                    .getCapability(ChunkFluidCapabilityProvider.CAPABILITY)
                    .ifPresent(entry -> {
                        if (!entry.isEmpty()) {
                            PacketChannel.sendToAllTracking(
                                    new PktParticleEvent(PktParticleEvent.LIQUID_FOUNTAIN, checkPos),
                                    serverLevel, checkPos);
                        }
                    });
        }
    }

    // -------------------------------------------------------------------------
    // Upgrade enum
    // -------------------------------------------------------------------------

    public enum ResonatorUpgrade {
        /** Base mode: shows starlight source locations and intensity. */
        STARLIGHT("starlight"),
        /** Domic upgrade: visualizes liquid starlight field effects. */
        FLUID_FIELDS("liquid"),
        /** Ichosic upgrade: previews area-of-influence for multiblock machines. */
        AREA_SIZE("structure");

        public final String appendix;

        ResonatorUpgrade(String appendix) {
            this.appendix = appendix;
        }
    }
}
