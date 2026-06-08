package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Central registry for all constellations. Provides typed lookup by category.
 * In 1.16, this delegated to RegistriesAS custom Forge registries.
 * In 1.20, we use internal sorted sets until the Forge custom registry
 * infrastructure is wired up (the external API is the same).
 *
 * <p>1.16 → 1.20 changes:
 * RegistriesAS.REGISTRY_CONSTELLATIONS.getValue → internal map lookup,
 * RegistriesAS.REGISTRY_CONSTELLATIONS.getValues → allConstellations map</p>
 */
public class ConstellationRegistry {

    private static final Map<ResourceLocation, IConstellation> allConstellations = new LinkedHashMap<>();
    private static final SortedSet<IMajorConstellation> majorConstellations = new TreeSet<>();
    private static final SortedSet<IWeakConstellation> weakConstellations = new TreeSet<>();
    private static final SortedSet<IMinorConstellation> minorConstellations = new TreeSet<>();
    private static final SortedSet<IConstellationSpecialShowup> specialShowupConstellations = new TreeSet<>();

    public static <T extends IConstellation> void addConstellation(@Nonnull T constellation) {
        allConstellations.put(constellation.getRegistryName(), constellation);

        if (constellation instanceof IWeakConstellation weak) {
            if (constellation instanceof IMajorConstellation major) {
                majorConstellations.add(major);
            }
            weakConstellations.add(weak);
        } else if (constellation instanceof IMinorConstellation minor) {
            minorConstellations.add(minor);
        } else {
            AstralSorcery.log.warn(
                    "Tried to register constellation that's neither minor nor major or weak: {}",
                    constellation);
            throw new IllegalArgumentException(
                    "Tried to register non-minor, non-weak and non-major constellation.");
        }
        if (constellation instanceof IConstellationSpecialShowup special) {
            specialShowupConstellations.add(special);
        }
    }

    @Nullable
    public static IConstellation getConstellation(@Nonnull ResourceLocation name) {
        return allConstellations.get(name);
    }

    @Nonnull
    public static Collection<IConstellationSpecialShowup> getSpecialShowupConstellations() {
        return Collections.unmodifiableCollection(specialShowupConstellations);
    }

    @Nonnull
    public static Collection<IWeakConstellation> getWeakConstellations() {
        return Collections.unmodifiableCollection(weakConstellations);
    }

    @Nonnull
    public static Collection<IMajorConstellation> getMajorConstellations() {
        return Collections.unmodifiableCollection(majorConstellations);
    }

    @Nonnull
    public static Collection<IMinorConstellation> getMinorConstellations() {
        return Collections.unmodifiableCollection(minorConstellations);
    }

    @Nonnull
    public static Collection<IConstellation> getAllConstellations() {
        List<IConstellation> all = new ArrayList<>(allConstellations.values());
        Collections.sort(all);
        return all;
    }
}
