/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that chains lightning through up to 3 nearby enemies on melee hit
 * with a 60% proc chance. Each arc deals 60% of the original hit's damage.
 * Logic in EventHandlerPerkEffects (LivingHurtEvent, LOWEST priority).
 */
public class KeyLightningArc extends KeyPerk {

    public static final float ARC_CHANCE   = 0.60f;
    public static final float ARC_DAMAGE   = 0.60f;
    public static final float ARC_RANGE    = 7.0f;
    public static final int   ARC_CHAINS   = 3;

    public KeyLightningArc(int x, int y) {
        super(AstralSorcery.key("key_lightning_arc"), x, y);
    }
}
