package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.fluid.FluidTypeLiquidStarlight;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidTypesAS {

    private FluidTypesAS() {}

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<FluidTypeLiquidStarlight> LIQUID_STARLIGHT =
            FLUID_TYPES.register("liquid_starlight", FluidTypeLiquidStarlight::new);
}
