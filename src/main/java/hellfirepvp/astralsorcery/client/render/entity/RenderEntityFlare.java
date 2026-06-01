/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the constellation flare projectile as a bright, camera-facing
 * particle with a soft halo. Color is based on the constellation that
 * launched it.
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context constructor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityFlare extends EntityRenderer<EntityFlare> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/effect/entity_flare.png");

    public RenderEntityFlare(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityFlare entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float tickF = entity.tickCount + partialTick;
        float pulse = 0.7f + 0.3f * (float) Math.sin(tickF * 0.15);

        // Constellation-tinted flare (default to starlight blue-white)
        float r = 0.6f;
        float g = 0.7f;
        float b = 1.0f;

        // Core
        VertexConsumer particle = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        RenderingUtils.renderFacingQuad(particle, poseStack,
                0, 0, 0,
                0.15f,
                r * pulse, g * pulse, b * pulse, 0.9f);

        // Halo
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.3f + 0.05f * (float) Math.sin(tickF * 0.2);
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                haloSize,
                r, g, b, 0.2f * pulse);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityFlare entity) {
        return TEXTURE;
    }
}
