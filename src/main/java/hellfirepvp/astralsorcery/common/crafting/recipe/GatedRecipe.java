/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Marks a recipe that requires a research gate to craft.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player.</p>
 */
public interface GatedRecipe {

    boolean hasProgressionServer(@Nonnull Player player);

    @OnlyIn(Dist.CLIENT)
    boolean hasProgressionClient();

    interface Progression extends GatedRecipe {

        @Nonnull
        ProgressionTier getRequiredProgression();

        @Override
        default boolean hasProgressionServer(@Nonnull Player player) {
            PlayerProgress progress = PlayerProgressHelper.getProgress(player);
            return progress != null && progress.isAtLeast(getRequiredProgression());
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        default boolean hasProgressionClient() {
            return PlayerProgressManager.getClientProgress().isAtLeast(getRequiredProgression());
        }
    }
}
