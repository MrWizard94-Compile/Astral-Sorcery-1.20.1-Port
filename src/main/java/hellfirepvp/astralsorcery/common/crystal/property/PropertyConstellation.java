/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;

/**
 * Crystal property for a specific constellation attunement on a crystal.
 * Modifiers for collector/ritual crystal source contexts are deferred until
 * the PropertySource/AttunedSourceInstance system is fully ported (Phase TBD).
 *
 * <p>1.16 → 1.20: ForgeRegistryEntry removed; carry ResourceLocation directly.
 * PlayerProgress.hasConstellationDiscovered deferred with research system.</p>
 */
public class PropertyConstellation extends CrystalProperty {

    private final IWeakConstellation cst;

    public PropertyConstellation(IWeakConstellation cst) {
        super(AstralSorcery.key("constellation." + cst.getSimpleName()));
        this.cst = cst;
        // Source-based collector/ritual modifiers deferred until PropertySource is wired
    }

    public IWeakConstellation getConstellation() {
        return cst;
    }

    @Override
    public int getMaxTier() {
        return 2;
    }
}
