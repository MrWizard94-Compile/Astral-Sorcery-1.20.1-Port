/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Key perk for the Octans constellation branch.
 * Effect: Water Breathing always; Dolphin's Grace and Regeneration when submerged.
 * Speed and life-steal bonuses via attributes.
 */
public class KeyOctans extends KeyPerk {

    public KeyOctans(int x, int y) {
        super(AstralSorcery.key("key_octans"), x, y);
        setRequiredConstellation(AstralSorcery.key("octans"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDITION, 0.10f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Water Breathing always active — ambient, no particles
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.WATER_BREATHING),
                40, 0, true, false));
        // Additional bonuses while in water
        if (player.isInWater()) {
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DOLPHINS_GRACE),
                    40, 0, true, false));
            if (player.tickCount % 40 == 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(0.5f);
            }
        }
    }
}
