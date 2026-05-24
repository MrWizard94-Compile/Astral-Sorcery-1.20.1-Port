package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class LiquidContext {
    public final List<BlockPos> digPositions = new ArrayList<>();
    public int ticksSinceLastDig = 0;
}
