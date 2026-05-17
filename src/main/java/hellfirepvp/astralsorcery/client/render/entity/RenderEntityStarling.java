/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityStarling;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the starling as a camera-facing glowing orb with its
 * synced color. Pulses gently and has a halo glow behind it.
 *
 * <p>The starling uses additive blending for a wispy light appearance
 * similar to firefly particles, but rendered as an entity so it
 * persists across chunk reloads.</p>
 *
 * <p>1.16 → 1.20: EntityRenderer constructor uses Context.
 * Camera-facing via RenderingUtils.renderFacingQuad().</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityStarling extends EntityRenderer<EntityStarling> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/entity/starling.png");

    public RenderEntityStarling(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityStarling entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float tickCount = entity.getTicksAlive() + partialTick;
        int color = entity.getStarlingColor();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // Pulse effect
        float pulse = 0.7f + 0.3f * (float) Math.sin(tickCount * 0.12);

        // Core glow — camera-facing particle quad
        VertexConsumer particle = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        float coreSize = 0.08f + 0.02f * (float) Math.sin(tickCount * 0.15);
        RenderingUtils.renderFacingQuad(particle, poseStack,
                0, 0, 0,
                coreSize,
                r * pulse, g * pulse, b * pulse, 0.9f);

        // Outer halo — larger, dimmer
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.2f + 0.04f * (float) Math.sin(tickCount * 0.08);
        float haloAlpha = 0.15f * pulse;
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                haloSize,
                r, g, b, haloAlpha);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityStarling entity) {
        return TEXTURE;
    }
}
