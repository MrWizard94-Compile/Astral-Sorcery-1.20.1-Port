package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.link.IItemLinkingTool;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.block.tile.BlockCollectorCrystal;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Resonating Wand — the mod's primary interaction tool.
 *
 * <p>Three functions:
 * <ol>
 *   <li>Right-click a crafting table beneath a shrine's Collector Crystal to
 *       convert it into the Discovery Altar (first altar tier).</li>
 *   <li>Links tile entities via {@link LinkHandler} (IItemLinkingTool interface,
 *       handled server-side by EventHandlerInteract).</li>
 *   <li>At night, reveals underground Rock Crystal ore deposits by spawning
 *       white END_ROD particles above the surface above each vein.</li>
 * </ol>
 * </p>
 *
 * <p>1.16 → 1.20: ActionResultType → InteractionResult; World → Level;
 * useOn() replaces the old right-click handler; inventoryTick added.</p>
 */
public class ItemWand extends ItemAS implements IItemLinkingTool {

    private static final Random RAND = new Random();

    /** Vertical range to scan below player for rock crystal ore. */
    private static final int ORE_SCAN_DEPTH = 72;
    /** Horizontal positions sampled per scan cycle. */
    private static final int ORE_SCAN_SAMPLES = 10;
    /** ±horizontal radius for ore sampling. */
    private static final int ORE_SCAN_RADIUS = 16;

    public ItemWand() {
        super(defaultProperties().stacksTo(1));
    }

    // =========================================================================
    // Block interaction — useOn() runs after PlayerInteractEvent.RightClickBlock
    // Client: return sidedSuccess to suppress block GUIs and trigger server packet.
    // Server: linking is attempted first via LinkHandler; if the clicked block is not
    //         linkable, falls through to Discovery Altar conversion (crafting table).
    // =========================================================================

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext ctx) {
        Level level = ctx.getLevel();

        // Client side: swing arm and prevent block GUI (crafting tables etc.)
        if (level.isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        // Server side
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        // Linking takes priority: if the clicked block is a LinkableTileEntity,
        // LinkHandler manages the two-click link sequence directly.
        BlockHitResult hit = new BlockHitResult(
                ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), false);
        InteractionResult linkResult = LinkHandler.handleInteraction(
                level, player, ctx.getHand(), ctx.getItemInHand(), hit);
        if (linkResult.consumesAction()) {
            return linkResult;
        }

        // Discovery Altar conversion: scan for a nearby collector crystal and, if
        // found, replace the crafting table with a Discovery Altar.
        BlockPos pos = ctx.getClickedPos();
        if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
            return tryConvertToAltar(level, player, pos);
        }

        return InteractionResult.FAIL;
    }

    /**
     * Looks for a {@link BlockCollectorCrystal} within 3-block horizontal and
     * 8-block vertical range above {@code craftingTablePos}. If found, replaces
     * the crafting table with the Tier-1 Discovery Altar block.
     */
    @Nonnull
    private static InteractionResult tryConvertToAltar(@Nonnull Level level,
                                                        @Nullable Player player,
                                                        @Nonnull BlockPos craftingTablePos) {
        boolean foundCrystal = false;
        outer:
        for (int dy = 0; dy <= 8; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (level.getBlockState(craftingTablePos.offset(dx, dy, dz))
                            .getBlock() instanceof BlockCollectorCrystal) {
                        foundCrystal = true;
                        break outer;
                    }
                }
            }
        }

        if (!foundCrystal) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("astralsorcery.wand.no_crystal_nearby"), true);
            }
            return InteractionResult.FAIL;
        }

        // Capture the crafting-table state for the particle burst before replacing it.
        int oldBlockId = Block.getId(level.getBlockState(craftingTablePos));

        level.setBlock(craftingTablePos,
                BlocksAS.ALTAR.get().defaultBlockState()
                        .setValue(BlockAltar.ALTAR_TYPE, BlockAltar.AltarType.DISCOVERY),
                Block.UPDATE_ALL);

        // Block-break particle + sound burst (uses the crafting table's look)
        level.levelEvent(null, 2001, craftingTablePos, oldBlockId);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("astralsorcery.altar.discovery.created"), false);
        }

        return InteractionResult.SUCCESS;
    }

    // =========================================================================
    // OverrideInteractItem — called server-side by EventHandlerInteract BEFORE
    // Block.use() fires. Handles linking and Discovery Altar conversion here so
    // EventHandlerInteract can cancel the event and prevent crafting table GUIs.
    // Item.useOn() below is a fallback in case EventHandlerInteract is skipped.
    // =========================================================================

    @Override
    @Nonnull
    public InteractionResult doInteract(@Nonnull Level level,
                                        @Nonnull Player player,
                                        @Nonnull InteractionHand hand,
                                        @Nonnull ItemStack stack,
                                        @Nonnull BlockHitResult hit) {
        // Try linking first
        InteractionResult linkResult = LinkHandler.handleInteraction(level, player, hand, stack, hit);
        if (linkResult.consumesAction()) return linkResult;

        // Try shrine conversion
        BlockPos pos = hit.getBlockPos();
        if (level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
            return tryConvertToAltar(level, player, pos);
        }

        return InteractionResult.PASS;
    }

    // =========================================================================
    // Rock Crystal ore detection — client-side particle effect at night
    // =========================================================================

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level,
                               @Nonnull Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() || !(entity instanceof Player player)) return;
        // Throttle to once every 8 ticks to keep overhead low.
        if (level.getGameTime() % 8 != 0) return;
        tickRockCrystalScan(level, player);
    }

    @OnlyIn(Dist.CLIENT)
    private static void tickRockCrystalScan(@Nonnull Level level, @Nonnull Player player) {
        float nightProgress = DayTimeHelper.getNightProgress(level);
        if (nightProgress <= 0f) return;

        BlockPos playerPos = player.blockPosition();
        int playerY = playerPos.getY();

        for (int i = 0; i < ORE_SCAN_SAMPLES; i++) {
            int dx = RAND.nextInt(ORE_SCAN_RADIUS * 2 + 1) - ORE_SCAN_RADIUS;
            int dz = RAND.nextInt(ORE_SCAN_RADIUS * 2 + 1) - ORE_SCAN_RADIUS;
            int x = playerPos.getX() + dx;
            int z = playerPos.getZ() + dz;

            // Scan downward starting just below player feet
            int scanTop = playerY - 2;
            int scanBottom = Math.max(scanTop - ORE_SCAN_DEPTH, level.getMinBuildHeight());

            for (int y = scanTop; y >= scanBottom; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                if (!level.isLoaded(checkPos)) break;
                if (level.getBlockState(checkPos).is(BlocksAS.ROCK_CRYSTAL_ORE.get())) {
                    spawnOreParticle(level, x, z, nightProgress);
                    break;
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnOreParticle(@Nonnull Level level, int oreX, int oreZ, float nightProgress) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, oreX, oreZ);

        double px = oreX + 0.5 + (RAND.nextDouble() - 0.5);
        double py = surfaceY + RAND.nextDouble() * 2.5;
        double pz = oreZ + 0.5 + (RAND.nextDouble() - 0.5);

        level.addParticle(ParticleTypes.END_ROD,
                px, py, pz,
                (RAND.nextDouble() - 0.5) * 0.01,
                0.02 + RAND.nextDouble() * 0.03 * nightProgress,
                (RAND.nextDouble() - 0.5) * 0.01);
    }
}
