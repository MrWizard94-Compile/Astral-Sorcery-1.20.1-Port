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

/** Bright radiant flare pulses from the altar center during crafting. */
@OnlyIn(Dist.CLIENT)
public class EffectLuminescenceFlare extends AltarRecipeEffect {

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        if (RAND.nextInt(20) != 0) return;
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        EffectHelper.flareStarlight(center);
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        for (int i = 0; i < 3; i++) {
            EffectHelper.flareStarlight(center.add(
                    (RAND.nextDouble() - 0.5) * 0.4, RAND.nextDouble() * 0.5,
                    (RAND.nextDouble() - 0.5) * 0.4));
        }
    }
}
