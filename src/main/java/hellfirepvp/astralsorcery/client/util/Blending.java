package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL14;

/**
 * Blend mode presets used by journal and effect rendering.
 *
 * <p>1.16 → 1.20: API signature unchanged, but RenderSystem.defaultBlendFunc() still works.
 * ADDITIVEDARK / OVERLAYDARK use glBlendFuncSeparate via GlStateManager.</p>
 */
@OnlyIn(Dist.CLIENT)
public enum Blending {

    DEFAULT {
        @Override
        public void apply() {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    },

    ADDITIVEDARK {
        @Override
        public void apply() {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }
    },

    OVERLAYDARK {
        @Override
        public void apply() {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
    };

    public abstract void apply();
}
