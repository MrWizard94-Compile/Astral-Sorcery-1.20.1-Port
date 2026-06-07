package hellfirepvp.astralsorcery.client.util;

import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Replacement for ObserverLib's RenderTypeDecorator.
 * Wraps a RenderType with pre-render setup and post-render cleanup callbacks.
 * Used to inject GL state changes (blend mode, texture binding, etc.)
 * around render type usage without modifying the render type itself.
 *
 * <p>Usage pattern from AS 1.16:</p>
 * <pre>{@code
 * RenderTypeDecorator decorated = RenderTypeDecorator.wrapSetup(
 *     renderType,
 *     () -> { RenderSystem.enableBlend(); ... },
 *     () -> { RenderSystem.disableBlend(); ... }
 * );
 * VertexConsumer buf = drawBuffer.getBuffer(decorated);
 * }</pre>
 *
 * <p>Extends RenderType so it can be passed to MultiBufferSource.getBuffer().</p>
 */
public class RenderTypeDecorator extends RenderType {

    private final RenderType wrapped;

    private RenderTypeDecorator(@Nonnull RenderType wrapped,
                                @Nonnull Runnable setupAction,
                                @Nonnull Runnable cleanupAction) {
        // Super constructor: name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear
        super(
                "astralsorcery:decorated_" + wrapped.toString(),
                wrapped.format(),
                wrapped.mode(),
                wrapped.bufferSize(),
                wrapped.affectsCrumbling(),
                false, // sortOnUpload — delegate to wrapped
                () -> {
                    wrapped.setupRenderState();
                    setupAction.run();
                },
                () -> {
                    cleanupAction.run();
                    wrapped.clearRenderState();
                }
        );
        this.wrapped = wrapped;
    }

    /**
     * Create a decorated render type that runs setup/cleanup around the wrapped type.
     *
     * @param wrapped       the base render type
     * @param setupAction   called before rendering (after the wrapped type's own setup)
     * @param cleanupAction called after rendering (before the wrapped type's own cleanup)
     * @return a decorated render type
     */
    @Nonnull
    public static RenderTypeDecorator wrapSetup(@Nonnull RenderType wrapped,
                                                 @Nonnull Runnable setupAction,
                                                 @Nonnull Runnable cleanupAction) {
        return new RenderTypeDecorator(wrapped, setupAction, cleanupAction);
    }

    /**
     * Get the underlying render type.
     *
     * @return the wrapped render type
     */
    @Nonnull
    public RenderType getWrapped() {
        return this.wrapped;
    }

    @Override
    @Nonnull
    public Optional<RenderType> outline() {
        return this.wrapped.outline();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        // Two decorators are equal only if they wrap the same type with the same actions
        // In practice, decorators are short-lived and identity equality suffices
        return false;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
