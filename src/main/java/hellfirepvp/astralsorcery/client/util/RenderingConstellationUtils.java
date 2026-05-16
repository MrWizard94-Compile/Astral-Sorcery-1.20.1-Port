/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * Rendering utilities for drawing constellations (stars + connection lines).
 * Used by the sky renderer, journal GUI, perk tree, and telescope screen.
 *
 * <p>Constellations are defined as a set of {@link StarLocation} points
 * connected by {@link StarConnection} edges. This class renders them
 * as textured quads (stars) and thin quads (connections) with optional
 * twinkle animation.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class RenderingConstellationUtils {

    private RenderingConstellationUtils() {}

    /** Default star size in screen coordinates. */
    private static final float DEFAULT_STAR_SIZE = 5.0f;

    /** Default connection line width in screen coordinates. */
    private static final float DEFAULT_LINE_WIDTH = 1.5f;

    // =========================================================================
    // Full constellation rendering
    // =========================================================================

    /**
     * Render a complete constellation (stars + connections) in 2D screen space.
     *
     * @param bufferSource the buffer source for obtaining vertex consumers
     * @param poseStack    current pose stack
     * @param constellation the constellation to render
     * @param offsetX      X offset for positioning
     * @param offsetY      Y offset for positioning
     * @param scale        scale factor for the constellation grid (31x31 → screen)
     * @param brightness   overall brightness multiplier [0, 1]
     * @param color        the constellation color
     */
    public static void renderConstellation(@Nonnull MultiBufferSource bufferSource,
                                            @Nonnull PoseStack poseStack,
                                            @Nonnull IConstellation constellation,
                                            float offsetX, float offsetY,
                                            float scale,
                                            float brightness,
                                            @Nonnull Color color) {
        renderConstellationConnections(bufferSource, poseStack, constellation,
                offsetX, offsetY, scale, brightness * 0.6f, color);
        renderConstellationStars(bufferSource, poseStack, constellation,
                offsetX, offsetY, scale, brightness, color);
    }

    /**
     * Render constellation with default white color.
     */
    public static void renderConstellation(@Nonnull MultiBufferSource bufferSource,
                                            @Nonnull PoseStack poseStack,
                                            @Nonnull IConstellation constellation,
                                            float offsetX, float offsetY,
                                            float scale,
                                            float brightness) {
        renderConstellation(bufferSource, poseStack, constellation,
                offsetX, offsetY, scale, brightness, Color.WHITE);
    }

    // =========================================================================
    // Star rendering
    // =========================================================================

    /**
     * Render only the star points of a constellation.
     */
    public static void renderConstellationStars(@Nonnull MultiBufferSource bufferSource,
                                                 @Nonnull PoseStack poseStack,
                                                 @Nonnull IConstellation constellation,
                                                 float offsetX, float offsetY,
                                                 float scale, float brightness,
                                                 @Nonnull Color color) {
        List<StarLocation> stars = constellation.getStars();
        if (stars.isEmpty()) return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.CONSTELLATION_STAR);
        Matrix4f matrix = poseStack.last().pose();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        for (StarLocation star : stars) {
            float sx = offsetX + star.x * scale;
            float sy = offsetY + star.y * scale;
            float starSize = DEFAULT_STAR_SIZE * (scale / 10.0f);
            float alpha = brightness;

            renderStar(buffer, matrix, sx, sy, starSize, r, g, b, alpha);
        }
    }

    /**
     * Render a single star quad centered at (x, y).
     */
    public static void renderStar(@Nonnull VertexConsumer buffer,
                                   @Nonnull Matrix4f matrix,
                                   float x, float y, float size,
                                   float r, float g, float b, float a) {
        float half = size / 2.0f;
        RenderingUtils.vertex(buffer, matrix, x - half, y + half, 0, r, g, b, a, 0, 1);
        RenderingUtils.vertex(buffer, matrix, x + half, y + half, 0, r, g, b, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x + half, y - half, 0, r, g, b, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x - half, y - half, 0, r, g, b, a, 0, 0);
    }

    // =========================================================================
    // Connection line rendering
    // =========================================================================

    /**
     * Render only the connection lines of a constellation.
     */
    public static void renderConstellationConnections(@Nonnull MultiBufferSource bufferSource,
                                                       @Nonnull PoseStack poseStack,
                                                       @Nonnull IConstellation constellation,
                                                       float offsetX, float offsetY,
                                                       float scale, float brightness,
                                                       @Nonnull Color color) {
        List<StarConnection> connections = constellation.getStarConnections();
        if (connections.isEmpty()) return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.CONSTELLATION_CONNECTION);
        Matrix4f matrix = poseStack.last().pose();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        for (StarConnection conn : connections) {
            float x1 = offsetX + conn.from.x * scale;
            float y1 = offsetY + conn.from.y * scale;
            float x2 = offsetX + conn.to.x * scale;
            float y2 = offsetY + conn.to.y * scale;

            renderConnectionLine(buffer, matrix, x1, y1, x2, y2,
                    DEFAULT_LINE_WIDTH * (scale / 10.0f),
                    r, g, b, brightness);
        }
    }

    /**
     * Render a single connection line as a thin textured quad.
     */
    public static void renderConnectionLine(@Nonnull VertexConsumer buffer,
                                             @Nonnull Matrix4f matrix,
                                             float x1, float y1,
                                             float x2, float y2,
                                             float width,
                                             float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001f) return;

        // Perpendicular direction for line width
        float nx = -dy / length * width;
        float ny = dx / length * width;

        RenderingUtils.vertex(buffer, matrix, x1 - nx, y1 - ny, 0, r, g, b, a, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x2 - nx, y2 - ny, 0, r, g, b, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x2 + nx, y2 + ny, 0, r, g, b, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x1 + nx, y1 + ny, 0, r, g, b, a, 0, 1);
    }

    // =========================================================================
    // Twinkle animation
    // =========================================================================

    /**
     * Calculate a twinkle brightness multiplier for a star.
     * Uses a sine wave with per-star phase offset for variation.
     *
     * @param starIndex index of the star (for phase offset)
     * @param tickCount current tick count (client ticks)
     * @param partialTick partial tick for smooth animation
     * @return brightness multiplier [0.3, 1.0]
     */
    public static float getTwinkleBrightness(int starIndex, int tickCount, float partialTick) {
        // Each star gets a pseudo-random phase based on its index
        Random phaseRng = new Random(starIndex * 31L + 7);
        float phase = phaseRng.nextFloat() * (float) Math.PI * 2.0f;
        float speed = 0.03f + phaseRng.nextFloat() * 0.04f;
        float time = (tickCount + partialTick) * speed + phase;
        // Map sine [-1, 1] to [0.3, 1.0]
        return 0.65f + 0.35f * (float) Math.sin(time);
    }

    /**
     * Render a constellation with twinkling stars.
     */
    public static void renderConstellationTwinkling(@Nonnull MultiBufferSource bufferSource,
                                                     @Nonnull PoseStack poseStack,
                                                     @Nonnull IConstellation constellation,
                                                     float offsetX, float offsetY,
                                                     float scale, float baseBrightness,
                                                     @Nonnull Color color,
                                                     int tickCount, float partialTick) {
        // Connections at constant brightness
        renderConstellationConnections(bufferSource, poseStack, constellation,
                offsetX, offsetY, scale, baseBrightness * 0.5f, color);

        // Stars with individual twinkle
        List<StarLocation> stars = constellation.getStars();
        if (stars.isEmpty()) return;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.CONSTELLATION_STAR);
        Matrix4f matrix = poseStack.last().pose();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        for (int i = 0; i < stars.size(); i++) {
            StarLocation star = stars.get(i);
            float sx = offsetX + star.x * scale;
            float sy = offsetY + star.y * scale;
            float starSize = DEFAULT_STAR_SIZE * (scale / 10.0f);
            float alpha = baseBrightness * getTwinkleBrightness(i, tickCount, partialTick);

            renderStar(buffer, matrix, sx, sy, starSize, r, g, b, alpha);
        }
    }
}
