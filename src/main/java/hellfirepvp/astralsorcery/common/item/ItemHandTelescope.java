package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Hand Telescope — a portable telescope for constellation discovery.
 * Right-clicking opens the constellation discovery GUI, allowing the player
 * to observe and discover constellations without a placed telescope block.
 *
 * <p>1.16 -> 1.20 changes:
 * onItemRightClick -> use,
 * ActionResult -> InteractionResultHolder,
 * PlayerEntity -> Player,
 * World -> Level,
 * Hand -> InteractionHand</p>
 */
public class ItemHandTelescope extends ItemAS {

    public ItemHandTelescope() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level,
                                                  @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            // TODO: Open constellation discovery GUI
            // ClientScreenHandler.openHandTelescopeScreen();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
