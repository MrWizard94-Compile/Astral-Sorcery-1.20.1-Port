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
 * Mantle of Aevitas — life/nature constellation.
 * Passive: Grants Regeneration I while worn.
 * Night bonus: Regeneration II under open sky at night.
 */
public class ItemMantleAevitas extends ItemMantle {

    public ItemMantleAevitas() {
        super(AstralSorcery.key("aevitas"));
    }

    @Override
    protected void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        int amplifier = 0;
        if (isNighttime(level) && isUnderOpenSky(player)) {
            amplifier = 1;
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                40, amplifier, true, false));
    }
}
