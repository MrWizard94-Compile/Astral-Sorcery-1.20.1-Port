package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.world.ChunkFluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Registers and attaches Astral Sorcery capabilities.
 *
 * <p>Two event buses are involved:
 * <ul>
 *   <li>MOD bus: {@link #registerCapabilities} (RegisterCapabilitiesEvent)</li>
 *   <li>FORGE bus: {@link #attachPlayerCaps}, {@link #attachChunkCaps},
 *       {@link #onPlayerClone} (game events)</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes:
 * RegisterCapabilitiesEvent replaces @CapabilityInject + CapabilityManager.register(),
 * PlayerEvent.Clone remains same,
 * AttachCapabilitiesEvent stable</p>
 */
public class CapabilitySetup {

    private static final ResourceLocation PLAYER_PROGRESS_KEY =
            AstralSorcery.key("player_progress");
    private static final ResourceLocation CHUNK_FLUID_KEY =
            AstralSorcery.key("chunk_fluid_entry");

    private CapabilitySetup() {}

    // ---- MOD bus events ----

    /**
     * Register capability types. Called on the MOD event bus.
     */
    public static void registerCapabilities(@Nonnull RegisterCapabilitiesEvent event) {
        event.register(PlayerProgress.class);
        event.register(ChunkFluidEntry.class);
    }

    // ---- FORGE bus events ----

    /**
     * Attach player progress capability to all players.
     * Note: registered via addGenericListener in CommonProxy — @SubscribeEvent not needed.
     */
    public static void attachPlayerCaps(@Nonnull AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PLAYER_PROGRESS_KEY, new PlayerCapabilityProvider());
        }
    }

    /**
     * Attach chunk fluid capability to all level chunks.
     * Note: registered via addGenericListener in CommonProxy — @SubscribeEvent not needed.
     */
    public static void attachChunkCaps(@Nonnull AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(CHUNK_FLUID_KEY, new ChunkFluidCapabilityProvider());
    }

    /**
     * Preserve player progress data across death/respawn/dimension change.
     * In 1.20, PlayerEvent.Clone fires with wasDeath flag.
     */
    @SubscribeEvent
    public static void onPlayerClone(@Nonnull PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // reviveCaps() must be called before accessing caps on the dead entity
        original.reviveCaps();

        original.getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(oldData -> {
            newPlayer.getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(newData -> {
                newData.copyFrom(oldData);
            });
        });

        original.invalidateCaps();
    }
}
