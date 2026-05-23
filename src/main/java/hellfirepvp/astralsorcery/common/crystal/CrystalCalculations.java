package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

/**
 * Calculation formulas for crystal-dependent systems.
 * Centralizes the math so individual systems don't reimplement formulas.
 *
 * <p>1.16 -> 1.20 changes: MathHelper -> Mth</p>
 */
public class CrystalCalculations {

    private CrystalCalculations() {}

    /**
     * Calculate starlight collection rate per tick for a collector crystal.
     *
     * @param properties the crystal properties
     * @param dayFactor  [0,1] how much daylight affects collection (0 = full night boost)
     * @return starlight units collected per tick
     */
    public static double getCollectionRate(@Nonnull CrystalProperties properties, double dayFactor) {
        double baseRate = 0.5;
        double sizeBonus = (double) properties.getSize() / CrystalProperties.MAX_SIZE;
        double purityBonus = (double) properties.getPurity() / CrystalProperties.MAX_PURITY;
        double nightBonus = 1.0 + (1.0 - dayFactor) * 2.0; // 3x at full night

        return baseRate * sizeBonus * purityBonus * nightBonus;
    }

    /**
     * Calculate transmission efficiency through a lens/prism.
     *
     * @param properties the lens crystal properties
     * @return fraction of starlight that passes through [0.1, 1.0]
     */
    public static double getTransmissionEfficiency(@Nonnull CrystalProperties properties) {
        double cuttingFactor = (double) properties.getCutting() / CrystalProperties.MAX_CUTTING;
        double purityFactor = (double) properties.getPurity() / CrystalProperties.MAX_PURITY;
        return Mth.clamp(0.1 + cuttingFactor * 0.7 + purityFactor * 0.2, 0.1, 1.0);
    }

    /**
     * Calculate tool effectiveness multiplier based on crystal quality.
     *
     * @param properties the tool's crystal properties
     * @return speed multiplier [0.5, 2.0]
     */
    public static float getToolSpeedMultiplier(@Nonnull CrystalProperties properties) {
        float cutting = (float) properties.getCutting() / CrystalProperties.MAX_CUTTING;
        return 0.5F + cutting * 1.5F;
    }

    /**
     * Calculate effective tool durability.
     *
     * @param properties   the crystal properties
     * @param baseDurability the tier's base durability
     * @return actual max durability
     */
    public static int getToolDurability(@Nonnull CrystalProperties properties, int baseDurability) {
        float sizeMultiplier = (float) properties.getSize() / (CrystalProperties.MAX_SIZE / 2.0F);
        return Math.max(1, Mth.floor(baseDurability * sizeMultiplier));
    }

    /**
     * Calculate ritual pedestal effect range.
     *
     * @param properties the pedestal crystal properties
     * @param baseRange  the constellation's base range
     * @return actual effect range in blocks
     */
    public static double getRitualRange(@Nonnull CrystalProperties properties, double baseRange) {
        double sizeFactor = (double) properties.getSize() / CrystalProperties.MAX_SIZE;
        return baseRange * (0.5 + sizeFactor * 0.5);
    }

    /**
     * Calculate altar crafting speed multiplier based on feeding crystal quality.
     *
     * @param properties optional crystal properties in relay
     * @return crafting speed multiplier [1.0, 3.0]
     */
    public static float getAltarSpeedMultiplier(@Nonnull CrystalProperties properties) {
        float purity = (float) properties.getPurity() / CrystalProperties.MAX_PURITY;
        return 1.0F + purity * 2.0F;
    }

    /**
     * Crystal count produced when using this crystal as a crafting input.
     * Size 0-299 → 1, 300-599 → 2, 600-900 → 3.
     */
    public static int getSizeCraftingAmount(@Nonnull CrystalProperties properties) {
        return Mth.clamp(properties.getSize() / 300 + 1, 1, 3);
    }
}
