package hellfirepvp.astralsorcery.common.util;

import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

/**
 * Holds separate client and server instances of a data type.
 *
 * @param <T> the data type
 */
public class SidedReference<T> {

    private T clientData = null;
    private T serverData = null;

    public Optional<T> getData(LogicalSide side) {
        if (side.isClient()) {
            return Optional.ofNullable(this.clientData);
        }
        return Optional.ofNullable(this.serverData);
    }

    public void setData(LogicalSide side, T data) {
        if (side.isClient()) {
            this.clientData = data;
        } else {
            this.serverData = data;
        }
    }
}
