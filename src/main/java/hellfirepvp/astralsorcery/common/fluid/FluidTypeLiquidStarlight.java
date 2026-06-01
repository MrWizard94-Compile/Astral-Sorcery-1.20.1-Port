package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class FluidTypeLiquidStarlight extends FluidType {

    private static final ResourceLocation STILL =
            AstralSorcery.key("block/liquid_starlight_still");
    private static final ResourceLocation FLOWING =
            AstralSorcery.key("block/liquid_starlight_flowing");

    public FluidTypeLiquidStarlight() {
        super(FluidType.Properties.create()
                .lightLevel(15)
                .density(1001)
                .viscosity(300)
                .temperature(40)
                .rarity(Rarity.EPIC));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING;
            }

            @Override
            public int getTintColor() {
                // Light blue-white tint, matching the starlight aesthetic
                return 0xFFADD8FF;
            }
        });
    }
}
