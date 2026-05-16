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
 * Key perk for the Evorsio constellation branch.
 * Effect: Mining drops extra resources at a low chance (fortune-like).
 * Provides +25% mining speed and +10% experience gain.
 */
public class KeyEvorsio extends KeyPerk {

    private static final float EXTRA_DROP_CHANCE = 0.15f;

    public KeyEvorsio(int x, int y) {
        super(AstralSorcery.key("key_evorsio"), x, y);
        setRequiredConstellation(AstralSorcery.key("evorsio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.25f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Extra resource drops handled via BlockBreak event hook
        // in PerkEffectHelper
    }
}
