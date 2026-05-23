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

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_TOOL_DURABILITY;

/**
 * Crystal property: Tool Durability — increases crystal tool durability scaling.
 */
public class PropertyToolDurability extends CrystalProperty {

    public PropertyToolDurability() {
        super(AstralSorcery.key("tool.durability"));
        this.addUsage(ctx -> ctx.uses(USE_TOOL_DURABILITY));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_TOOL_DURABILITY, value, () -> value * (1.0 + (0.25 * propertyLevel))));
    }
}
