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

/** Periodic liquid-starlight bursts erupting from the altar center. */
@OnlyIn(Dist.CLIENT)
public class EffectLiquidBurst extends AltarRecipeEffect {

    private static final Color LIQUID_COLOR = new Color(100, 160, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        if (RAND.nextInt(12) != 0) return;
        Vec3 center = altarCenter(altar).add(0, 0.3, 0);
        EffectHelper.burst(center, LIQUID_COLOR, 0.12f, 8, 0.10f);
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        EffectHelper.burst(center, LIQUID_COLOR, 0.18f, 16, 0.18f);
    }
}
