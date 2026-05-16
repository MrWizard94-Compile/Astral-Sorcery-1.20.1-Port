/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Random;

/**
 * Player render layer that adds a soft starry glow effect around players
 * who are attuned to a constellation. The glow color matches the player's
 * attuned constellation color.
 *
 * <p>Renders small star-like quads orbiting the player model at various heights,
 * with twinkling animation. Only visible when the player has been attuned
 * (checked via capability data).</p>
 *
 * <p>1.16 → 1.20:
 * {@code LayerRenderer<AbstractClientPlayerEntity, PlayerModel<>>}
 * → {@code RenderLayer<AbstractClientPlayer, PlayerModel<>>}.
 * {@code IVertexBuilder} → {@code VertexConsumer}.
 * {@code MatrixStack} → {@code PoseStack}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class LayerStarryGlow extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEX_STAR =
            AstralSorcery.key("textures/effect/star.png");

    /** Number of star particles rendered around the player. */
    private static final int STAR_COUNT = 8;

    /** Maximum orbit radius around the player. */
    private static final float MAX_RADIUS = 0.8f;

    /** Star quad size. */
    private static final float STAR_SIZE = 0.06f;

    private final Random random = new Random();

    public LayerStarryGlow(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, @Nonnull AbstractClientPlayer player,
                        float limbSwing, float limbSwingAmount,
                        float partialTick, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        // TODO: Check if player is attuned (via capability)
        // For now, always render a subtle effect for testing
        if (!isPlayerAttuned(player)) {
            return;
        }

        Color starColor = getAttunedConstellationColor(player);
        float r = starColor.getRed() / 255f;
        float g = starColor.getGreen() / 255f;
        float b = starColor.getBlue() / 255f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        Matrix4f matrix = poseStack.last().pose();

        float time = ageInTicks + partialTick;

        for (int i = 0; i < STAR_COUNT; i++) {
            random.setSeed(player.getId() * 31L + i * 7L);
            float phase = random.nextFloat() * (float) Math.PI * 2.0f;
            float height = 0.3f + random.nextFloat() * 1.4f; // y offset [0.3, 1.7]
            float radius = 0.3f + random.nextFloat() * (MAX_RADIUS - 0.3f);
            float speed = 0.02f + random.nextFloat() * 0.03f;

            float angle = phase + time * speed;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float y = height + (float) Math.sin(time * 0.05f + phase) * 0.05f;

            // Twinkle
            float alpha = 0.4f + 0.3f * (float) Math.sin(time * 0.1f + phase * 3);

            renderStar(buffer, matrix, x, y, z, STAR_SIZE, r, g, b, alpha);
        }
    }

    /**
     * Render a single star quad at the given local position.
     */
    private void renderStar(@Nonnull VertexConsumer buffer,
                             @Nonnull Matrix4f matrix,
                             float x, float y, float z, float size,
                             float r, float g, float b, float a) {
        float half = size;
        RenderingUtils.vertex(buffer, matrix, x - half, y - half, z, r, g, b, a, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x + half, y - half, z, r, g, b, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x + half, y + half, z, r, g, b, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x - half, y + half, z, r, g, b, a, 0, 1);
    }

    /**
     * Check if the player is attuned to a constellation.
     * TODO: Read from player capability when implemented.
     */
    private boolean isPlayerAttuned(@Nonnull AbstractClientPlayer player) {
        // Placeholder: always show for testing
        // In production: check player.getCapability(AstralCapabilities.PLAYER_PROGRESS)
        return true;
    }

    /**
     * Get the constellation color for the player's attunement.
     * TODO: Read from player capability when implemented.
     */
    @Nonnull
    private Color getAttunedConstellationColor(@Nonnull AbstractClientPlayer player) {
        // Placeholder: blue-white starlight color
        return new Color(160, 200, 255);
    }
}
