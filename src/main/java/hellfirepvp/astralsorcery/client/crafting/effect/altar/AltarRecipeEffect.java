package hellfirepvp.astralsorcery.client.crafting.effect.altar;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Base class for visual effects that play on an altar while a recipe is crafting.
 * Subclasses implement {@link #onTick} for per-tick particle spawning,
 * {@link #onRender} for BER geometry rendering, and {@link #onCraftingFinish}
 * for completion celebration effects.
 *
 * <p>All methods are client-only.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class AltarRecipeEffect {

    protected static final Random RAND = new Random();

    // Corner pillar offsets by altar tier
    private static final Vec3[] PILLAR_OFFSETS_T2 = {
            new Vec3( 2, 0,  2),
            new Vec3(-2, 0,  2),
            new Vec3( 2, 0, -2),
            new Vec3(-2, 0, -2)
    };
    private static final Vec3[] PILLAR_OFFSETS_T3 = {
            new Vec3( 3, 0,  3),
            new Vec3(-3, 0,  3),
            new Vec3( 3, 0, -3),
            new Vec3(-3, 0, -3)
    };

    /** World-center of the altar block. */
    @Nonnull
    protected static Vec3 altarCenter(@Nonnull BlockEntityAltar altar) {
        return Vec3.atCenterOf(altar.getBlockPos());
    }

    /** Pillar offset for a random corner of this altar tier. */
    @Nonnull
    protected static Vec3 randomPillarOffset(@Nonnull BlockAltar.AltarType tier) {
        Vec3[] offsets = pillarOffsets(tier);
        return offsets[RAND.nextInt(offsets.length)];
    }

    /** Pillar offset for a specific corner index (0-3). */
    @Nonnull
    protected static Vec3 pillarOffset(@Nonnull BlockAltar.AltarType tier, int index) {
        Vec3[] offsets = pillarOffsets(tier);
        return offsets[index % offsets.length];
    }

    protected static int pillarCount(@Nonnull BlockAltar.AltarType tier) {
        return tier == BlockAltar.AltarType.DISCOVERY ? 0 : 4;
    }

    protected static double pillarReach(@Nonnull BlockAltar.AltarType tier) {
        return switch (tier) {
            case ATTUNEMENT -> 2.0;
            case CONSTELLATION, RADIANCE -> 3.0;
            default -> 0.0;
        };
    }

    protected static int pillarHeight(@Nonnull BlockAltar.AltarType tier) {
        return switch (tier) {
            case ATTUNEMENT -> 2;
            case CONSTELLATION, RADIANCE -> 3;
            default -> 0;
        };
    }

    private static Vec3[] pillarOffsets(@Nonnull BlockAltar.AltarType tier) {
        return switch (tier) {
            case ATTUNEMENT -> PILLAR_OFFSETS_T2;
            case CONSTELLATION, RADIANCE -> PILLAR_OFFSETS_T3;
            default -> new Vec3[]{ Vec3.ZERO };
        };
    }

    // =========================================================================
    // Abstract interface
    // =========================================================================

    /**
     * Called every client tick while the altar has an active recipe.
     * Spawn particles here using {@link hellfirepvp.astralsorcery.client.effect.EffectHelper}.
     */
    @OnlyIn(Dist.CLIENT)
    public abstract void onTick(@Nonnull BlockEntityAltar altar,
                                 @Nonnull ActiveSimpleAltarRecipe.CraftState state);

    /**
     * Called each render frame from the altar's block entity renderer.
     * Use the PoseStack and MultiBufferSource for geometry (quads, lines).
     * Leave empty if the effect uses only tick-spawned particles.
     */
    @OnlyIn(Dist.CLIENT)
    public abstract void onRender(@Nonnull BlockEntityAltar altar,
                                   @Nonnull ActiveSimpleAltarRecipe.CraftState state,
                                   @Nonnull PoseStack poseStack,
                                   @Nonnull MultiBufferSource bufferSource,
                                   float partialTick,
                                   int packedLight);

    /**
     * Called once when a crafting operation completes (successfully or via upgrade).
     *
     * @param isChaining true if the altar immediately starts another recipe after this one
     */
    @OnlyIn(Dist.CLIENT)
    public abstract void onCraftingFinish(@Nonnull BlockEntityAltar altar, boolean isChaining);
}
