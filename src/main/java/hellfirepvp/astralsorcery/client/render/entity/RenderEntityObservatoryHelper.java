/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.entity.EntityObservatoryHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * No-op renderer for the observatory helper entity.
 * This entity is invisible by design — it exists only as a mount
 * for the player during observatory usage. Nothing is rendered.
 *
 * <p>1.16 → 1.20: EntityRenderer constructor uses Context.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderEntityObservatoryHelper extends EntityRenderer<EntityObservatoryHelper> {

    public RenderEntityObservatoryHelper(@Nonnull EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@Nonnull EntityObservatoryHelper entity, float entityYaw,
                        float partialTick, @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource, int packedLight) {
        // Intentionally empty — observatory helper is invisible
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityObservatoryHelper entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
