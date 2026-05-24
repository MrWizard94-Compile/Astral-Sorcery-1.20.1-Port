package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidTypesAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class FluidLiquidStarlight extends ForgeFlowingFluid {

    private FluidLiquidStarlight(Properties properties) {
        super(properties);
    }

    public static Properties makeProperties() {
        return new ForgeFlowingFluid.Properties(
                () -> FluidTypesAS.LIQUID_STARLIGHT.get(),
                () -> FluidsAS.LIQUID_STARLIGHT.get(),
                () -> FluidsAS.LIQUID_STARLIGHT_FLOWING.get())
                .block(() -> BlocksAS.FLUID_LIQUID_STARLIGHT.get())
                .bucket(() -> ItemsAS.BUCKET_LIQUID_STARLIGHT.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(2);
    }

    public static class Source extends FluidLiquidStarlight {

        public Source() {
            super(makeProperties());
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends FluidLiquidStarlight {

        public Flowing() {
            super(makeProperties());
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
