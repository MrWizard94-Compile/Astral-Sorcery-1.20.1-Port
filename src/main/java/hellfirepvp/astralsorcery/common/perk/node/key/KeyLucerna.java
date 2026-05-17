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
 * Key perk for the Lucerna constellation branch.
 * Effect: Enhanced night vision and improved starlight collection.
 * The player gains increased experience and starlight collection rate.
 */
public class KeyLucerna extends KeyPerk {

    public KeyLucerna(int x, int y) {
        super(AstralSorcery.key("key_lucerna"), x, y);
        setRequiredConstellation(AstralSorcery.key("lucerna"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.25f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Night vision effect applied by PerkEffectHelper when it's nighttime
    }
}
