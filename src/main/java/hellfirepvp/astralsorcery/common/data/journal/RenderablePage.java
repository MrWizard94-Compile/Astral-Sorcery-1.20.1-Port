package hellfirepvp.astralsorcery.common.data.journal;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side page renderer for a single journal page.
 * Produced by {@link JournalPage#buildRenderPage}.
 */
@OnlyIn(Dist.CLIENT)
public interface RenderablePage {

    void render(GuiGraphics graphics, int offsetX, int offsetY,
                float partialTick, int mouseX, int mouseY);

    default void postRender(GuiGraphics graphics, int offsetX, int offsetY,
                             float partialTick, int mouseX, int mouseY) {}

    default boolean propagateMouseClick(double mouseX, double mouseY) { return false; }

    default boolean propagateMouseDrag(double offsetX, double offsetY) { return false; }
}
