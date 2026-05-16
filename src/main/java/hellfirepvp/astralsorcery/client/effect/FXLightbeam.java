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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Light beam effect: a camera-facing strip from source to target position.
 * Used for starlight network transmission visualization between nodes.
 */
@OnlyIn(Dist.CLIENT)
public class FXLightbeam extends EntityVisualFX {

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private float beamWidth = 0.15f;

    /**
     * @param fromX source X
     * @param fromY source Y
     * @param fromZ source Z
     * @param toX   target X
     * @param toY   target Y
     * @param toZ   target Z
     */
    public FXLightbeam(double fromX, double fromY, double fromZ,
                        double toX, double toY, double toZ) {
        super(fromX, fromY, fromZ);
        this.targetX = toX;
        this.targetY = toY;
        this.targetZ = toZ;
    }

    @Nonnull
    public FXLightbeam setBeamWidth(float width) {
        this.beamWidth = width;
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
        float fx = (float) getInterpolatedX(partialTick);
        float fy = (float) getInterpolatedY(partialTick);
        float fz = (float) getInterpolatedZ(partialTick);

        Vec3 from = new Vec3(fx, fy, fz);
        Vec3 to = new Vec3(targetX, targetY, targetZ);

        float alpha = getAlpha();
        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float width = beamWidth * getScaleX();

        RenderingUtils.renderLightBeam(buffer, poseStack, from, to, width, r, g, b, alpha);
    }
}
