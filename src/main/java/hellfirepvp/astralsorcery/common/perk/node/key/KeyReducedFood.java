/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import javax.annotation.Nonnull;

/**
 * Key perk that reduces effective food consumption.
 * Every tick there is a 1% chance to restore 1 food level and a small
 * amount of saturation if the player is not at maximum.
 */
public class KeyReducedFood extends KeyPerk {

    public KeyReducedFood(int x, int y) {
        super(AstralSorcery.key("key_reduced_food"), x, y);
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        if (player.getRandom().nextFloat() < 0.01f) {
            FoodData stats = player.getFoodData();
            if (stats.getFoodLevel() < 20 || stats.getSaturationLevel() < 5.0f) {
                stats.eat(1, 0.3f);
            }
        }
    }
}
