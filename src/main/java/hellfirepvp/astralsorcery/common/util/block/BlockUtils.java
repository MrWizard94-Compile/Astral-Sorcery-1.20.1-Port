package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.tile.TileUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Block breaking, loot dropping, and block state utilities.
 *
 * <p>1.16 → 1.20 changes:
 * ServerWorld → ServerLevel, ServerPlayerEntity → ServerPlayer,
 * World → Level, IBlockReader → BlockGetter,
 * TileEntity → BlockEntity,
 * LootContext.Builder → LootParams.Builder, LootParameters → LootContextParams,
 * Vector3d.copyCentered → Vec3.atCenterOf,
 * Material removed (blocksMovement → blocksMotion on BlockState),
 * ToolType removed (harvest level → tag-based tool matching via isCorrectToolForDrops),
 * isAirBlock → isEmptyBlock, isReplaceable → canBeReplaced,
 * BlockItemUseContext → BlockPlaceContext, Hand → InteractionHand,
 * Effects → MobEffects, EffectUtils → direct effect checks,
 * interactionManager → gameMode, tryHarvestBlock → destroyBlock,
 * removedByPlayer → onDestroyedByPlayer,
 * onBlockHarvested → playerWillDestroy, onPlayerDestroy → destroy,
 * harvestBlock → playerDestroy, dropXpOnBlockBreak → popExperience,
 * onBlockDestroyed → mineBlock, canPlayerBreakBlockWhileHolding → canAttackBlock,
 * areEyesInFluid → isEyeInFluid, isOnGround → onGround,
 * getBlockHardness → getDestroySpeed, getDefaultState → defaultBlockState,
 * setBlockState → setBlock, getRegistryName → ForgeRegistries lookup,
 * withNullableParameter → withOptionalParameter,
 * withRandom removed (random is level-internal in 1.20),
 * World.isOutsideBuildHeight(static) → level.isOutsideBuildHeight(instance),
 * up → above, down → below</p>
 */
public class BlockUtils {

    // ---- Position helpers ----

    @Nonnull
    public static BlockPos getWorldTopPos(@Nonnull Level level, @Nonnull BlockPos at) {
        BlockPos it = at;
        while (!level.isOutsideBuildHeight(it)) {
            it = it.above();
        }
        return it;
    }

    @Nonnull
    public static BlockPos firstSolidDown(@Nonnull BlockGetter level, @Nonnull BlockPos at) {
        BlockState state = level.getBlockState(at);
        while (at.getY() > level.getMinBuildHeight()
                && state.getCollisionShape(level, at).isEmpty() && state.getFluidState().isEmpty()) {
            at = at.below();
            state = level.getBlockState(at);
        }
        return at;
    }

    // ---- Replaceability ----

    public static boolean isReplaceable(@Nonnull Level level, @Nonnull BlockPos pos) {
        return isReplaceable(level, pos, level.getBlockState(pos));
    }

    public static boolean isReplaceable(@Nonnull Level level, @Nonnull BlockPos pos,
                                        @Nonnull BlockState state) {
        if (level.isEmptyBlock(pos)) {
            return true;
        }
        BlockPlaceContext ctx = TestBlockUseContext.getHandContext(
                level, null, InteractionHand.MAIN_HAND, pos, Direction.UP);
        return state.canBeReplaced(ctx);
    }

    // ---- Break speed ----

