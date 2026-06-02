/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.render.CrystalModelRenderer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRitualPedestal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * BER for the Ritual Pedestal.
 *
 * <p>Renders:
 * <ul>
 *   <li>Floating spinning crystal when a crystal is placed</li>
 *   <li>Glow tinted by the crystal's attuned constellation color</li>
 *   <li>Area ring that brightens when the ritual is active</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class RenderRitualPedestal implements BlockEntityRenderer<BlockEntityRitualPedestal> {

    public RenderRitualPedestal(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityRitualPedestal pedestal, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        float tickCount = pedestal.getTicksExisted() + partialTick;
        boolean active   = pedestal.isRitualActive();
        boolean hasCrystal = !pedestal.getHeldCrystal().isEmpty();

        // Resolve constellation color (default blue-white)
        Color color = resolveColor(pedestal);
        float r = color.getRed()   / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue()  / 255f;

        // --- Floating crystal above pedestal ---
        if (hasCrystal) {
            float bob      = (float) Math.sin(tickCount * 0.04) * 0.05f;
            float rotation = tickCount * 0.8f;
            float crystalY = 1.3f + bob;

            poseStack.pushPose();
            poseStack.translate(0, crystalY, 0);
            CrystalModelRenderer.renderCrystal(poseStack, bufferSource,
                    color, 0.9f, rotation, 0.85f, packedLight);
            poseStack.popPose();

            // Crystal glow (constellation-tinted)
            float pulse    = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.06);
            float glowAlpha = (active ? 0.35f : 0.15f) + pulse * 0.1f;
            float glowSize  = 0.22f + pulse * 0.04f;

            VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
            Matrix4f matrix = poseStack.last().pose();
            float gy = 1.3f + (float) Math.sin(tickCount * 0.04) * 0.05f;

            RenderingUtils.vertex(halo, matrix, -glowSize, gy, -glowSize, r, g, b, glowAlpha, 0, 0);
            RenderingUtils.vertex(halo, matrix, -glowSize, gy,  glowSize, r, g, b, glowAlpha, 0, 1);
            RenderingUtils.vertex(halo, matrix,  glowSize, gy,  glowSize, r, g, b, glowAlpha, 1, 1);
            RenderingUtils.vertex(halo, matrix,  glowSize, gy, -glowSize, r, g, b, glowAlpha, 1, 0);
        }

        // --- Area ring — brighter while active ---
        {
            float pulse     = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.02);
            float baseAlpha = active ? 0.14f : 0.05f;
            float ringAlpha = baseAlpha + pulse * 0.04f;
            float ringSize  = 1.5f;

            VertexConsumer ring = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
            Matrix4f matrix = poseStack.last().pose();

            RenderingUtils.vertex(ring, matrix, -ringSize, 0.02f, -ringSize, r, g, b, ringAlpha, 0, 0);
            RenderingUtils.vertex(ring, matrix, -ringSize, 0.02f,  ringSize, r, g, b, ringAlpha, 0, 1);
            RenderingUtils.vertex(ring, matrix,  ringSize, 0.02f,  ringSize, r, g, b, ringAlpha, 1, 1);
            RenderingUtils.vertex(ring, matrix,  ringSize, 0.02f, -ringSize, r, g, b, ringAlpha, 1, 0);
        }

        poseStack.popPose();
    }

    private Color resolveColor(@Nonnull BlockEntityRitualPedestal pedestal) {
        ResourceLocation key = pedestal.getAttunedConstellation();
        if (key != null) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst instanceof IWeakConstellation wc) {
                java.awt.Color c = wc.getConstellationColor();
                // Blend toward white slightly so it isn't pure-saturated
                int r = Math.min(255, c.getRed()   + 60);
                int g = Math.min(255, c.getGreen() + 60);
                int b = Math.min(255, c.getBlue()  + 60);
                return new Color(r, g, b);
            }
        }
        return new Color(160, 190, 255); // default blue-white
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityRitualPedestal p) {
        return true;
    }
}
