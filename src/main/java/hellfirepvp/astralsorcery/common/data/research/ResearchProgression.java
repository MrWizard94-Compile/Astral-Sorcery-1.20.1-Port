package hellfirepvp.astralsorcery.common.data.research;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Progression stages of Astral Sorcery research. Each stage contains
 * a set of {@link ResearchNode}s that appear in the journal's galaxy view.
 *
 * <p>1.16 → 1.20: removed IExtensibleEnum (not available in Forge 1.20);
 * ITextComponent → Component; TranslationTextComponent → Component.translatable().</p>
 */
public enum ResearchProgression {

    DISCOVERY(ProgressionTier.DISCOVERY),
    BASIC_CRAFT(ProgressionTier.BASIC_CRAFT, DISCOVERY),
    ATTUNEMENT(ProgressionTier.ATTUNEMENT, BASIC_CRAFT),
    CONSTELLATION(ProgressionTier.CONSTELLATION_CRAFT, ATTUNEMENT),
    RADIANCE(ProgressionTier.TRAIT_CRAFT, CONSTELLATION),
    BRILLIANCE(ProgressionTier.BRILLIANCE, RADIANCE);

    private final List<ResearchProgression> preConditions = new LinkedList<>();
    private final List<ResearchNode> researchNodes = new LinkedList<>();
    private final ProgressionTier requiredProgress;
    private final String translationKey;
    private final ResourceLocation registryKey;

    ResearchProgression(ProgressionTier requiredProgress, ResearchProgression... preConditions) {
        this.preConditions.addAll(Arrays.asList(preConditions));
        this.requiredProgress = requiredProgress;
        this.translationKey = AstralSorcery.MODID + ".journal.research." + name().toLowerCase(Locale.ROOT);
        this.registryKey = AstralSorcery.key("research." + name().toLowerCase(Locale.ROOT));
    }

    public Consumer<ResearchNode> getRegistrar() {
        return this::addResearchToGroup;
    }

    void addResearchToGroup(ResearchNode res) {
        for (ResearchNode node : researchNodes) {
            if (node.renderPosX == res.renderPosX && node.renderPosZ == res.renderPosZ) {
                throw new IllegalArgumentException("Two research nodes at same position x=" + res.renderPosX + " z=" + res.renderPosZ +
                        ": " + node.getKey() + " vs " + res.getKey());
            }
        }
        this.researchNodes.add(res);
    }

    public List<ResearchNode> getResearchNodes() {
        return researchNodes;
    }

    public ProgressionTier getRequiredProgress() {
        return requiredProgress;
    }

    public List<ResearchProgression> getPreConditions() {
        return Collections.unmodifiableList(preConditions);
    }

    public ResourceLocation getRegistryKey() {
        return registryKey;
    }

    public Component getName() {
        return Component.translatable(this.translationKey);
    }

    @Nullable
    public static ResearchNode findNode(String name) {
        for (ResearchProgression prog : values()) {
            for (ResearchNode node : prog.getResearchNodes()) {
                if (node.getKey().equals(name)) {
                    return node;
                }
            }
        }
        return null;
    }

    @Nonnull
    public static Collection<ResearchProgression> findProgression(ResearchNode n) {
        Collection<ResearchProgression> progressions = Lists.newArrayList();
        for (ResearchProgression prog : values()) {
            if (prog.getResearchNodes().contains(n)) {
                progressions.add(prog);
            }
        }
        return progressions;
    }
}
