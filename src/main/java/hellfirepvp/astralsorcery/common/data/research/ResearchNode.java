package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.client.resource.query.TextureQuery;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupRegistry;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A node in the research/journal galaxy, representing one knowledge entry.
 * Nodes are positioned in a 2D plane (renderPosX, renderPosZ) within their
 * {@link ResearchProgression} cluster, and can link to other nodes via
 * {@link #addSourceConnectionFrom}.
 *
 * <p>1.16 → 1.20: IItemProvider → ItemLike; ITextComponent → Component;
 * TranslationTextComponent → Component.translatable().</p>
 */
public class ResearchNode {

    private static int counter = 0;

    private final int id;
    private final NodeRenderType nodeRenderType;
    public final float renderPosX, renderPosZ;
    private final String unlocName;

    private ItemStack[] renderItemStacks;
    @OnlyIn(Dist.CLIENT)
    private SpriteQuery renderSpriteQuery;
    @OnlyIn(Dist.CLIENT)
    private TextureQuery backgroundTextureQuery;

    private Color textureColorHint = new Color(0xFFFFFFFF, true);

    private final List<ResearchNode> connectionsTo = new ArrayList<>();
    private final List<JournalPage> pages = new LinkedList<>();

    private ResearchNode(NodeRenderType type, String unlocName, float rPosX, float rPosZ) {
        this.id = counter++;
        this.nodeRenderType = type;
        this.renderPosX = rPosX;
        this.renderPosZ = rPosZ;
        this.unlocName = unlocName;
    }

    public ResearchNode(ItemLike item, String unlocName, float renderPosX, float renderPosZ) {
        this(new ItemStack(item), unlocName, renderPosX, renderPosZ);
    }

    public ResearchNode(ItemStack itemStack, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = new ItemStack[] { itemStack };
    }

    public ResearchNode(ItemLike[] items, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            this.renderItemStacks[i] = new ItemStack(items[i]);
        }
    }

    public ResearchNode(ItemStack[] stacks, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = stacks;
    }

    @OnlyIn(Dist.CLIENT)
    public ResearchNode(SpriteQuery query, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.TEXTURE_SPRITE, unlocName, renderPosX, renderPosZ);
        this.renderSpriteQuery = query;
    }

    @OnlyIn(Dist.CLIENT)
    private void initBackgroundQuery() {
        if (backgroundTextureQuery == null) {
            backgroundTextureQuery = new TextureQuery(AssetLoader.TextureLocation.GUI, "research_frame_wood");
        }
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode node) {
        this.connectionsTo.add(node);
        return this;
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode... nodes) {
        return addSourceConnectionsFrom(Arrays.asList(nodes));
    }

    public ResearchNode addSourceConnectionsFrom(Collection<ResearchNode> nodes) {
        this.connectionsTo.addAll(nodes);
        return this;
    }

    public List<ResearchNode> getConnectionsTo() {
        return connectionsTo;
    }

    public ResearchNode addPage(JournalPage page) {
        pages.add(page);
        return this;
    }

    public boolean canSee(@Nullable PlayerProgress progress) {
        return true;
    }

    public ResearchNode setTextureColorHintWithAlpha(Color textureColorHint) {
        this.textureColorHint = textureColorHint;
        return this;
    }

    public ResearchNode register(ResearchProgression progression) {
        progression.getRegistrar().accept(this);
        return this;
    }

    public ResearchNode addTomeLookup(ItemLike item, int nodePage, ResearchProgression progression) {
        BookLookupRegistry.registerItemLookup(item, this, nodePage, progression);
        return this;
    }

    public ResearchNode addTomeLookup(ItemStack item, int nodePage, ResearchProgression progression) {
        BookLookupRegistry.registerItemLookup(item, this, nodePage, progression);
        return this;
    }

    public Color getTextureColorHint() {
        return textureColorHint;
    }

    public NodeRenderType getNodeRenderType() {
        return nodeRenderType;
    }

    public ItemStack getRenderItemStack() {
        return getRenderItemStack(0);
    }

    public ItemStack getRenderItemStack(long tick) {
        return renderItemStacks[((int) ((tick / 40) % renderItemStacks.length))];
    }

    @OnlyIn(Dist.CLIENT)
    public TextureQuery getBackgroundTexture() {
        initBackgroundQuery();
        return backgroundTextureQuery;
    }

    @OnlyIn(Dist.CLIENT)
    public SpriteQuery getSpriteTexture() {
        return renderSpriteQuery;
    }

    public List<JournalPage> getPages() {
        return pages;
    }

    public Component getName() {
        return Component.translatable(String.format("astralsorcery.journal.node.%s.name", this.getKey()));
    }

    public String getKey() {
        return this.unlocName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResearchNode that = (ResearchNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public enum NodeRenderType {
        ITEMSTACK, TEXTURE_SPRITE
    }
}
