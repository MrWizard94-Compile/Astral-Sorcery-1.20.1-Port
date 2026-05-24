package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.client.screen.ClientScreenHandler;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Tome of Knowledge — the Astral Sorcery journal that tracks discoveries,
 * constellations, and research progress. Opened by right-clicking.
 *
 * <p>1.16 -> 1.20 changes: ActionResult -> InteractionResultHolder,
 * World -> Level, PlayerEntity -> Player.</p>
 */
public class ItemTome extends ItemAS {

    public ItemTome() {
        super(new Properties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        if (level.isClientSide()) {
            openJournalClient();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @OnlyIn(Dist.CLIENT)
    private static void openJournalClient() {
        ClientScreenHandler.openJournalScreen();
    }
}
