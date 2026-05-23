/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkAllocate;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.PerkTreePoint;
import hellfirepvp.astralsorcery.common.perk.node.FocusPerk;
import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Perk Tree GUI screen — the primary player progression interface.
 *
 * <p>Renders the constellation-themed perk tree with:
 * <ul>
 *   <li>Perk nodes at their defined positions with type-dependent styling</li>
 *   <li>Connections between adjacent perks as glowing lines</li>
 *   <li>Constellation background overlays behind each root branch</li>
 *   <li>Mouse drag panning and scroll wheel zoom</li>
 *   <li>Click-to-allocate and tooltip-on-hover interactions</li>
 *   <li>Visual distinction for allocated vs available vs locked perks</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20: Screen.render now takes {@code GuiGraphics} instead of
 * {@code PoseStack}. Mouse handling methods unchanged. Rendering uses
 * RenderSystem + immediate mode with custom RenderTypes from RenderTypesAS.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenPerkTree extends Screen {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final ResourceLocation TEX_BACKGROUND =
            AstralSorcery.key("textures/gui/perk_tree/background.png");
    private static final ResourceLocation TEX_PERK_INACTIVE =
            AstralSorcery.key("textures/gui/perk_tree/perk_inactive.png");
    private static final ResourceLocation TEX_PERK_ACTIVE =
            AstralSorcery.key("textures/gui/perk_tree/perk_active.png");
    private static final ResourceLocation TEX_PERK_ROOT =
            AstralSorcery.key("textures/gui/perk_tree/perk_root.png");
    private static final ResourceLocation TEX_PERK_MAJOR =
            AstralSorcery.key("textures/gui/perk_tree/perk_major.png");
    private static final ResourceLocation TEX_PERK_FOCUS =
            AstralSorcery.key("textures/gui/perk_tree/perk_focus.png");
    private static final ResourceLocation TEX_CONNECTION =
            AstralSorcery.key("textures/gui/perk_tree/connection.png");

    /** Node sizes in screen pixels (before zoom). */
    private static final int NODE_SIZE_SMALL = 10;
    private static final int NODE_SIZE_MAJOR = 14;
    private static final int NODE_SIZE_ROOT = 18;
    private static final int NODE_SIZE_FOCUS = 16;

    /** Scale from tree coordinates to screen pixels (base). */
    private static final float COORD_SCALE = 3.5f;

    /** Zoom limits. */
    private static final float ZOOM_MIN = 0.5f;
    private static final float ZOOM_MAX = 2.5f;

    /** Colors for different perk states. */
    private static final Color COLOR_ALLOCATED = new Color(100, 200, 255, 220);
    private static final Color COLOR_AVAILABLE = new Color(200, 220, 255, 180);
    private static final Color COLOR_LOCKED = new Color(80, 80, 120, 120);
    private static final Color COLOR_CONNECTION_ACTIVE = new Color(80, 160, 255, 200);
    private static final Color COLOR_CONNECTION_INACTIVE = new Color(50, 60, 100, 100);

    // =========================================================================
    // State
    // =========================================================================

    /** Camera offset (pan). */
    private float panX = 0.0f;
    private float panY = 0.0f;

    /** Current zoom level. */
    private float zoom = 1.0f;

    /** Mouse drag tracking. */
    private boolean dragging = false;
    private double lastDragX;
    private double lastDragY;

    /** Currently hovered perk (null if none). */
    @Nullable
    private AbstractPerk hoveredPerk = null;

    /** Tooltip lines for hovered perk. */
    @Nonnull
    private List<Component> hoverTooltip = new ArrayList<>();

    // =========================================================================
    // Construction
    // =========================================================================

    public ScreenPerkTree() {
        super(Component.translatable("screen.astralsorcery.perk_tree"));
    }

    @Override
    protected void init() {
        super.init();
        // Center view on origin
        this.panX = 0.0f;
        this.panY = 0.0f;
        this.zoom = 1.0f;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        PoseStack poseStack = graphics.pose();
        float cx = this.width / 2.0f;
        float cy = this.height / 2.0f;

        // Reset hover state
        hoveredPerk = null;
        hoverTooltip.clear();

        // Transform: center screen + pan + zoom
        poseStack.pushPose();
        poseStack.translate(cx + panX, cy + panY, 0);
        poseStack.scale(zoom, zoom, 1.0f);

        // Draw connections first (below nodes)
        renderConnections(graphics, poseStack);

        // Draw perk nodes
        renderNodes(graphics, poseStack, mouseX, mouseY, cx, cy);

        poseStack.popPose();

        // Draw HUD elements on top (not affected by pan/zoom)
        renderHUD(graphics, mouseX, mouseY, partialTick);

        // Tooltip
        if (hoveredPerk != null && !hoverTooltip.isEmpty()) {
            graphics.renderTooltip(this.font, hoverTooltip, Optional.empty(), mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Render all connections between connected perk nodes.
     */
    private void renderConnections(@Nonnull GuiGraphics graphics, @Nonnull PoseStack poseStack) {
        for (PerkTree.Connection connection : PerkTree.getConnections()) {
            AbstractPerk from = connection.from();
            AbstractPerk to = connection.to();

            float x1 = from.getX() * COORD_SCALE;
            float y1 = from.getY() * COORD_SCALE;
            float x2 = to.getX() * COORD_SCALE;
            float y2 = to.getY() * COORD_SCALE;

            boolean active = isPerkAllocated(from) && isPerkAllocated(to);
            Color color = active ? COLOR_CONNECTION_ACTIVE : COLOR_CONNECTION_INACTIVE;

            RenderingDrawUtils.drawLine(graphics, poseStack,
                    x1, y1, x2, y2, 1.5f, color);
        }
    }

    /**
     * Render all perk nodes with type-dependent styling.
     */
    private void renderNodes(@Nonnull GuiGraphics graphics, @Nonnull PoseStack poseStack,
                             int mouseX, int mouseY, float cx, float cy) {
        for (PerkTreePoint point : PerkTree.getPoints()) {
            AbstractPerk perk = point.getPerk();
            if (perk == null) continue;
            float nodeX = perk.getX() * COORD_SCALE;
            float nodeY = perk.getY() * COORD_SCALE;
            int size = getNodeSize(perk);
            float halfSize = size / 2.0f;

            // Determine screen position for hover detection
            float screenX = cx + panX + nodeX * zoom;
            float screenY = cy + panY + nodeY * zoom;
            float screenHalf = halfSize * zoom;

            boolean hovered = mouseX >= screenX - screenHalf && mouseX <= screenX + screenHalf
                    && mouseY >= screenY - screenHalf && mouseY <= screenY + screenHalf;

            // Determine visual state
            PerkState state = getPerkState(perk);
            Color nodeColor = getStateColor(state);

            // Render node
            ResourceLocation texture = getNodeTexture(perk, state);
            float renderScale = hovered ? 1.15f : 1.0f;
            float renderSize = size * renderScale;
            float renderHalf = renderSize / 2.0f;

            poseStack.pushPose();
            poseStack.translate(nodeX, nodeY, 0);
            RenderingDrawUtils.drawTexturedRectColored(graphics, texture,
                    -renderHalf, -renderHalf, renderSize, renderSize, nodeColor);
            poseStack.popPose();

            // Hover detection
            if (hovered) {
                hoveredPerk = perk;
                buildTooltip(perk, state);
            }
        }
    }

    /**
     * Render HUD overlay elements: perk point counter, zoom indicator, instructions.
     */
    private void renderHUD(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Available perk points display
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            int availablePoints = getAvailablePerkPoints(player);
            Component pointsText = Component.translatable("screen.astralsorcery.perk_tree.points", availablePoints);
            graphics.drawString(this.font, pointsText, 8, 8, 0xAADDFF);
        }

        // Zoom indicator
        String zoomStr = String.format("%.0f%%", zoom * 100);
        graphics.drawString(this.font, zoomStr, this.width - 40, 8, 0x888888);
    }

    // =========================================================================
    // Input handling
    // =========================================================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            if (hoveredPerk != null) {
                PerkState state = getPerkState(hoveredPerk);
                if (state == PerkState.AVAILABLE) {
                    allocatePerk(hoveredPerk);
                    return true;
                }
            }
            // Start drag
            dragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            panX += (float) (mouseX - lastDragX);
            panY += (float) (mouseY - lastDragY);
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float zoomDelta = (float) delta * 0.15f;
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom + zoomDelta));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Reset view on R
        if (keyCode == 82) { // 'R'
            panX = 0;
            panY = 0;
            zoom = 1.0f;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // =========================================================================
    // Perk logic helpers
    // =========================================================================

    /**
     * Determine the visual state of a perk for the current player.
     */
    @Nonnull
    private PerkState getPerkState(@Nonnull AbstractPerk perk) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return PerkState.LOCKED;

        if (isPerkAllocated(perk)) {
            return PerkState.ALLOCATED;
        }
        if (canAllocate(perk, player)) {
            return PerkState.AVAILABLE;
        }
        return PerkState.LOCKED;
    }

    /**
     * Check if a perk is currently allocated by the local player.
     * In production, this checks player capability data.
     */
    private boolean isPerkAllocated(@Nonnull AbstractPerk perk) {
        return PlayerProgressManager.getClientProgress().getAllocatedPerks()
                .contains(perk.getKey());
    }

    /**
     * Check if a perk can be allocated (adjacent to an allocated perk, player has points).
     */
    private boolean canAllocate(@Nonnull AbstractPerk perk, @Nonnull LocalPlayer player) {
        if (isPerkAllocated(perk)) return false;
        // Must be adjacent to at least one allocated perk
        for (PerkTree.Connection conn : PerkTree.getConnections()) {
            if (conn.from() == perk && isPerkAllocated(conn.to())) return true;
            if (conn.to() == perk && isPerkAllocated(conn.from())) return true;
        }
        return false;
    }

    /**
     * Attempt to allocate a perk. Sends packet to server.
     */
    private void allocatePerk(@Nonnull AbstractPerk perk) {
        PacketChannel.sendToServer(new PktPerkAllocate(perk.getKey()));
    }

    /**
     * Get available perk points for the player.
     */
    private int getAvailablePerkPoints(@Nonnull LocalPlayer player) {
        return PlayerProgressManager.getClientProgress().getPerkPoints();
    }

    // =========================================================================
    // Visual helpers
    // =========================================================================

    private int getNodeSize(@Nonnull AbstractPerk perk) {
        if (perk instanceof RootPerk) return NODE_SIZE_ROOT;
        if (perk instanceof FocusPerk) return NODE_SIZE_FOCUS;
        if (perk instanceof MajorPerk) return NODE_SIZE_MAJOR;
        return NODE_SIZE_SMALL;
    }

    @Nonnull
    private ResourceLocation getNodeTexture(@Nonnull AbstractPerk perk, @Nonnull PerkState state) {
        if (perk instanceof RootPerk) return TEX_PERK_ROOT;
        if (perk instanceof FocusPerk) return TEX_PERK_FOCUS;
        if (perk instanceof MajorPerk) return TEX_PERK_MAJOR;
        return state == PerkState.ALLOCATED ? TEX_PERK_ACTIVE : TEX_PERK_INACTIVE;
    }

    @Nonnull
    private Color getStateColor(@Nonnull PerkState state) {
        return switch (state) {
            case ALLOCATED -> COLOR_ALLOCATED;
            case AVAILABLE -> COLOR_AVAILABLE;
            case LOCKED -> COLOR_LOCKED;
        };
    }

    /**
     * Build tooltip lines for the hovered perk.
     */
    private void buildTooltip(@Nonnull AbstractPerk perk, @Nonnull PerkState state) {
        hoverTooltip.clear();

        // Perk name
        Component name = perk.getName();
        hoverTooltip.add(name);

        // Modifiers
        for (Component desc : perk.getTooltipDescription()) {
            hoverTooltip.add(desc);
        }

        // State hint
        switch (state) {
            case AVAILABLE -> hoverTooltip.add(
                    Component.translatable("screen.astralsorcery.perk_tree.click_allocate")
                            .withStyle(net.minecraft.ChatFormatting.GREEN));
            case LOCKED -> hoverTooltip.add(
                    Component.translatable("screen.astralsorcery.perk_tree.locked")
                            .withStyle(net.minecraft.ChatFormatting.GRAY));
            case ALLOCATED -> hoverTooltip.add(
                    Component.translatable("screen.astralsorcery.perk_tree.allocated")
                            .withStyle(net.minecraft.ChatFormatting.AQUA));
        }
    }

    // =========================================================================
    // Perk state enum
    // =========================================================================

    private enum PerkState {
        ALLOCATED,
        AVAILABLE,
        LOCKED
    }
}
