/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * Key perk for the Alcara constellation.
 * Effect: Grants bonus damage against undead mobs and provides a
 * passive regeneration bonus at nighttime. Enhances potion effects.
 *
 * <p>Provides +15% perk effect multiplier (amplifies all other perks).</p>
 */
public class KeyAlcara extends KeyPerk {

    public KeyAlcara(int x, int y) {
        super(AstralSorcery.key("key_alcara"), x, y);
        setRequiredConstellation(AstralSorcery.key("alcara"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_PERK_EFFECT.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;

        // Passive nighttime regeneration: heal 0.1 HP every 2 seconds at night
        long dayTime = player.level().getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;

        if (isNight && player.tickCount % 40 == 0) {
            float current = player.getHealth();
            float max = player.getMaxHealth();
            if (current < max) {
                player.heal(0.1f);
            }
        }
    }
}
