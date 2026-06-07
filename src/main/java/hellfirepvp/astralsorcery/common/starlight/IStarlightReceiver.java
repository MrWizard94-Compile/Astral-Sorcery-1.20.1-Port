package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Interface for block entities that consume starlight.
 * Receivers accept starlight from connected sources via the transmission network.
 *
 * <p>Implementations: BlockEntityAltar, BlockEntityInfuser, BlockEntityRitualPedestal,
 * BlockEntityChalice</p>
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level</p>
 */
public interface IStarlightReceiver extends ILocatable {

    /**
     * Receive starlight from the network.
     *
     * @param amount        the amount of starlight being delivered
     * @param constellation the constellation of the incoming starlight, if any
     */
    void receiveStarlight(double amount, @Nullable ResourceLocation constellation);

    /**
     * Get the maximum amount of starlight this receiver can accept per tick.
     *
     * @return max input per tick
     */
    double getMaxStarlightInput();

    /**
     * Get the level this receiver exists in.
     */
    @Nullable
    Level getReceiverLevel();
}
