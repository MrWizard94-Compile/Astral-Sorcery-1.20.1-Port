/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

/**
 * Functional interface for modifying a crystal calculation value based on property tier and context.
 * 1.16 → 1.20: package unchanged, no API differences.
 */
public interface CrystalPropertyModifierFunction {

    double modify(double value, double originalValue, int propertyLevel, CalculationContext context);
}
