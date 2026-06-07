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
 * A brief bright flash/flare that rapidly expands then fades.
 * Used for crafting completion, perk activation, and tier-up effects.
 *
 * <p>The flare starts small, expands to a maximum size over its lifetime,
 * and simultaneously fades out with a sharp quadratic alpha curve. Rendered
 * as a camera-facing quad at full brightness.</p>
 *
 * <p>Default lifetime: 8 ticks. Scale grows by 4x over lifetime.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXFlare extends EntityVisualFX {

    public FXFlare(double x, double y, double z) {
        super(x, y, z);
        setMaxAge(8);
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

        // Scale expands rapidly: starts at base scale, grows to 4x
        float effectiveScale = getScaleX() * (1.0f + progress * 3.0f);
        // Alpha fades sharply with quadratic falloff
        float effectiveAlpha = getAlpha() * (1.0f - progress * progress);

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
