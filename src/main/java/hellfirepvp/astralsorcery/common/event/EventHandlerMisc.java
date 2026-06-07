/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;

/**
 * Miscellaneous Forge event handlers.
 *
 * <p>Handles: lectern + ItemTome â†’ open journal; crystal item toss owner tracking.</p>
 *
 * <p>1.16 â†’ 1.20: EntityJoinWorldEvent â†’ EntityJoinLevelEvent;
 * TileEntity â†’ BlockEntity; PlayerSleepInBedEvent dropped (minor feature);
 * AreaEffectCloud cancel dropped (EffectDropModifier no longer creates clouds);
 * ItemEntity.setThrower(Entity) stable; LecternBlockEntity.getBook() stable.</p>
 */
public final class EventHandlerMisc {

    private EventHandlerMisc() {}

    /**
     * Tag crystal item entities with the thrower's UUID so they can be
     * attributed to the correct player when picked up or processed.
     */
    @SubscribeEvent
    public static void onCrystalToss(@Nonnull ItemTossEvent event) {
        if (event.getPlayer().level().isClientSide()) return;
        ItemStack thrown = event.getEntity().getItem();
        if (thrown.getItem() instanceof ItemCrystalBase) {
            event.getEntity().setThrower(event.getPlayer().getUUID());
        }
    }

    /**
     * If a player right-clicks a lectern that contains an ItemTome (the Astral
     * Journal), open the journal screen instead of the vanilla book UI.
     */
    @SubscribeEvent
    public static void onLecternInteract(@Nonnull PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        net.minecraft.world.level.Level interactLevel = event.getLevel();
        BlockEntity be = interactLevel.getBlockEntity(pos);
        if (!(be instanceof LecternBlockEntity lectern)) return;
        if (!(lectern.getBook().getItem() instanceof ItemTome)) return;
        event.setCanceled(true);
        if (interactLevel.isClientSide()) {
            DistExecutor.safeRunWhenOn(Dist.CLIENT,
                    () -> hellfirepvp.astralsorcery.client.screen.ClientScreenHandler::openJournalScreen);
        }
    }

}
