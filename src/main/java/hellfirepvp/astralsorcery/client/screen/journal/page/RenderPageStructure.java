/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen.journal.page;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.journal.RenderablePage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Journal page renderer that displays a structure schematic as a rotatable 3D isometric view.
 *
 * <p>Replaces the text placeholder. Uses {@link StructureTemplate} NBT data to extract
 * block positions and renders them via {@code BlockRenderDispatcher.renderSingleBlock}.</p>
 *
 * <p>Controls:
 * <ul>
 *   <li>Mouse drag — rotate the schematic around the Y axis</li>
 *   <li>S button — toggle slice/full view</li>
 *   <li>A button — toggle air block visibility</li>
 *   <li>+/- buttons — step through Y slices</li>
 * </ul>
 *
 * <p>1.16 → 1.20: ObserverLib {@code StructureRenderer} removed — replaced with
 * direct {@code BlockRenderDispatcher.renderSingleBlock} using {@code StructureTemplate} NBT.</p>
 */
public class RenderPageStructure implements RenderablePage {

    private static final float BLOCK_SCALE = 7f;
    private static final int COLOR_LABEL = 0x88AAAACC;

    private final List<BlockEntry> sortedBlocks;
    private final Vec3i size;

    private float rotationY = 225f;
    private Optional<Integer> slice = Optional.empty();
    private boolean showAir = false;

    // Current-frame button bounds (rebuilt each render call)
    private Rectangle.Float btnToggleSlice;
    private Rectangle.Float btnToggleAir;
    private Rectangle.Float btnSliceUp;
    private Rectangle.Float btnSliceDown;

    public RenderPageStructure(@Nullable StructureTemplate template) {
        if (template == null || template.getSize().equals(Vec3i.ZERO)) {
            this.sortedBlocks = List.of();
            this.size = Vec3i.ZERO;
        } else {
            this.size = template.getSize();
            this.sortedBlocks = readAndSort(template);
        }
    }

    // =========================================================================
    // RenderablePage
    // =========================================================================

