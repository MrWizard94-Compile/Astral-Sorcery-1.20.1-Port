/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;

/**
 * Miscellaneous Forge event handlers.
 *
 * <p>Handles: lectern + ItemTome → open journal; crystal item toss owner
 * tracking; block change dispatch to {@link BlockChangeNotifier} for the
 * starlight network auto-link system.</p>
 *
 * <p>1.16 → 1.20: EntityJoinWorldEvent → EntityJoinLevelEvent;
 * TileEntity → BlockEntity; PlayerSleepInBedEvent dropped (minor feature);
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
        if (event.getLevel().isClientSide()) return;
        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof LecternBlockEntity lectern)) return;
        ItemStack contained = lectern.getBook();
        if (contained.getItem() instanceof ItemTome) {
            event.setCanceled(true);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> hellfirepvp.astralsorcery.client.screen.ClientScreenHandler::openJournalScreen);
        }
    }

    /**
     * Dispatch block change notifications to {@link BlockChangeNotifier} when
     * blocks are placed. Used by the starlight network to detect new altars and
     * crafting tables for auto-linking.
     */
    @SubscribeEvent
    public static void onBlockPlace(@Nonnull BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;
        LevelChunk chunk = level.getChunkAt(event.getPos());
        BlockChangeNotifier.notifyChange(level, chunk, event.getPos(),
                event.getPlacedAgainst(), event.getPlacedBlock());
    }

    /**
     * Dispatch block change notifications when blocks are broken.
     */
    @SubscribeEvent
    public static void onBlockBreak(@Nonnull BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;
        LevelChunk chunk = level.getChunkAt(event.getPos());
        BlockChangeNotifier.notifyChange(level, chunk, event.getPos(),
                event.getState(), level.getBlockState(event.getPos()));
    }
}
