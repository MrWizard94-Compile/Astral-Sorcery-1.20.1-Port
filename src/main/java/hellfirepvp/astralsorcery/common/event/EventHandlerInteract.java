/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Intercepts player block interactions for items implementing
 * {@link OverrideInteractItem}. Calls {@link OverrideInteractItem#doInteract}
 * before vanilla block processing; cancels the event if consumed.
 *
 * <p>1.16 → 1.20: BlockRayTraceResult → BlockHitResult;
 * getHitVec() now returns BlockHitResult directly on the event;
 * ActionResultType → InteractionResult; setRemote check → isClientSide().</p>
 */
public final class EventHandlerInteract {

    private EventHandlerInteract() {}

    /**
     * Forward right-click-block events to OverrideInteractItem implementations.
     * Registered on the Forge event bus from CommonProxy.
     */
    @SubscribeEvent
    public static void onBlockInteract(@Nonnull PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof OverrideInteractItem item)) return;
        if (event.getEntity().isCrouching() && !item.shouldInteractWhenSneaking()) return;

        BlockHitResult hit = event.getHitVec();
        InteractionResult result = item.doInteract(
                event.getLevel(),
                event.getEntity(),
                event.getHand(),
                held,
                hit);

        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
