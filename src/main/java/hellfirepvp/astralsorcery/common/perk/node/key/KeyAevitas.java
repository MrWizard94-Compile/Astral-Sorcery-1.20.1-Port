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
 * Key perk for the Aevitas constellation branch.
 * Effect: Passively regenerates nearby crops and heals the player slowly.
 * Provides +4 max health and 5% life steal.
 */
public class KeyAevitas extends KeyPerk {

    private static final int CROP_GROWTH_RADIUS = 5;
    private static final int CROP_TICK_INTERVAL = 40;

    public KeyAevitas(int x, int y) {
        super(AstralSorcery.key("key_aevitas"), x, y);
        setRequiredConstellation(AstralSorcery.key("aevitas"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(),
                ModifierType.ADDITION, 4.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_LIFE_STEAL.getKey(),
                ModifierType.ADDITION, 0.05f));
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        // Crop growth and passive heal are triggered from PerkEffectHelper's
        // player tick handler when this perk is allocated
    }
}
