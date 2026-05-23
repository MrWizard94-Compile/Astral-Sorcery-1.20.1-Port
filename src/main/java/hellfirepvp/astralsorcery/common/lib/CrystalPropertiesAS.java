/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.CrystalPropertyRegistry;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.crystal.property.*;

import java.util.List;

/**
 * Defines all crystal property and usage singletons, and registers them.
 * Call {@link #init()} during FMLCommonSetupEvent (after ConstellationsAS.init()).
 *
 * <p>1.16 → 1.20: ForgeRegistry replaced with CrystalPropertyRegistry (plain map).
 * PropertySource / CrystalCalculations / CrystalAttributeGenItem wiring deferred until
 * the collector/ritual tile entity system is ported.</p>
 */
public class CrystalPropertiesAS {

    private CrystalPropertiesAS() {}

    // =========================================================================
    // Properties
    // =========================================================================

    public static class Properties {

        public static CrystalProperty PROPERTY_SIZE;
        public static CrystalProperty PROPERTY_PURITY;
        public static CrystalProperty PROPERTY_SHAPE;

        public static CrystalProperty PROPERTY_TOOL_DURABILITY;
        public static CrystalProperty PROPERTY_TOOL_EFFICIENCY;

        public static CrystalProperty PROPERTY_RITUAL_RANGE;
        public static CrystalProperty PROPERTY_RITUAL_EFFECT;
        public static CrystalProperty PROPERTY_COLLECTOR_COLLECTION_RATE;

        public static CrystalProperty PROPERTY_CST_DISCIDIA;
        public static CrystalProperty PROPERTY_CST_ARMARA;
        public static CrystalProperty PROPERTY_CST_AEVITAS;
        public static CrystalProperty PROPERTY_CST_VICIO;
        public static CrystalProperty PROPERTY_CST_EVORSIO;
        public static CrystalProperty PROPERTY_CST_MINERALIS;
        public static CrystalProperty PROPERTY_CST_LUCERNA;
        public static CrystalProperty PROPERTY_CST_BOOTES;
        public static CrystalProperty PROPERTY_CST_OCTANS;
        public static CrystalProperty PROPERTY_CST_HOROLOGIUM;
        public static CrystalProperty PROPERTY_CST_FORNAX;
        public static CrystalProperty PROPERTY_CST_PELOTRIO;
    }

    // =========================================================================
    // Usages
    // =========================================================================

    public static class Usages {

        public static PropertyUsage USE_RITUAL_EFFECT;
        public static PropertyUsage USE_RITUAL_RANGE;
        public static PropertyUsage USE_COLLECTOR_CRYSTAL;
        public static PropertyUsage USE_LENS_TRANSFER;
        public static PropertyUsage USE_LENS_EFFECT;
        public static PropertyUsage USE_TOOL_DURABILITY;
        public static PropertyUsage USE_TOOL_EFFECTIVENESS;
    }

    public static void init() {
        // Usages must be set before property constructors run (they reference Usages.*).
        Usages.USE_RITUAL_EFFECT       = new PropertyUsage(AstralSorcery.key("ritual.effect"));
        Usages.USE_RITUAL_RANGE        = new PropertyUsage(AstralSorcery.key("ritual.range"));
        Usages.USE_COLLECTOR_CRYSTAL   = new PropertyUsage(AstralSorcery.key("collector.crystal"));
        Usages.USE_LENS_TRANSFER       = new PropertyUsage(AstralSorcery.key("lens.transfer"));
        Usages.USE_LENS_EFFECT         = new PropertyUsage(AstralSorcery.key("lens.effect"));
        Usages.USE_TOOL_DURABILITY     = new PropertyUsage(AstralSorcery.key("tool.durability"));
        Usages.USE_TOOL_EFFECTIVENESS  = new PropertyUsage(AstralSorcery.key("tool.effectiveness"));

        CrystalPropertyRegistry reg = CrystalPropertyRegistry.INSTANCE;

        Properties.PROPERTY_SIZE                   = register(reg, new PropertySize());
        Properties.PROPERTY_PURITY                 = register(reg, new PropertyPurity());
        Properties.PROPERTY_SHAPE                  = register(reg, new PropertyShape());
        Properties.PROPERTY_TOOL_DURABILITY        = register(reg, new PropertyToolDurability());
        Properties.PROPERTY_TOOL_EFFICIENCY        = register(reg, new PropertyToolEfficiency());
        Properties.PROPERTY_RITUAL_RANGE           = register(reg, new PropertyRitualRange());
        Properties.PROPERTY_RITUAL_EFFECT          = register(reg, new PropertyRitualEffect());
        Properties.PROPERTY_COLLECTOR_COLLECTION_RATE = register(reg, new PropertyCollectionRate());

        // Constellation-attuned properties — requires ConstellationsAS.init() to have run first
        Properties.PROPERTY_CST_DISCIDIA    = register(reg, new PropertyConstellation(ConstellationsAS.DISCIDIA));
        Properties.PROPERTY_CST_ARMARA      = register(reg, new PropertyConstellation(ConstellationsAS.ARMARA));
        Properties.PROPERTY_CST_AEVITAS     = register(reg, new PropertyConstellation(ConstellationsAS.AEVITAS));
        Properties.PROPERTY_CST_VICIO       = register(reg, new PropertyConstellation(ConstellationsAS.VICIO));
        Properties.PROPERTY_CST_EVORSIO     = register(reg, new PropertyConstellation(ConstellationsAS.EVORSIO));
        Properties.PROPERTY_CST_MINERALIS   = register(reg, new PropertyConstellation(ConstellationsAS.MINERALIS));
        Properties.PROPERTY_CST_LUCERNA     = register(reg, new PropertyConstellation(ConstellationsAS.LUCERNA));
        Properties.PROPERTY_CST_BOOTES      = register(reg, new PropertyConstellation(ConstellationsAS.BOOTES));
        Properties.PROPERTY_CST_OCTANS      = register(reg, new PropertyConstellation(ConstellationsAS.OCTANS));
        Properties.PROPERTY_CST_HOROLOGIUM  = register(reg, new PropertyConstellation(ConstellationsAS.HOROLOGIUM));
        Properties.PROPERTY_CST_FORNAX      = register(reg, new PropertyConstellation(ConstellationsAS.FORNAX));
        Properties.PROPERTY_CST_PELOTRIO    = register(reg, new PropertyConstellation(ConstellationsAS.PELOTRIO));
    }

    private static <T extends CrystalProperty> T register(CrystalPropertyRegistry reg, T prop) {
        reg.register(prop);
        return prop;
    }
}
