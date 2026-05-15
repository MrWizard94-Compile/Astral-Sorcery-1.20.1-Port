package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for block entities that relay/transmit starlight between nodes.
 * Transmission nodes pass starlight from sources to receivers with some
 * efficiency loss based on crystal quality.
 *
 * <p>Implementations: BlockEntityLens, BlockEntityPrism</p>
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level</p>
 */
public interface IStarlightTransmission extends ILocatable {

    /**
     * Get the transmission efficiency of this node [0, 1].
     * 1.0 means no loss; lower values lose starlight.
     */
    double getTransmissionEfficiency();

    /**
     * Get the positions this node transmits TO (downstream connections).
     *
     * @return list of connected receiver/transmission node positions
     */
    @Nonnull
    List<BlockPos> getTransmissionTargets();

    /**
     * Whether this node can accept an incoming link from the given position.
     *
     * @param from the position trying to link
     * @return true if link is acceptable
     */
    boolean canAcceptLink(@Nonnull BlockPos from);

    /**
     * Get the level this transmission node exists in.
     */
    @Nullable
    Level getTransmissionLevel();
}
