/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.effect;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Event handler that applies and ticks perk effects on players.
 * Registered on the Forge event bus.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Tick all allocated perks with tick effects each server tick</li>
 *   <li>Re-apply vanilla attribute modifiers on login, respawn, dimension change</li>
 *   <li>Clean up attribute modifiers on logout</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes: PlayerEntity -> Player, ServerPlayerEntity -> ServerPlayer</p>
 */
public class PerkEffectHelper {

    /**
     * Ticks perk effects for each online player every server tick.
     */
    @SubscribeEvent
    public void onPlayerTick(@Nonnull TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) {
            return;
        }

        Set<ResourceLocation> allocated = progress.getAllocatedPerks();
        if (allocated.isEmpty()) {
            return;
        }

        PerkTree.tickPerks(player, allocated);
    }

    /**
     * Re-applies vanilla attribute modifiers on login.
     */
    @SubscribeEvent
    public void onPlayerLogin(@Nonnull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            refreshModifiers(serverPlayer);
        }
    }

    /**
     * Re-applies vanilla attribute modifiers on respawn (death).
     */
    @SubscribeEvent
    public void onPlayerRespawn(@Nonnull PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            refreshModifiers(serverPlayer);
        }
    }

    /**
     * Re-applies vanilla attribute modifiers on dimension change.
     */
    @SubscribeEvent
    public void onPlayerDimensionChange(@Nonnull PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            refreshModifiers(serverPlayer);
        }
    }

    /**
     * Cleans up vanilla attribute modifiers on logout.
     */
    @SubscribeEvent
    public void onPlayerLogout(@Nonnull PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PerkAttributeHelper.removeAllVanillaModifiers(serverPlayer);
        }
    }

    /**
     * Refreshes all perk-based vanilla attribute modifiers for a player.
     */
    private void refreshModifiers(@Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) {
            return;
        }
        Set<ResourceLocation> allocated = progress.getAllocatedPerks();
        PerkAttributeHelper.applyVanillaModifiers(player, allocated);
        AstralSorcery.log.debug("Refreshed perk modifiers for player {} ({} perks)",
                player.getName().getString(), allocated.size());
    }
}
