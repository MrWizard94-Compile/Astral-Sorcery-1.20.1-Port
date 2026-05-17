/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the illumination spark as a soft, camera-facing light orb
 * that fades with age. Uses synced color and alpha from the entity.
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context constructor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityIlluminationSpark extends EntityRenderer<EntityIlluminationSpark> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/entity/illumination_spark.png");

    public RenderEntityIlluminationSpark(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityIlluminationSpark entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        int color = entity.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float alpha = entity.getAlpha();

        // Age-based fade
        float ageFade = 1.0f;
        int maxAge = entity.getMaxAge();
        if (maxAge > 0) {
            ageFade = 1.0f - (float) entity.getAge() / maxAge;
        }

        float tickF = entity.tickCount + partialTick;
        float pulse = 0.8f + 0.2f * (float) Math.sin(tickF * 0.1);

        // Core glow
        VertexConsumer particle = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        float coreSize = 0.06f * ageFade;
        RenderingUtils.renderFacingQuad(particle, poseStack,
                0, 0, 0,
                coreSize,
                r * pulse, g * pulse, b * pulse, alpha * ageFade);

        // Outer halo
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.15f * ageFade;
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                haloSize,
                r, g, b, alpha * ageFade * 0.3f);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityIlluminationSpark entity) {
        return TEXTURE;
    }
}
