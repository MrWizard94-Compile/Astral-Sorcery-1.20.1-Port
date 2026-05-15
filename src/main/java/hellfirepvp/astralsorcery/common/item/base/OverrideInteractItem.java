package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;

/**
 * Marker interface for items that override default block/entity interaction.
 * Items implementing this have priority in right-click handling — the mod's
 * event handler intercepts the interaction before vanilla/block processing.
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, PlayerEntity -> Player,
 * Hand -> InteractionHand, ActionResultType -> InteractionResult,
 * BlockRayTraceResult -> BlockHitResult</p>
 */
public interface OverrideInteractItem {

    /**
     * Called when the player right-clicks while holding this item.
     * Return {@link InteractionResult#SUCCESS} or {@link InteractionResult#CONSUME}
     * to prevent further processing.
     *
     * @param level  the level
     * @param player the player
     * @param hand   the hand used
     * @param stack  the held item stack
     * @param hit    the block hit result, or null if no block targeted
     * @return the interaction result
     */
    @Nonnull
    InteractionResult doInteract(@Nonnull Level level,
                                 @Nonnull Player player,
                                 @Nonnull InteractionHand hand,
                                 @Nonnull ItemStack stack,
                                 @Nonnull BlockHitResult hit);

    /**
     * Whether this item should override interaction even when sneaking.
     */
    default boolean shouldInteractWhenSneaking() {
        return false;
    }
}
