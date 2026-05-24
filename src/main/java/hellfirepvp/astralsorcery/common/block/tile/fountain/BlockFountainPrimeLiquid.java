package hellfirepvp.astralsorcery.common.block.tile.fountain;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BlockFountainPrimeLiquid extends BlockFountainPrime {

    @Nonnull
    @Override
    public ResourceLocation getEffectId() {
        return AstralSorcery.key("effect_liquid");
    }
}
