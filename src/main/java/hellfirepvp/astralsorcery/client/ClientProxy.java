/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.client.event.ClientRenderEventHandler;
import hellfirepvp.astralsorcery.client.input.KeyBindingsAS;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityCelestialCrystal;
import hellfirepvp.astralsorcery.client.render.overlay.OverlayStarlightGauge;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityFlare;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityGrapplingHook;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityIlluminationSpark;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityItemHighlighted;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityLiquidSpark;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityNocturnalSpark;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityObservatoryHelper;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityShootingStar;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntitySpectralTool;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityStarling;
import hellfirepvp.astralsorcery.client.render.layer.LayerStarryGlow;
import hellfirepvp.astralsorcery.client.render.tile.RenderAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderAttunementAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderChalice;
import hellfirepvp.astralsorcery.client.render.tile.RenderCollectorCrystal;
import hellfirepvp.astralsorcery.client.render.tile.RenderFountain;
import hellfirepvp.astralsorcery.client.render.tile.RenderGateway;
import hellfirepvp.astralsorcery.client.render.tile.RenderInfuser;
import hellfirepvp.astralsorcery.client.render.tile.RenderLens;
import hellfirepvp.astralsorcery.client.render.tile.RenderObservatory;
import hellfirepvp.astralsorcery.client.render.tile.RenderPrism;
import hellfirepvp.astralsorcery.client.render.tile.RenderRelay;
import hellfirepvp.astralsorcery.client.render.tile.RenderRitualPedestal;
import hellfirepvp.astralsorcery.client.render.tile.RenderTelescope;
import hellfirepvp.astralsorcery.client.render.tile.RenderTreeBeacon;
import hellfirepvp.astralsorcery.client.render.tile.RenderWell;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarAttunement;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarConstellation;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarDiscovery;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarRadiance;
import hellfirepvp.astralsorcery.client.sky.AstralSkyRenderer;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * Class: ClientProxy
 * Created by HellFirePvP
 * Ported to 1.20.1 by Rob & Corwin
 *
 * Client-only proxy. All rendering registration, particle systems, keybinds,
 * screen registration, and client event handlers live here.
 *
 * IMPORTANT: Never reference client-only classes from CommonProxy.
 * DistExecutor keeps this class from loading on the server.
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void attachLifecycle(@Nonnull IEventBus modBus) {
        super.attachLifecycle(modBus);

        // Block entity renderer registration (mod bus event)
        modBus.addListener(this::onRegisterRenderers);

        // Player layer registration
        modBus.addListener(this::onAddLayers);

        // Keybind registration
        modBus.addListener(KeyBindingsAS::register);

        // HUD overlay registration
        modBus.addListener(this::onRegisterOverlays);

        // Client setup (menu screens, etc.)
        modBus.addListener(this::onClientSetup);
    }

    @Override
    public void attachEventHandlers(@Nonnull IEventBus forgeBus) {
        super.attachEventHandlers(forgeBus);

        // Effect tick + render, world unload cleanup
        forgeBus.register(new ClientRenderEventHandler());

        // Custom sky renderer (constellation overlay)
        forgeBus.register(new AstralSkyRenderer());
    }

    // =========================================================================
    // Mod bus event handlers
    // =========================================================================

    /**
     * Register block entity renderers.
     * Fired on the mod event bus during client setup.
     */
    private void onRegisterRenderers(@Nonnull EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypesAS.ALTAR.get(), RenderAltar::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.ATTUNEMENT_ALTAR.get(), RenderAttunementAltar::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), RenderCollectorCrystal::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.WELL.get(), RenderWell::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.INFUSER.get(), RenderInfuser::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.LENS.get(), RenderLens::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.PRISM.get(), RenderPrism::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.RELAY.get(), RenderRelay::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.RITUAL_PEDESTAL.get(), RenderRitualPedestal::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.CHALICE.get(), RenderChalice::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.TELESCOPE.get(), RenderTelescope::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.GATEWAY.get(), RenderGateway::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.FOUNTAIN.get(), RenderFountain::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.OBSERVATORY.get(), RenderObservatory::new);
        event.registerBlockEntityRenderer(BlockEntityTypesAS.TREE_BEACON.get(), RenderTreeBeacon::new);

        // Entity renderers
        event.registerEntityRenderer(EntityTypesAS.SPECTRAL_TOOL.get(), RenderEntitySpectralTool::new);
        event.registerEntityRenderer(EntityTypesAS.CELESTIAL_CRYSTAL.get(), RenderEntityCelestialCrystal::new);
        event.registerEntityRenderer(EntityTypesAS.OBSERVATORY_HELPER.get(), RenderEntityObservatoryHelper::new);
        event.registerEntityRenderer(EntityTypesAS.STARLING.get(), RenderEntityStarling::new);
        event.registerEntityRenderer(EntityTypesAS.SHOOTING_STAR.get(), RenderEntityShootingStar::new);
        event.registerEntityRenderer(EntityTypesAS.FLARE.get(), RenderEntityFlare::new);
        event.registerEntityRenderer(EntityTypesAS.ILLUMINATION_SPARK.get(), RenderEntityIlluminationSpark::new);
        event.registerEntityRenderer(EntityTypesAS.NOCTURNAL_SPARK.get(), RenderEntityNocturnalSpark::new);
        event.registerEntityRenderer(EntityTypesAS.LIQUID_SPARK.get(), RenderEntityLiquidSpark::new);
        event.registerEntityRenderer(EntityTypesAS.GRAPPLING_HOOK.get(), RenderEntityGrapplingHook::new);
        event.registerEntityRenderer(EntityTypesAS.ITEM_HIGHLIGHTED.get(), RenderEntityItemHighlighted::new);
    }

    /**
     * Register player render layers (starry glow effect for attuned players).
     */
    @SuppressWarnings("unchecked")
    private void onAddLayers(@Nonnull EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer =
                    (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)
                            (LivingEntityRenderer<?, ?>) event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new LayerStarryGlow(renderer));
            }
        }
    }

    @SuppressWarnings("null")
    private void onRegisterOverlays(@Nonnull RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("starlight_gauge", OverlayStarlightGauge.INSTANCE);
    }

    /**
     * Client setup: register menu screens, keybinds, etc.
     * Fired on the mod event bus after registry events.
     */
    @SuppressWarnings("null")
    private void onClientSetup(@Nonnull FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register menu screens — all 4 altar tiers
            MenuScreens.register(MenuTypesAS.ALTAR_DISCOVERY.get(), ScreenAltarDiscovery::new);
            MenuScreens.register(MenuTypesAS.ALTAR_ATTUNEMENT.get(), ScreenAltarAttunement::new);
            MenuScreens.register(MenuTypesAS.ALTAR_CONSTELLATION.get(), ScreenAltarConstellation::new);
            MenuScreens.register(MenuTypesAS.ALTAR_RADIANCE.get(), ScreenAltarRadiance::new);
        });
    }
}
