/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.EffectManager;
import hellfirepvp.astralsorcery.common.starlight.ClientStarlightNetworkCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Central client-side event handler for rendering and tick events.
 *
 * <p>Handles:
 * <ul>
 *   <li>Client tick: ticks the effect manager</li>
 *   <li>World render: renders effects after entities</li>
 *   <li>World unload: clears effect and cache state</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class ClientRenderEventHandler {

    @SubscribeEvent
    public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().isPaused()) return;

        EffectManager.getInstance().tick();
    }

    @SubscribeEvent
    public void onRenderLevelStage(@Nonnull RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        // Render all AS visual effects
        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();
        EffectManager.getInstance().render(poseStack, bufferSource, partialTick);
        bufferSource.endBatch();
    }

    @SubscribeEvent
    public void onWorldUnload(@Nonnull net.minecraftforge.event.level.LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;

        EffectManager.getInstance().clear();
        ClientStarlightNetworkCache.clear();
    }
}
