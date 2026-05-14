/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.FluidTypesAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;

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
     * Registers all DeferredRegisters and mod lifecycle event handlers.
     */
    public void attachLifecycle(@Nonnull IEventBus modBus) {
        // Vanilla Forge registries
        BlocksAS.BLOCKS.register(modBus);
        ItemsAS.ITEMS.register(modBus);
        BlockEntityTypesAS.BLOCK_ENTITY_TYPES.register(modBus);
        MenuTypesAS.MENU_TYPES.register(modBus);
        EntityTypesAS.ENTITY_TYPES.register(modBus);
        FluidsAS.FLUIDS.register(modBus);
        FluidTypesAS.FLUID_TYPES.register(modBus);
        EnchantmentsAS.ENCHANTMENTS.register(modBus);
        EffectsAS.MOB_EFFECTS.register(modBus);
        SoundsAS.SOUND_EVENTS.register(modBus);
        RecipeTypesAS.RECIPE_TYPES.register(modBus);
        RecipeSerializersAS.RECIPE_SERIALIZERS.register(modBus);
        CreativeTabsAS.CREATIVE_TABS.register(modBus);

        // Mod lifecycle events
        modBus.addListener(this::onCommonSetup);
    }

    /**
     * Attaches listeners to the Forge game event bus.
     * Use for: player events, world events, entity events, etc.
     */
    public void attachEventHandlers(@Nonnull IEventBus forgeBus) {}

    private void onCommonSetup(@Nonnull FMLCommonSetupEvent event) {
        // Runs after all registries are frozen.
        // Use event.enqueueWork(() -> { ... }) for thread-unsafe operations.
    }
}
