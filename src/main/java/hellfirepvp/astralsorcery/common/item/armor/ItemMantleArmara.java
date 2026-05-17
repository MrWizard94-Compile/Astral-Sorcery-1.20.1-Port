/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Mantle of Armara — defense constellation.
 * Passive: Grants Resistance I while worn.
 * Night bonus: Resistance I + Fire Resistance under open sky at night.
 */
public class ItemMantleArmara extends ItemMantle {

    public ItemMantleArmara() {
        super(AstralSorcery.key("armara"));
    }

    @Override
    protected void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                40, 0, true, false));
        if (isNighttime(level) && isUnderOpenSky(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                    40, 0, true, false));
        }
    }
}
