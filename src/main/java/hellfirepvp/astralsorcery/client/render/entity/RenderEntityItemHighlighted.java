/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.entity.EntityItemHighlighted;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Renders the highlighted item entity using the standard item renderer
 * plus an additional colored halo glow. The glow color and intensity
 * are provided by the entity's synced data.
 *
 * <p>Extends {@link ItemEntityRenderer} to get normal item rendering,
 * then overlays an additive halo in the highlight color.</p>
 *
 * <p>1.16 → 1.20: ItemEntityRenderer constructor uses Context.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityItemHighlighted extends ItemEntityRenderer {

    public RenderEntityItemHighlighted(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull net.minecraft.world.entity.item.ItemEntity entity,
                        float entityYaw, float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        // Render normal item first
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // Add highlight glow if this is our highlighted entity
        if (entity instanceof EntityItemHighlighted highlighted) {
            int color = highlighted.getHighlightColor();
            float intensity = highlighted.getHighlightIntensity();
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            float tickF = entity.tickCount + partialTick;
            float pulse = 0.7f + 0.3f * (float) Math.sin(tickF * 0.1);
            float alpha = intensity * pulse * 0.4f;

            // Halo glow around the item
            VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
            float haloSize = 0.35f + 0.05f * (float) Math.sin(tickF * 0.08);
            RenderingUtils.renderFacingQuad(halo, poseStack,
                    0, 0.25f, 0,
                    haloSize,
                    r, g, b, alpha);
        }
    }
}
