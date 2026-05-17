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
 * Key perk for the Ulteria constellation.
 * Effect: Enhanced loot drops — provides extra experience and slightly
 * increased drop rates. Looting-like bonus applied through perk effect events.
 *
 * <p>Passive +20% experience bonus, +10% mining speed.</p>
 */
public class KeyUlteria extends KeyPerk {

    public KeyUlteria(int x, int y) {
        super(AstralSorcery.key("key_ulteria"), x, y);
        setRequiredConstellation(AstralSorcery.key("ulteria"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Loot bonuses applied via LivingDropsEvent in PerkEffectHelper
    }
}
