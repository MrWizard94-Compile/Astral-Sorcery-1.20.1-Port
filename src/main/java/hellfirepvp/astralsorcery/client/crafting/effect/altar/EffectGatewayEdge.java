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

/** Cyan-tinted orbital ring effect for gateway-related altar recipes. */
@OnlyIn(Dist.CLIENT)
public class EffectGatewayEdge extends AltarRecipeEffect {

    private static final Color GATEWAY_COLOR = new Color(120, 220, 200);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        if (RAND.nextInt(2) != 0) return;
        Vec3 center = altarCenter(altar).add(0, 0.2, 0);
        float radius = (float) Math.max(1.0, pillarReach(altar.getAltarType()) * 0.7);
        EffectHelper.orbitalRingFX(center, GATEWAY_COLOR, radius, 1, 0.09f, 35);
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {
        Vec3 center = altarCenter(altar).add(0, 0.5, 0);
        EffectHelper.burst(center, GATEWAY_COLOR, 0.15f, 10, 0.15f);
    }
}
