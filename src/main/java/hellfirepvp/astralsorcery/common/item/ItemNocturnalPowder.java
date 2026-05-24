package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

/**
 * Nocturnal Powder — dark counterpart to Illumination Powder.
 * Produced by infusing illumination powder under the Lucerna constellation.
 * Right-click throws a nocturnal spark that seeks and damages nearby hostile mobs.
 */
public class ItemNocturnalPowder extends ItemAS {

    public ItemNocturnalPowder() {
        super(defaultProperties());
    }

    @Nonnull
    @Override
    @SuppressWarnings("null")
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            EntityNocturnalSpark spark = new EntityNocturnalSpark(
                    EntityTypesAS.NOCTURNAL_SPARK.get(), level,
                    player.getX(), player.getEyeY() - 0.1, player.getZ());
            Vec3 look = player.getLookAngle();
            spark.setDeltaMovement(look.scale(0.7));
            level.addFreshEntity(spark);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
