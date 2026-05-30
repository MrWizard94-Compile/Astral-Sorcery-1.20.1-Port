package hellfirepvp.astralsorcery.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

/**
 * Fired (server-side) when an item cooldown is set on a player, allowing perk effects
 * (e.g. Horologium) to reduce the cooldown before it is applied.
 *
 * <p>Fire this event from any site that calls
 * {@code player.getCooldowns().addCooldown(item, ticks)} so the Horologium
 * cooldown-reduction perk can intercept it.</p>
 *
 * <p>1.16 → 1.20: PlayerEntity → Player; field types unchanged.</p>
 */
public class CooldownSetEvent extends Event {

    private final Player player;
    private final int originalCooldown;
    private int cooldown;

    public CooldownSetEvent(@Nonnull Player player, int originalCooldown) {
        this.player = player;
        this.originalCooldown = originalCooldown;
        this.cooldown = originalCooldown;
    }

    @Nonnull
    public Player getPlayer() {
        return player;
    }

    public int getOriginalCooldown() {
        return originalCooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getResultCooldown() {
        return cooldown;
    }
}
