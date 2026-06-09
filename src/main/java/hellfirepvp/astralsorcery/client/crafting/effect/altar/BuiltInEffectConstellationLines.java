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

/** Constellation-tier: pillar-to-center lightbeam lines suggesting a star pattern. */
@OnlyIn(Dist.CLIENT)
public class BuiltInEffectConstellationLines extends AltarRecipeEffect {

    private static final Color LINE_COLOR = new Color(180, 210, 255);

    @Override
    public void onTick(@Nonnull BlockEntityAltar altar,
                        @Nonnull ActiveSimpleAltarRecipe.CraftState state) {
        if (state != ActiveSimpleAltarRecipe.CraftState.ACTIVE) return;
        int count = pillarCount(altar.getAltarType());
        if (count == 0 || RAND.nextInt(4) != 0) return;
        Vec3 center = altarCenter(altar).add(0, 0.1, 0);
        int from = RAND.nextInt(count);
        int to   = RAND.nextInt(count);
        if (from == to) return;
        Vec3 fromOff = pillarOffset(altar.getAltarType(), from);
        Vec3 toOff   = pillarOffset(altar.getAltarType(), to);
        Vec3 start = center.add(fromOff);
        Vec3 end   = center.add(toOff);
        EffectHelper.lightbeam(start, end, LINE_COLOR, 0.04f, 30 + RAND.nextInt(20));
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
        EffectHelper.flareStarlight(center);
    }
}
