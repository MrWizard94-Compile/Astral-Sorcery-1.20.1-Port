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

/** Random lightbeams rising from around the altar base while crafting. */
@OnlyIn(Dist.CLIENT)
public class EffectAltarDefaultLightbeams extends AltarRecipeEffect {

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        if (RAND.nextInt(8) != 0) return;
        Vec3 center = altarCenter(altar);
        double reach = Math.max(1.5, pillarReach(altar.getAltarType()) * 0.85);
        Vec3 from = new Vec3(
                center.x + (RAND.nextDouble() - 0.5) * reach * 2,
                center.y - 0.6,
                center.z + (RAND.nextDouble() - 0.5) * reach * 2);
        Vec3 to = from.add(0, 5 + RAND.nextFloat() * 3, 0);
        EffectHelper.lightbeamStarlight(from, to, 40 + RAND.nextInt(30));
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
