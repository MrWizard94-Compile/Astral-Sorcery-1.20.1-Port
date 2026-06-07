/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crystal;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Properties.*;

/**
 * Generates and upgrades {@link CrystalAttributes} on crystal item stacks.
 *
 * <p>1.16 → 1.20: RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES.getValues()
 * replaced by CrystalPropertyRegistry.INSTANCE.getAllProperties().
 * MathHelper.clamp → Mth.clamp.</p>
 */
public class CrystalGenerator {

    private static final int COUNT_PHYSICAL_PROPERTY_TIERS = 5;
    private static final float CHANCE_PHYSICAL_PROPERTIES = 0.65F;
    private static final List<CrystalProperty> PHYSICAL_PROPERTIES = Lists.newArrayList(
            PROPERTY_SIZE,
            PROPERTY_SHAPE,
            PROPERTY_PURITY
    );

    private static final int COUNT_USAGE_PROPERTY_TIERS = 4;
    private static final float CHANCE_USAGE_PROPERTIES = 0.55F;
    private static final List<CrystalProperty> USAGE_PROPERTIES = Lists.newArrayList(
            PROPERTY_TOOL_DURABILITY,
            PROPERTY_TOOL_EFFICIENCY,
            PROPERTY_RITUAL_RANGE,
            PROPERTY_RITUAL_EFFECT,
            PROPERTY_COLLECTOR_COLLECTION_RATE
    );

    private static final Random RAND = new Random();

    @Nonnull
    public static CrystalAttributes upgradeProperties(ItemStack stack) {
        return upgradeProperties(stack, RAND);
    }

    @Nonnull
    public static CrystalAttributes upgradeProperties(ItemStack stack, Random random) {
        if (!(stack.getItem() instanceof CrystalAttributeItem)) {
            return generateNewAttributes(stack, random);
        }
        CrystalAttributes attr = ((CrystalAttributeItem) stack.getItem()).getAttributes(stack);
        if (attr == null) {
            return generateNewAttributes(stack, random);
        }
        if (!(stack.getItem() instanceof CrystalAttributeGenItem genItem)) {
            return attr;
        }
        int existing = attr.getTotalTierLevel();
        int expected = Mth.clamp(existing + 1,
                genItem.getGeneratedPropertyTiers(),
                genItem.getMaxPropertyTiers());
        int generate = expected - attr.getTotalTierLevel();

        CrystalAttributes.Builder builder = CrystalAttributes.Builder.newBuilder(false);
        builder.addAll(attr);
        for (int i = 0; i < generate; i++) {
            Collection<CrystalProperty> remaining = new ArrayList<>(
                    CrystalPropertyRegistry.INSTANCE.getAllProperties());
            if (canAddAnyProperty(builder, remaining)) {
                while (!addRandomProperty(builder, remaining, random)) {}
            }
        }

        return builder.build();
    }

    @Nullable
    public static CrystalProperty getRandomProperty() {
        return getRandomProperty(RAND);
    }

    @Nullable
    public static CrystalProperty getRandomProperty(Random random) {
        if (random.nextFloat() <= CHANCE_PHYSICAL_PROPERTIES) {
            return MiscUtils.getRandomEntry(PHYSICAL_PROPERTIES, random);
        }
        if (random.nextFloat() <= CHANCE_USAGE_PROPERTIES) {
            return MiscUtils.getRandomEntry(USAGE_PROPERTIES, random);
        }

        Collection<CrystalProperty> remaining = new ArrayList<>(
                CrystalPropertyRegistry.INSTANCE.getAllProperties());
        remaining.removeAll(USAGE_PROPERTIES);
        remaining.removeAll(PHYSICAL_PROPERTIES);
        return MiscUtils.getRandomEntry(remaining, random);
    }

    @Nonnull
    public static CrystalAttributes generateNewAttributes(ItemStack item) {
        return generateNewAttributes(item, RAND);
    }

    @Nonnull
    public static CrystalAttributes generateNewAttributes(ItemStack item, Random random) {
        int toGenerate = 4;
        if (item.getItem() instanceof CrystalAttributeGenItem genItem) {
            toGenerate = genItem.getGeneratedPropertyTiers();
        }
        CrystalAttributes.Builder attrBuilder = CrystalAttributes.Builder.newBuilder(false);

        int totalAdded = 0;
        for (int x = 0; x < COUNT_PHYSICAL_PROPERTY_TIERS; x++) {
            if (totalAdded >= toGenerate) break;
            if (random.nextFloat() <= CHANCE_PHYSICAL_PROPERTIES
                    && canAddAnyProperty(attrBuilder, PHYSICAL_PROPERTIES)) {
                while (!addRandomProperty(attrBuilder, PHYSICAL_PROPERTIES, random)) {}
                totalAdded++;
            }
        }
        for (int x = 0; x < COUNT_USAGE_PROPERTY_TIERS; x++) {
            if (totalAdded >= toGenerate) break;
            if (random.nextFloat() <= CHANCE_USAGE_PROPERTIES
                    && canAddAnyProperty(attrBuilder, USAGE_PROPERTIES)) {
                while (!addRandomProperty(attrBuilder, USAGE_PROPERTIES, random)) {}
                totalAdded++;
            }
        }

        Collection<CrystalProperty> remaining = new ArrayList<>(
                CrystalPropertyRegistry.INSTANCE.getAllProperties());
        remaining.removeAll(USAGE_PROPERTIES);
        remaining.removeAll(PHYSICAL_PROPERTIES);
        while (totalAdded < toGenerate && canAddAnyProperty(attrBuilder, remaining)) {
            while (!addRandomProperty(attrBuilder, remaining, random)) {}
            totalAdded++;
        }

        return attrBuilder.build();
    }

    private static boolean canAddAnyProperty(CrystalAttributes.Builder builder,
                                              Collection<CrystalProperty> properties) {
        return properties.stream()
                .anyMatch(p -> builder.getPropertyLvl(p, 0) < p.getMaxTier());
    }

    private static boolean addRandomProperty(CrystalAttributes.Builder builder,
                                              Collection<CrystalProperty> properties,
                                              Random random) {
        List<CrystalProperty> existing = builder.getProperties();
        existing.removeIf(o -> !properties.contains(o));
        existing.removeIf(property -> builder.getPropertyLvl(property, 0) >= property.getMaxTier());
        CrystalProperty propExisting = MiscUtils.getRandomEntry(existing, random);
        CrystalProperty prop = (random.nextFloat() <= 0.85F && propExisting != null) ? propExisting :
                MiscUtils.getRandomEntry(properties, random);
        if (prop == null) return false;
        int existingLvl = builder.getPropertyLvl(prop, 0);
        if (existingLvl < prop.getMaxTier()) {
            builder.addProperty(prop, 1);
            return true;
        }
        return false;
    }
}
