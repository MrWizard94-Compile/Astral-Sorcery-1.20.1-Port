package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for combining multiple VoxelShapes.
 *
 * <p>1.16 → 1.20 renames:
 * IBooleanFunction → BooleanOp,
 * VoxelShapes → Shapes.</p>
 */
public class VoxelUtils {

    @Nonnull
    public static VoxelShape combineAll(@Nonnull BooleanOp fct, @Nonnull VoxelShape... shapes) {
        return combineAll(fct, Arrays.asList(shapes));
    }

    @Nonnull
    public static VoxelShape combineAll(@Nonnull BooleanOp fct, @Nonnull List<VoxelShape> shapes) {
        if (shapes.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape first = shapes.get(0);
        for (int i = 1; i < shapes.size(); i++) {
            first = Shapes.join(first, shapes.get(i), fct);
        }
        return first;
    }
}
