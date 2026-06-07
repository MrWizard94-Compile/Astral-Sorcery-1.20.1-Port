package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktGatewayTeleport;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestGatewayList;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncGatewayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Gateway selection screen — lists all gateways in the network so the player
 * can click one to teleport there.
 * Opened when the player right-clicks a Celestial Gateway block.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenGateway extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 200;
    private static final int ENTRY_H = 22;
    private static final int PADDING = 8;

    private int panelLeft;
    private int panelTop;
    private int hoveredEntry = -1;

    public ScreenGateway() {
        super(Component.translatable("astralsorcery.screen.gateway.title"));
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (width - PANEL_W) / 2;
        panelTop  = (height - PANEL_H) / 2;
        // Request fresh gateway list from server
        PacketChannel.sendToServer(new PktRequestGatewayList());
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        // Panel background
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_W, panelTop + PANEL_H, 0xCC0A0A1E);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_W, panelTop + 1, 0xFF6060C0);
        graphics.fill(panelLeft, panelTop + PANEL_H - 1, panelLeft + PANEL_W, panelTop + PANEL_H, 0xFF6060C0);
        graphics.fill(panelLeft, panelTop, panelLeft + 1, panelTop + PANEL_H, 0xFF6060C0);
        graphics.fill(panelLeft + PANEL_W - 1, panelTop, panelLeft + PANEL_W, panelTop + PANEL_H, 0xFF6060C0);

        // Title
        graphics.drawCenteredString(font, title, width / 2, panelTop + PADDING, 0xCCCCFF);

        List<PktSyncGatewayList.GatewayClientEntry> entries = PktSyncGatewayList.GatewayClientCache.getEntries();

        if (entries.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("astralsorcery.screen.gateway.none"),
                    width / 2, panelTop + PANEL_H / 2 - 4, 0xAAAAAA);
        } else {
            hoveredEntry = -1;
            int entryY = panelTop + PADDING + 14;
            for (int i = 0; i < entries.size(); i++) {
                PktSyncGatewayList.GatewayClientEntry entry = entries.get(i);
                int ex = panelLeft + PADDING;
                int ey = entryY + i * ENTRY_H;

                boolean hovered = mouseX >= ex && mouseX < panelLeft + PANEL_W - PADDING
                        && mouseY >= ey && mouseY < ey + ENTRY_H - 2;
                if (hovered) hoveredEntry = i;

                int bg = hovered ? 0x803050A0 : 0x40203060;
                graphics.fill(ex, ey, panelLeft + PANEL_W - PADDING, ey + ENTRY_H - 2, bg);

                String name = entry.displayName() != null && !entry.displayName().isEmpty()
                        ? entry.displayName()
                        : entry.position().toShortString();
                String dim = entry.dimension().getPath();
                graphics.drawString(font, name, ex + 4, ey + 3, 0xDDDDFF, false);
                graphics.drawString(font, dim, panelLeft + PANEL_W - PADDING - font.width(dim) - 4,
                        ey + 3, 0x8888AA, false);

                if (entryY + (i + 1) * ENTRY_H > panelTop + PANEL_H - PADDING) break;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredEntry >= 0) {
            List<PktSyncGatewayList.GatewayClientEntry> entries =
                    PktSyncGatewayList.GatewayClientCache.getEntries();
            if (hoveredEntry < entries.size()) {
                PktSyncGatewayList.GatewayClientEntry entry = entries.get(hoveredEntry);
                PacketChannel.sendToServer(
                        new PktGatewayTeleport(entry.position(), entry.dimension().toString()));
                if (minecraft != null) minecraft.setScreen(null);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
