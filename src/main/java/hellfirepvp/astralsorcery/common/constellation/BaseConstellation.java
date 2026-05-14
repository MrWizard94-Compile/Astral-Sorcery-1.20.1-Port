package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base implementation providing star/connection management and registry name storage.
 * In 1.16 this extended ForgeRegistryEntry; in 1.20 we store the ResourceLocation directly
 * since IForgeRegistryEntry was removed.
 *
 * <p>1.16 → 1.20 changes:
 * extends ForgeRegistryEntry&lt;IConstellation&gt; → stores ResourceLocation directly</p>
 */
public abstract class BaseConstellation implements IConstellation {

    private final List<StarLocation> starLocations = new ArrayList<>();
    private final List<StarConnection> connections = new ArrayList<>();

    @Nonnull
    private ResourceLocation registryName = new ResourceLocation("astralsorcery", "unset");

    protected void setRegistryName(@Nonnull ResourceLocation name) {
        this.registryName = name;
    }

    @Override
    @Nonnull
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @Override
    @Nullable
    public StarLocation addStar(int x, int y) {
        x %= STAR_GRID_INDEX;
        y %= STAR_GRID_INDEX;
        StarLocation star = new StarLocation(x, y);
        if (!starLocations.contains(star)) {
            starLocations.add(star);
            return star;
        }
        return null;
    }

    @Override
    @Nullable
    public StarConnection addConnection(@Nonnull StarLocation star1, @Nonnull StarLocation star2) {
        if (star1.equals(star2)) return null;
        StarConnection sc = new StarConnection(star1, star2);
        if (!connections.contains(sc)) {
            connections.add(sc);
            return sc;
        }
        return null;
    }

    @Override
    @Nonnull
    public List<StarLocation> getStars() {
        return Collections.unmodifiableList(this.starLocations);
    }

    @Override
    @Nonnull
    public List<StarConnection> getStarConnections() {
        return Collections.unmodifiableList(this.connections);
    }
}
