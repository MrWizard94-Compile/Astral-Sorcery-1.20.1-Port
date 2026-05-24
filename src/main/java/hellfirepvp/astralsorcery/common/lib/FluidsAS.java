package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidsAS {

    private FluidsAS() {}

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, AstralSorcery.MODID);

    public static final RegistryObject<FluidLiquidStarlight.Source> LIQUID_STARLIGHT =
            FLUIDS.register("liquid_starlight", FluidLiquidStarlight.Source::new);

    public static final RegistryObject<FluidLiquidStarlight.Flowing> LIQUID_STARLIGHT_FLOWING =
            FLUIDS.register("liquid_starlight_flowing", FluidLiquidStarlight.Flowing::new);
}
