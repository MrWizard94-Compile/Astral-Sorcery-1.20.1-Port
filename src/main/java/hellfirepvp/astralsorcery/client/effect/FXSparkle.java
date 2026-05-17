/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * A sparkle particle — the most common visual effect in Astral Sorcery.
 * Renders as a camera-facing quad with additive blending that fades out
 * over its lifetime. Used for:
 * <ul>
 *   <li>Altar crafting sparkles</li>
 *   <li>Starlight beam endpoint glows</li>
 *   <li>Perk activation flashes</li>
 *   <li>Crystal resonance indicators</li>
 *   <li>General ambient starlight particles</li>
 * </ul>
 *
 * <p>Default lifetime: 20-30 ticks. Default scale: 0.1-0.3.
 * Default color: white/light blue with additive blending.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXSparkle extends EntityVisualFX {

    private boolean twinkle;
    private float baseScale;

    public FXSparkle(double x, double y, double z) {
        super(x, y, z);
        this.twinkle = true;
        this.baseScale = 0.15f;
        setMaxAge(20 + Minecraft.getInstance().level.random.nextInt(15));
        setScale(baseScale);
    }

    @Nonnull
    public FXSparkle setTwinkle(boolean twinkle) {
        this.twinkle = twinkle;
        return this;
    }

    @Nonnull
    public FXSparkle setBaseScale(float baseScale) {
        this.baseScale = baseScale;
        setScale(baseScale);
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
        float progress = getAgeProgress();
        float alphaFade = 1.0f - (progress * progress); // Quadratic fade-out

        // Twinkle: oscillate scale based on age
        float scaleMod = 1.0f;
        if (twinkle) {
            float twinklePhase = (getAge() + partialTick) * 0.3f;
            scaleMod = 0.7f + 0.3f * (float) Math.sin(twinklePhase);
        }

        float effectiveScale = baseScale * scaleMod * (1.0f - progress * 0.3f);
        float effectiveAlpha = getAlpha() * alphaFade;

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
