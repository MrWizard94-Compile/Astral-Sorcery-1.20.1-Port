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

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_RITUAL_RANGE;

/**
 * Crystal property: Ritual Range — extends ritual constellation effect radius.
 */
public class PropertyRitualRange extends CrystalProperty {

    public PropertyRitualRange() {
        super(AstralSorcery.key("ritual.range"));
        this.addUsage(ctx -> ctx.uses(USE_RITUAL_RANGE));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_RITUAL_RANGE)) {
                return value * (1.0 + (0.1 * propertyLevel));
            }
            return value;
        });
    }

    @Override
    public int getMaxTier() {
        return 2;
    }
}
