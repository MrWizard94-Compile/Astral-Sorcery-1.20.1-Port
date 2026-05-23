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

/**
 * Crystal property: Collection Rate — increases collector crystal starlight gathering.
 */
public class PropertyCollectionRate extends CrystalProperty {

    public PropertyCollectionRate() {
        super(AstralSorcery.key("collector.rate"));
        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_COLLECTOR_CRYSTAL, value, () -> value * (1.0 + (0.2 * propertyLevel))));
    }
}
