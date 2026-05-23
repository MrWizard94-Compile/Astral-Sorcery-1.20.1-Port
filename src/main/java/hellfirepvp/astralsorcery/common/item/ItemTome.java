package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Tome of Knowledge — the Astral Sorcery journal that tracks discoveries,
 * constellations, and research progress. Opened by right-clicking.
 *
 * <p>The full journal GUI is Phase 13 work (screen system). This stub
 * registers the item and makes it non-stackable so it can be held.</p>
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
        // TODO Phase 13: open journal GUI (ContainerTomeProvider / TomeScreen)
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
