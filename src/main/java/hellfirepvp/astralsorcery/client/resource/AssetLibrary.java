package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registry for custom textures loaded outside the block/item atlas.
 *
 * <p>Register via {@link net.minecraftforge.client.event.RegisterClientReloadListenersEvent}
 * in {@code ClientProxy}. On resource reload, all registered resources are
 * invalidated so they reload from disk on the next bind.</p>
 *
 * <p>1.16 → 1.20: {@code ISelectiveResourceReloadListener} replaced by
 * {@code SimplePreparableReloadListener}. The VanillaResourceType texture
 * predicate is gone — reload always applies (Forge handles filtering upstream).
 * {@code AstralSkyRenderer.reset()} call removed (sky renderer handles its
 * own reload registration separately).</p>
 */
@OnlyIn(Dist.CLIENT)
public class AssetLibrary extends SimplePreparableReloadListener<Void> {

    public static final AssetLibrary INSTANCE = new AssetLibrary();
    private static boolean reloading = false;

    private static final Map<AssetLoader.TextureLocation, Map<String, AbstractRenderableTexture>> loadedTextures = new HashMap<>();
    private static final Map<ResourceLocation, GeneratedResource> dynamicTextures = new HashMap<>();
    private static final List<ReloadableResource> reloadableResources = new ArrayList<>();

    private AssetLibrary() {}

    public static Supplier<AbstractRenderableTexture> loadReference(AssetLoader.TextureLocation location, String... path) {
        AbstractRenderableTexture tex = loadTexture(location, path);
        return () -> tex;
    }

    public static AbstractRenderableTexture loadTexture(AssetLoader.TextureLocation location, String... path) {
        String name = String.join("/", path);
        AbstractRenderableTexture resource = loadedTextures
                .computeIfAbsent(location, l -> new HashMap<>())
                .computeIfAbsent(name, str -> AssetLoader.loadTexture(location, str));
        reloadableResources.add((ReloadableResource) resource);
        return resource;
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key,
                                                           Supplier<BufferedImage> imageGenerator,
                                                           boolean blur) {
        return loadGeneratedResource(key, imageGenerator, blur, false);
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key,
                                                           Supplier<BufferedImage> imageGenerator,
                                                           boolean blur, boolean clamp) {
        if (dynamicTextures.containsKey(key)) {
            return dynamicTextures.get(key);
        }
        GeneratedResource resource = new GeneratedResource(key, imageGenerator, blur, clamp);
        reloadableResources.add(resource);
        dynamicTextures.put(key, resource);
        return resource;
    }

    public static boolean isReloading() {
        return reloading;
    }

    // ---- SimplePreparableReloadListener ----

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        return null; // No async preparation needed
    }

    @Override
    protected void apply(Void pObject, ResourceManager manager, ProfilerFiller profiler) {
        if (reloading) return;
        reloading = true;
        AstralSorcery.log.info("[AssetLibrary] Refreshing and Invalidating Resources");
        reloadableResources.forEach(ReloadableResource::invalidateAndReload);
        reloading = false;
        AstralSorcery.log.info("[AssetLibrary] Successfully reloaded library.");
    }
}
