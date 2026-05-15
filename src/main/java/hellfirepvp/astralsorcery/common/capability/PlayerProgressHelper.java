package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Convenience methods for accessing {@link PlayerProgress} on a Player.
 * Avoids verbose capability.ifPresent patterns throughout the codebase.
 *
 * <p>1.16 -> 1.20 changes: Capability access patterns unchanged.</p>
 */
public final class PlayerProgressHelper {

    private PlayerProgressHelper() {}

    /**
     * Get the player's progress data, or null if the capability is missing.
     */
    @Nullable
    public static PlayerProgress getProgress(@Nonnull Player player) {
        return player.getCapability(PlayerCapabilityProvider.CAPABILITY)
                .orElse(null);
    }

    /**
     * Get the player's progress data, throwing if the capability is missing.
     * Only use when you're certain the player has the capability (server-side, normal gameplay).
     */
    @Nonnull
    public static PlayerProgress getProgressOrThrow(@Nonnull Player player) {
        return player.getCapability(PlayerCapabilityProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException(
                        "Player " + player.getName().getString() + " missing AS progress capability"));
    }
}
