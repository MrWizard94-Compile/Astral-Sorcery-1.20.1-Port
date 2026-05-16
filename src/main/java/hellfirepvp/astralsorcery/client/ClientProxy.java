/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.client.event.ClientRenderEventHandler;
import hellfirepvp.astralsorcery.client.render.tile.RenderAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderAttunementAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderChalice;
import hellfirepvp.astralsorcery.client.render.tile.RenderCollectorCrystal;
import hellfirepvp.astralsorcery.client.render.tile.RenderFountain;
import hellfirepvp.astralsorcery.client.render.tile.RenderGateway;
import hellfirepvp.astralsorcery.client.render.tile.RenderInfuser;
import hellfirepvp.astralsorcery.client.render.tile.RenderLens;
import hellfirepvp.astralsorcery.client.render.tile.RenderPrism;
import hellfirepvp.astralsorcery.client.render.tile.RenderRelay;
import hellfirepvp.astralsorcery.client.render.tile.RenderRitualPedestal;
import hellfirepvp.astralsorcery.client.render.tile.RenderTelescope;
import hellfirepvp.astralsorcery.client.render.tile.RenderWell;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarAttunement;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarConstellation;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarDiscovery;
import hellfirepvp.astralsorcery.client.screen.ScreenAltarRadiance;
import hellfirepvp.astralsorcery.client.sky.AstralSkyRenderer;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
    }

    /**
     * Client setup: register menu screens, keybinds, etc.
     * Fired on the mod event bus after registry events.
     */
    private void onClientSetup(@Nonnull FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register menu screens — all 4 altar tiers
            MenuScreens.register(MenuTypesAS.ALTAR_DISCOVERY.get(), ScreenAltarDiscovery::new);
            MenuScreens.register(MenuTypesAS.ALTAR_ATTUNEMENT.get(), ScreenAltarAttunement::new);
            MenuScreens.register(MenuTypesAS.ALTAR_CONSTELLATION.get(), ScreenAltarConstellation::new);
            MenuScreens.register(MenuTypesAS.ALTAR_RADIANCE.get(), ScreenAltarRadiance::new);

            // TODO: Register keybinds
            // TODO: Register overlay renderers
        });
    }
}
