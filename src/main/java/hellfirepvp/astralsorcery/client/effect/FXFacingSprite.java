/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * A camera-facing textured sprite effect. Used for:
 * <ul>
 *   <li>Constellation star points (bright dots in the sky)</li>
 *   <li>Hovering icons over altars/pedestals</li>
 *   <li>Gateway portal particles</li>
 *   <li>Ritual pedestal effect indicators</li>
 * </ul>
 *
 * <p>Unlike FXSparkle which always fades out, FXFacingSprite supports
 * persistent rendering (infinite lifetime) for static UI-like indicators.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXFacingSprite extends EntityVisualFX {

    private float uMin = 0;
    private float vMin = 0;
    private float uMax = 1;
    private float vMax = 1;

    public FXFacingSprite(double x, double y, double z) {
        super(x, y, z);
        setMaxAge(Integer.MAX_VALUE); // Persistent until removed
    }

    /**
     * Sets the UV coordinates for texture atlas sampling.
     */
    @Nonnull
    public FXFacingSprite setUV(float uMin, float vMin, float uMax, float vMax) {
        this.uMin = uMin;
        this.vMin = vMin;
        this.uMax = uMax;
        this.vMax = vMax;
        return this;
    }

    @Nonnull
    @Override
    public RenderType getRenderType() {
        return RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE;
    }

    @Override
    public void renderEffect(@Nonnull PoseStack poseStack,
                              @Nonnull VertexConsumer buffer,
                              float partialTick) {
        float effectiveAlpha = getAlpha();
        if (effectiveAlpha <= 0.01f) return;

        Vec3 pos = getInterpolatedPosition(partialTick);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        double relX = pos.x - cameraPos.x;
        double relY = pos.y - cameraPos.y;
        double relZ = pos.z - cameraPos.z;

        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);
        poseStack.mulPose(camera.rotation());

        Matrix4f matrix = poseStack.last().pose();

        float r = getColor().getRed() / 255.0f;
        float g = getColor().getGreen() / 255.0f;
        float b = getColor().getBlue() / 255.0f;
        int light = 0xF000F0;
        int l1 = light >> 16 & 0xFFFF;
        int l2 = light & 0xFFFF;

        float half = getScaleX();

        buffer.vertex(matrix, -half, -half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(uMin, vMax)
                .uv2(l1, l2)
                .endVertex();
        buffer.vertex(matrix, half, -half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(uMax, vMax)
                .uv2(l1, l2)
                .endVertex();
        buffer.vertex(matrix, half, half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(uMax, vMin)
                .uv2(l1, l2)
                .endVertex();
        buffer.vertex(matrix, -half, half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(uMin, vMin)
                .uv2(l1, l2)
                .endVertex();

        poseStack.popPose();
    }
}
