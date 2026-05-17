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
 * Key perk for the Fornax constellation branch.
 * Effect: Fire resistance and smelting-on-dig capability.
 * Mined ores drop their smelted result, and the player takes
 * reduced fire damage. Also provides elemental resistance.
 */
public class KeyFornax extends KeyPerk {

    public KeyFornax(int x, int y) {
        super(AstralSorcery.key("key_fornax"), x, y);
        setRequiredConstellation(AstralSorcery.key("fornax"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey(),
                ModifierType.ADDITION, 0.30f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Auto-smelt and fire resistance handled by event subscribers in PerkEffectHelper
    }
}
