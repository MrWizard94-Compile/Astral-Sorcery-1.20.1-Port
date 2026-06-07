package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/** Sparkles emitted from the four corner pillar positions. */
@OnlyIn(Dist.CLIENT)
public class EffectPillarSparkle extends AltarRecipeEffect {

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        int count = pillarCount(altar.getAltarType());
        if (count == 0) return;
        Vec3 center = altarCenter(altar);
        int pillar = RAND.nextInt(count);
        Vec3 offset = pillarOffset(altar.getAltarType(), pillar);
        Vec3 at = center.add(offset).add(
                (RAND.nextDouble() - 0.5) * 0.4,
                pillarHeight(altar.getAltarType()) * RAND.nextDouble(),
                (RAND.nextDouble() - 0.5) * 0.4);
        EffectHelper.sparkleFloating(at, EffectHelper.randomStarlightColor(),
                0.1f + RAND.nextFloat() * 0.1f, 20 + RAND.nextInt(20));
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