    /**
     * Compute break speed for a living entity, similar to Player.getDestroySpeed
     * but without firing an event and not position-based.
     */
    public static float getSimpleBreakSpeed(@Nonnull LivingEntity entity,
                                            @Nonnull ItemStack tool,
                                            @Nonnull BlockState state) {
        float breakSpeed = tool.getDestroySpeed(state);
        if (breakSpeed > 1.0F) {
            float efficiencyLevel = EnchantmentHelper.getBlockEfficiency(entity);
            if (efficiencyLevel > 0 && !tool.isEmpty()) {
                breakSpeed += efficiencyLevel * efficiencyLevel + 1;
            }
        }

        if (entity.hasEffect(MobEffects.DIG_SPEED)) {
            MobEffectInstance digSpeed = entity.getEffect(MobEffects.DIG_SPEED);
            if (digSpeed != null) {
                breakSpeed *= 1.0F + (digSpeed.getAmplifier() + 1F) * 0.2F;
            }
        }

        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            MobEffectInstance digSlow = entity.getEffect(MobEffects.DIG_SLOWDOWN);
            if (digSlow != null) {
                int amplifier = digSlow.getAmplifier();
                float fatigueMultiplier = switch (amplifier) {
                    case 0 -> (float) Math.pow(0.3F, 1);
                    case 1 -> (float) Math.pow(0.3F, 2);
                    case 2 -> (float) Math.pow(0.3F, 3);
                    default -> (float) Math.pow(0.3F, 4);
                };
                breakSpeed *= fatigueMultiplier;
            }
        }

        if (entity.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(entity)) {
            breakSpeed /= 5.0F;
        }

