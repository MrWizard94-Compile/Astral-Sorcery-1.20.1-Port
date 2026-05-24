package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages saved perk tree configurations for the Shifting Stone.
 * Players have {@link PlayerProgress#MAX_PERK_CONFIGS} slots. Each use of
 * the Shifting Stone saves the current allocation to the active slot, then
 * rotates to the next slot and restores that slot's allocation (if non-empty).
 *
 * <p>Switching calls {@link AbstractPerk#onDeallocate} / {@link AbstractPerk#onAllocate}
 * for each perk removed/added, then reapplies vanilla attribute modifiers and
 * syncs progress to the client.</p>
 */
public final class PerkTreeManager {

    private PerkTreeManager() {}

    /**
     * Cycles the player's active perk tree configuration to the next saved slot.
     */
    public static void cycleConfiguration(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) return;

        int currentSlot = progress.getActivePerkConfig();
        Set<ResourceLocation> currentPerks = new HashSet<>(progress.getAllocatedPerks());

        // Save current allocation to the current slot
        progress.savePerkConfig(currentSlot, currentPerks);

        // Advance to next slot
        int nextSlot = (currentSlot + 1) % PlayerProgress.MAX_PERK_CONFIGS;
        progress.setActivePerkConfig(nextSlot);

        Set<ResourceLocation> targetPerks = progress.getPerkConfig(nextSlot);
        if (targetPerks.isEmpty()) {
            // Next slot is blank — stay on current allocation but note the slot advance
            AstralSorcery.log.debug("{}: cycled to empty config slot {} (keeping current perks)",
                    player.getGameProfile().getName(), nextSlot);
            PlayerProgressManager.syncProgress(player);
            return;
        }

        // Compute delta
        Set<ResourceLocation> toRemove = new HashSet<>(currentPerks);
        toRemove.removeAll(targetPerks);

        Set<ResourceLocation> toAdd = new HashSet<>(targetPerks);
        toAdd.removeAll(currentPerks);

        // Apply removals first (frees dependencies before additions)
        for (ResourceLocation key : toRemove) {
            progress.deallocatePerk(key);
            AbstractPerk perk = PerkTree.getPerk(key);
            if (perk != null) {
                perk.onDeallocate(player);
            }
        }

        // Apply additions
        for (ResourceLocation key : toAdd) {
            progress.allocatePerk(key);
            AbstractPerk perk = PerkTree.getPerk(key);
            if (perk != null) {
                perk.onAllocate(player);
            }
        }

        // Reapply vanilla attribute modifiers (armor, health, speed, etc.)
        PerkAttributeHelper.applyVanillaModifiers(player, progress.getAllocatedPerks());

        AstralSorcery.log.debug("{}: cycled from config slot {} to {} ({} removed, {} added)",
                player.getGameProfile().getName(), currentSlot, nextSlot,
                toRemove.size(), toAdd.size());

        PlayerProgressManager.syncProgress(player);
    }
}
