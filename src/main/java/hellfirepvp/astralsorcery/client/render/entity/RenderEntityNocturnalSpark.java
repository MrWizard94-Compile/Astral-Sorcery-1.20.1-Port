/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the nocturnal spark as a dark violet homing particle.
 * Has an aggressive, pulsing appearance distinct from the gentle
 * illumination spark.
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context constructor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityNocturnalSpark extends EntityRenderer<EntityNocturnalSpark> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/effect/particle_small.png");

    public RenderEntityNocturnalSpark(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityNocturnalSpark entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float tickF = entity.tickCount + partialTick;

        // Fast aggressive pulse
        float pulse = 0.6f + 0.4f * (float) Math.sin(tickF * 0.25);

        // Dark violet color
        float r = 0.4f * pulse;
        float g = 0.1f * pulse;
        float b = 0.6f * pulse;

        // Core
        VertexConsumer particle = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        RenderingUtils.renderFacingQuad(particle, poseStack,
                0, 0, 0,
                0.1f,
                r, g, b, 0.85f);

        // Dark halo
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.2f + 0.05f * (float) Math.sin(tickF * 0.3);
        RenderingUtils.renderFacingQuad(halo, poseStack,
                0, 0, 0,
                haloSize,
                0.3f, 0.05f, 0.5f, 0.25f * pulse);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityNocturnalSpark entity) {
        return TEXTURE;
    }
}
