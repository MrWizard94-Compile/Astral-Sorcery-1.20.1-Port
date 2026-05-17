/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Key perk for the Pelotrio constellation.
 * Effect: When the player would die, they are instead healed to 25% HP
 * with a temporary regeneration and resistance buff. This can trigger
 * once every 5 minutes (6000 ticks).
 *
 * <p>Also provides a passive +10% max health bonus.</p>
 */
public class KeyPelotrio extends KeyPerk {

    private static final int COOLDOWN_TICKS = 6000; // 5 minutes
    private static final float REVIVE_HEALTH_FRACTION = 0.25f;
    private static final int REVIVE_REGEN_DURATION = 200; // 10 seconds
    private static final int REVIVE_RESIST_DURATION = 100; // 5 seconds

    /**
     * Tracks cooldowns per player UUID. Not persisted — resets on server restart.
     * This is intentional for balance (gives a fresh charge after restart).
     */
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public KeyPelotrio(int x, int y) {
        super(AstralSorcery.key("key_pelotrio"), x, y);
        setRequiredConstellation(AstralSorcery.key("pelotrio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MAX_HEALTH.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }

    @Override
    public boolean hasTickEffect() {
        return false; // No per-tick cost; the revive triggers via event
    }

    /**
     * Attempts to revive the player instead of letting them die.
     * Called by the damage event handler when the player would take lethal damage.
     *
     * @param player the player about to die
     * @return true if the revive was triggered, false if on cooldown
     */
    public boolean tryRevive(@Nonnull ServerPlayer player) {
        long currentTick = player.level().getGameTime();
        Long lastUsed = COOLDOWNS.get(player.getUUID());

        if (lastUsed != null && (currentTick - lastUsed) < COOLDOWN_TICKS) {
            return false;
        }

        float maxHealth = player.getMaxHealth();
        player.setHealth(maxHealth * REVIVE_HEALTH_FRACTION);

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                REVIVE_REGEN_DURATION, 2, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                REVIVE_RESIST_DURATION, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                REVIVE_RESIST_DURATION, 0, false, true));

        COOLDOWNS.put(player.getUUID(), currentTick);

        AstralSorcery.log.debug("Pelotrio revived player {} at {}/{} HP",
                player.getName().getString(),
                player.getHealth(), maxHealth);
        return true;
    }

    /**
     * Clears cooldown data for a player. Called on logout.
     */
    public static void clearCooldown(@Nonnull UUID playerUUID) {
        COOLDOWNS.remove(playerUUID);
    }
}
