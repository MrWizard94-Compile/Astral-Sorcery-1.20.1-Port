package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_COLLECTOR_CRYSTAL;

/**
 * Calculation formulas for crystal-dependent systems.
 * Centralizes the math so individual systems don't reimplement formulas.
 *
 * <p>1.16 -> 1.20 changes: MathHelper -> Mth</p>
 */
public class CrystalCalculations {

    private CrystalCalculations() {}

    /**
     * Apply CrystalAttributes property modifiers to derive the collection rate multiplier.
     * Starts at 1.0 and applies each property's modifier with USE_COLLECTOR_CRYSTAL context.
     * Range approximately 1.0–11.0 depending on property tiers.
     */
    public static double getCollectorCrystalCollectionRate(@Nonnull CrystalAttributes attributes) {
        CalculationContext ctx = CalculationContext.Builder.withUsage(USE_COLLECTOR_CRYSTAL).build();
        double rate = 1.0;
        for (CrystalAttributes.Attribute attr : attributes.getCrystalAttributes()) {
            rate = attr.getProperty().modify(rate, attr.getTier(), ctx);
        }
        return rate;
    }

    /**
     * Calculate transmission efficiency through a lens/prism.
     *
     * @return fraction of starlight that passes through [0.1, 1.0]
     */
    public static double getTransmissionEfficiency(@Nonnull CrystalProperties properties) {
        double cuttingFactor = (double) properties.getCutting() / CrystalProperties.MAX_CUTTING;
        double purityFactor = (double) properties.getPurity() / CrystalProperties.MAX_PURITY;
        return Mth.clamp(0.1 + cuttingFactor * 0.7 + purityFactor * 0.2, 0.1, 1.0);
    }

    /**
     * Calculate tool speed multiplier based on crystal cutting quality.
     *
     * @return speed multiplier [0.5, 2.0]
     */
    public static float getToolSpeedMultiplier(@Nonnull CrystalProperties properties) {
        float cutting = (float) properties.getCutting() / CrystalProperties.MAX_CUTTING;
        return 0.5F + cutting * 1.5F;
    }

    /**
     * Calculate effective tool durability.
     *
     * @return actual max durability
     */
    public static int getToolDurability(@Nonnull CrystalProperties properties, int baseDurability) {
        float sizeMultiplier = (float) properties.getSize() / (CrystalProperties.MAX_SIZE / 2.0F);
        return Math.max(1, Mth.floor(baseDurability * sizeMultiplier));
    }

    /**
     * Crystal count produced when using this crystal as a crafting input.
     * Size 0-299 → 1, 300-599 → 2, 600-900 → 3.
     */
    public static int getSizeCraftingAmount(@Nonnull CrystalProperties properties) {
        return Mth.clamp(properties.getSize() / 300 + 1, 1, 3);
    }
}
