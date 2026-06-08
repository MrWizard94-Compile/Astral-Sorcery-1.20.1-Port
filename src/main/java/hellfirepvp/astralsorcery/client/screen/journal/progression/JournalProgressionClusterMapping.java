package hellfirepvp.astralsorcery.client.screen.journal.progression;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps each {@link ResearchProgression} to its {@link JournalCluster} layout.
 * Initialized lazily on first access (after TexturesAS.init() has run).
 */
@OnlyIn(Dist.CLIENT)
public class JournalProgressionClusterMapping {

    private static volatile Map<ResearchProgression, JournalCluster> clusterMapping;

    private static void ensureInit() {
        if (clusterMapping != null) return;
        synchronized (JournalProgressionClusterMapping.class) {
            if (clusterMapping != null) return;
            Map<ResearchProgression, JournalCluster> map = new HashMap<>();

            AbstractRenderableTexture tex;

            tex = TexturesAS.TEX_GUI_CLUSTER_DISCOVERY;
            map.put(ResearchProgression.DISCOVERY,  new JournalCluster(tex, tex, -2, -2,  0,  0));

            tex = TexturesAS.TEX_GUI_CLUSTER_BASICCRAFT;
            map.put(ResearchProgression.BASIC_CRAFT, new JournalCluster(tex, tex,  0,  1,  3,  3));

            tex = TexturesAS.TEX_GUI_CLUSTER_ATTUNEMENT;
            map.put(ResearchProgression.ATTUNEMENT, new JournalCluster(tex, tex,  2, -2,  4,  0));

            tex = TexturesAS.TEX_GUI_CLUSTER_CONSTELLATION;
            map.put(ResearchProgression.CONSTELLATION, new JournalCluster(tex, tex, 4,  0,  7,  2));

            tex = TexturesAS.TEX_GUI_CLUSTER_RADIANCE;
            map.put(ResearchProgression.RADIANCE,    new JournalCluster(tex, tex,  5, -3,  8, -1));

            tex = TexturesAS.TEX_GUI_CLUSTER_BRILLIANCE;
            map.put(ResearchProgression.BRILLIANCE,  new JournalCluster(tex, tex,  8, -1, 10,  1));

            clusterMapping = map;
        }
    }

    public static void register(ResearchProgression prog, JournalCluster cluster) {
        ensureInit();
        clusterMapping.put(prog, cluster);
    }

    @Nonnull
    public static JournalCluster getClusterMapping(ResearchProgression progression) {
        ensureInit();
        JournalCluster cluster = clusterMapping.get(progression);
        if (cluster == null) {
            throw new IllegalArgumentException("ResearchProgression " + progression.name() + " has no registered cluster!");
        }
        return cluster;
    }
}
