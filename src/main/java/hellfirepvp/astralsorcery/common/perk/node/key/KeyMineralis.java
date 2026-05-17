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
 * Key perk for the Mineralis constellation branch.
 * Effect: Enhanced mining speed and ore-related effects.
 * Provides a significant mining speed bonus and reach increase.
 */
public class KeyMineralis extends KeyPerk {

    public KeyMineralis(int x, int y) {
        super(AstralSorcery.key("key_mineralis"), x, y);
        setRequiredConstellation(AstralSorcery.key("mineralis"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.30f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 2.0f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Mining speed enhancement applied via PerkEffectHelper on block break events
    }
}
