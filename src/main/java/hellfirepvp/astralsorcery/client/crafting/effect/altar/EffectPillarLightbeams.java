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

/** Lightbeams rising from the corner pillars toward the altar center. */
@OnlyIn(Dist.CLIENT)
public class EffectPillarLightbeams extends AltarRecipeEffect {

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        int count = pillarCount(altar.getAltarType());
        if (count == 0 || RAND.nextInt(6) != 0) return;
        Vec3 center = altarCenter(altar);
        Vec3 offset = randomPillarOffset(altar.getAltarType());
        Vec3 from = center.add(offset.x, -0.5, offset.z);
        Vec3 to = from.add(0, pillarHeight(altar.getAltarType()) + 1 + RAND.nextFloat() * 2, 0);
        EffectHelper.lightbeamStarlight(from, to, 35 + RAND.nextInt(25));
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
