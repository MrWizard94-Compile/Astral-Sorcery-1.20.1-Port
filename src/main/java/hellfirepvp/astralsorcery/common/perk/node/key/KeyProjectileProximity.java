/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;

/**
 * Key perk that boosts projectile damage when the shooter is within 5 blocks
 * of the target at impact (+40% damage for point-blank shots).
 * Logic in EventHandlerPerkEffects (ProjectileImpactEvent).
 */
public class KeyProjectileProximity extends KeyPerk {

    public static final float PROXIMITY_BONUS    = 0.40f;
    public static final float PROXIMITY_DISTANCE = 5.0f;

    public KeyProjectileProximity(int x, int y) {
        super(AstralSorcery.key("key_projectile_proximity"), x, y);
    }
}
