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
 * Mantle of Vicio — speed/movement constellation.
 * Passive: Grants Speed I while worn.
 * Night bonus: Speed II + slow falling under open sky at night.
 */
public class ItemMantleVicio extends ItemMantle {

    public ItemMantleVicio() {
        super(AstralSorcery.key("vicio"));
    }

    @Override
    public void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        int amplifier = 0;
        if (isNighttime(level) && isUnderOpenSky(player)) {
            amplifier = 1;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                    40, 0, true, false));
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                40, amplifier, true, false));
    }
}
