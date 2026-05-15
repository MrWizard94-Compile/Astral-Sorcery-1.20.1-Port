package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility for rotating and mirroring block positions and states.
 * Used by multiblock pattern matching to check all 4 rotations.
 *
 * <p>1.16 -> 1.20 changes: APIs stable (Rotation, Mirror enums unchanged)</p>
 */
public class RotationHelper {

    private RotationHelper() {}

    /**
     * Rotate a position around the origin by the given rotation.
     *
     * @param pos      the position to rotate
     * @param rotation the rotation to apply
     * @return the rotated position
     */
    @Nonnull
    public static BlockPos rotatePos(@Nonnull BlockPos pos, @Nonnull Rotation rotation) {
        return switch (rotation) {
            case NONE -> pos;
            case CLOCKWISE_90 -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        };
    }

    /**
     * Mirror a position around the given axis.
     *
     * @param pos    the position to mirror
     * @param mirror the mirror axis
     * @return the mirrored position
     */
    @Nonnull
    public static BlockPos mirrorPos(@Nonnull BlockPos pos, @Nonnull Mirror mirror) {
        return switch (mirror) {
            case NONE -> pos;
            case LEFT_RIGHT -> new BlockPos(pos.getX(), pos.getY(), -pos.getZ());
            case FRONT_BACK -> new BlockPos(-pos.getX(), pos.getY(), pos.getZ());
        };
    }

    /**
     * Rotate a block state by the given rotation.
     *
     * @param state    the state to rotate
     * @param rotation the rotation
     * @return the rotated state
     */
    @Nonnull
    public static BlockState rotateState(@Nonnull BlockState state, @Nonnull Rotation rotation) {
        return state.rotate(rotation);
    }

    /**
     * Get all 4 rotations of a position set (used for multiblock matching).
     *
     * @param positions the original positions
     * @return list of 4 position sets (one per rotation)
     */
    @Nonnull
    public static List<List<BlockPos>> getAllRotations(@Nonnull Collection<BlockPos> positions) {
        List<List<BlockPos>> rotations = new ArrayList<>(4);
        for (Rotation rotation : Rotation.values()) {
            List<BlockPos> rotated = new ArrayList<>(positions.size());
            for (BlockPos pos : positions) {
                rotated.add(rotatePos(pos, rotation));
            }
            rotations.add(rotated);
        }
        return rotations;
    }

    /**
     * Rotate a direction by the given rotation.
     *
     * @param dir      the direction
     * @param rotation the rotation
     * @return the rotated direction
     */
    @Nonnull
    public static Direction rotateDirection(@Nonnull Direction dir, @Nonnull Rotation rotation) {
        return rotation.rotate(dir);
    }
}
