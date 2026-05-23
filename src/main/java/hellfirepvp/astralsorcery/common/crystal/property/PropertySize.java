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
 * Crystal property: Size — influences collector crystal collection rate and tool durability.
 */
public class PropertySize extends CrystalProperty {

    public PropertySize() {
        super(AstralSorcery.key("size"));
        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_COLLECTOR_CRYSTAL, value, () -> value * (1.0 + (0.2 * propertyLevel))));
        this.addUsage(ctx -> ctx.uses(USE_TOOL_DURABILITY));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_TOOL_DURABILITY, value, () -> value * (1.0 + (0.15 * propertyLevel))));
    }
}
