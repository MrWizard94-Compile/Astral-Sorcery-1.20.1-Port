/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.capability.CapabilitySetup;
import hellfirepvp.astralsorcery.common.event.EventHandlerBlockStorage;
import hellfirepvp.astralsorcery.common.event.EventHandlerInteract;
import hellfirepvp.astralsorcery.common.event.EventHandlerMisc;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperDamageCancelling;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperInvulnerability;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperTemporaryFlight;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.AttunementCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.FountainEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldFreezingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldMeltableRegistry;
import hellfirepvp.astralsorcery.common.cmd.CommandAstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.event.EventHandlerCelestial;
import hellfirepvp.astralsorcery.common.event.EventHandlerEffects;
import hellfirepvp.astralsorcery.common.event.EventHandlerEnchantmentTick;
import hellfirepvp.astralsorcery.common.event.EventHandlerMantleTick;
import hellfirepvp.astralsorcery.common.event.EventHandlerMining;
import hellfirepvp.astralsorcery.common.event.EventHandlerPerkCombat;
import hellfirepvp.astralsorcery.common.event.EventHandlerPerkEffects;
import hellfirepvp.astralsorcery.common.event.EventHandlerServerTick;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.EngravingEffectsAS;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.lib.StructuresAS;
import hellfirepvp.astralsorcery.common.registry.RegistryResearch;
import hellfirepvp.astralsorcery.common.perk.PerkTreeData;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.loot.GlobalLootModifierAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.perk.effect.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.starlight.StarlightNetworkRegistry;
import hellfirepvp.astralsorcery.common.world.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.structure.StructureTypesAS;
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

        // World generation
        WorldGenerationAS.FEATURES.register(modBus);
        StructureTypesAS.STRUCTURE_TYPES.register(modBus);

        // Global loot modifiers
        GlobalLootModifierAS.LOOT_MODIFIERS.register(modBus);
        LootAS.LOOT_FUNCTION_TYPES.register(modBus);

        // Mod lifecycle events
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(CapabilitySetup::registerCapabilities);
    }

    /**
     * Attaches listeners to the Forge game event bus.
     * Use for: player events, world events, entity events, etc.
     */
    public void attachEventHandlers(@Nonnull IEventBus forgeBus) {
        forgeBus.addGenericListener(net.minecraft.world.entity.Entity.class, CapabilitySetup::attachPlayerCaps);
        forgeBus.addGenericListener(net.minecraft.world.level.chunk.LevelChunk.class, CapabilitySetup::attachChunkCaps);
        forgeBus.addListener(CapabilitySetup::onPlayerClone);

        // Starlight network tick + player sync events
        forgeBus.register(new StarlightNetworkRegistry());

        // Perk effect tick + attribute modifier events
        forgeBus.register(new PerkEffectHelper());

        // Mob effect event hooks (EffectCheatDeath death cancel, EffectDropModifier loot)
        forgeBus.register(new EventHandlerEffects());

        // Sky handler: manages per-dim WorldContext for constellation phase tracking
        forgeBus.register(SkyHandler.getInstance());

        // Celestial events (shooting stars, starlight distribution)
        forgeBus.register(new EventHandlerCelestial());

        // Perk combat effects (damage, crit, life steal, elemental resist)
        forgeBus.register(new EventHandlerPerkCombat());

        // Perk gameplay effects (knockback cancel, rampage, magnet drops, tree fell, etc.)
        forgeBus.register(new EventHandlerPerkEffects());

        // Mining perk effects (break speed, auto-smelt, exp bonus)
        forgeBus.register(new EventHandlerMining());

        // Server tick handling (transmutation decay, starlight network per-dim tick)
        forgeBus.register(new EventHandlerServerTick());

        // Per-player tick callbacks for EnchantmentPlayerTick subclasses (e.g. NightVision)
        forgeBus.register(new EventHandlerEnchantmentTick());

        // Per-player tick callbacks for MantleEffect subclasses + alignment charge regen
        forgeBus.register(new EventHandlerMantleTick());
        forgeBus.register(AlignmentChargeHandler.INSTANCE);

        // Horologium time-stop freeze tick handler + clear on world unload
        forgeBus.register(hellfirepvp.astralsorcery.common.auxiliary.TimeStopController.INSTANCE);
        forgeBus.addListener((net.minecraftforge.event.level.LevelEvent.Unload e) -> {
            if (!e.getLevel().isClientSide())
                hellfirepvp.astralsorcery.common.auxiliary.TimeStopController.INSTANCE.clear();
        });

        // Amulet enchantment tick + DynamicEnchantmentEvent.Add wiring
        forgeBus.register(PlayerAmuletHandler.INSTANCE);
        forgeBus.addListener(PlayerAmuletHandler::onEnchantmentAdd);

        // Server-side scheduled task queue
        forgeBus.register(CommonScheduler.INSTANCE);

        // Commands (registered via RegisterCommandsEvent)
        forgeBus.register(new CommandAstralSorcery());

        // Item interaction dispatch: OverrideInteractItem (wands, linking tools)
        forgeBus.register(EventHandlerInteract.class);

        // Block-storage wand clear on left-click (architect wand, exchange wand)
        forgeBus.register(EventHandlerBlockStorage.class);

        // Misc: lectern+tome journal open, crystal toss tracking, effect cloud cancel,
        //       BlockChangeNotifier dispatch for starlight network auto-link
        forgeBus.register(EventHandlerMisc.class);

        // Event helpers: register event listeners and tick handlers
        EventHelperDamageCancelling.attachListeners(forgeBus);
        EventHelperInvulnerability.attachListeners(forgeBus);
        EventHelperSpawnDeny.attachListeners(forgeBus);
        EventHelperTemporaryFlight.attachListeners(forgeBus);
        EventHelperInvulnerability.attachTickListener(EventHandlerServerTick.SERVER_TICK_MANAGER::register);
        EventHelperSpawnDeny.attachTickListener(EventHandlerServerTick.SERVER_TICK_MANAGER::register);
        EventHelperTemporaryFlight.attachTickListener(EventHandlerServerTick.SERVER_TICK_MANAGER::register);
    }

    private void onCommonSetup(@Nonnull FMLCommonSetupEvent event) {
        // Runs after all registries are frozen.
        // Use event.enqueueWork(() -> { ... }) for thread-unsafe operations.
        event.enqueueWork(() -> {
            PacketChannel.init();
            PerkAttributeTypesAS.init();
            ConstellationsAS.init();
            CrystalPropertiesAS.init();
            EngravingEffectsAS.init();
            ConstellationEffectRegistry.init();
            MantleEffectRegistry.init();
            AstralAdvancementTriggers.init();
            StructuresAS.init();
            RegistryResearch.init();
            PerkTreeData.buildTree();
            LiquidStarlightCraftingRegistry.INSTANCE.init();
            AttunementCraftingRegistry.INSTANCE.init();
            WorldMeltableRegistry.INSTANCE.init();
            WorldFreezingRegistry.INSTANCE.init();
            FountainEffectRegistry.registerAll();
        });
    }
}
