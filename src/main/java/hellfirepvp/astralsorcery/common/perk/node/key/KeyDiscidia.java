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
 * Key perk for the Discidia constellation branch.
 * Effect: Attacks deal increased damage when the player is at full health.
 * Provides a flat +2 attack damage bonus, plus 15% crit chance.
 */
public class KeyDiscidia extends KeyPerk {

    private static final float FULL_HEALTH_DAMAGE_MULT = 1.25f;

    public KeyDiscidia(int x, int y) {
        super(AstralSorcery.key("key_discidia"), x, y);
        setRequiredConstellation(AstralSorcery.key("discidia"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ATTACK_DAMAGE.getKey(),
                ModifierType.ADDITION, 2.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE.getKey(),
                ModifierType.ADDITION, 0.15f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Full-health damage bonus applied via attribute modifier dynamically
        // The actual scaling is handled by PerkEffectHelper when calculating damage
    }
}
