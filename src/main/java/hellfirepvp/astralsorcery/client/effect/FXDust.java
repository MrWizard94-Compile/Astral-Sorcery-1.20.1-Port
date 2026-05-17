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
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Gravity-affected falling dust particles (stardust, crystal fragments).
 * Used for stardust crafting residue, crystal grinding effects, and
 * ambient falling particles around celestial structures.
 *
 * <p>The dust particle falls under gravity (0.04 per tick), gently
 * shrinking and fading over its lifetime. Rendered as a small
 * camera-facing quad with full brightness.</p>
 *
 * <p>Default lifetime: 30-50 ticks. Default gravity: 0.04.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXDust extends EntityVisualFX {

    public FXDust(double x, double y, double z) {
        super(x, y, z);
        setGravity(0.04f);
        setMaxAge(30 + Minecraft.getInstance().level.random.nextInt(20));
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
        float progress = getAgeProgress();

        // Scale shrinks gently over lifetime
        float effectiveScale = getScaleX() * (1.0f - progress * 0.5f);
        // Alpha fades linearly
        float effectiveAlpha = getAlpha() * (1.0f - progress);

        if (effectiveAlpha <= 0.01f) return;

        Vec3 pos = getInterpolatedPosition(partialTick);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        double relX = pos.x - cameraPos.x;
        double relY = pos.y - cameraPos.y;
        double relZ = pos.z - cameraPos.z;

        poseStack.pushPose();
        poseStack.translate(relX, relY, relZ);

        // Billboard to face camera
        poseStack.mulPose(camera.rotation());

        Matrix4f matrix = poseStack.last().pose();

        float r = getColor().getRed() / 255.0f;
        float g = getColor().getGreen() / 255.0f;
        float b = getColor().getBlue() / 255.0f;
        int light = 0xF000F0; // Full brightness (self-luminous)
        int packedLight1 = light >> 16 & 0xFFFF;
        int packedLight2 = light & 0xFFFF;

        float half = effectiveScale;

        // Camera-facing quad
        buffer.vertex(matrix, -half, -half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(0, 1)
                .uv2(packedLight1, packedLight2)
                .endVertex();
        buffer.vertex(matrix, half, -half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(1, 1)
                .uv2(packedLight1, packedLight2)
                .endVertex();
        buffer.vertex(matrix, half, half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(1, 0)
                .uv2(packedLight1, packedLight2)
                .endVertex();
        buffer.vertex(matrix, -half, half, 0)
                .color(r, g, b, effectiveAlpha)
                .uv(0, 0)
                .uv2(packedLight1, packedLight2)
                .endVertex();

        poseStack.popPose();
    }
}
