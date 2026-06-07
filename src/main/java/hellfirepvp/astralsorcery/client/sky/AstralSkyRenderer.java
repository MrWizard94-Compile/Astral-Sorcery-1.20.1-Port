/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.data.config.ClientConfig;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * Custom sky renderer that adds Astral Sorcery's constellation layer
 * on top of vanilla sky rendering.
 *
 * <p>1.16 → 1.20 changes:
 * {@code ISkyRenderHandler} is gone. Instead, we hook into
 * {@link RenderLevelStageEvent} at the {@code AFTER_SKY} stage.
 * {@code RenderSystem.pushMatrix()} / {@code popMatrix()} are gone — use PoseStack.
 * {@code RenderSystem.shadeModel()} is gone — shading is done via shaders.
 * {@code MatrixStack} → {@code PoseStack}.</p>
 *
 * <p>The constellation layer renders discovered constellations as
 * twinkling star patterns on the sky dome. Visibility depends on:
 * <ul>
 *   <li>Time of day (only at night)</li>
 *   <li>Weather (reduced in rain, invisible in thunder)</li>
 *   <li>Dimension (overworld only by default)</li>
 *   <li>Player progress (only discovered constellations)</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class AstralSkyRenderer {

    /** Distance from camera to sky dome constellation positions. */
    private static final float SKY_DISTANCE = 100.0f;

    /** Maximum number of constellations visible simultaneously. */
    private static final int MAX_VISIBLE_CONSTELLATIONS = 8;

    /** Seed offset for constellation placement randomization. */
    private static final long PLACEMENT_SEED = 0x4153_5472_4C53_4B59L; // "ASTrLSKY"

    @SubscribeEvent
    public void onRenderLevelStage(@Nonnull RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        if (!ClientConfig.CONFIG.customSkyRenderer.get()) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        // Only render in overworld (or dimensions with a sky)
        if (!level.dimensionType().hasSkyLight()) return;

        float nightBrightness = getNightBrightness(level);
        if (nightBrightness <= 0.01f) return;

        float weatherFactor = getWeatherFactor(level);
        if (weatherFactor <= 0.01f) return;

        float overallAlpha = nightBrightness * weatherFactor;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        renderConstellationLayer(poseStack, level, overallAlpha, partialTick);
    }

    // =========================================================================
    // Constellation sky layer
    // =========================================================================

    private void renderConstellationLayer(@Nonnull PoseStack poseStack,
                                           @Nonnull ClientLevel level,
                                           float alpha,
                                           float partialTick) {
        PlayerProgress progress = ResearchHelper.getClientProgress();
        List<IConstellation> allConstellations = ConstellationRegistry.getAllConstellations().stream()
                .filter(c -> progress.hasDiscovered(c.getRegistryName()))
                .collect(java.util.stream.Collectors.toList());
        if (allConstellations.isEmpty()) return;

        // Determine which constellations are visible based on time/day
        long dayTime = level.getDayTime() % 24000L;
        // ClientLevel does not expose a world seed; use dimension key hash for reproducible placement
        Random rng = new Random((long) level.dimension().location().hashCode() ^ PLACEMENT_SEED ^ (dayTime / 24000L));

        poseStack.pushPose();

        // Rotate sky dome with time
        float rotation = getStarRotation(level, partialTick);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        ShaderInstance shader = GameRenderer.getPositionColorTexShader();
        if (shader != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        }

        int visibleCount = Math.min(allConstellations.size(), MAX_VISIBLE_CONSTELLATIONS);
        for (int i = 0; i < visibleCount; i++) {
            IConstellation constellation = allConstellations.get(
                    rng.nextInt(allConstellations.size()));

            // Random placement on sky dome
            float azimuth = rng.nextFloat() * 360.0f;
            float elevation = 20.0f + rng.nextFloat() * 50.0f; // 20° to 70° above horizon

            float constellationScale = 3.0f + rng.nextFloat() * 2.0f;
            float constellationAlpha = alpha * (0.5f + rng.nextFloat() * 0.5f);

            renderSkyConstellation(poseStack, constellation,
                    azimuth, elevation, SKY_DISTANCE,
                    constellationScale, constellationAlpha,
                    Minecraft.getInstance().gui.getGuiTicks(), partialTick);
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        poseStack.popPose();
    }

    /**
     * Render a single constellation at a sky dome position.
     */
    private void renderSkyConstellation(@Nonnull PoseStack poseStack,
                                         @Nonnull IConstellation constellation,
                                         float azimuth, float elevation,
                                         float distance,
                                         float scale, float alpha,
                                         int tickCount, float partialTick) {
        poseStack.pushPose();

        // Position on sky dome
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(azimuth));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(elevation));
        poseStack.translate(0, 0, -distance);

        // Render using immediate mode since we're in world rendering context
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // Render stars
        RenderSystem.setShaderTexture(0, RenderTypesAS.TEX_CONSTELLATION_STAR);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        Color starColor = Color.WHITE;
        float r = starColor.getRed() / 255f;
        float g = starColor.getGreen() / 255f;
        float b = starColor.getBlue() / 255f;

        for (int i = 0; i < constellation.getStars().size(); i++) {
            var star = constellation.getStars().get(i);
            float sx = (star.x - 15) * scale;
            float sy = (star.y - 15) * scale;
            float starSize = 2.0f * scale;
            float starAlpha = alpha * RenderingConstellationUtils.getTwinkleBrightness(
                    i, tickCount, partialTick);
            float half = starSize / 2.0f;

            bufferBuilder.vertex(matrix, sx - half, sy + half, 0)
                    .color(r, g, b, starAlpha).uv(0, 1).endVertex();
            bufferBuilder.vertex(matrix, sx + half, sy + half, 0)
                    .color(r, g, b, starAlpha).uv(1, 1).endVertex();
            bufferBuilder.vertex(matrix, sx + half, sy - half, 0)
                    .color(r, g, b, starAlpha).uv(1, 0).endVertex();
            bufferBuilder.vertex(matrix, sx - half, sy - half, 0)
                    .color(r, g, b, starAlpha).uv(0, 0).endVertex();
        }

        tesselator.end();

        // Render connections
        RenderSystem.setShaderTexture(0, RenderTypesAS.TEX_CONSTELLATION_CONNECTION);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        float connAlpha = alpha * 0.4f;
        for (var conn : constellation.getStarConnections()) {
            float x1 = (conn.from.x - 15) * scale;
            float y1 = (conn.from.y - 15) * scale;
            float x2 = (conn.to.x - 15) * scale;
            float y2 = (conn.to.y - 15) * scale;

            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len < 0.001f) continue;
            float nx = -dy / len * scale * 0.3f;
            float ny = dx / len * scale * 0.3f;

            bufferBuilder.vertex(matrix, x1 - nx, y1 - ny, 0)
                    .color(r, g, b, connAlpha).uv(0, 0).endVertex();
            bufferBuilder.vertex(matrix, x2 - nx, y2 - ny, 0)
                    .color(r, g, b, connAlpha).uv(1, 0).endVertex();
            bufferBuilder.vertex(matrix, x2 + nx, y2 + ny, 0)
                    .color(r, g, b, connAlpha).uv(1, 1).endVertex();
            bufferBuilder.vertex(matrix, x1 + nx, y1 + ny, 0)
                    .color(r, g, b, connAlpha).uv(0, 1).endVertex();
        }

        tesselator.end();

        poseStack.popPose();
    }

    // =========================================================================
    // Sky condition helpers
    // =========================================================================

    /**
     * Calculate night brightness factor [0, 1].
     * 0 = full day, 1 = full night.
     */
    private float getNightBrightness(@Nonnull ClientLevel level) {
        float celestialAngle = level.getTimeOfDay(Minecraft.getInstance().getFrameTime());
        // celestialAngle: 0.0 = noon, 0.5 = midnight
        // We want brightness to peak at midnight and be zero during day
        float nightFactor;
        if (celestialAngle >= 0.25f && celestialAngle <= 0.75f) {
            // Night time: 0.25 (sunset) to 0.75 (sunrise)
            if (celestialAngle <= 0.30f) {
                // Fade in (sunset)
                nightFactor = (celestialAngle - 0.25f) / 0.05f;
            } else if (celestialAngle >= 0.70f) {
                // Fade out (sunrise)
                nightFactor = (0.75f - celestialAngle) / 0.05f;
            } else {
                nightFactor = 1.0f;
            }
        } else {
            nightFactor = 0.0f;
        }
        return Math.max(0, Math.min(1, nightFactor));
    }

    /**
     * Calculate weather visibility factor [0, 1].
     * 1 = clear, 0 = heavy storm.
     */
    private float getWeatherFactor(@Nonnull ClientLevel level) {
        float rain = level.getRainLevel(Minecraft.getInstance().getFrameTime());
        float thunder = level.getThunderLevel(Minecraft.getInstance().getFrameTime());
        return Math.max(0, 1.0f - rain * 0.6f - thunder * 0.4f);
    }

    /**
     * Calculate star field rotation angle based on time.
     */
    private float getStarRotation(@Nonnull ClientLevel level, float partialTick) {
        float celestialAngle = level.getTimeOfDay(partialTick);
        return celestialAngle * 360.0f;
    }
}
