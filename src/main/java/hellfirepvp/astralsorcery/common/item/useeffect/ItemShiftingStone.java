package hellfirepvp.astralsorcery.common.item.useeffect;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Shifting Stone — cycles between perk tree configurations when used.
 * Allows the player to switch between saved perk tree allocations.
 *
 * <p>1.16 -> 1.20 changes:
 * onItemRightClick -> use,
 * ActionResult -> InteractionResultHolder,
 * PlayerEntity -> Player,
 * World -> Level,
 * Hand -> InteractionHand</p>
 */
public class ItemShiftingStone extends ItemAS {

    public ItemShiftingStone() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level,
                                                  @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            // TODO: Cycle perk tree configuration
            // PerkTreeManager.cycleConfiguration(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
