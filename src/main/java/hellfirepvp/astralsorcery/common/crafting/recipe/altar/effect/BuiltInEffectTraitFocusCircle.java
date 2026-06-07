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
import java.awt.Color;

/** Radiance-tier: orbital ring around the floating focus relay position. */
@OnlyIn(Dist.CLIENT)
public class BuiltInEffectTraitFocusCircle extends AltarRecipeEffect {

    private static final Color TRAIT_COLOR = new Color(200, 215, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        if (RAND.nextInt(2) != 0) return;
        Vec3 center = altarCenter(altar).add(0, 4.5, 0); // focus relay height
        EffectHelper.orbitalRingFX(center, TRAIT_COLOR, 0.6f, 1, 0.12f, 25);
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 focus = altarCenter(altar).add(0, 4.5, 0);
        EffectHelper.burstStarlight(focus);
        EffectHelper.flareStarlight(focus);
    }
}
