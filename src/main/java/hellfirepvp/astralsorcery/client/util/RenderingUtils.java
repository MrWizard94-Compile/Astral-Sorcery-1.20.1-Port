/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.function.Consumer;

/**
 * Core rendering utilities: vertex submission helpers, coordinate transforms,
 * color handling, and commonly used render operations.
 *
 * <p>1.16 → 1.20 changes:
 * {@code MatrixStack} → {@code PoseStack}.
 * {@code IVertexBuilder} → {@code VertexConsumer}.
 * {@code IRenderTypeBuffer} → {@code MultiBufferSource}.
 * {@code Matrix4f} moved to JOML ({@code org.joml.Matrix4f}).
 * {@code Vector3f} → {@code org.joml.Vector3f}.
 * {@code RenderSystem.pushMatrix()} / {@code popMatrix()} removed — use PoseStack.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class RenderingUtils {

    private RenderingUtils() {}

    // =========================================================================
    // Vertex submission helpers
    // =========================================================================

    /**
     * Submit a position+color vertex.
     */
    public static void vertex(@Nonnull VertexConsumer buffer,
                               @Nonnull Matrix4f matrix,
                               float x, float y, float z,
                               float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

    /**
     * Submit a position+color+texture vertex.
     */
    public static void vertex(@Nonnull VertexConsumer buffer,
                               @Nonnull Matrix4f matrix,
                               float x, float y, float z,
                               float r, float g, float b, float a,
                               float u, float v) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).uv(u, v).endVertex();
    }

    /**
     * Submit a position+color+texture+lightmap vertex.
     */
    public static void vertex(@Nonnull VertexConsumer buffer,
                               @Nonnull Matrix4f matrix,
                               float x, float y, float z,
                               float r, float g, float b, float a,
                               float u, float v,
                               int packedLight) {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .uv2(packedLight)
                .endVertex();
    }

    /**
     * Submit a full BLOCK format vertex (position, color, uv, uv2/light, normal).
     */
    public static void vertexBlock(@Nonnull VertexConsumer buffer,
                                    @Nonnull Matrix4f matrix,
                                    float x, float y, float z,
                                    float r, float g, float b, float a,
                                    float u, float v,
                                    int overlay, int packedLight,
                                    float nx, float ny, float nz) {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(packedLight)
                .normal(nx, ny, nz)
                .endVertex();
    }

    // =========================================================================
    // Textured quad helpers
    // =========================================================================

    /**
     * Render a camera-facing (billboarded) textured quad at a world position.
     *
     * @param buffer     vertex consumer for the render type
     * @param poseStack  current pose stack (should include camera transform)
     * @param x          center X
     * @param y          center Y
     * @param z          center Z
     * @param scale      half-size of the quad
     * @param r          red 0-1
     * @param g          green 0-1
     * @param b          blue 0-1
     * @param a          alpha 0-1
     */
    public static void renderFacingQuad(@Nonnull VertexConsumer buffer,
                                         @Nonnull PoseStack poseStack,
                                         float x, float y, float z,
                                         float scale,
                                         float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Billboard: face camera
        Minecraft mc = Minecraft.getInstance();
        float yaw = -mc.gameRenderer.getMainCamera().getYRot();
        float pitch = mc.gameRenderer.getMainCamera().getXRot();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch));

        Matrix4f mat = poseStack.last().pose();
        vertex(buffer, mat, -scale, -scale, 0, r, g, b, a, 0, 1);
        vertex(buffer, mat,  scale, -scale, 0, r, g, b, a, 1, 1);
        vertex(buffer, mat,  scale,  scale, 0, r, g, b, a, 1, 0);
        vertex(buffer, mat, -scale,  scale, 0, r, g, b, a, 0, 0);

        poseStack.popPose();
    }

    /**
     * Render a flat textured quad in the XZ plane (horizontal).
     */
    public static void renderHorizontalQuad(@Nonnull VertexConsumer buffer,
                                             @Nonnull Matrix4f matrix,
                                             float x, float z,
                                             float scale,
                                             float y,
                                             float r, float g, float b, float a) {
        vertex(buffer, matrix, x - scale, y, z - scale, r, g, b, a, 0, 0);
        vertex(buffer, matrix, x - scale, y, z + scale, r, g, b, a, 0, 1);
        vertex(buffer, matrix, x + scale, y, z + scale, r, g, b, a, 1, 1);
        vertex(buffer, matrix, x + scale, y, z - scale, r, g, b, a, 1, 0);
    }

    // =========================================================================
    // Light beam rendering
    // =========================================================================

    /**
     * Render a light beam between two world positions.
     * The beam is a camera-facing strip (two triangles per segment).
     *
     * @param buffer    vertex consumer (should use EFFECT_FX_LIGHTBEAM render type)
     * @param poseStack current pose stack
     * @param from      start position
     * @param to        end position
     * @param beamWidth half-width of the beam
     * @param r         red 0-1
     * @param g         green 0-1
     * @param b         blue 0-1
     * @param a         alpha 0-1
     */
    public static void renderLightBeam(@Nonnull VertexConsumer buffer,
                                        @Nonnull PoseStack poseStack,
                                        @Nonnull Vec3 from,
                                        @Nonnull Vec3 to,
                                        float beamWidth,
                                        float r, float g, float b, float a) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 beamDir = to.subtract(from).normalize();
        Vec3 cameraToBeam = from.subtract(cameraPos).normalize();
        Vec3 perpendicular = beamDir.cross(cameraToBeam).normalize().scale(beamWidth);

        Matrix4f mat = poseStack.last().pose();
        float fx = (float) from.x;
        float fy = (float) from.y;
        float fz = (float) from.z;
        float tx = (float) to.x;
        float ty = (float) to.y;
        float tz = (float) to.z;
        float px = (float) perpendicular.x;
        float py = (float) perpendicular.y;
        float pz = (float) perpendicular.z;

        vertex(buffer, mat, fx - px, fy - py, fz - pz, r, g, b, a, 0, 0);
        vertex(buffer, mat, tx - px, ty - py, tz - pz, r, g, b, a, 0, 1);
        vertex(buffer, mat, tx + px, ty + py, tz + pz, r, g, b, a, 1, 1);
        vertex(buffer, mat, fx + px, fy + py, fz + pz, r, g, b, a, 1, 0);
    }

    // =========================================================================
    // Color utilities
    // =========================================================================

    /**
     * Extract RGBA float components from a {@link Color}.
     *
     * @param color the color
     * @return float[4] {r, g, b, a} each in [0, 1]
     */
    @Nonnull
    public static float[] colorToFloats(@Nonnull Color color) {
        return new float[] {
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        };
    }

    /**
     * Linearly interpolate between two colors.
     *
     * @param from the start color
     * @param to   the end color
     * @param t    interpolation factor [0, 1]
     * @return the interpolated color
     */
    @Nonnull
    public static Color lerpColor(@Nonnull Color from, @Nonnull Color to, float t) {
        float clamped = Math.max(0, Math.min(1, t));
        int r = (int) (from.getRed()   + (to.getRed()   - from.getRed())   * clamped);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int b = (int) (from.getBlue()  + (to.getBlue()  - from.getBlue())  * clamped);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * clamped);
        return new Color(r, g, b, a);
    }

    // =========================================================================
    // Coordinate utilities
    // =========================================================================

    /**
     * Get the camera-relative position for rendering.
     * In 1.20, all world rendering is relative to the camera.
     *
     * @param worldPos the absolute world position
     * @return the camera-relative position
     */
    @Nonnull
    public static Vec3 getCameraRelative(@Nonnull Vec3 worldPos) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return worldPos.subtract(cam);
    }

    /**
     * Full-bright packed lightmap value (skyLight=15, blockLight=15).
     */
    public static final int FULL_BRIGHT_LIGHT = 0xF000F0;

    // =========================================================================
    // Immediate-mode quad drawing (journal / GUI)
    // =========================================================================

    /**
     * Draw a textured, colored quad using the currently bound texture.
     * Caller is responsible for blend state and shader setup.
     *
     * @param matrix  the pose matrix
     * @param x       left edge
     * @param y       top edge
     * @param w       width
     * @param h       height
     * @param r       red 0-1
     * @param g       green 0-1
     * @param b       blue 0-1
     * @param a       alpha 0-1
     */
    public static void drawColorTex(@Nonnull Matrix4f matrix,
                                     float x, float y, float w, float h,
                                     float r, float g, float b, float a) {
        drawColorTex(matrix, x, y, w, h, r, g, b, a, 0, 0, 1, 1);
    }

    @SuppressWarnings("null")
    public static void drawColorTex(@Nonnull Matrix4f matrix,
                                     float x, float y, float w, float h,
                                     float r, float g, float b, float a,
                                     float u0, float v0, float u1, float v1) {
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        buf.vertex(matrix, x,     y + h, 0).color(r, g, b, a).uv(u0, v1).endVertex();
        buf.vertex(matrix, x + w, y + h, 0).color(r, g, b, a).uv(u1, v1).endVertex();
        buf.vertex(matrix, x + w, y,     0).color(r, g, b, a).uv(u1, v0).endVertex();
        buf.vertex(matrix, x,     y,     0).color(r, g, b, a).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }

    /**
     * Draw a textured quad, binding the given texture and setting the position+color+tex shader.
     */
    @SuppressWarnings("null")
    public static void drawTexturedQuad(@Nonnull ResourceLocation texture,
                                         @Nonnull Matrix4f matrix,
                                         float x, float y, float w, float h,
                                         float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, texture);
        drawColorTex(matrix, x, y, w, h, r, g, b, a);
    }

    /**
     * Draw a colored (no texture) quad using the position+color shader.
     */
    @SuppressWarnings("null")
    public static void drawColorQuad(@Nonnull Matrix4f matrix,
                                      float x, float y, float w, float h,
                                      float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, x,     y + h, 0).color(r, g, b, a).endVertex();
        buf.vertex(matrix, x + w, y + h, 0).color(r, g, b, a).endVertex();
        buf.vertex(matrix, x + w, y,     0).color(r, g, b, a).endVertex();
        buf.vertex(matrix, x,     y,     0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }

    /**
     * Execute {@code filler} inside a QUADS + POSITION_COLOR_TEX buffer.
     * Sets the position+color+tex shader; caller must bind the texture first.
     */
    @SuppressWarnings("null")
    public static void drawTexQuads(@Nonnull Matrix4f matrix,
                                     @Nonnull Consumer<BufferBuilder> filler) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        filler.accept(buf);
        BufferUploader.drawWithShader(buf.end());
    }
}
