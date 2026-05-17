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
 * Key perk for the Bootes constellation branch.
 * Effect: Enhanced farming and animal husbandry. Nearby animals
 * grow faster and crops have increased yield. Also provides a
 * moderate experience bonus from farming activities.
 */
public class KeyBootes extends KeyPerk {

    public KeyBootes(int x, int y) {
        super(AstralSorcery.key("key_bootes"), x, y);
        setRequiredConstellation(AstralSorcery.key("bootes"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.15f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Crop growth and animal breeding bonuses applied by event listeners
    }
}
