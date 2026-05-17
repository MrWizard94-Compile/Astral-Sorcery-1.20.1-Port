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

/**
 * Draws a constellation line (thin glowing segment between two star positions).
 * Used in the sky renderer and journal UI to connect constellation stars.
 *
 * <p>The line is rendered as a thin quad strip between the start and end points,
 * using the EFFECT_FX_LIGHTBEAM render type for additive glow. Lines are
 * persistent by default (maxAge = Integer.MAX_VALUE) and must be manually removed.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXConstellationLine extends EntityVisualFX {

    private final double endX;
    private final double endY;
    private final double endZ;
    private float lineWidth = 0.02f;

    /**
     * @param x  start X
     * @param y  start Y
     * @param z  start Z
     * @param x2 end X
     * @param y2 end Y
     * @param z2 end Z
     */
    public FXConstellationLine(double x, double y, double z,
                                double x2, double y2, double z2) {
        super(x, y, z);
        this.endX = x2;
        this.endY = y2;
        this.endZ = z2;
        setMaxAge(Integer.MAX_VALUE); // Persistent until removed
    }

    @Nonnull
    public FXConstellationLine setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        return this;
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
        float x1 = (float) getInterpolatedX(partialTick);
        float y1 = (float) getInterpolatedY(partialTick);
        float z1 = (float) getInterpolatedZ(partialTick);

        float x2 = (float) endX;
        float y2 = (float) endY;
        float z2 = (float) endZ;

        float alpha = getAlpha();
        if (alpha <= 0.01f) return;

        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float halfWidth = lineWidth * getScaleX() * 0.5f;

        Matrix4f matrix = poseStack.last().pose();

        // Render as a thin quad strip between the two points
        // Horizontal quad (width in X axis)
        RenderingUtils.vertex(buffer, matrix, x1 - halfWidth, y1, z1, r, g, b, alpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x1 + halfWidth, y1, z1, r, g, b, alpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x2 + halfWidth, y2, z2, r, g, b, alpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x2 - halfWidth, y2, z2, r, g, b, alpha, 0, 1);

        // Vertical quad (width in Y axis) for cross-section volume
        RenderingUtils.vertex(buffer, matrix, x1, y1 - halfWidth, z1, r, g, b, alpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x1, y1 + halfWidth, z1, r, g, b, alpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x2, y2 + halfWidth, z2, r, g, b, alpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x2, y2 - halfWidth, z2, r, g, b, alpha, 0, 1);
    }
}
