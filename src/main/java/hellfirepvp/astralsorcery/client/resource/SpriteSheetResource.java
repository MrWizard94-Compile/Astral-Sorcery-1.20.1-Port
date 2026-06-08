package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.effect.EffectManager;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Wraps an {@link AbstractRenderableTexture} to expose it as a sprite sheet
 * whose frames are addressed by (row, column) position in the texture atlas.
 *
 * <p>UV coordinates are computed either from the global client tick counter
 * ({@link EffectManager#getClientTick()}) or from a specific effect's age/maxAge
 * lifecycle, allowing both looping and one-shot sprite animations.</p>
 *
 * <p>1.16 → 1.20: {@code ClientScheduler.getClientTick()} →
 * {@code EffectManager.getClientTick()}; {@code EntityComplexFX} →
 * {@code EntityVisualFX}; {@code MathHelper.floor} → {@code Mth.floor};
 * {@code Tuple} import updated to {@code net.minecraft.util.Tuple}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class SpriteSheetResource extends AbstractRenderableTexture {

    protected final float uPart;
    protected final float vPart;
    protected final int frameCount;
    protected final int rows;
    protected final int columns;

    private final AbstractRenderableTexture resource;

    public SpriteSheetResource(AbstractRenderableTexture resource) {
        this(resource, 1, 1);
    }

    public SpriteSheetResource(AbstractRenderableTexture resource, int rows, int columns) {
        super(NameUtil.suffixPath(resource.getKey(), "_sprite"));
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("SpriteSheetResource requires at least 1 row and 1 column");
        }
        this.resource = resource;
        this.rows = rows;
        this.columns = columns;
        this.frameCount = rows * columns;
        this.uPart = 1F / columns;
        this.vPart = 1F / rows;
    }

    @Override
    public void bindTexture() {
        this.resource.bindTexture();
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return this.resource.asState();
    }

    @Override
    public Tuple<Float, Float> getUVOffset() {
        return getUVOffset(EffectManager.getClientTick());
    }

    @Override
    public float getUWidth() {
        return uPart;
    }

    @Override
    public float getVWidth() {
        return vPart;
    }

    public AbstractRenderableTexture getResource() {
        return resource;
    }

    public float getULength() { return uPart; }
    public float getVLength() { return vPart; }
    public int   getFrameCount() { return frameCount; }
    public int   getRows()       { return rows; }
    public int   getColumns()    { return columns; }

    /** UV offset for the given absolute frame timer. */
    public Tuple<Float, Float> getUVOffset(long frameTimer) {
        int frame = (int) (frameTimer % frameCount);
        return new Tuple<>((frame % columns) * uPart, ((float) frame / columns) * vPart);
    }

    /**
     * UV offset for an effect whose sprite position is driven by its age.
     *
     * @param fx                  the effect providing age/maxAge
     * @param pTicks              partial ticks for interpolation
     * @param spriteDisplayFactor multiplier controlling how fast the sprite cycles
     */
    public Tuple<Float, Float> getUVOffset(EntityVisualFX fx, float pTicks, float spriteDisplayFactor) {
        float agePart = fx.getAge() * spriteDisplayFactor + pTicks;
        float perc    = agePart / fx.getMaxAge();
        long frame    = Mth.floor(this.frameCount * perc);
        return getUVOffset(frame);
    }
}
