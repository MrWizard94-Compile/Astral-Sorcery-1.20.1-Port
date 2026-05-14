/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client;

import hellfirepvp.astralsorcery.common.CommonProxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;

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
    public void attachLifecycle(IEventBus modBus) {
        super.attachLifecycle(modBus);
        // FMLClientSetupEvent, EntityRenderersEvent, ParticleFactoryRegisterEvent, etc.
    }

    @Override
    public void attachEventHandlers(IEventBus forgeBus) {
        super.attachEventHandlers(forgeBus);
        // RenderLevelStageEvent (sky renderer), ClientTickEvent, etc.
    }
}
