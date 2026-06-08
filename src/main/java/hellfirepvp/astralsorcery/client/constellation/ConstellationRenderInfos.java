/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.constellation;

import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages per-constellation background render info (render type + texture).
 * Backgrounds are registered during client setup by calling {@link #init()},
 * which loads each constellation's background PNG from
 * {@code textures/constellation/background_<simpleName>.png}.
 *
 * <p>Call {@link #getBackgroundRenderInfo(IConstellation)} to obtain the info
 * for rendering a constellation background in a GUI or world context.</p>
 *
 * <p>1.16 note: the original {@code getBackgroundRenderInfo()} always returned
 * {@code null} because the map look-up was commented out. This port fixes that
 * so the registered backgrounds are actually usable.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ConstellationRenderInfos {

    private static final Map<IConstellation, ConstellationBackgroundInfo> backgroundRenderMap = new HashMap<>();

    /**
     * Registers a custom background render type and texture for the given constellation.
     * Calling this more than once for the same constellation overwrites the previous entry.
     */
    public static void registerBackground(@Nonnull IConstellation cst,
                                          @Nonnull RenderType renderType,
                                          @Nonnull AbstractRenderableTexture texture) {
        backgroundRenderMap.put(cst, new ConstellationBackgroundInfo(renderType, texture));
    }

    /**
     * Returns the background render info for the given constellation, or {@code null}
     * if no background has been registered.
     */
    @Nullable
    public static ConstellationBackgroundInfo getBackgroundRenderInfo(@Nonnull IConstellation cst) {
        return backgroundRenderMap.get(cst);
    }

    /**
     * Loads and registers background render info for every constellation known to
     * {@link ConstellationRegistry}. Each constellation's background texture is
     * expected at {@code textures/constellation/background_<simpleName>.png}.
     *
     * <p>Call once from the client setup event, after {@code TexturesAS.init()}.</p>
     */
    public static void init() {
        backgroundRenderMap.clear();
        for (IConstellation cst : ConstellationRegistry.getAllConstellations()) {
            AbstractRenderableTexture tex = AssetLoader.loadTexture(
                    AssetLoader.TextureLocation.CONSTELLATION,
                    "background_" + cst.getSimpleName());
            RenderType rType = RenderTypesAS.createConstellationBackgroundType(
                    cst.getSimpleName(), tex);
            registerBackground(cst, rType, tex);
        }
    }
}
