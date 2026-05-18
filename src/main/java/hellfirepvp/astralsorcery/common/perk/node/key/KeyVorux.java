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
 * Key perk for the Vorux constellation.
 * Effect: Berserker mode — attack damage scales inversely with current health.
 * The lower the player's health, the more damage they deal.
 *
 * <p>Passive +15% attack speed, +10% crit chance.</p>
 *
 * <p>The damage scaling is applied via the LivingHurtEvent handler in
 * {@link hellfirepvp.astralsorcery.common.perk.effect.PerkDamageHandler}.</p>
 */
public class KeyVorux extends KeyPerk {

    /** Maximum bonus damage multiplier at 1 HP. */
    public static final float MAX_LOW_HEALTH_BONUS = 0.50f;

    public KeyVorux(int x, int y) {
        super(AstralSorcery.key("key_vorux"), x, y);
        setRequiredConstellation(AstralSorcery.key("vorux"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE.getKey(),
                ModifierType.ADDITION, 0.10f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Strength I when below 50% health — berserker rage
        if (player.getHealth() / player.getMaxHealth() < 0.5f) {
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DAMAGE_BOOST),
                    40, 0, true, false));
        }
    }

    /**
     * Computes the bonus damage multiplier based on the player's health fraction.
     * Returns 0.0 at full health, up to {@link #MAX_LOW_HEALTH_BONUS} at 1 HP.
     *
     * @param player the attacking player
     * @return bonus damage multiplier (0.0 to MAX_LOW_HEALTH_BONUS)
     */
    public static float getLowHealthBonus(@Nonnull Player player) {
        float healthFraction = player.getHealth() / player.getMaxHealth();
        float missingFraction = 1.0f - healthFraction;
        return missingFraction * MAX_LOW_HEALTH_BONUS;
    }
}