    @Override
    public void render(GuiGraphics graphics, int offsetX, int offsetY,
                        float partialTick, int mouseX, int mouseY) {
        renderStructure(graphics, offsetX, offsetY);
        renderInfo(graphics, offsetX, offsetY);
        renderControls(graphics, offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public boolean propagateMouseDrag(double dx, double dy) {
        rotationY = (rotationY + (float) dx * 2f) % 360f;
        return true;
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseY) {
        if (btnToggleSlice != null && btnToggleSlice.contains(mouseX, mouseY)) {
            slice = slice.isPresent() ? Optional.empty() : Optional.of(getMinSlice());
            return true;
        }
        if (btnToggleAir != null && btnToggleAir.contains(mouseX, mouseY)) {
            showAir = !showAir;
            return true;
        }
        if (btnSliceUp != null && slice.isPresent() && btnSliceUp.contains(mouseX, mouseY)) {
            slice = Optional.of(Math.min(slice.get() + 1, getMaxSlice()));
            return true;
        }
        if (btnSliceDown != null && slice.isPresent() && btnSliceDown.contains(mouseX, mouseY)) {
            slice = Optional.of(Math.max(slice.get() - 1, getMinSlice()));
            return true;
        }
        return false;
    }

    // =========================================================================
    // Rendering helpers
    // =========================================================================

    private void renderStructure(@Nonnull GuiGraphics graphics, int offsetX, int offsetY) {
        if (sortedBlocks.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PoseStack pose = graphics.pose();

        float cx = offsetX + JournalPage.DEFAULT_WIDTH  * 0.5f;
        float cy = offsetY + JournalPage.DEFAULT_HEIGHT * 0.52f;

        pose.pushPose();
        pose.translate(cx, cy, 200);
        pose.mulPose(Axis.XP.rotationDegrees(30));
        pose.mulPose(Axis.YP.rotationDegrees(rotationY));
        pose.scale(BLOCK_SCALE, -BLOCK_SCALE, BLOCK_SCALE);
        pose.translate(-size.getX() * 0.5, -size.getY() * 0.5, -size.getZ() * 0.5);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (BlockEntry entry : sortedBlocks) {
            BlockState state = entry.state();
            if (state.isAir() && !showAir) continue;
            if (slice.isPresent() && entry.pos().getY() != slice.get()) continue;

            pose.pushPose();
            pose.translate(entry.pos().getX(), entry.pos().getY(), entry.pos().getZ());
            mc.getBlockRenderer().renderSingleBlock(
                    state, pose, bufferSource,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY, RenderType.cutoutMipped());
            pose.popPose();
        }

        bufferSource.endBatch();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private void renderInfo(@Nonnull GuiGraphics graphics, int offsetX, int offsetY) {
        if (size.equals(Vec3i.ZERO)) return;
        String szText = size.getX() + " x " + size.getY() + " x " + size.getZ();
        graphics.drawString(Minecraft.getInstance().font, szText, offsetX + 2, offsetY + 8, COLOR_LABEL, false);

        if (slice.isPresent()) {
            int min = getMinSlice();
            int max = getMaxSlice();
            String layerText = "Layer " + (slice.get() - min + 1) + "/" + (max - min + 1);
            graphics.drawString(Minecraft.getInstance().font, layerText, offsetX + 2, offsetY + 20, COLOR_LABEL, false);
        }
    }

    private void renderControls(@Nonnull GuiGraphics graphics, int offsetX, int offsetY,
                                 int mouseX, int mouseY) {
        float bx = offsetX + JournalPage.DEFAULT_WIDTH - 20f;
        float by = offsetY + 6f;

        // [S] toggle slice mode
        btnToggleSlice = new Rectangle.Float(bx, by, 16, 14);
        int sliceColor = slice.isPresent() ? 0xFF4488BB : 0xFF334455;
        drawButton(graphics, btnToggleSlice, sliceColor, "S", mouseX, mouseY);

        // [A] toggle air blocks
        btnToggleAir = new Rectangle.Float(bx - 18, by, 16, 14);
        int airColor = showAir ? 0xFF4488BB : 0xFF334455;
        drawButton(graphics, btnToggleAir, airColor, "A", mouseX, mouseY);

        // [+] / [-] slice step (only in slice mode)
        btnSliceUp   = null;
        btnSliceDown = null;
        if (slice.isPresent()) {
            float sbx = bx;
            float sby = by + 16f;
            if (slice.get() < getMaxSlice()) {
                btnSliceUp = new Rectangle.Float(sbx, sby, 16, 14);
                drawButton(graphics, btnSliceUp, 0xFF336644, "+", mouseX, mouseY);
            }
            if (slice.get() > getMinSlice()) {
                btnSliceDown = new Rectangle.Float(sbx, sby + 16f, 16, 14);
                drawButton(graphics, btnSliceDown, 0xFF664433, "-", mouseX, mouseY);
            }
        }
    }

    private static void drawButton(@Nonnull GuiGraphics graphics, @Nonnull Rectangle.Float rect,
                                    int bgColor, @Nonnull String label,
                                    int mouseX, int mouseY) {
        boolean hovered = rect.contains(mouseX, mouseY);
        int color = hovered ? (bgColor | 0xFF888888) : bgColor;
        graphics.fill((int) rect.x, (int) rect.y,
                (int) (rect.x + rect.width), (int) (rect.y + rect.height), 0xFF000000 | color);
        graphics.drawString(Minecraft.getInstance().font, label,
                (int) (rect.x + 5), (int) (rect.y + 3), 0xFFFFFF, false);
    }

    // =========================================================================
    // Slice helpers
    // =========================================================================

    private int getMinSlice() {
        return sortedBlocks.stream()
                .filter(e -> !e.state().isAir() || showAir)
                .mapToInt(e -> e.pos().getY())
                .min().orElse(0);
    }

    private int getMaxSlice() {
        return sortedBlocks.stream()
                .filter(e -> !e.state().isAir() || showAir)
                .mapToInt(e -> e.pos().getY())
                .max().orElse(0);
    }

    // =========================================================================
    // NBT parsing
    // =========================================================================

    @Nonnull
    private static List<BlockEntry> readAndSort(@Nonnull StructureTemplate template) {
        try {
            CompoundTag nbt = template.save(new CompoundTag());
            ListTag paletteTag = nbt.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocksTag  = nbt.getList("blocks",  Tag.TAG_COMPOUND);

            // Build block state palette from the client level's live registry
            net.minecraft.world.level.Level clientLevel = Minecraft.getInstance().level;
            if (clientLevel == null) return new ArrayList<>();
            HolderGetter<Block> blockGetter = clientLevel.registryAccess()
                    .registryOrThrow(Registries.BLOCK).asLookup();
            BlockState[] palette = new BlockState[paletteTag.size()];
            for (int i = 0; i < paletteTag.size(); i++) {
                try {
                    palette[i] = NbtUtils.readBlockState(blockGetter, paletteTag.getCompound(i));
                } catch (Exception ignored) {
                    palette[i] = Blocks.AIR.defaultBlockState();
                }
            }

            // Parse block list
            List<BlockEntry> list = new ArrayList<>(blocksTag.size());
            for (int i = 0; i < blocksTag.size(); i++) {
                CompoundTag bt = blocksTag.getCompound(i);
                int stateIdx = bt.getInt("state");
                ListTag posTag = bt.getList("pos", Tag.TAG_INT);
                if (posTag.size() >= 3 && stateIdx >= 0 && stateIdx < palette.length) {
                    BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
                    list.add(new BlockEntry(pos, palette[stateIdx]));
                }
            }

            // Sort back-to-front for default 225° rotation:
            // lower Y first, then larger Z first (farther back), then smaller X first
            list.sort(Comparator.comparingInt((BlockEntry e) -> e.pos().getY())
                    .thenComparingInt(e -> -e.pos().getZ())
                    .thenComparingInt(e ->  e.pos().getX()));
            return list;

        } catch (Exception ignored) {
            return List.of();
        }
    }

    // =========================================================================
    // Data
    // =========================================================================

    private record BlockEntry(@Nonnull BlockPos pos, @Nonnull BlockState state) {}
}
