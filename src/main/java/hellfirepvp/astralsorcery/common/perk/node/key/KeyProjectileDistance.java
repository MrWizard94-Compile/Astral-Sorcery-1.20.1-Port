/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that scales projectile damage with travel distance.
 * Every 10 blocks traveled beyond 5 adds 10% extra damage, capped at +50%.
 * Logic in EventHandlerPerkEffects (ProjectileImpactEvent).
 */
public class KeyProjectileDistance extends KeyPerk {

    public static final float BONUS_PER_10_BLOCKS = 0.10f;
    public static final float MAX_BONUS           = 0.50f;
    public static final float BASE_DISTANCE       = 5.0f;

    public KeyProjectileDistance(int x, int y) {
        super(AstralSorcery.key("key_projectile_distance"), x, y);
    }
}
