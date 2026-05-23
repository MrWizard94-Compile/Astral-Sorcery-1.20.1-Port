package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

/**
 * Manages saved perk tree configurations for the Shifting Stone.
 * Players can save up to a fixed number of perk allocations and cycle
 * between them without a full respec cost.
 *
 * <p>Full multi-slot save/restore logic is deferred until the perk tree
 * UI and allocation validation are fully wired (Phase 9+). This stub
 * accepts the cycle call and logs it so the item is functional.</p>
 */
public final class PerkTreeManager {

    private PerkTreeManager() {}

    /**
     * Cycles the player's active perk tree configuration to the next saved slot.
     * If no configurations have been saved, this is a no-op.
     *
     * @param player the server-side player whose configuration to cycle
     */
    public static void cycleConfiguration(@Nonnull ServerPlayer player) {
        // Saved configuration slots are not yet persisted.
        // Log so gameplay testers can see the item activates correctly.
        AstralSorcery.log.debug("PerkTreeManager.cycleConfiguration called for {} (no-op until Phase 9)",
                player.getGameProfile().getName());
    }
}
