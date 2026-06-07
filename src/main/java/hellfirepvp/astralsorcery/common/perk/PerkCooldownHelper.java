/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.client.event.ClientRenderEventHandler;
import hellfirepvp.astralsorcery.common.event.EventHandlerServerTick;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutListContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Manages per-player per-perk cooldowns using tick-based timeouts.
 * Both server and client sides maintain independent containers.
 *
 * <p>1.16 → 1.20: ObserverLib ITickHandler → port's ITickHandler / TickManager;
 * getUniqueID → getUUID; getEntityWorld().isRemote → level().isClientSide();
 * PerkTree.PERK_TREE.getPerk(side, key) → PerkTree.getPerk(key).</p>
 */
public final class PerkCooldownHelper {

    private static final TimeoutListContainer<UUID, ResourceLocation> SERVER_COOLDOWNS =
            new TimeoutListContainer<>(new PerkTimeoutHandler(LogicalSide.SERVER),
                    TickEvent.Type.SERVER);

    private static final TimeoutListContainer<UUID, ResourceLocation> CLIENT_COOLDOWNS =
            new TimeoutListContainer<>(new PerkTimeoutHandler(LogicalSide.CLIENT),
                    TickEvent.Type.CLIENT);

    private PerkCooldownHelper() {}

    /**
     * Registers the cooldown containers with their respective tick managers.
     * Must be called once during mod initialisation (after both sides are set up).
     */
    public static void registerTickHandlers() {
        EventHandlerServerTick.SERVER_TICK_MANAGER.register(SERVER_COOLDOWNS);
        registerClientHandlers();
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerClientHandlers() {
        ClientRenderEventHandler.CLIENT_TICK_MANAGER.register(CLIENT_COOLDOWNS);
    }

    // =========================================================================
    // Cache management
    // =========================================================================

    public static void clearCache(@Nonnull LogicalSide side) {
        getContainer(side).clear();
    }

    public static void removeAllCooldowns(@Nonnull Player player, @Nonnull LogicalSide side) {
        UUID id = player.getUUID();
        TimeoutListContainer<UUID, ResourceLocation> container = getContainer(side);
        if (container.hasList(id)) container.removeList(id);
    }

    public static void removePerkCooldowns(@Nonnull LogicalSide side, @Nonnull AbstractPerk perk) {
        if (!(perk instanceof CooldownPerk)) return;
        getContainer(side).removeList(key -> key.equals(perk.getKey()));
    }

    // =========================================================================
    // Query / mutation
    // =========================================================================

    public static boolean isCooldownActive(@Nonnull Player player, @Nonnull AbstractPerk perk) {
        if (!(perk instanceof CooldownPerk)) return false;
        TimeoutListContainer<UUID, ResourceLocation> container =
                player.level().isClientSide() ? CLIENT_COOLDOWNS : SERVER_COOLDOWNS;
        UUID id = player.getUUID();
        return container.hasList(id) && container.getOrCreateList(id).contains(perk.getKey());
    }

    public static void setCooldown(@Nonnull Player player, @Nonnull AbstractPerk perk, int ticks) {
        if (!(perk instanceof CooldownPerk)) return;
        getContainerFor(player).getOrCreateList(player.getUUID()).setOrAddTimeout(ticks, perk.getKey());
    }

    public static void forceSetCooldown(@Nonnull Player player, @Nonnull AbstractPerk perk, int ticks) {
        if (!(perk instanceof CooldownPerk)) return;
        var list = getContainerFor(player).getOrCreateList(player.getUUID());
        if (!list.setTimeout(ticks, perk.getKey())) {
            list.setOrAddTimeout(ticks, perk.getKey());
        }
    }

    public static int getActiveCooldown(@Nonnull Player player, @Nonnull AbstractPerk perk) {
        if (!(perk instanceof CooldownPerk)) return -1;
        UUID id = player.getUUID();
        var container = getContainerFor(player);
        if (!container.hasList(id)) return -1;
        return container.getOrCreateList(id).getTimeout(perk.getKey());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static TimeoutListContainer<UUID, ResourceLocation> getContainer(@Nonnull LogicalSide side) {
        return side.isClient() ? CLIENT_COOLDOWNS : SERVER_COOLDOWNS;
    }

    private static TimeoutListContainer<UUID, ResourceLocation> getContainerFor(@Nonnull Player player) {
        return player.level().isClientSide() ? CLIENT_COOLDOWNS : SERVER_COOLDOWNS;
    }

    // =========================================================================
    // Timeout delegate — fires onCooldownTimeout when a perk cooldown expires
    // =========================================================================

    private static final class PerkTimeoutHandler
            implements TimeoutListContainer.ContainerTimeoutDelegate<UUID, ResourceLocation> {

        private final LogicalSide side;

        PerkTimeoutHandler(@Nonnull LogicalSide side) {
            this.side = side;
        }

        @Override
        public void onContainerTimeout(UUID playerUUID, ResourceLocation perkKey) {
            Player player = side.isClient()
                    ? EntityUtils.getPlayerClient(playerUUID)
                    : EntityUtils.getPlayerServer(playerUUID);
            if (player == null) return;
            AbstractPerk perk = PerkTree.getPerk(perkKey);
            if (perk instanceof CooldownPerk cd) {
                cd.onCooldownTimeout(player);
            }
        }
    }
}
