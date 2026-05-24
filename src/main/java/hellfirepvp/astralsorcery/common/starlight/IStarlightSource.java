package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for block entities that produce starlight.
 * Sources collect starlight from the sky and make it available
 * for transmission to receivers.
 *
 * <p>Implementations: BlockEntityCollectorCrystal, BlockEntityCelestialCollectorCrystal</p>
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, ResourceLocation stable</p>
 */
public interface IStarlightSource extends ILocatable {

    /**
     * Get the amount of starlight this source can provide per tick.
     *
     * @return starlight units available this tick
     */
    double getStarlightProduction();

    /**
     * Get the registry key of the constellation this source is attuned to, if any.
     * Named distinctly from {@code ConstellationTile.getAttunedConstellation()} to avoid
     * return-type conflict when both interfaces are implemented.
     *
     * @return constellation key, or null if unattuned
     */
    @Nullable
    ResourceLocation getAttunedConstellationKey();

    /**
     * Get the level this source exists in.
     */
    @Nullable
    Level getSourceLevel();

    /**
     * Whether this source has clear sky access for collection.
     */
    boolean hasSkyAccess();
}
