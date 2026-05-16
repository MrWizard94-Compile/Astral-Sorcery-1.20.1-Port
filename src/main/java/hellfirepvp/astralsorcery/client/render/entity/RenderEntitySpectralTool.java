/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the spectral tool entity as a floating, rotating item model
 * with a translucent ghost-like appearance.
 *
 * <p>1.16 → 1.20 changes:
 * {@code EntityRenderer<T>} constructor now takes {@code EntityRendererProvider.Context}.
 * {@code ItemCameraTransforms.TransformType} → {@code ItemDisplayContext}.
 * {@code MatrixStack} → {@code PoseStack}.
 * {@code Vector3f.XP/YP/ZP.rotationDegrees()} → {@code Axis.XP/YP/ZP.rotationDegrees()}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntitySpectralTool extends EntityRenderer<EntitySpectralTool> {

    public RenderEntitySpectralTool(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntitySpectralTool entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        ItemStack tool = entity.getToolItem();
        if (tool.isEmpty()) return;

        poseStack.pushPose();

        // Floating bob
        float bob = (float) Math.sin((entity.tickCount + partialTick) * 0.1) * 0.1f;
        poseStack.translate(0.0, 0.5 + bob, 0.0);

        // Rotation
        float rotation = entity.getRotation() + partialTick * 5.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Slight tilt for visual interest
        poseStack.mulPose(Axis.XP.rotationDegrees(15.0f));

        // Scale (slightly larger than normal held item)
        poseStack.scale(1.2f, 1.2f, 1.2f);

        // Ghost transparency effect via packed light override
        // The item renderer doesn't directly support alpha, but we can use
        // a dim light level + the translucent render type to approximate ghostliness
        int ghostLight = 0x00F000F0; // Full bright to look ethereal

        Minecraft.getInstance().getItemRenderer().renderStatic(
                tool,
                ItemDisplayContext.GROUND,
                ghostLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntitySpectralTool entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
