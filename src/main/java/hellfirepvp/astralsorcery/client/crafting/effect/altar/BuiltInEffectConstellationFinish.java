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

/** Celebration burst + rain of sparkles when a Constellation-tier craft completes. */
@OnlyIn(Dist.CLIENT)
public class BuiltInEffectConstellationFinish extends AltarRecipeEffect {

    private static final Color FINISH_COLOR = new Color(210, 230, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {}

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        // Central burst + flare
        EffectHelper.burstStarlight(center);
        EffectHelper.burstStarlight(center.add(0, 0.4, 0));
        EffectHelper.flareStarlight(center);
        // Pillar sparkle showers
        int count = pillarCount(altar.getAltarType());
        for (int i = 0; i < count; i++) {
            Vec3 pillarTop = center.add(pillarOffset(altar.getAltarType(), i))
                                   .add(0, pillarHeight(altar.getAltarType()), 0);
            EffectHelper.sparkleCloud(pillarTop, 0.4f, FINISH_COLOR, 6, 0.12f, 30);
            EffectHelper.burstStarlight(pillarTop);
        }
        // Dust rain from above
        EffectHelper.dustCloud(center.add(0, 3, 0), FINISH_COLOR, 16, 1.0f);
    }
}
