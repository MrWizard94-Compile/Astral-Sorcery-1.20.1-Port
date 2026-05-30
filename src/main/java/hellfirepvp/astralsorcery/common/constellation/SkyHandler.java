package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tick handler that manages per-dimension {@link WorldContext} instances.
 * Each dimension gets its own context seeded by the world seed so that
 * constellation phase assignments are deterministic and dimension-specific.
 *
 * <p>Client-side: the seed is requested from the server via {@link WorldSeedCache}
 * and the context is created lazily on the first client tick that has the seed.</p>
 *
 * <p>1.16 → 1.20: ITickHandler → @SubscribeEvent; RegistryKey&lt;World&gt; →
 * ResourceKey&lt;Level&gt;; ServerWorld → ServerLevel; world.isRemote → !level.isClientSide().</p>
 */
public class SkyHandler {

    private static final SkyHandler INSTANCE = new SkyHandler();

    private final Map<ResourceKey<Level>, WorldContext> serverContexts = new HashMap<>();
    private final Map<ResourceKey<Level>, WorldContext> clientContexts = new HashMap<>();

    private SkyHandler() {}

    public static SkyHandler getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Static access helpers
    // -------------------------------------------------------------------------

    @Nullable
    public static WorldContext getContext(@Nullable Level level) {
        if (level == null) return null;
        return level.isClientSide()
                ? INSTANCE.clientContexts.get(level.dimension())
                : INSTANCE.serverContexts.get(level.dimension());
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        ResourceKey<Level> key = level.dimension();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            WorldContext ctx = serverContexts.computeIfAbsent(key,
                    k -> new WorldContext(serverLevel.getSeed()));
            ctx.tick(level);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        ResourceKey<Level> key = level.dimension();
        if (!clientContexts.containsKey(key)) {
            Optional<Long> seedOpt = WorldSeedCache.getSeedIfPresent(key);
            seedOpt.ifPresent(seed -> clientContexts.put(key, new WorldContext(seed)));
            if (!clientContexts.containsKey(key)) return;
        }
        clientContexts.get(key).tick(level);
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            ResourceKey<Level> key = level.dimension();
            if (level.isClientSide()) {
                clientContexts.remove(key);
            } else {
                serverContexts.remove(key);
            }
        }
    }

    public void clientClearCache() {
        clientContexts.clear();
    }
}
