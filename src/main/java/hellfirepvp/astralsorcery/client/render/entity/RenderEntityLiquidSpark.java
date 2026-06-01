/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityLiquidSpark;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the liquid spark as a tiny blue-white point of light
 * that orbits around lightwells and chalices. Uses synced scale
 * and alpha from the entity, with age-based fade.
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context constructor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityLiquidSpark extends EntityRenderer<EntityLiquidSpark> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/effect/particle_small.png");

    public RenderEntityLiquidSpark(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityLiquidSpark entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float scale = entity.getSparkScale();
        float alpha = entity.getSparkAlpha();
        float tickF = entity.tickCount + partialTick;

        // Age-based fade
        float ageFade = 1.0f;
        int maxAge = entity.getMaxAge();
        if (maxAge > 0) {
            ageFade = 1.0f - (float) entity.getAge() / maxAge;
        }

        float effectiveAlpha = alpha * ageFade;
        float pulse = 0.8f + 0.2f * (float) Math.sin(tickF * 0.12);

        // Liquid starlight blue-white
        float r = 0.5f + 0.3f * pulse;
        float g = 0.7f + 0.2f * pulse;
        float b = 1.0f;

        // Tiny core
        VertexConsumer particle = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        RenderingUtils.renderFacingQuad(particle, poseStack,
                0, 0, 0,
                0.04f * scale,
                r, g, b, effectiveAlpha * 0.9f);

        // Soft halo
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                0.1f * scale,
                r, g, b, effectiveAlpha * 0.2f);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityLiquidSpark entity) {
        return TEXTURE;
    }
}
