package hellfirepvp.astralsorcery.client.resource.query;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Lazy-loading handle for an animated {@link SpriteSheetResource}.
 *
 * <p>Wraps a {@link TextureQuery} and composes the resulting texture with a
 * {@code SpriteSheetResource} on first resolution.</p>
 */
@OnlyIn(Dist.CLIENT)
public class SpriteQuery extends TextureQuery {

    private final int rows;
    private final int columns;

    private SpriteSheetResource spriteResource;

    public SpriteQuery(AssetLoader.TextureLocation location, int rows, int columns, String... path) {
        super(location, path);
        this.rows = rows;
        this.columns = columns;
    }

    private SpriteQuery(SpriteSheetResource res) {
        super(null);
        this.spriteResource = res;
        this.rows = res.getRows();
        this.columns = res.getColumns();
    }

    /** Wrap an already-resolved {@link SpriteSheetResource} as a pre-resolved query. */
    @OnlyIn(Dist.CLIENT)
    public static SpriteQuery of(SpriteSheetResource res) {
        return new SpriteQuery(res);
    }

    public int getRows()    { return rows; }
    public int getColumns() { return columns; }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public SpriteSheetResource resolveSprite() {
        if (spriteResource == null) {
            AbstractRenderableTexture base = resolve();
            spriteResource = new SpriteSheetResource(base, rows, columns);
        }
        return spriteResource;
    }
}
