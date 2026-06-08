/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.constellation;

import com.google.common.base.Preconditions;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Pairs a {@link RenderType} and its backing {@link AbstractRenderableTexture} for
 * a constellation background. Instances are created and registered by
 * {@link ConstellationRenderInfos#init()}.
 *
 * <p>1.16 → 1.20: exact port; no API changes needed.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ConstellationBackgroundInfo {

    private final RenderType renderType;
    private final AbstractRenderableTexture texture;

    ConstellationBackgroundInfo(RenderType renderType, AbstractRenderableTexture texture) {
        Preconditions.checkNotNull(renderType, "renderType");
        Preconditions.checkNotNull(texture, "texture");
        this.renderType = renderType;
        this.texture = texture;
    }

    public RenderType getRenderType() {
        return this.renderType;
    }

    public AbstractRenderableTexture getBackgroundTexture() {
        return this.texture;
    }
}
