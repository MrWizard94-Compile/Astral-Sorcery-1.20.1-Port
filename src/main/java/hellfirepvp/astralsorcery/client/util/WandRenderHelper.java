package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

/**
 * Shared client-side rendering helpers for the Architect and Exchange Wands.
 * Renders ghost block placement previews as colored wireframe boxes in world space,
 * and draws the stored block inventory as item stacks on the HUD.
 */
@OnlyIn(Dist.CLIENT)
public final class WandRenderHelper {

    private static final float R = 0.3f, G = 0.75f, B = 1.0f, A = 0.75f;

    private WandRenderHelper() {}

    /**
     * Renders a colored wireframe outline for each block position in the preview map.
     * Called during AFTER_PARTICLES stage — poseStack is camera-relative.
     */
    @SuppressWarnings("null")
    public static void renderGhostBlocks(Map<BlockPos, BlockState> blocks, PoseStack poseStack) {
        if (blocks.isEmpty()) return;

        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(2.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (BlockPos pos : blocks.keySet()) {
            float x0 = (float) (pos.getX() - cam.x);
            float y0 = (float) (pos.getY() - cam.y);
            float z0 = (float) (pos.getZ() - cam.z);
            float x1 = x0 + 1f, y1 = y0 + 1f, z1 = z0 + 1f;

            // 12 edges of the unit cube
            line(buf, matrix, normal, x0, y0, z0, x1, y0, z0);
            line(buf, matrix, normal, x0, y1, z0, x1, y1, z0);
            line(buf, matrix, normal, x0, y0, z1, x1, y0, z1);
            line(buf, matrix, normal, x0, y1, z1, x1, y1, z1);
            line(buf, matrix, normal, x0, y0, z0, x0, y1, z0);
            line(buf, matrix, normal, x1, y0, z0, x1, y1, z0);
            line(buf, matrix, normal, x0, y0, z1, x0, y1, z1);
            line(buf, matrix, normal, x1, y0, z1, x1, y1, z1);
            line(buf, matrix, normal, x0, y0, z0, x0, y0, z1);
            line(buf, matrix, normal, x1, y0, z0, x1, y0, z1);
            line(buf, matrix, normal, x0, y1, z0, x0, y1, z1);
            line(buf, matrix, normal, x1, y1, z0, x1, y1, z1);
        }

        tess.end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private static void line(BufferBuilder buf, Matrix4f mat, Matrix3f norm,
                              float x0, float y0, float z0, float x1, float y1, float z1) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) return;
        float nx = dx / len, ny = dy / len, nz = dz / len;
        buf.vertex(mat, x0, y0, z0).color(R, G, B, A).normal(norm, nx, ny, nz).endVertex();
        buf.vertex(mat, x1, y1, z1).color(R, G, B, A).normal(norm, nx, ny, nz).endVertex();
    }

    /**
     * Renders the stored block stacks vertically centred on the left edge of the screen.
     * Returns true if anything was drawn.
     */
    @SuppressWarnings("null")
    public static boolean renderStoredBlocksOverlay(GuiGraphics graphics,
                                                     List<Tuple<ItemStack, Integer>> stacks) {
        if (stacks.isEmpty()) return false;

        Minecraft mc = Minecraft.getInstance();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int slotH = 20;
        int x = 4;
        int startY = screenH / 2 - (stacks.size() * slotH) / 2;

        for (int i = 0; i < stacks.size(); i++) {
            Tuple<ItemStack, Integer> entry = stacks.get(i);
            int y = startY + i * slotH;
            graphics.renderItem(entry.getA(), x, y);
            String countStr = entry.getB() < 0 ? "∞" : String.valueOf(entry.getB());
            graphics.drawString(mc.font, countStr, x + 18, y + 6, 0xFFFFFF, true);
        }
        return true;
    }
}
