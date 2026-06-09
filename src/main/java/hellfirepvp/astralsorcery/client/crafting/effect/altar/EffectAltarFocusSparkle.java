package hellfirepvp.astralsorcery.client.crafting.effect.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;

/** Sparkles concentrated around the focus relay position for Radiance-tier crafts. */
@OnlyIn(Dist.CLIENT)
public class EffectAltarFocusSparkle extends AltarRecipeEffect {

    private static final Color FOCUS_COLOR = new Color(220, 230, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        Vec3 center = altarCenter(altar);
        // Focus relay is 4.5 blocks above the altar for Radiance
        double focusY = altar.getAltarType() == BlockAltar.AltarType.RADIANCE
                ? center.y + 4.5 : center.y + 1.0;
        Vec3 focus = new Vec3(center.x, focusY, center.z);
        for (int i = 0; i < 2; i++) {
            Vec3 at = focus.add(
                    (RAND.nextDouble() - 0.5) * 0.6,
                    (RAND.nextDouble() - 0.5) * 0.4,
                    (RAND.nextDouble() - 0.5) * 0.6);
            EffectHelper.sparkleFloating(at, FOCUS_COLOR, 0.12f + RAND.nextFloat() * 0.1f,
                    15 + RAND.nextInt(15));
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
