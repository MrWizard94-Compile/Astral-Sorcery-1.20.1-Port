package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Replacement for ObserverLib's BufferDecoratorBuilder.
 * Wraps a VertexConsumer to apply color and lightmap overrides
 * to vertex data before it reaches the underlying buffer.
 *
 * <p>Usage patterns from AS 1.16:</p>
 * <ul>
 *   <li>{@code BufferDecoratorBuilder.withColor((r,g,b,a) -> colors).decorate(buffer)}</li>
 *   <li>{@code new BufferDecoratorBuilder().setColorDecorator(...).decorate(vb, consumer)}</li>
 * </ul>
 */
public class BufferDecoratorBuilder {

    @Nullable
    private ColorDecorator colorDecorator;
    @Nullable
    private LightmapDecorator lightmapDecorator;

    public BufferDecoratorBuilder() {}

    /**
     * Create a builder with a color decorator already set.
     *
     * @param decorator transforms RGBA color values
     * @return a new builder
     */
    @Nonnull
    public static BufferDecoratorBuilder withColor(@Nonnull ColorDecorator decorator) {
        BufferDecoratorBuilder builder = new BufferDecoratorBuilder();
        builder.colorDecorator = decorator;
        return builder;
    }

    /**
     * Create a builder with a lightmap decorator already set.
     *
     * @param decorator transforms skyLight and blockLight values
     * @return a new builder
     */
    @Nonnull
    public static BufferDecoratorBuilder withLightmap(@Nonnull LightmapDecorator decorator) {
        BufferDecoratorBuilder builder = new BufferDecoratorBuilder();
        builder.lightmapDecorator = decorator;
        return builder;
    }

    /**
     * Set or replace the color decorator.
     *
     * @param decorator the color transform
     * @return this builder
     */
    @Nonnull
    public BufferDecoratorBuilder setColorDecorator(@Nonnull ColorDecorator decorator) {
        this.colorDecorator = decorator;
        return this;
    }

    /**
     * Set or replace the lightmap decorator.
     *
     * @param decorator the lightmap transform
     * @return this builder
     */
    @Nonnull
    public BufferDecoratorBuilder setLightmapDecorator(@Nonnull LightmapDecorator decorator) {
        this.lightmapDecorator = decorator;
        return this;
    }

    /**
     * Wrap a vertex consumer with this builder's decorators.
     *
     * @param buffer the original vertex consumer
     * @return a decorated vertex consumer
     */
    @Nonnull
    public VertexConsumer decorate(@Nonnull VertexConsumer buffer) {
        return new DecoratedVertexConsumer(buffer, this.colorDecorator, this.lightmapDecorator);
    }

    /**
     * Wrap a vertex consumer and pass it to a callback for immediate use.
     *
     * @param buffer   the original vertex consumer
     * @param consumer receives the decorated buffer
     */
    public void decorate(@Nonnull VertexConsumer buffer,
                         @Nonnull Consumer<VertexConsumer> consumer) {
        consumer.accept(decorate(buffer));
    }

    /**
     * Transforms RGBA color components.
     * Input: original (r, g, b, a) as ints 0-255.
     * Output: replacement int[4] {r, g, b, a}.
     */
    @FunctionalInterface
    public interface ColorDecorator {

        /**
         * @param r red 0-255
         * @param g green 0-255
         * @param b blue 0-255
         * @param a alpha 0-255
         * @return int[4] replacement {r, g, b, a}
         */
        @Nonnull
        int[] decorate(int r, int g, int b, int a);
    }

    /**
     * Transforms lightmap coordinates.
     * Input: original (skyLight, blockLight).
     * Output: replacement int[2] {skyLight, blockLight}.
     */
    @FunctionalInterface
    public interface LightmapDecorator {

        /**
         * @param skyLight   the sky light value
         * @param blockLight the block light value
         * @return int[2] replacement {skyLight, blockLight}
         */
        @Nonnull
        int[] decorate(int skyLight, int blockLight);
    }

    /**
     * A VertexConsumer wrapper that intercepts color() and uv2() calls
     * to apply decorator transforms before passing to the delegate.
     */
    private static class DecoratedVertexConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        @Nullable
        private final ColorDecorator colorDecorator;
        @Nullable
        private final LightmapDecorator lightmapDecorator;

        DecoratedVertexConsumer(@Nonnull VertexConsumer delegate,
                                @Nullable ColorDecorator colorDecorator,
                                @Nullable LightmapDecorator lightmapDecorator) {
            this.delegate = delegate;
            this.colorDecorator = colorDecorator;
            this.lightmapDecorator = lightmapDecorator;
        }

        @Override
        @Nonnull
        public VertexConsumer vertex(double x, double y, double z) {
            this.delegate.vertex(x, y, z);
            return this;
        }

        @Override
        @Nonnull
        public VertexConsumer color(int r, int g, int b, int a) {
            if (this.colorDecorator != null) {
                int[] c = this.colorDecorator.decorate(r, g, b, a);
                this.delegate.color(c[0], c[1], c[2], c[3]);
            } else {
                this.delegate.color(r, g, b, a);
            }
            return this;
        }

        @Override
        @Nonnull
        public VertexConsumer uv(float u, float v) {
            this.delegate.uv(u, v);
            return this;
        }

        @Override
        @Nonnull
        public VertexConsumer overlayCoords(int u, int v) {
            this.delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        @Nonnull
        public VertexConsumer uv2(int u, int v) {
            if (this.lightmapDecorator != null) {
                int[] lm = this.lightmapDecorator.decorate(u, v);
                this.delegate.uv2(lm[0], lm[1]);
            } else {
                this.delegate.uv2(u, v);
            }
            return this;
        }

        @Override
        @Nonnull
        public VertexConsumer normal(float x, float y, float z) {
            this.delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            if (this.colorDecorator != null) {
                int[] c = this.colorDecorator.decorate(r, g, b, a);
                this.delegate.defaultColor(c[0], c[1], c[2], c[3]);
            } else {
                this.delegate.defaultColor(r, g, b, a);
            }
        }

        @Override
        public void unsetDefaultColor() {
            this.delegate.unsetDefaultColor();
        }
    }
}
