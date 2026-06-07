/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.EffectManager;
import hellfirepvp.astralsorcery.client.input.KeyBindingsAS;
import hellfirepvp.astralsorcery.client.render.overlay.OverlayAlignmentCharge;
import hellfirepvp.astralsorcery.client.render.overlay.OverlayPerkExperience;
import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.util.tick.TickManager;
import hellfirepvp.astralsorcery.common.starlight.ClientStarlightNetworkCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Central client-side event handler for rendering and tick events.
 *
 * <p>Handles:
 * <ul>
 *   <li>Client tick: ticks the effect manager</li>
 *   <li>World render: renders effects after entities</li>
 *   <li>World unload: clears effect and cache state</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class ClientRenderEventHandler {

    /** Central tick manager for all client-side ITickHandler instances. */
    public static final TickManager CLIENT_TICK_MANAGER = new TickManager();

    @SubscribeEvent
    public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().isPaused()) return;

        CLIENT_TICK_MANAGER.tick(event);
        EffectManager.getInstance().tick();
        KeyBindingsAS.handleInput();
        OverlayAlignmentCharge.INSTANCE.tick();
        OverlayPerkExperience.INSTANCE.tick();
        hellfirepvp.astralsorcery.client.effect.AttunementCameraEffect.INSTANCE.tick();
    }

    @SubscribeEvent
    public void onRenderLevelStage(@Nonnull RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        // Render all AS visual effects
        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();
        EffectManager.getInstance().render(poseStack, bufferSource, partialTick);
        bufferSource.endBatch();
    }

    @SubscribeEvent
    public void onWorldUnload(@Nonnull net.minecraftforge.event.level.LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;

        EffectManager.getInstance().clear();
        ClientStarlightNetworkCache.clear();
    }

    /**
     * Appends dynamic modifier and dynamic enchantment lines to item tooltips.
     *
     * Dynamic modifiers are perk-engraved attribute bonuses stored in item NBT.
     * Dynamic enchantments are Prism/Amulet/perk enchant bonuses that have no
     * real NBT entry — they only appear via {@link EnchantmentHelper#getEnchantments}
     * after our {@link hellfirepvp.astralsorcery.mixin.MixinEnchantmentHelper} hook.
     */
    @SubscribeEvent
    public void onItemTooltip(@Nonnull ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();
        if (tooltip == null) return;

        // Dynamic perk-engraved attribute modifiers (stored in item NBT)
        DynamicModifierHelper.addModifierTooltip(stack, tooltip);

        // Dynamic enchantments (Prism/Amulet) — guard against REAL NBT enchantments only.
        // isEnchanted() is patched by MixinItemStack to return true for dynamic-only items,
        // which would skip this block entirely. getEnchantmentTags() reads raw NBT.
        if (stack.getEnchantmentTags().isEmpty()) {
            Map<Enchantment, Integer> dynamic = EnchantmentHelper.getEnchantments(stack);
            if (!dynamic.isEmpty()) {
                List<Component> enchLines = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> e : dynamic.entrySet()) {
                    enchLines.add(e.getKey().getFullname(e.getValue()));
                }
                tooltip.addAll(enchLines);
            }
        }
    }
}
