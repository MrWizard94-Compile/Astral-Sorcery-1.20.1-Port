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
 * Key perk for the Armara constellation branch.
 * Effect: Grants a damage reduction shield when standing still.
 * Provides +4 armor, +2 armor toughness, and 20% knockback resistance.
 */
public class KeyArmara extends KeyPerk {

    private static final int STILL_TICKS_REQUIRED = 20;

    public KeyArmara(int x, int y) {
        super(AstralSorcery.key("key_armara"), x, y);
        setRequiredConstellation(AstralSorcery.key("armara"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(),
                ModifierType.ADDITION, 4.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS.getKey(),
                ModifierType.ADDITION, 2.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST.getKey(),
                ModifierType.ADDITION, 0.2f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Standing-still damage reduction is applied dynamically
        // by PerkEffectHelper during damage calculation events
    }
}
