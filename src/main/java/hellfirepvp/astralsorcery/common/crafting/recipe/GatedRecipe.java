/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Marks a recipe that requires a research gate to craft.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player; ResearchHelper/ResearchProgression not yet
 * ported — {@link Progression} stubs both checks as {@code false} until they are.</p>
 */
public interface GatedRecipe {

    boolean hasProgressionServer(Player player);

    @OnlyIn(Dist.CLIENT)
    boolean hasProgressionClient();

    interface Progression extends GatedRecipe {

        @Nonnull
        ProgressionTier getRequiredProgression();

        // TODO: replace stub once ResearchHelper and ResearchProgression are ported
        @Override
        default boolean hasProgressionServer(Player player) {
            return false;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        default boolean hasProgressionClient() {
            return false;
        }
    }
}
