/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityCelestialCrystal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Renders the celestial crystal entity as a translucent, slowly rotating
 * crystal shape with a halo glow. The crystal bobs gently and emits
 * starlight-colored light while descending.
 *
 * <p>Visual composition:
 * <ul>
 *   <li>A diamond-shaped crystal core (two crossing quads)</li>
 *   <li>An additive halo behind the crystal</li>
 *   <li>Intensity pulsing based on tick count</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20: EntityRenderer constructor uses Context.
 * PoseStack for transforms. Axis for rotations.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityCelestialCrystal extends EntityRenderer<EntityCelestialCrystal> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/effect/particle_small.png");

    public RenderEntityCelestialCrystal(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityCelestialCrystal entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float tickCount = entity.getTicksAlive() + partialTick;
        float rotSpeed = entity.getRotationSpeed();

        poseStack.pushPose();

        // Gentle bobbing motion
        float bob = (float) Math.sin(tickCount * 0.06) * 0.08f;
        poseStack.translate(0.0, 0.5 + bob, 0.0);

        // Slow Y-axis rotation
        float rotation = tickCount * rotSpeed;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Slight tilt for visual interest
        poseStack.mulPose(Axis.ZP.rotationDegrees(15.0f));

        Matrix4f matrix = poseStack.last().pose();
        float pulse = 0.7f + 0.3f * (float) Math.sin(tickCount * 0.08);

        // Crystal core: diamond shape from two crossing quads
        VertexConsumer crystal = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        float coreW = 0.15f;
        float coreH = 0.35f;

        // Front/back quad (XY plane)
        RenderingUtils.vertex(crystal, matrix, -coreW, 0, 0, 0.6f, 0.8f, 1.0f, 0.85f * pulse, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, 0, -coreH, 0, 0.7f, 0.85f, 1.0f, 0.9f * pulse, 0.5f, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, coreW, 0, 0, 0.6f, 0.8f, 1.0f, 0.85f * pulse, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, 0, coreH, 0, 0.8f, 0.9f, 1.0f, 0.9f * pulse, 0.5f, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        // Side quad (YZ plane)
        RenderingUtils.vertex(crystal, matrix, 0, 0, -coreW, 0.6f, 0.8f, 1.0f, 0.85f * pulse, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, 0, -coreH, 0, 0.7f, 0.85f, 1.0f, 0.9f * pulse, 0.5f, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, 0, 0, coreW, 0.6f, 0.8f, 1.0f, 0.85f * pulse, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(crystal, matrix, 0, coreH, 0, 0.8f, 0.9f, 1.0f, 0.9f * pulse, 0.5f, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        // Halo glow behind crystal
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float haloSize = 0.4f + 0.05f * (float) Math.sin(tickCount * 0.1);
        float haloAlpha = 0.25f * pulse;

        RenderingUtils.vertex(halo, matrix, -haloSize, -haloSize, -0.01f, 0.5f, 0.7f, 1.0f, haloAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -haloSize, haloSize, -0.01f, 0.5f, 0.7f, 1.0f, haloAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, haloSize, haloSize, -0.01f, 0.5f, 0.7f, 1.0f, haloAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, haloSize, -haloSize, -0.01f, 0.5f, 0.7f, 1.0f, haloAlpha, 1, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityCelestialCrystal entity) {
        return TEXTURE;
    }
}
