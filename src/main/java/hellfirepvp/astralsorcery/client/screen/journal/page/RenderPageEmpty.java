package hellfirepvp.astralsorcery.client.screen.journal.page;

import hellfirepvp.astralsorcery.common.data.journal.RenderablePage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** A blank journal page — no content. */
@OnlyIn(Dist.CLIENT)
public class RenderPageEmpty implements RenderablePage {

    public static final RenderPageEmpty INSTANCE = new RenderPageEmpty();

    private RenderPageEmpty() {}

    @Override
    public void render(GuiGraphics graphics, int offsetX, int offsetY,
                       float partialTick, int mouseX, int mouseY) {}
}
