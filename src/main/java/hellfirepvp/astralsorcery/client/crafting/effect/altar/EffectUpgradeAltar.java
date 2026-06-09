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

/** Intense column of beams + burst for the altar tier-upgrade recipe. */
@OnlyIn(Dist.CLIENT)
public class EffectUpgradeAltar extends AltarRecipeEffect {

    private static final Color UPGRADE_COLOR = new Color(230, 240, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        Vec3 center = altarCenter(altar);
        // Dense sparkle cloud rising above the altar
        if (RAND.nextInt(3) == 0) {
            EffectHelper.sparkleCloud(center.add(0, 1 + RAND.nextDouble() * 2, 0),
                    0.5f, UPGRADE_COLOR, 2, 0.15f, 30);
        }
        // Occasional lightbeam column
        if (RAND.nextInt(5) == 0) {
            Vec3 from = center.add(0, -0.5, 0);
            EffectHelper.lightbeamStarlight(from, from.add(0, 8 + RAND.nextFloat() * 4, 0),
                    50 + RAND.nextInt(30));
        }
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        EffectHelper.burstStarlight(center);
        EffectHelper.burstStarlight(center.add(0, 0.5, 0));
        EffectHelper.flareStarlight(center);
        EffectHelper.dustCloud(center, UPGRADE_COLOR, 20, 1.5f);
    }
}
