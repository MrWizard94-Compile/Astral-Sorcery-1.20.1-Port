/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Central manager for all Astral Sorcery client-side visual effects.
 * Handles spawning, ticking, and batch-rendering of effects grouped
 * by their {@link RenderType}.
 *
 * <p>Effects are ticked each client tick and rendered each frame.
 * Dead effects are removed during the tick phase.</p>
 *
 * <p>1.16 → 1.20: Replaces the old EffectHandler / RenderDispatcher
 * pattern. Uses {@link MultiBufferSource} for batched rendering.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class EffectManager {

    private static final EffectManager INSTANCE = new EffectManager();

    /** Effects grouped by render type for batched rendering. */
    private final Map<RenderType, List<EntityVisualFX>> effectsByType = new HashMap<>();

    /** Pending additions (added during tick, applied at start of next tick). */
    private final List<EntityVisualFX> pendingAdd = new ArrayList<>();

    private EffectManager() {}

    @Nonnull
    public static EffectManager getInstance() {
        return INSTANCE;
    }

    // =========================================================================
    // Spawning
    // =========================================================================

    /**
     * Spawn a visual effect with the given properties.
     *
     * @param effect     the effect instance (must have position set)
     * @param properties optional properties to apply; null uses defaults
     * @return the spawned effect, for further configuration
     */
    @Nonnull
    public <T extends EntityVisualFX> T spawn(@Nonnull T effect,
                                               @Nullable EffectProperties properties) {
        if (properties != null) {
            properties.applyTo(effect);
        }
        synchronized (pendingAdd) {
            pendingAdd.add(effect);
        }
        return effect;
    }

    /**
     * Spawn a visual effect with default properties.
     */
    @Nonnull
    public <T extends EntityVisualFX> T spawn(@Nonnull T effect) {
        return spawn(effect, null);
    }

    // =========================================================================
    // Tick
    // =========================================================================

    /**
     * Called every client tick. Integrates pending effects, ticks all,
     * and removes dead ones.
     */
    public void tick() {
        // Integrate pending additions
        synchronized (pendingAdd) {
            for (EntityVisualFX fx : pendingAdd) {
                effectsByType.computeIfAbsent(fx.getRenderType(), k -> new ArrayList<>())
                        .add(fx);
            }
            pendingAdd.clear();
        }

        // Tick and prune
        for (List<EntityVisualFX> effects : effectsByType.values()) {
            Iterator<EntityVisualFX> it = effects.iterator();
            while (it.hasNext()) {
                EntityVisualFX fx = it.next();
                fx.tick();
                if (fx.isDead()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Render all active effects, batched by render type.
     *
     * @param poseStack    the camera-relative pose stack
     * @param bufferSource the buffer source for obtaining vertex consumers
     * @param partialTick  partial tick for interpolation
     */
    public void render(@Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        float partialTick) {
        for (Map.Entry<RenderType, List<EntityVisualFX>> entry : effectsByType.entrySet()) {
            List<EntityVisualFX> effects = entry.getValue();
            if (effects.isEmpty()) continue;

            VertexConsumer buffer = bufferSource.getBuffer(entry.getKey());
            for (EntityVisualFX fx : effects) {
                fx.renderEffect(poseStack, buffer, partialTick);
            }
        }
    }

    /**
     * Remove all active effects. Called on world unload or dimension change.
     */
    public void clear() {
        effectsByType.clear();
        synchronized (pendingAdd) {
            pendingAdd.clear();
        }
    }

    /**
     * @return total number of active effects across all render types
     */
    public int getActiveEffectCount() {
        int count = 0;
        for (List<EntityVisualFX> effects : effectsByType.values()) {
            count += effects.size();
        }
        return count;
    }
}
