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

/** Pulses sparkles outward from the spectral relay positions while crafting. */
@OnlyIn(Dist.CLIENT)
public class BuiltInEffectTraitRelayHighlight extends AltarRecipeEffect {

    private static final Color RELAY_COLOR = new Color(200, 220, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        int count = pillarCount(altar.getAltarType());
        if (count == 0 || RAND.nextInt(3) != 0) return;
        Vec3 center = altarCenter(altar);
        Vec3 offset = randomPillarOffset(altar.getAltarType());
        Vec3 relay = center.add(offset.x, 0.5, offset.z);
        EffectHelper.sparkleCloud(relay, 0.3f, RELAY_COLOR, 2, 0.10f, 20);
    }

    @Override
    public void onRender(@Nonnull BlockEntityAltar altar,
                          @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                          @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                          float partialTick, int packedLight) {}

    @Override
    public void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining) {}
}
