/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

/**
 * A perk that enforces additional research/progression gates before it can be allocated.
 * Works alongside the base constellation requirement already checked by {@link PerkTree}.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player; JSONUtils → removed (no JSON deserialization here);
 * {@code hasConstellationDiscovered(cst)} → {@code hasDiscovered(cst.getRegistryName())}.</p>
 */
public class ProgressGatedPerk extends AbstractPerk {

    private BiPredicate<Player, PlayerProgress> gate = (player, progress) -> true;

    public ProgressGatedPerk(@Nonnull ResourceLocation key, int x, int y) {
        super(key, x, y, PerkCategory.SMALL);
    }

    public ProgressGatedPerk(@Nonnull ResourceLocation key, int x, int y,
                              @Nonnull PerkCategory category) {
        super(key, x, y, category);
    }

    // =========================================================================
    // Gate builders
    // =========================================================================

    public ProgressGatedPerk requireTier(@Nonnull ProgressionTier tier) {
        gate = gate.and((player, progress) -> progress.isAtLeast(tier));
        return this;
    }

    public ProgressGatedPerk requireResearch(@Nonnull ResearchProgression research) {
        gate = gate.and((player, progress) -> progress.hasResearch(research));
        return this;
    }

    public ProgressGatedPerk requireConstellation(@Nonnull ResourceLocation constellationKey) {
        gate = gate.and((player, progress) -> progress.hasDiscovered(constellationKey));
        return this;
    }

    public ProgressGatedPerk addGate(@Nonnull BiPredicate<Player, PlayerProgress> predicate) {
        gate = gate.and(predicate);
        return this;
    }

    // =========================================================================
    // Gate check
    // =========================================================================

    /**
     * Returns {@code true} if the given player meets all research gates for this perk.
     */
    public boolean canSee(@Nonnull Player player, @Nonnull LogicalSide side) {
        PlayerProgress prog = side.isClient()
                ? ResearchHelper.getClientProgress()
                : (player instanceof ServerPlayer sp ? PlayerProgressManager.getProgress(sp) : null);
        return prog != null && gate.test(player, prog);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canSeeClient() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        PlayerProgress prog = ResearchHelper.getClientProgress();
        return gate.test(player, prog);
    }

    /** Returns {@code true} if the player meets the gate to allocate this perk. */
    public boolean meetsPerkGate(@Nonnull Player player, @Nonnull PlayerProgress progress) {
        return gate.test(player, progress);
    }
}
