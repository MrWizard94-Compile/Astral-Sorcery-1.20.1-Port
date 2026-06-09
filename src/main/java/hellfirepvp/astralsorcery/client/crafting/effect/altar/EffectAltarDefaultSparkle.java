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

/** Sparse white sparkles scattered across the altar surface while crafting. */
@OnlyIn(Dist.CLIENT)
public class EffectAltarDefaultSparkle extends AltarRecipeEffect {

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        Vec3 center = altarCenter(altar);
        double reach = pillarReach(altar.getAltarType());
        if (reach == 0) reach = 1.5;
        double spread = reach * 2 + 1;
        Vec3 at = new Vec3(
                center.x - reach + RAND.nextDouble() * spread,
                center.y + 0.02,
                center.z - reach + RAND.nextDouble() * spread);
        EffectHelper.sparkleFloating(at, EffectHelper.randomStarlightColor(),
                0.1f + RAND.nextFloat() * 0.15f, 25 + RAND.nextInt(20));
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
