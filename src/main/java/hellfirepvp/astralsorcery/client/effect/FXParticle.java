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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Standard billboarded particle effect. Renders as a camera-facing
 * textured quad with color and alpha modulation.
 *
 * <p>This is the most common effect type in AS — used for starlight
 * sparkles, altar particles, perk allocation effects, etc.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXParticle extends EntityVisualFX {

    private final boolean useDepth;

    /**
     * @param x        world X
     * @param y        world Y
     * @param z        world Z
     * @param useDepth if true, particle is occluded by world geometry
     */
    public FXParticle(double x, double y, double z, boolean useDepth) {
        super(x, y, z);
        this.useDepth = useDepth;
    }

    public FXParticle(double x, double y, double z) {
        this(x, y, z, false);
    }

    @Override
    @Nonnull
    public RenderType getRenderType() {
        return useDepth
                ? RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE_DEPTH
                : RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE;
    }

    @Override
    public void renderEffect(@Nonnull PoseStack poseStack,
                              @Nonnull VertexConsumer buffer,
                              float partialTick) {
        float x = (float) getInterpolatedX(partialTick);
        float y = (float) getInterpolatedY(partialTick);
        float z = (float) getInterpolatedZ(partialTick);

        float scale = getScaleX();
        float alpha = getAlpha();
        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        RenderingUtils.renderFacingQuad(buffer, poseStack, x, y, z, scale, r, g, b, alpha);
    }
}
