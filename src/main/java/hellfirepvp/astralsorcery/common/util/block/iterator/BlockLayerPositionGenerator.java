package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.block.BlockGeometry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * Iterates through horizontal layers (Y-planes) top-to-bottom and cycles.
 * Each layer is a full XZ plane at the given Y offset, shuffled deterministically.
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag, MathHelper -> Mth,
 * pos.add(x,y,z) -> pos.offset(x,y,z)</p>
 */
public class BlockLayerPositionGenerator extends BlockPositionGenerator {

    private int layer = 0;
    private final LinkedList<BlockPos> currentPositions = new LinkedList<>();

    @Override
    @Nonnull
    protected BlockPos genNext(@Nonnull Vector3 offset, double radius) {
        int size = Mth.floor(radius);

        while (currentPositions.isEmpty()) {
            generatePositions(size);
        }
        return this.currentPositions.pop();
    }

    private void generatePositions(int maxLayers) {
        if (maxLayers <= 0) {
            this.currentPositions.add(BlockPos.ZERO);
            return;
        }
        this.layer++;
        if (this.layer > maxLayers) {
            this.layer = -maxLayers;
        }
        Collection<BlockPos> positions = BlockGeometry.getPlane(Direction.UP, maxLayers);
        positions.forEach(pos -> this.currentPositions.add(pos.offset(0, this.layer, 0)));
        Collections.shuffle(this.currentPositions, new Random(0xF518E23A05B27C19L));
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {
        nbt.putInt("layer", this.layer);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag nbt) {
        this.layer = nbt.getInt("layer");
    }
}
