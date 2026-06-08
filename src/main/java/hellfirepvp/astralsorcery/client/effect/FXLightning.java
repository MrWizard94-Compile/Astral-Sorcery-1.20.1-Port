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
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Random;

/**
 * Lightning bolt effect: a segmented jagged line between two points.
 * Used for attunement effects, ritual pedestals, collector crystal
 * charging, and constellation activation.
 *
 * <p>The bolt is generated procedurally with random displacement at
 * each segment. Segments are rendered as thin quads facing the camera.
 * The bolt can optionally regenerate its shape each tick for a
 * flickering appearance.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXLightning extends EntityVisualFX {

    private static final Random RAND = new Random();

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int segmentCount;
    private final float displacement;
    private final boolean regenerate;
    private float boltWidth = 0.02f;

    // Cached segment offsets (perpendicular displacements)
    private float[] offsetsX;
    private float[] offsetsY;
    private float[] offsetsZ;

    /**
     * @param fromX        source X
     * @param fromY        source Y
     * @param fromZ        source Z
     * @param toX          target X
     * @param toY          target Y
     * @param toZ          target Z
     * @param segmentCount number of bolt segments (4-12 typical)
     * @param displacement max perpendicular displacement per segment
     * @param regenerate   if true, re-randomize shape each tick (flickering)
     */
    public FXLightning(double fromX, double fromY, double fromZ,
                        double toX, double toY, double toZ,
                        int segmentCount, float displacement, boolean regenerate) {
        super(fromX, fromY, fromZ);
        this.targetX = toX;
        this.targetY = toY;
        this.targetZ = toZ;
        this.segmentCount = Math.max(2, segmentCount);
        this.displacement = displacement;
        this.regenerate = regenerate;
        generateOffsets();
        setMaxAge(8);
    }

    public FXLightning(double fromX, double fromY, double fromZ,
                        double toX, double toY, double toZ) {
        this(fromX, fromY, fromZ, toX, toY, toZ, 6, 0.15f, true);
    }

    @Nonnull
    public FXLightning setBoltWidth(float width) {
        this.boltWidth = width;
        return this;
    }

    private void generateOffsets() {
        offsetsX = new float[segmentCount + 1];
        offsetsY = new float[segmentCount + 1];
        offsetsZ = new float[segmentCount + 1];

        // Endpoints are fixed (no displacement)
        offsetsX[0] = offsetsY[0] = offsetsZ[0] = 0;
        offsetsX[segmentCount] = offsetsY[segmentCount] = offsetsZ[segmentCount] = 0;

        // Midpoints get random displacement
        for (int i = 1; i < segmentCount; i++) {
            // Displacement magnitude decreases toward endpoints for cleaner look
            float distFromEnd = Math.min(i, segmentCount - i) / ((float) segmentCount / 2);
            float maxDisp = displacement * distFromEnd;
            offsetsX[i] = (RAND.nextFloat() - 0.5f) * 2 * maxDisp;
            offsetsY[i] = (RAND.nextFloat() - 0.5f) * 2 * maxDisp;
            offsetsZ[i] = (RAND.nextFloat() - 0.5f) * 2 * maxDisp;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (regenerate && !isDead()) {
            generateOffsets();
        }
    }

    @Override
    @Nonnull
    public RenderType getRenderType() {
        return RenderTypesAS.EFFECT_FX_LIGHTBEAM;
    }

    @Override
    public void renderEffect(@Nonnull PoseStack poseStack,
                              @Nonnull VertexConsumer buffer,
                              float partialTick) {
        float srcX = (float) getInterpolatedX(partialTick);
        float srcY = (float) getInterpolatedY(partialTick);
        float srcZ = (float) getInterpolatedZ(partialTick);

        float dx = (float) (targetX - getPosX());
        float dy = (float) (targetY - getPosY());
        float dz = (float) (targetZ - getPosZ());

        float alpha = getAlpha() * (1.0f - getAgeProgress());
        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        if (alpha <= 0.01f) return;

        Matrix4f matrix = poseStack.last().pose();
        float halfWidth = boltWidth * getScaleX() * 0.5f;

        for (int i = 0; i < segmentCount; i++) {
            float t0 = (float) i / segmentCount;
            float t1 = (float) (i + 1) / segmentCount;

            // Segment start/end along the main line, plus displacement
            float x0 = srcX + dx * t0 + offsetsX[i];
            float y0 = srcY + dy * t0 + offsetsY[i];
            float z0 = srcZ + dz * t0 + offsetsZ[i];
            float x1 = srcX + dx * t1 + offsetsX[i + 1];
            float y1 = srcY + dy * t1 + offsetsY[i + 1];
            float z1 = srcZ + dz * t1 + offsetsZ[i + 1];

            // Render segment as a thin quad (2 triangles via 4 vertices)
            // Use Y-offset for width since lightning is typically vertical
            RenderingUtils.vertex(buffer, matrix, x0 - halfWidth, y0, z0, r, g, b, alpha, 0, 0);
            RenderingUtils.vertex(buffer, matrix, x0 + halfWidth, y0, z0, r, g, b, alpha, 1, 0);
            RenderingUtils.vertex(buffer, matrix, x1 + halfWidth, y1, z1, r, g, b, alpha, 1, 1);
            RenderingUtils.vertex(buffer, matrix, x1 - halfWidth, y1, z1, r, g, b, alpha, 0, 1);

            // Cross-quad for volume (perpendicular)
            RenderingUtils.vertex(buffer, matrix, x0, y0, z0 - halfWidth, r, g, b, alpha, 0, 0);
            RenderingUtils.vertex(buffer, matrix, x0, y0, z0 + halfWidth, r, g, b, alpha, 1, 0);
            RenderingUtils.vertex(buffer, matrix, x1, y1, z1 + halfWidth, r, g, b, alpha, 1, 1);
            RenderingUtils.vertex(buffer, matrix, x1, y1, z1 - halfWidth, r, g, b, alpha, 0, 1);
        }
    }
}
