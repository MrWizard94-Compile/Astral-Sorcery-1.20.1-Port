package hellfirepvp.astralsorcery.common.auxiliary.link;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the linking state for players using linking tools.
 * Tracks which tile entity was selected first (the "source") so the
 * second click can complete the link between source and target.
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, PlayerEntity -> Player,
 * TileEntity -> BlockEntity, BlockRayTraceResult -> BlockHitResult,
 * player.isSneaking() -> player.isShiftKeyDown(),
 * ITextComponent -> Component</p>
 */
public class LinkHandler {

    private static final Map<UUID, LinkSession> activeSessions = new HashMap<>();

    private LinkHandler() {}

    /**
     * Handle a right-click interaction from a linking tool.
     *
     * @param level  the level
     * @param player the player
     * @param hand   the hand used
     * @param stack  the item stack (must be a linking tool)
     * @param hit    the block hit result
     * @return the interaction result
     */
    @Nonnull
    public static InteractionResult handleInteraction(@Nonnull Level level,
                                                      @Nonnull Player player,
                                                      @Nonnull InteractionHand hand,
                                                      @Nonnull ItemStack stack,
                                                      @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof LinkableTileEntity linkable)) {
            return InteractionResult.PASS;
        }

        UUID playerId = player.getUUID();

        // Sneak-click to unlink
        if (player.isShiftKeyDown()) {
            clearSession(playerId);
            return InteractionResult.SUCCESS;
        }

        LinkSession session = activeSessions.get(playerId);

        if (session == null) {
            // First click — select source
            if (!linkable.onSelect(player)) {
                return InteractionResult.FAIL;
            }
            activeSessions.put(playerId, new LinkSession(level.dimension(), pos));
            return InteractionResult.SUCCESS;
        }

        // Second click — attempt link
        if (!session.dimension().equals(level.dimension())) {
            clearSession(playerId);
            return InteractionResult.FAIL;
        }

        BlockPos sourcePos = session.sourcePos();
        BlockEntity sourceBe = level.getBlockEntity(sourcePos);

        if (!(sourceBe instanceof LinkableTileEntity sourceLinkable)) {
            clearSession(playerId);
            return InteractionResult.FAIL;
        }

        if (sourcePos.equals(pos)) {
            // Clicking same block — deselect
            clearSession(playerId);
            return InteractionResult.SUCCESS;
        }

        // Attempt bidirectional link
        boolean sourceLinked = sourceLinkable.tryLinkBlock(player, pos);
        boolean targetLinked = linkable.tryLinkBlock(player, sourcePos);

        if (sourceLinked) {
            sourceLinkable.onBlockLinkCreate(player, pos);
        }
        if (targetLinked) {
            linkable.onBlockLinkCreate(player, sourcePos);
        }

        clearSession(playerId);
        return (sourceLinked || targetLinked) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Handle a right-click interaction on an entity from a linking tool.
     *
     * @param level  the level
     * @param player the player
     * @param entity the targeted entity
     * @return the interaction result
     */
    @Nonnull
    public static InteractionResult handleEntityInteraction(@Nonnull Level level,
                                                            @Nonnull Player player,
                                                            @Nonnull Entity entity) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        UUID playerId = player.getUUID();
        LinkSession session = activeSessions.get(playerId);

        if (session == null) {
            return InteractionResult.PASS;
        }

        if (!session.dimension().equals(level.dimension())) {
            clearSession(playerId);
            return InteractionResult.FAIL;
        }

        BlockPos sourcePos = session.sourcePos();
        BlockEntity sourceBe = level.getBlockEntity(sourcePos);

        if (!(sourceBe instanceof LinkableTileEntity sourceLinkable)) {
            clearSession(playerId);
            return InteractionResult.FAIL;
        }

        boolean linked = sourceLinkable.tryLinkEntity(player, entity);
        if (linked) {
            sourceLinkable.onEntityLinkCreate(player, entity);
        }

        clearSession(playerId);
        return linked ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Get the currently selected source position for a player, if any.
     *
     * @param player the player
     * @return the session, or null if no link is in progress
     */
    @Nullable
    public static LinkSession getSession(@Nonnull Player player) {
        return activeSessions.get(player.getUUID());
    }

    /**
     * Clear any active linking session for the given player.
     *
     * @param playerId the player's UUID
     */
    public static void clearSession(@Nonnull UUID playerId) {
        activeSessions.remove(playerId);
    }

    /**
     * Clear all sessions (e.g., on server stop).
     */
    public static void clearAll() {
        activeSessions.clear();
    }

    /**
     * A linking session holds the dimension and position of the first-selected tile.
     */
    public record LinkSession(@Nonnull net.minecraft.resources.ResourceKey<Level> dimension,
                              @Nonnull BlockPos sourcePos) {
    }
}
