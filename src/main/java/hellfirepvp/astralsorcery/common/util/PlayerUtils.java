package hellfirepvp.astralsorcery.common.util;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Player and entity interaction utilities: attack/placement permission checks,
 * hand lookup, connection state, and cross-dimension teleport.
 */
public class PlayerUtils {

    public static boolean canPlayerAttackServer(@Nullable LivingEntity source,
                                                @Nonnull LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player plTarget) {
            if (target.level() instanceof ServerLevel serverLevel) {
                MinecraftServer srv = serverLevel.getServer();
                if (srv != null && !srv.isPvpAllowed()) {
                    return false;
                }
            }
            if (plTarget.isSpectator() || plTarget.isCreative()) {
                return false;
            }
            if (source instanceof Player sourcePlayer
                    && !sourcePlayer.canHarmPlayer(plTarget)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlayerBreakBlockPos(@Nonnull Player player,
                                                 @Nonnull BlockPos tryBreak) {
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent(
                player.level(), tryBreak,
                player.level().getBlockState(tryBreak), player);
        MinecraftForge.EVENT_BUS.post(ev);
        return !ev.isCanceled();
    }

    public static boolean canPlayerPlaceBlockPos(@Nonnull Player player,
                                                 @Nonnull BlockState tryPlace,
                                                 @Nonnull BlockPos pos,
                                                 @Nonnull Direction againstSide) {
        Level level = player.level();
        level.captureBlockSnapshots = true;
        level.setBlock(pos, tryPlace, 3);
        level.captureBlockSnapshots = false;

        @SuppressWarnings("unchecked")
        List<BlockSnapshot> blockSnapshots =
                (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();

        boolean cancelPlacement = false;
        if (blockSnapshots.size() > 1) {
            cancelPlacement = ForgeEventFactory.onMultiBlockPlace(player, blockSnapshots, againstSide);
        } else if (blockSnapshots.size() == 1) {
            cancelPlacement = ForgeEventFactory.onBlockPlace(player, blockSnapshots.get(0), againstSide);
        }
        for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots)) {
            level.restoringBlockSnapshots = true;
            blocksnapshot.restore(true, false);
            level.restoringBlockSnapshots = false;
        }
        return !cancelPlacement;
    }

    public static boolean isConnectionEstablished(@Nonnull ServerPlayer player) {
        return player.connection != null
                && player.connection.connection != null
                && player.connection.connection.isConnected();
    }

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(
            @Nonnull LivingEntity entity, @Nonnull Item search) {
        return getMainOrOffHand(entity,
                stack -> !stack.isEmpty() && stack.getItem().equals(search));
    }

    @Nullable
    public static Tuple<InteractionHand, ItemStack> getMainOrOffHand(
            @Nonnull LivingEntity entity, @Nonnull Predicate<ItemStack> acceptorFnc) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack held = entity.getItemInHand(hand);
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            hand = InteractionHand.OFF_HAND;
            held = entity.getItemInHand(hand);
        }
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            return null;
        }
        return new Tuple<>(hand, held);
    }

    /**
     * Teleports any entity to the given position in the target level.
     * Players use the 1.20 {@code ServerPlayer.teleportTo} API; other entities
     * use {@code changeDimension} with a {@link NoOpTeleporter}.
     */
    public static void transferEntityTo(@Nonnull Entity entity,
                                        @Nonnull ServerLevel targetLevel,
                                        @Nonnull BlockPos target) {
        if (entity instanceof ServerPlayer sp) {
            sp.teleportTo(targetLevel,
                    target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                    sp.getYRot(), sp.getXRot());
        } else {
            entity.changeDimension(targetLevel, new NoOpTeleporter(targetLevel, target));
        }
    }
}
