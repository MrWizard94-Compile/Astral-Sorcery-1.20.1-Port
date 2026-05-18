/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Key perk that slowly auto-repairs worn armor and held tools using
 * starlight energy. Every ~800 ticks (40 seconds on average), one
 * damaged item from the player's equipment is repaired by 1 durability.
 */
public class KeyMending extends KeyPerk {

    private static final int CHANCE_DENOMINATOR = 800;

    public KeyMending(int x, int y) {
        super(AstralSorcery.key("key_mending"), x, y);
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        if (player.getRandom().nextInt(CHANCE_DENOMINATOR) != 0) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamaged()) {
                stack.setDamageValue(stack.getDamageValue() - 1);
                break;
            }
        }
    }
}
