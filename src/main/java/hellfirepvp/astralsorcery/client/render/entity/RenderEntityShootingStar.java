/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityShootingStar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the shooting star as a bright, camera-facing star shape with
 * a trailing tail. The star pulses and its trail stretches in the
 * direction of motion.
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context constructor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityShootingStar extends EntityRenderer<EntityShootingStar> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/effect/particle_small.png");

    public RenderEntityShootingStar(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityShootingStar entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float scale = entity.getStarScale();
        int color = entity.getStarColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float tickF = entity.tickCount + partialTick;

        poseStack.pushPose();

        // Pulse brightness
        float pulse = 0.8f + 0.2f * (float) Math.sin(tickF * 0.3);

        // Core star — camera-facing bright quad
        VertexConsumer star = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_BURST);
        float coreSize = 0.3f * scale;
        RenderingUtils.renderFacingQuad(star, poseStack,
                0, 0, 0,
                coreSize,
                r * pulse, g * pulse, b * pulse, 0.95f);

        // Outer glow halo
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.6f * scale + 0.1f * (float) Math.sin(tickF * 0.2);
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                haloSize,
                r, g, b, 0.3f * pulse);

        // Trail — several diminishing quads behind the star (along velocity direction)
        VertexConsumer trail = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        double vx = entity.getDeltaMovement().x;
        double vy = entity.getDeltaMovement().y;
        double vz = entity.getDeltaMovement().z;
        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);

        if (speed > 0.01) {
            float nx = (float) (-vx / speed);
            float ny = (float) (-vy / speed);
            float nz = (float) (-vz / speed);

            for (int i = 1; i <= 5; i++) {
                float frac = i / 5.0f;
                float dist = i * 0.3f * scale;
                float trailAlpha = 0.5f * (1.0f - frac) * pulse;
                float trailSize = coreSize * (1.0f - frac * 0.6f);

                RenderingUtils.renderFacingQuad(trail, poseStack,
                        nx * dist, ny * dist, nz * dist,
                        trailSize,
                        r, g, b, trailAlpha);
            }
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityShootingStar entity) {
        return TEXTURE;
    }
}
