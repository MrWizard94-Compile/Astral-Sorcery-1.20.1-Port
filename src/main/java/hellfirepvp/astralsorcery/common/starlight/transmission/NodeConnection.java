package hellfirepvp.astralsorcery.common.starlight.transmission;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in the starlight network with its connections.
 * A node can be a source, receiver, or transmission node (or multiple).
 *
 * <p>1.16 -> 1.20 changes:
 * RegistryKey&lt;World&gt; -> ResourceKey&lt;Level&gt;</p>
 */
public class NodeConnection {

    @Nonnull
    private final BlockPos pos;
    @Nonnull
    private final ResourceKey<Level> dimension;
    @Nonnull
    private final List<BlockPos> connectedTo;

    private boolean isSource;
    private boolean isReceiver;
    private boolean isTransmission;

    public NodeConnection(@Nonnull BlockPos pos, @Nonnull ResourceKey<Level> dimension) {
        this.pos = pos.immutable();
        this.dimension = dimension;
        this.connectedTo = new ArrayList<>();
    }

    @Nonnull
    public BlockPos getPos() {
        return pos;
    }

    @Nonnull
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Nonnull
    public List<BlockPos> getConnectedTo() {
        return Collections.unmodifiableList(connectedTo);
    }

    public void addConnection(@Nonnull BlockPos target) {
        if (!connectedTo.contains(target)) {
            connectedTo.add(target.immutable());
        }
    }

    public boolean removeConnection(@Nonnull BlockPos target) {
        return connectedTo.remove(target);
    }

    public boolean isSource() {
        return isSource;
    }

    public void setSource(boolean source) {
        this.isSource = source;
    }

    public boolean isReceiver() {
        return isReceiver;
    }

    public void setReceiver(boolean receiver) {
        this.isReceiver = receiver;
    }

    public boolean isTransmission() {
        return isTransmission;
    }

    public void setTransmission(boolean transmission) {
        this.isTransmission = transmission;
    }

    public boolean hasConnections() {
        return !connectedTo.isEmpty();
    }
}
