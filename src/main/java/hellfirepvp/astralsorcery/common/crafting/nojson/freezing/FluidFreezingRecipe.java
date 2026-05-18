/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidType;

public class FluidFreezingRecipe extends BlockFreezingRecipe {

    public FluidFreezingRecipe() {
        super(AstralSorcery.key("all_fluids_freezing"),
                (level, pos, state) -> state.getFluidState().isSource() &&
                        state.getFluidState().createLegacyBlock().equals(state),
                (worldPos, state) -> {
                    FluidType fType = state.getFluidState().getType().getFluidType();
                    int temp = fType.getTemperature();
                    if (temp <= 300) {
                        return Blocks.ICE.defaultBlockState();
                    } else if (temp >= 500) {
                        return Blocks.OBSIDIAN.defaultBlockState();
                    }
                    return state;
                });
    }
}
