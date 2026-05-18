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
 * Key perk for the Horologium constellation branch.
 * Effect: Passive Speed I + Haste I at all times; Regeneration I during nighttime.
 * Attack speed, movement speed, and perk effect bonuses via attributes.
 */
public class KeyHorologium extends KeyPerk {

    public KeyHorologium(int x, int y) {
        super(AstralSorcery.key("key_horologium"), x, y);
        setRequiredConstellation(AstralSorcery.key("horologium"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Passive Speed I + Haste I — ambient, no particles
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.MOVEMENT_SPEED),
                40, 0, true, false));
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DIG_SPEED),
                40, 0, true, false));
        // Bonus Regeneration I at night
        long dayTime = player.level().getDayTime() % 24000L;
        if (dayTime >= 13000L && dayTime <= 23000L) {
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.REGENERATION),
                    40, 0, true, false));
        }
    }
}
