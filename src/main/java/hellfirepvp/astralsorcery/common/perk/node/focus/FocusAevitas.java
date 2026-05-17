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
 * Focus perk for the Aevitas constellation (deep life).
 * Grants +5% life steal (ADDED_MULTIPLY 0.05). Has a tick effect
 * for passive health regeneration.
 */
public class FocusAevitas extends FocusPerk {

    public FocusAevitas(int x, int y) {
        super(AstralSorcery.key("focus_aevitas"), x, y, AstralSorcery.key("aevitas"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.05f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Passive health regeneration applied per-tick by PerkEffectHelper
    }
}
