/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Abstract base for a single crystal property (size, purity, shape, etc.).
 * 1.16 → 1.20: ForgeRegistryEntry removed; carry ResourceLocation directly.
 * ResearchProgression requirement deferred (research system not yet ported).
 * ITextComponent → Component, TranslationTextComponent → Component.translatable.
 */
public abstract class CrystalProperty implements Comparable<CrystalProperty> {

    private static int counter = 0;
    private final int sortingId;
    private final ResourceLocation registryName;

    private final List<CrystalPropertyModifierFunction> modifiers = new ArrayList<>();
    private Predicate<CalculationContext> usageTests = ctx -> false;

    public CrystalProperty(ResourceLocation registryName) {
        this.sortingId = counter++;
        this.registryName = registryName;
    }

    public final ResourceLocation getRegistryName() {
        return registryName;
    }

    public CrystalProperty addModifier(CrystalPropertyModifierFunction modifierFunction) {
        this.modifiers.add(modifierFunction);
        return this;
    }

    public CrystalProperty addUsage(Predicate<CalculationContext> usage) {
        this.usageTests = this.usageTests.or(usage);
        return this;
    }

    public int getMaxTier() {
        return 3;
    }

    public boolean hasUsageFor(CalculationContext ctx) {
        return this.usageTests.test(ctx);
    }

    public double modify(double value, int tier, CalculationContext context) {
        double originalValue = value;
        for (CrystalPropertyModifierFunction fn : modifiers) {
            value = fn.modify(value, originalValue, tier, context);
        }
        return value;
    }

    public Component getName(int currentTier) {
        return Component.translatable(String.format("crystal.property.%s.%s.name",
                registryName.getNamespace(), registryName.getPath()));
    }

    @Override
    public int compareTo(CrystalProperty other) {
        return this.sortingId - other.sortingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrystalProperty that = (CrystalProperty) o;
        return Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName);
    }
}
