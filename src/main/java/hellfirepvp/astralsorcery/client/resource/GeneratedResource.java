package hellfirepvp.astralsorcery.client.resource;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nonnull;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Supplier;

/**
 * A texture generated at runtime from a {@link BufferedImage} supplier.
 * Used for procedurally generated content (e.g., constellation star maps).
 *
 * <p>1.16 → 1.20: {@code Texture} → {@code AbstractTexture};
 * {@code NativeImage.PixelFormat.RGBA} removed (format is always RGBA);
 * {@code NativeImage.uploadTextureSub} → {@code NativeImage.upload};
 * {@code TextureManager.loadTexture} → {@code TextureManager.register};
 * registration and upload moved to {@link #invalidateAndReload()}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GeneratedResource extends BindableResource {

    private final Supplier<BufferedImage> imageGen;
    private final boolean blur;
    private final boolean clamp;

    GeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur, boolean clamp) {
        super(NameUtil.prefixPath(key, "dynamic_"));
        this.imageGen = imageGenerator;
        this.blur = blur;
        this.clamp = clamp;
    }

    @Override
    public void bindTexture() {
        if (AssetLibrary.isReloading()) return;
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        // Ensure generated texture is registered — use DynamicAbstractTexture wrapper
        AbstractTexture existing = mgr.getTexture(this.getKey());
        // getTexture returns a SimpleTexture auto-created stub if unknown;
        // we detect ours by checking the custom subclass
        if (!(existing instanceof GeneratedAbstractTexture)) {
            // Not yet registered with our generator — register now
            GeneratedAbstractTexture tex = new GeneratedAbstractTexture(imageGen, blur, clamp);
            mgr.register(this.getKey(), tex);
        }
        mgr.getTexture(this.getKey()).bind();
    }

    @Override
    public void invalidateAndReload() {
        // Release the cached texture; next bindTexture() call re-generates it
        Minecraft.getInstance().getTextureManager().release(this.getKey());
    }

    // ---- Inner implementation ----

    private static class GeneratedAbstractTexture extends AbstractTexture {

        private final Supplier<BufferedImage> imageGen;
        private final boolean blur;
        private final boolean clamp;

        GeneratedAbstractTexture(Supplier<BufferedImage> imageGen, boolean blur, boolean clamp) {
            this.imageGen = imageGen;
            this.blur = blur;
            this.clamp = clamp;
        }

        @Override
        public void load(@Nonnull ResourceManager manager) throws IOException {
            BufferedImage bufferedImage = imageGen.get();
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);

            if (!RenderSystem.isOnRenderThreadOrInit()) {
                // Defer to render thread
                int[] pixelsCopy = pixels.clone();
                RenderSystem.recordRenderCall(() -> uploadToGL(pixelsCopy, width, height));
            } else {
                uploadToGL(pixels, width, height);
            }
        }

        private void uploadToGL(int[] argbPixels, int width, int height) {
            // Convert ARGB int[] to RGBA byte[]
            ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.BIG_ENDIAN);
            for (int argb : argbPixels) {
                buf.put((byte) ((argb >> 16) & 0xFF)); // R
                buf.put((byte) ((argb >> 8)  & 0xFF)); // G
                buf.put((byte) ( argb        & 0xFF)); // B
                buf.put((byte) ((argb >> 24) & 0xFF)); // A
            }
            buf.flip();

            int minFilter = blur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
            int magFilter = blur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
            int wrapMode   = clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT;
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapMode);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapMode);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        }
    }
}
