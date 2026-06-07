/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Renders the AS crystal model used by collector crystals, celestial crystals,
 * and rock crystal items.
 *
 * <p>In 1.16, AS used a custom OBJ model loaded via {@code ObjModelRender} with
 * raw VertexBuffer manipulation. In 1.20, Forge's model loader can handle OBJ
 * files directly for item/block models, but for TESR crystal rendering we use
 * a procedurally generated crystal shape built from hardcoded geometry.</p>
 *
 * <p>The crystal is a faceted hexagonal prism with tapered ends — simple enough
 * to define procedurally without loading a .obj file. This avoids the complexity
 * of the old VertexBuffer API rewrite while producing the same visual result.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class CrystalModelRenderer {

    private CrystalModelRenderer() {}

    /** Number of sides in the crystal cross-section. */
    private static final int SIDES = 6;

    /** Crystal geometry proportions. */
    private static final float BODY_HEIGHT = 0.6f;
    private static final float TIP_HEIGHT = 0.2f;
    private static final float RADIUS = 0.15f;

    // Precomputed vertices
    private static final float[][] RING_X = new float[SIDES][];
    private static final float[][] RING_Z = new float[SIDES][];

    static {
        for (int i = 0; i < SIDES; i++) {
            float angle = (float) (i * Math.PI * 2.0 / SIDES);
            RING_X[i] = new float[]{(float) Math.cos(angle) * RADIUS};
            RING_Z[i] = new float[]{(float) Math.sin(angle) * RADIUS};
        }
    }

    /**
     * Render a crystal at the current pose position.
     *
     * @param poseStack    current transforms
     * @param bufferSource buffer for render types
     * @param color        crystal tint color
     * @param alpha        crystal transparency [0, 1]
     * @param rotationDeg  Y-axis rotation in degrees
     * @param scale        overall scale factor
     * @param packedLight  packed lightmap coords
     */
    public static void renderCrystal(@Nonnull PoseStack poseStack,
                                      @Nonnull MultiBufferSource bufferSource,
                                      @Nonnull Color color,
                                      float alpha,
                                      float rotationDeg,
                                      float scale,
                                      int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDeg));
        poseStack.scale(scale, scale, scale);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float bodyBottom = -BODY_HEIGHT / 2.0f;
        float bodyTop = BODY_HEIGHT / 2.0f;
        float tipBottom = bodyBottom - TIP_HEIGHT;
        float tipTop = bodyTop + TIP_HEIGHT;

        // Render body (hexagonal prism sides)
        for (int i = 0; i < SIDES; i++) {
            int next = (i + 1) % SIDES;
            float x1 = RING_X[i][0];
            float z1 = RING_Z[i][0];
            float x2 = RING_X[next][0];
            float z2 = RING_Z[next][0];

            float u1 = (float) i / SIDES;
            float u2 = (float) (i + 1) / SIDES;
            RenderingUtils.vertex(buffer, matrix, x1, bodyBottom, z1, r, g, b, alpha, u1, 1,    packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, bodyBottom, z2, r, g, b, alpha, u2, 1,    packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, bodyTop,    z2, r, g, b, alpha, u2, 0.3f, packedLight);
            RenderingUtils.vertex(buffer, matrix, x1, bodyTop,    z1, r, g, b, alpha, u1, 0.3f, packedLight);
        }

        // Top tip (degenerate quad)
        for (int i = 0; i < SIDES; i++) {
            int next = (i + 1) % SIDES;
            float x1 = RING_X[i][0];
            float z1 = RING_Z[i][0];
            float x2 = RING_X[next][0];
            float z2 = RING_Z[next][0];

            RenderingUtils.vertex(buffer, matrix, x1, bodyTop, z1, r, g, b, alpha, 0,    0.3f, packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, bodyTop, z2, r, g, b, alpha, 1,    0.3f, packedLight);
            RenderingUtils.vertex(buffer, matrix, 0,  tipTop,  0,  r, g, b, alpha, 0.5f, 0,    packedLight);
            RenderingUtils.vertex(buffer, matrix, 0,  tipTop,  0,  r, g, b, alpha, 0.5f, 0,    packedLight);
        }

        // Bottom tip (degenerate quad)
        for (int i = 0; i < SIDES; i++) {
            int next = (i + 1) % SIDES;
            float x1 = RING_X[i][0];
            float z1 = RING_Z[i][0];
            float x2 = RING_X[next][0];
            float z2 = RING_Z[next][0];

            RenderingUtils.vertex(buffer, matrix, 0,  tipBottom,  0,  r, g, b, alpha, 0.5f, 1,    packedLight);
            RenderingUtils.vertex(buffer, matrix, 0,  tipBottom,  0,  r, g, b, alpha, 0.5f, 1,    packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, bodyBottom, z2, r, g, b, alpha, 1,    0.7f, packedLight);
            RenderingUtils.vertex(buffer, matrix, x1, bodyBottom, z1, r, g, b, alpha, 0,    0.7f, packedLight);
        }

        poseStack.popPose();
    }

    /**
     * Render a standard blue-white collector crystal.
     */
    public static void renderCollectorCrystal(@Nonnull PoseStack poseStack,
                                               @Nonnull MultiBufferSource bufferSource,
                                               float rotationDeg, int packedLight) {
        renderCrystal(poseStack, bufferSource,
                new Color(160, 200, 255), 0.85f,
                rotationDeg, 1.0f, packedLight);
    }

    /**
     * Render a golden celestial collector crystal.
     */
    public static void renderCelestialCrystal(@Nonnull PoseStack poseStack,
                                               @Nonnull MultiBufferSource bufferSource,
                                               float rotationDeg, int packedLight) {
        renderCrystal(poseStack, bufferSource,
                new Color(255, 230, 120), 0.9f,
                rotationDeg, 1.2f, packedLight);
    }
}
