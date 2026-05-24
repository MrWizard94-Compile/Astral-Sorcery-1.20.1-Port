package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface AlignmentChargeConsumer extends AlignmentChargeRevealer {

    float getAlignmentChargeCost(Player player, ItemStack stack);
}
