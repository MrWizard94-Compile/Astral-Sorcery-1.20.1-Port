/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.*;

/**
 * Crystal property: Shape — influences tool effectiveness, collector/ritual output, and lens effect.
 */
public class PropertyShape extends CrystalProperty {

    public PropertyShape() {
        super(AstralSorcery.key("shape"));
        this.addUsage(ctx -> ctx.uses(USE_TOOL_EFFECTIVENESS));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_TOOL_EFFECTIVENESS)) {
                return value * (1.0 + (0.1F * Math.min(propertyLevel, 6)));
            }
            return value;
        });
        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addUsage(ctx -> ctx.uses(USE_RITUAL_EFFECT));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_COLLECTOR_CRYSTAL) || context.uses(USE_RITUAL_EFFECT)) {
                return value * (1.0 + (0.25F * propertyLevel));
            }
            return value;
        });
        this.addUsage(ctx -> ctx.uses(USE_LENS_EFFECT));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_LENS_EFFECT)) {
                return value * (1.0 + (0.2F * Math.min(propertyLevel, this.getMaxTier())));
            }
            return value;
        });
    }
}
