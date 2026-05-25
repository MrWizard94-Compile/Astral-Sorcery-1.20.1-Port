package hellfirepvp.astralsorcery.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A bindable, file-backed texture managed by Minecraft's TextureManager.
 *
 * <p>1.16 → 1.20 changes:
 * <ul>
 *   <li>{@code Texture} → {@code AbstractTexture}</li>
 *   <li>{@code TextureManager.loadTexture()} → {@code TextureManager.register()};
 *       auto-create via {@code getTexture()} is sufficient for simple textures</li>
 *   <li>{@code TextureManager.deleteTexture()} → {@code TextureManager.release()}</li>
 *   <li>{@code RenderSystem.bindTexture(glId)} → {@code AbstractTexture.bind()}</li>
 *   <li>{@code RenderState.TextureState} → {@code RenderStateShard.TextureStateShard}</li>
 * </ul></p>
 *
 * <p>In 1.20.1, {@code TextureManager.getTexture(ResourceLocation)} auto-creates
 * a {@code SimpleTexture} for any unregistered RL, so no explicit pre-loading
 * is required for standard file-backed textures.</p>
 */
@OnlyIn(Dist.CLIENT)
public class BindableResource extends AbstractRenderableTexture.Full implements ReloadableResource {

    protected BindableResource(ResourceLocation key) {
        super(key);
    }

    public SpriteSheetResource asSpriteSheet(int rows, int columns) {
        return new SpriteSheetResource(this, rows, columns);
    }

    @Override
    public void bindTexture() {
        if (AssetLibrary.isReloading()) return;
        // getTexture() auto-loads via SimpleTexture if not already registered
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(this.getKey());
        texture.bind();
    }

    @Override
    public void invalidateAndReload() {
        Minecraft.getInstance().getTextureManager().release(this.getKey());
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return new RenderStateShard.TextureStateShard(this.getKey(), false, false);
    }
}
