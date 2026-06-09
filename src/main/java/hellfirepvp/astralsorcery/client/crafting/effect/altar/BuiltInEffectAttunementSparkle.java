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

/** Attunement-tier crafting: pillar sparkles plus central orbital ring. */
@OnlyIn(Dist.CLIENT)
public class BuiltInEffectAttunementSparkle extends AltarRecipeEffect {

    private static final Color ATTUNEMENT_COLOR = new Color(170, 200, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        Vec3 center = altarCenter(altar);
        // Sparkles at each of the 4 pillars
        for (int i = 0; i < 4; i++) {
            if (RAND.nextInt(3) != 0) continue;
            Vec3 offset = pillarOffset(altar.getAltarType(), i);
            Vec3 at = center.add(offset).add(
                    (RAND.nextDouble() - 0.5) * 0.5,
                    RAND.nextDouble() * pillarHeight(altar.getAltarType()),
                    (RAND.nextDouble() - 0.5) * 0.5);
            EffectHelper.sparkleFloating(at, ATTUNEMENT_COLOR,
                    0.1f + RAND.nextFloat() * 0.1f, 20 + RAND.nextInt(15));
        }
        // Occasional orbital ring at altar surface
        if (RAND.nextInt(4) == 0) {
            EffectHelper.orbitalStarlight(center.add(0, 0.1, 0), 1.8f);
        }
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.3, 0);
        EffectHelper.burstStarlight(center);
    }
}
