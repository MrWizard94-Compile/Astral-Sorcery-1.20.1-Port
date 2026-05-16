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
 * Key perk for the Vicio constellation branch.
 * Effect: Grants slow fall and step assist when moving.
 * Provides +15% movement speed and +1 reach.
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
    public void onPlayerTick(@Nonnull Player player) {
        // Movement-triggered slow fall and step assist
        // Applied via movement event hooks in PerkEffectHelper
    }
}
