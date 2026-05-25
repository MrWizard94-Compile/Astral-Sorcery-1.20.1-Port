package hellfirepvp.astralsorcery.client.resource.query;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * Lazy-loading handle for a single texture loaded via {@link AssetLibrary}.
 *
 * <p>Resolution is deferred to the first call of {@link #resolve()} so that
 * textures are not allocated before the rendering context is ready.</p>
 */
@OnlyIn(Dist.CLIENT)
public class TextureQuery {

    private final AssetLoader.TextureLocation location;
    private final String[] path;

    private Supplier<AbstractRenderableTexture> resource;

    public TextureQuery(AssetLoader.TextureLocation location, String... path) {
        this.location = location;
        this.path = path;
    }

    @OnlyIn(Dist.CLIENT)
    public AbstractRenderableTexture resolve() {
        if (resource == null) {
            resource = AssetLibrary.loadReference(location, path);
        }
        return resource.get();
    }
}
