/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Entry associating a fluid with a rarity weight and spawn amount range for the Evershifting Fountain.
 */
public class FluidRarityEntry {

    private final ResourceLocation fluidName;
    private final int rarity;
    private final int guaranteedAmount;
    private final int additionalRandomAmount;

    public FluidRarityEntry(@Nonnull ResourceLocation fluidName, int rarity,
                             int guaranteedAmount, int additionalRandomAmount) {
        this.fluidName = fluidName;
        this.rarity = rarity;
        this.guaranteedAmount = guaranteedAmount;
        this.additionalRandomAmount = additionalRandomAmount;
    }

    @Nullable
    public Fluid getFluid() {
        return ForgeRegistries.FLUIDS.getValue(fluidName);
    }

    @Nonnull
    public ResourceLocation getFluidName() {
        return fluidName;
    }

    public int getRarity() {
        return rarity;
    }

    public int getRandomAmount(@Nonnull Random rand) {
        return guaranteedAmount + (additionalRandomAmount > 0 ? rand.nextInt(additionalRandomAmount) : 0);
    }

    @Nonnull
    public String serialize() {
        return fluidName + ";" + guaranteedAmount + ";" + additionalRandomAmount + ";" + rarity;
    }

    @Nullable
    public static FluidRarityEntry deserialize(@Nonnull String str) {
        String[] split = str.split(";");
        if (split.length != 4) return null;
        try {
            ResourceLocation fluidName = new ResourceLocation(split[0]);
            int guaranteed = Integer.parseInt(split[1]);
            int randomAmt  = Integer.parseInt(split[2]);
            int rarity     = Integer.parseInt(split[3]);
            return new FluidRarityEntry(fluidName, rarity, guaranteed, randomAmt);
        } catch (Exception ignored) {
            return null;
        }
    }
}
