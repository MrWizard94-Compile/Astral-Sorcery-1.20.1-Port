/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that instantly kills mobs below 15% max health on melee hit.
 * Logic in EventHandlerPerkEffects (LivingDamageEvent, LOW priority).
 */
public class KeyCullingAttack extends KeyPerk {

    public static final float CULL_THRESHOLD = 0.15f;

    public KeyCullingAttack(int x, int y) {
        super(AstralSorcery.key("key_culling_attack"), x, y);
    }
}
