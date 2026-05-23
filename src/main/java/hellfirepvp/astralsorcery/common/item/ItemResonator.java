package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Resonator — reveals starlight sources and shows their area of influence.
 * When held, visualizes where starlight flows in the world.
 *
 * <p>Full visualization logic (particle effects, area-of-influence preview)
 * is deferred to Phase 12 (client rendering).</p>
 *
 * <p>1.16 -> 1.20 changes: ActionResult -> InteractionResultHolder,
 * World -> Level, PlayerEntity -> Player.</p>
 */
public class ItemResonator extends ItemAS {

    public ItemResonator() {
        super(new Properties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        // TODO Phase 12: show area-of-influence preview + starlight visualization
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
