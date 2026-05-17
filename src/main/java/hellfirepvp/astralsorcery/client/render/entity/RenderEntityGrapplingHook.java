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
import hellfirepvp.astralsorcery.common.entity.EntityGrapplingHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Renders the grappling hook projectile as a small hook shape with a
 * trailing starlight line back to the player who threw it.
 *
 * <p>The hook itself is a simple textured quad that faces the direction
 * of motion. The line uses the lightbeam render type for a glowing
 * rope appearance.</p>
 *
 * <p>1.16 → 1.20: EntityRenderer uses Context. PoseStack for transforms.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityGrapplingHook extends EntityRenderer<EntityGrapplingHook> {

    private static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/entity/grappling_hook.png");

    public RenderEntityGrapplingHook(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityGrapplingHook entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        float tickF = entity.tickCount + partialTick;

        poseStack.pushPose();

        // Rotate hook to face direction of travel
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        // Hook head — small quad
        VertexConsumer hookBuf = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE_DEPTH);
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.08f;

        RenderingUtils.vertex(hookBuf, matrix, -size, -size, 0, 0.8f, 0.8f, 0.9f, 0.95f, 0, 0);
        RenderingUtils.vertex(hookBuf, matrix,  size, -size, 0, 0.8f, 0.8f, 0.9f, 0.95f, 1, 0);
        RenderingUtils.vertex(hookBuf, matrix,  size,  size, 0, 0.8f, 0.8f, 0.9f, 0.95f, 1, 1);
        RenderingUtils.vertex(hookBuf, matrix, -size,  size, 0, 0.8f, 0.8f, 0.9f, 0.95f, 0, 1);

        poseStack.popPose();

        // Trailing line from hook back to owner
        Entity owner = entity.getOwner();
        if (owner != null) {
            Vec3 hookPos = entity.getPosition(partialTick);
            Vec3 ownerPos = owner.getPosition(partialTick).add(0, owner.getEyeHeight() * 0.7, 0);

            // Camera-relative positions
            Vec3 hookRel = RenderingUtils.getCameraRelative(hookPos);
            Vec3 ownerRel = RenderingUtils.getCameraRelative(ownerPos);

            VertexConsumer beam = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);
            float lineAlpha = 0.5f + 0.15f * (float) Math.sin(tickF * 0.1);

            RenderingUtils.renderLightBeam(beam, poseStack,
                    hookRel, ownerRel,
                    0.015f,
                    0.5f, 0.7f, 1.0f, lineAlpha);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityGrapplingHook entity) {
        return TEXTURE;
    }
}
