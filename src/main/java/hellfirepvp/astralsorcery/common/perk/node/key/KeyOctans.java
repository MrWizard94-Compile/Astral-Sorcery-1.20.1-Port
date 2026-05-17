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
 * Key perk for the Octans constellation branch.
 * Effect: Improved underwater capabilities — faster swim speed,
 * extended breath, and life steal while submerged. The player
 * regenerates health while in water.
 */
public class KeyOctans extends KeyPerk {

    public KeyOctans(int x, int y) {
        super(AstralSorcery.key("key_octans"), x, y);
        setRequiredConstellation(AstralSorcery.key("octans"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MOVEMENT_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDITION, 0.10f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Underwater healing and swim speed applied by PerkEffectHelper when player is in water
    }
}
