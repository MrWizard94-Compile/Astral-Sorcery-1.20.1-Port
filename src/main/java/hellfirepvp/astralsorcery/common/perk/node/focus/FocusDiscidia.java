/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.focus;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.FocusPerk;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * Focus perk for the Discidia constellation (deep offense).
 * Grants +15% crit multiplier (STACKING_MULTIPLY 1.15) and
 * +5% crit chance. Has a tick effect for dynamic combat bonuses.
 */
public class FocusDiscidia extends FocusPerk {

    public FocusDiscidia(int x, int y) {
        super(AstralSorcery.key("focus_discidia"), x, y, AstralSorcery.key("discidia"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_MULTIPLIER.getKey(),
                ModifierType.STACKING_MULTIPLY, 1.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_CRIT_CHANCE.getKey(),
                ModifierType.ADDITION, 0.05f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Dynamic combat bonuses applied per-tick by PerkEffectHelper
    }
}
