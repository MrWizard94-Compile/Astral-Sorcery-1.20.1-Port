/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

/**
 * Extension of {@link CrystalAttributeItem} for items that randomly generate their attributes
 * on first inventory tick (e.g. rock crystals dropped from ore).
 */
public interface CrystalAttributeGenItem extends CrystalAttributeItem {

    int getGeneratedPropertyTiers();

    int getMaxPropertyTiers();
}
