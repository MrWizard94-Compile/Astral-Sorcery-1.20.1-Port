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
 * Key perk for the Vicio constellation branch.
 * Effect: Grants Slow Falling while airborne and Speed I passively.
 * Provides +15% movement speed and +1 reach via attribute.
 */
public class KeyVicio extends KeyPerk {

    public KeyVicio(int x, int y) {
        super(AstralSorcery.key("key_vicio"), x, y);
        setRequiredConstellation(AstralSorcery.key("vicio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 1.0f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Slow Falling when airborne (falling), ambient, no particles
        if (!player.onGround() && player.getDeltaMovement().y < 0) {
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.SLOW_FALLING),
                    40, 0, true, false));
        }
        // Passive Speed I
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.MOVEMENT_SPEED),
                40, 0, true, false));
    }
}