        if (!entity.onGround()) {
            breakSpeed /= 5.0F;
        }
        return breakSpeed;
    }

    // ---- Fluid checks ----

    public static boolean isFluidBlock(@Nonnull Level level, @Nonnull BlockPos pos) {
        return isFluidBlock(level.getBlockState(pos));
    }

    public static boolean isFluidBlock(@Nonnull BlockState state) {
        return state == state.getFluidState().createLegacyBlock();
    }

    // ---- State matching ----

    @Nullable
    public static BlockState getMatchingState(@Nonnull Collection<BlockState> applicableStates,
                                              @Nullable BlockState test) {
        for (BlockState state : applicableStates) {
            if (matchStateExact(state, test)) {
                return state;
            }
        }
        return null;
    }

    public static boolean matchStateExact(@Nullable BlockState state,
                                          @Nullable BlockState stateToTest) {
        if (state == null) {
            return stateToTest == null;
        } else if (stateToTest == null) {
            return false;
        }

        // In 1.20, same registry entry = same Block object; identity check suffices
        if (state.getBlock() != stateToTest.getBlock()) {
            return false;
        }

        for (Property<?> prop : state.getProperties()) {
            Comparable<?> original = state.getValue(prop);
            try {
                Comparable<?> test = stateToTest.getValue(prop);
                if (!original.equals(test)) {
                    return false;
                }
            } catch (Exception exc) {
                return false;
            }
        }
        return true;
    }

    // ---- Tool / harvest checks ----

    /**
     * Check if a tool can break a block without a real player context.
     * In 1.20, the ToolType/harvest-level system was removed in favor of
     * tag-based tool matching via {@link ItemStack#isCorrectToolForDrops(BlockState)}.
     */
    public static boolean canToolBreakBlockWithoutPlayer(@Nonnull Level level,
                                                        @Nonnull BlockPos pos,
                                                        @Nonnull BlockState state,
                                                        @Nonnull ItemStack stack) {
        if (state.getDestroySpeed(level, pos) == -1) {
            return false; // Unbreakable (bedrock, etc.)
        }
        if (!state.requiresCorrectToolForDrops()) {
            return true; // Any tool works
        }
        return stack.isCorrectToolForDrops(state);
    }

    // ---- Block breaking ----

    public static boolean breakBlockWithPlayer(@Nonnull BlockPos pos,
                                               @Nonnull ServerPlayer player) {
        return player.gameMode.destroyBlock(pos);
    }

    /**
     * Break a block without a real player, using a FakePlayer.
     * Emulates ForgeHooks.onBlockBreak and ServerPlayerGameMode.destroyBlock
     * but without an active player.
     */
    public static boolean breakBlockWithoutPlayer(@Nonnull ServerLevel level,
                                                  @Nonnull BlockPos pos) {
        return breakBlockWithoutPlayer(level, pos, level.getBlockState(pos),
                ItemStack.EMPTY, true, false);
    }

    public static boolean breakBlockWithoutPlayer(@Nonnull ServerLevel level,
                                                  @Nonnull BlockPos pos,
                                                  @Nonnull BlockState stateBroken,
                                                  @Nonnull ItemStack heldItem,
                                                  boolean breakBlock,
                                                  boolean ignoreHarvestRestrictions) {
        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(level);
        int xp;
        try {
            boolean preCancelEvent = false;
            if (!heldItem.isEmpty()
                    && !heldItem.getItem().canAttackBlock(stateBroken, level, pos, fakePlayer)) {
                preCancelEvent = true;
            }
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, stateBroken, fakePlayer);
            event.setCanceled(preCancelEvent);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                return false;
            }
            xp = event.getExpToDrop();
        } catch (Exception exc) {
            return false;
        }
        if (xp == -1) {
            return false;
        }

        if (heldItem.getItem().onBlockStartBreak(heldItem, pos, fakePlayer)) {
            return false;
        }

        boolean harvestable = true;
        try {
            if (!ignoreHarvestRestrictions) {
                harvestable = fakePlayer.hasCorrectToolForDrops(stateBroken);
            }
        } catch (Exception exc) {
            return false;
        }

        ItemStack heldCopy = heldItem.isEmpty() ? ItemStack.EMPTY : heldItem.copy();
        try {
            heldCopy.mineBlock(level, stateBroken, pos, fakePlayer);
        } catch (Exception exc) {
            return false;
        }

        boolean wasCapturingStates = level.captureBlockSnapshots;
        List<BlockSnapshot> previousCapturedStates = new ArrayList<>(level.capturedBlockSnapshots);

        level.captureBlockSnapshots = true;
        try {
            if (breakBlock) {
                if (!stateBroken.onDestroyedByPlayer(level, pos, fakePlayer,
                        harvestable, Fluids.EMPTY.defaultFluidState())) {
                    restoreWorldState(level, wasCapturingStates, previousCapturedStates);
                    return false;
                }
            } else {
                stateBroken.getBlock().playerWillDestroy(level, pos, stateBroken, fakePlayer);
            }
        } catch (Exception exc) {
            restoreWorldState(level, wasCapturingStates, previousCapturedStates);
            return false;
        }

        stateBroken.getBlock().destroy(level, pos, stateBroken);

        if (harvestable) {
            try {
                BlockEntity blockEntity = TileUtils.getTileAt(level, pos, BlockEntity.class, true);
                ItemStack harvestStack = heldCopy.isEmpty() ? ItemStack.EMPTY : heldCopy.copy();
                stateBroken.getBlock().playerDestroy(level, fakePlayer, pos,
                        stateBroken, blockEntity, harvestStack);
            } catch (Exception exc) {
                restoreWorldState(level, wasCapturingStates, previousCapturedStates);
                return false;
            }
        }

        if (xp > 0) {
            stateBroken.getBlock().popExperience(level, pos, xp);
        }
        BlockDropCaptureAssist.startCapturing();
        try {
            // Replay captured block snapshots then replace with air
            level.captureBlockSnapshots = false;
            level.restoringBlockSnapshots = true;
            level.capturedBlockSnapshots.forEach(s -> s.restore(true));
            level.restoringBlockSnapshots = false;
            level.capturedBlockSnapshots.forEach(
                    s -> level.setBlock(s.getPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
        } finally {
            BlockDropCaptureAssist.getCapturedStacksAndStop(); // Discard captured drops

            // Restore previous snapshot state
            level.capturedBlockSnapshots.clear();
            level.captureBlockSnapshots = wasCapturingStates;
            level.capturedBlockSnapshots.addAll(previousCapturedStates);
        }
        return true;
    }

    private static void restoreWorldState(@Nonnull Level level, boolean prevCaptureFlag,
                                          @Nonnull List<BlockSnapshot> prevSnapshots) {
        level.captureBlockSnapshots = false;

        level.restoringBlockSnapshots = true;
        level.capturedBlockSnapshots.forEach(s -> s.restore(true));
        level.restoringBlockSnapshots = false;

        level.capturedBlockSnapshots.clear();

        level.captureBlockSnapshots = prevCaptureFlag;
        level.capturedBlockSnapshots.addAll(prevSnapshots);
    }
}
