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

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_RITUAL_EFFECT;

/**
 * Crystal property: Ritual Effect — amplifies ritual constellation effect strength.
 */
public class PropertyRitualEffect extends CrystalProperty {

    public PropertyRitualEffect() {
        super(AstralSorcery.key("ritual.effect"));
        this.addUsage(ctx -> ctx.uses(USE_RITUAL_EFFECT));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_RITUAL_EFFECT)) {
                return value * (1.0 + (0.3 * propertyLevel));
            }
            return value;
        });
    }
}
