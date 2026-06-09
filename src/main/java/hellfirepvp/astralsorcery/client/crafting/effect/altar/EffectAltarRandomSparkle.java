package hellfirepvp.astralsorcery.client.crafting.effect.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;

/** Denser random sparkles with mild upward drift, filling a wider area. */
@OnlyIn(Dist.CLIENT)
public class EffectAltarRandomSparkle extends AltarRecipeEffect {

    private static final Color COLOR = new Color(180, 210, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        Vec3 center = altarCenter(altar);
        double reach = Math.max(1.5, pillarReach(altar.getAltarType()));
        for (int i = 0; i < 2; i++) {
            Vec3 at = new Vec3(
                    center.x + (RAND.nextDouble() - 0.5) * reach * 2,
                    center.y + 0.1 + RAND.nextDouble() * 0.5,
                    center.z + (RAND.nextDouble() - 0.5) * reach * 2);
            EffectHelper.sparkleFloating(at, COLOR, 0.08f + RAND.nextFloat() * 0.12f,
                    20 + RAND.nextInt(25));
        }
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
