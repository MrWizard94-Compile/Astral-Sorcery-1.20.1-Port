package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.entity.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Grapple Wand — launches a starlight grappling hook that pulls the player toward
 * the targeted block. Consumes alignment charge per use.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player, World → Level,
 * ActionResult → InteractionResultHolder, addEntity → addFreshEntity,
 * getCooldownTracker → getCooldowns.</p>
 */
public class ItemGrappleWand extends ItemAS implements AlignmentChargeConsumer {

    private static final float COST_PER_GRAPPLE = 450F;

    public ItemGrappleWand() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        return player.getCooldowns().isOnCooldown(this) ? 0F : COST_PER_GRAPPLE;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide() || held.isEmpty()) {
            return InteractionResultHolder.consume(held);
        }
        if (!player.getCooldowns().isOnCooldown(this) &&
                AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_GRAPPLE, false)) {
            level.addFreshEntity(new EntityGrapplingHook(EntityTypesAS.GRAPPLING_HOOK.get(), level, player));
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.consume(held);
    }
}
