package hellfirepvp.astralsorcery.client.resource;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractRenderableTexture {

    private final ResourceLocation key;

    protected AbstractRenderableTexture(ResourceLocation key) {
        this.key = key;
    }

    public final ResourceLocation getKey() {
        return key;
    }

    /** Bind this texture to GL texture unit 0. */
    public abstract void bindTexture();

    /** Return a RenderStateShard that binds this texture when applied. */
    public abstract RenderStateShard.TextureStateShard asState();

    /** UV offset of the texture region: [u, v]. */
    public abstract Tuple<Float, Float> getUVOffset();

    /** Width of the texture region in UV space (0..1). */
    public abstract float getUWidth();

    /** Height of the texture region in UV space (0..1). */
    public abstract float getVWidth();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRenderableTexture that = (AbstractRenderableTexture) o;
        return Objects.equals(this.getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getKey());
    }

    /** Full-texture variant — UV offset is (0, 0) and UV width is 1. */
    public abstract static class Full extends AbstractRenderableTexture {

        public Full(ResourceLocation key) {
            super(key);
        }

        @Override
        public Tuple<Float, Float> getUVOffset() {
            return new Tuple<>(0F, 0F);
        }

        @Override
        public final float getUWidth() {
            return 1.0F;
        }

        @Override
        public final float getVWidth() {
            return 1.0F;
        }
    }
}
