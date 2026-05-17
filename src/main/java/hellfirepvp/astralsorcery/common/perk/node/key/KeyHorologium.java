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
 * Key perk for the Horologium constellation branch.
 * Effect: Time manipulation — increased action speed, faster
 * cooldown recovery, and a chance to "freeze" incoming damage
 * momentarily. The most powerful and rarest constellation perk.
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
    public void onPlayerTick(@Nonnull Player player) {
        // Time freeze mechanic handled by event listener on LivingHurtEvent
    }
}
