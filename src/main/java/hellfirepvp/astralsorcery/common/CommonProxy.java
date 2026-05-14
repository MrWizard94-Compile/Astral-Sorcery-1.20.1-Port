/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * Class: CommonProxy
 * Created by HellFirePvP
 * Ported to 1.20.1 by Rob & Corwin
 *
 * Server-side (and shared) proxy. All server logic, registrations, and
 * event subscriptions that run on both sides live here or in systems
 * called from here.
 */
public class CommonProxy {

    /**
     * Called once during mod construction, before lifecycle events fire.
     * Safe for one-time static setup that does not depend on registries.
     */
    public void initialize() {}

    /**
     * Attaches listeners to the mod event bus.
     * Use for: DeferredRegister attachment, FMLCommonSetupEvent,
     * RegisterCapabilitiesEvent, and all other mod lifecycle events.
     */
    public void attachLifecycle(IEventBus modBus) {}

    /**
     * Attaches listeners to the Forge game event bus.
     * Use for: player events, world events, entity events, etc.
     */
    public void attachEventHandlers(IEventBus forgeBus) {}
}
