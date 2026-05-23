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

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_TOOL_EFFECTIVENESS;

/**
 * Crystal property: Tool Efficiency — increases crystal tool mining speed.
 */
public class PropertyToolEfficiency extends CrystalProperty {

    public PropertyToolEfficiency() {
        super(AstralSorcery.key("tool.efficiency"));
        this.addUsage(ctx -> ctx.uses(USE_TOOL_EFFECTIVENESS));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_TOOL_EFFECTIVENESS, value, () -> value * (1.0 + (0.15 * Math.min(propertyLevel, 4)))));
    }
}
