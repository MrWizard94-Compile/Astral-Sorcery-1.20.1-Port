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

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_COLLECTOR_CRYSTAL;
import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_LENS_TRANSFER;
import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_RITUAL_EFFECT;
import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_RITUAL_RANGE;

/**
 * Crystal property: Purity — influences starlight transfer efficiency, collector/ritual output.
 */
public class PropertyPurity extends CrystalProperty {

    public PropertyPurity() {
        super(AstralSorcery.key("purity"));
        this.addUsage(ctx -> ctx.uses(USE_LENS_TRANSFER));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_LENS_TRANSFER, value, () -> value * (1.0 + ((1.0 / 6.0) * propertyLevel))));
        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addUsage(ctx -> ctx.uses(USE_RITUAL_EFFECT));
        this.addUsage(ctx -> ctx.uses(USE_RITUAL_RANGE));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_COLLECTOR_CRYSTAL) ||
                    context.uses(USE_RITUAL_EFFECT) ||
                    context.uses(USE_RITUAL_RANGE)) {
                return value * (1.0 + (0.4 * propertyLevel));
            }
            return value;
        });
    }

    @Override
    public int getMaxTier() {
        return 2;
    }
}
