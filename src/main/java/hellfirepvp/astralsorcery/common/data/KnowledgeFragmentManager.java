package hellfirepvp.astralsorcery.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-side cache of the player's collected knowledge fragments.
 * Updated by {@link hellfirepvp.astralsorcery.common.network.play.server.PktSyncKnowledgeFragments}.
 *
 * <p>Knowledge fragments are loot-drop items that unlock specific journal entries.
 * The server is authoritative; this class only holds the client-side mirror.</p>
 */
public final class KnowledgeFragmentManager {

    private KnowledgeFragmentManager() {}

    private static final Set<ResourceLocation> clientFragments = new HashSet<>();

    /**
     * Replaces the client-side fragment set with the list received from the server.
     */
    @OnlyIn(Dist.CLIENT)
    public static void setClientFragments(@Nonnull List<ResourceLocation> keys) {
        clientFragments.clear();
        clientFragments.addAll(keys);
    }

    /**
     * Returns an unmodifiable view of the client-side fragment set.
     */
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Set<ResourceLocation> getClientFragments() {
        return Collections.unmodifiableSet(clientFragments);
    }

    /**
     * Returns true if the client has the given knowledge fragment.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean hasFragment(@Nonnull ResourceLocation key) {
        return clientFragments.contains(key);
    }
}
