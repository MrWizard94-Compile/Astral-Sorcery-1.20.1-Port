# Astral Sorcery 1.20.1 Port — Full Audit
**Date:** 2026-06-01  
**Method:** Systematic per-system diff of port (830 Java files) vs 1.16 reference (1,319 Java files).  
**Delta:** 789 files in 1.16 not in port; 300 files in port not in 1.16 (new port-specific code).  
**Status legend:** ✅ Complete · ⚠️ Partial · ❌ Missing · 🔄 Intentionally Simplified

---

## SYSTEM 1 — Lib Constants & Registry Setup

### Findings
The port uses Forge `DeferredRegister` in lib classes (ItemsAS, BlocksAS, BlockEntityTypesAS, etc.) which is correct for 1.20.1. However, 12 lib constant files and 37 registry setup classes are absent.

**Missing lib constant files:**
| File | What it holds |
|------|--------------|
| `AdvancementsAS` | 5 advancement trigger constants (ALTAR_CRAFT, ATTUNE_CRYSTAL, ATTUNE_SELF, DISCOVER_CONSTELLATION, PERK_LEVEL) |
| `AltarRecipeEffectsAS` | 19 static AltarRecipeEffect constants (sparkle, beam, liquid burst, etc.) — irrelevant if effect system stays removed |
| `CapabilitiesAS` | CHUNK_FLUID_KEY + Capability<ChunkFluidEntry> field |
| `ConstellationEffectsAS` | 12 ConstellationEffectProvider constants (one per major constellation) |
| `ContainerTypesAS` | 6 ContainerType constants (TOME, OBSERVATORY, ALTAR_DISCOVERY, ALTAR_ATTUNEMENT, ALTAR_CONSTELLATION, ALTAR_RADIANCE) |
| `DataAS` | DOMAIN_AS + 4 SaveKey constants (gateway cache, light network, storage network, rock crystal buffer) |
| `DataSerializersAS` | 3 entity data serializers (LONG, VECTOR, FLUID) |
| `GameRulesAS` | IGNORE_SKYLIGHT_CHECK_RULE |
| `IngredientSerializersAS` | FLUID_SERIALIZER + CRYSTAL_SERIALIZER |
| `MantleEffectsAS` | 12 MantleEffect constants (one per major constellation) |
| `MaterialsAS` | 3 block Material constants (MARBLE, BLACK_MARBLE, INFUSED_WOOD) — may be handled inline in 1.20 |
| `PerkConvertersAS` | 3 PerkConverter constants (IDENTITY, FOCUS_ALCARA, FOCUS_GELU) |
| `PerkCustomModifiersAS` | 3 perk attribute modifiers (FOCUS_GELU, FOCUS_ULTERIA, FOCUS_VORUX) |
| `PerkNamesAS` | 54 perk attribute name strings (INC_*/ADD_* constants) |
| `StructureTypesAS` | 11 StructureType constants (altar patterns, ritual pedestal, infuser, etc.) |
| `WorldGenerationAS` | Feature + placement + config constants for world gen setup |

**Missing registry classes (37 total — only RegistryResearch.java exists):**
Critical ones:
- `RegistryBlocks` — registers 80+ blocks (entire marble/blackmarble/infusedwood/ore/tile families)
- `RegistryItems` — registers 100+ items
- `RegistryEntities` — registers 9 entity types + renderers + attributes
- `RegistryCapabilities` — initializes chunk fluid capability
- `RegistryContainerTypes` — registers 6 menu types + screen handlers
- `RegistryData` — creates world cache domain + 4 SaveKey constants
- `RegistryPerkAttributeTypes` — registers 24 attribute types with limiter caps
- `RegistryPerks` — registers all perk nodes
- `RegistryConstellationEffects` — links effects to constellations
- `RegistryMantleEffects` — links mantle effects to constellations
- `RegistryWorldGeneration` — wires configured features and placements
- `RegistryIngredientTypes` / `RegistryRecipeSerializers` / `RegistryRecipeTypes`
- `RegistryLoot` / `RegistryGameRules` / `RegistrySounds` / `RegistryEffects`
- `RegistryEngravingEffects` / `RegistryConstellations`
- `RegistryCrystalProperties` / `RegistryCrystalPropertyUsages`

**Status:** ❌ Registry system essentially absent. The port has DeferredRegisters in lib classes but no class that _calls_ `.register()` on non-trivial systems.

---

## SYSTEM 2 — Starlight Network (CRITICAL)

### Findings
**The starlight network has zero flow physics.** The port tracks network topology but starlight does not actually flow.

**What exists in port:**
- `WorldNetworkHandler` — flat graph: sources, receivers, transmissions as position entries per dimension (SavedData)
- `StarlightNetworkHelper` — static helpers to register/unregister nodes
- `StarlightNetworkRegistry` — maps block types to interface implementations
- `ClientStarlightNetworkCache` — client-side sync cache
- `TransmissionLink`, `NodeConnection` — data containers for links

**What is MISSING (18+ classes):**

| Missing Class | Purpose |
|--------------|---------|
| `TransmissionChain` | Recursive starlight propagation with loss multipliers; resolves source → endpoint chains |
| `TransmissionWorldHandler` | Per-dimension chain management + source-to-chain mapping |
| `StarlightTransmissionHandler` | Top-level: maps dimensions to TransmissionWorldHandler |
| `StarlightUpdateHandler` | Queue of nodes needing recalculation (crystal property changes, efficiency) |
| `TransmissionChunkTracker` | Chunk load/unload event listener; pauses/resumes network segments |
| `BlockTransmutationHandler` | Delivers starlight to non-tile blocks (starlight-driven block conversion recipes) |
| `IPrismTransmissionNode` | Core node interface: throughput, consumption multipliers, link notifications |
| `ITransmissionNode` | Single-output node variant |
| `ITransmissionReceiver` | Extends IPrismTransmissionNode; receives starlight + constellation type |
| `ITransmissionSource` | Extends IPrismTransmissionNode; creates IndependentStarlightSource instances |
| `SimplePrismTransmissionNode` | Base concrete implementation of IPrismTransmissionNode |
| `SimpleTransmissionNode` | Single-output concrete impl |
| `SimpleTransmissionReceiver` | Receiver concrete impl |
| `SimpleTransmissionSourceNode` | Source concrete impl |
| `CrystalTransmissionNode` | Crystal-specific node (crystal properties affect throughput) |
| `CrystalPrismTransmissionNode` | Crystal prism variant |
| `IndependentCrystalSource` | Standalone source (collector crystals) |
| `TransmissionProvider` | Factory for node instances from block type |
| `TransmissionClassRegistry` | Maps block → transmission node class |
| `SourceClassRegistry` | Maps block → source class |

**Tile entity base classes — MISSING (8 classes):**
- `BlockEntityNetwork<T>` — links tile entities to starlight network; first-tick registration; node lifecycle management
- `BlockEntitySourceBase<T>` — extends BlockEntityNetwork; implements IStarlightSource; outgoing link persistence
- `BlockEntityReceiverBase<T>` — extends BlockEntityNetwork; implements IStarlightReceiver
- `BlockEntityTransmissionBase` — base for prism/lens tiles
- `TileAreaOfInfluence` — base for area-effect tiles
- `TileOwned` — ownership tracking (player UUID)
- `TileRequiresMultiblock` — multiblock validation base
- (All concrete BlockEntity* implementations exist but call `StarlightNetworkHelper.register*()` ad-hoc without base class lifecycle management)

**Status:** ❌ Starlight network non-functional. Topology tracked, physics absent.

---

## SYSTEM 3 — Network Packets

### Findings
Port has 30 packets; 24 are missing.

**Missing client→server packets (8):**
| Packet | Effect if missing |
|--------|-------------------|
| `PktAttunePlayerConstellation` | Attunement ceremony can never commit |
| `PktPerkGemModification` | Gem socket insert/drop broken |
| `PktRequestPerkSealAction` | Perk sealing/unsealing broken |
| `PktRequestTeleport` | Gateway teleportation disabled |
| `PktRevokeGatewayAccess` | Gateway access control broken |
| `PktRotateTelescope` | Telescope rotation locked |
| `PktToggleClientOption` | Client option toggles inaccessible |
| `PktUnlockPerk` | Perk tree allocation broken |

**Missing server→client packets (11):**
| Packet | Effect if missing |
|--------|-------------------|
| `PktOpenGui` | Server-initiated GUI opens fail (altars, infusers) |
| `PktOreScan` | Ore-scan visual effects suppressed |
| `PktProgressionUpdate` | Tier-advancement notifications silent |
| `PktShootEntity` | Entity motion/spell effects missing |
| `PktSyncCharge` | Alignment charge bar not updated |
| `PktSyncData` | Dynamic data registry stale on client |
| `PktSyncKnowledge` | Research/knowledge not synced on login |
| `PktSyncModifierSource` | Perk modifiers not applied client-side |
| `PktSyncPerkActivity` | Perk activate/deactivate not synced |
| `PktSyncStepAssist` | Step-height perk non-functional |
| `PktUpdateGateways` | Gateway list stale on client |

**Missing login packets (5):**
| Packet | Effect if missing |
|--------|-------------------|
| `ASLoginPacket` (base) | No login packet infrastructure |
| `PktLoginAcknowledge` | No handshake acknowledgment |
| `PktLoginSyncDataHolder` | Player joins with missing cached data |
| `PktLoginSyncGateway` | Gateway cache empty on join |
| `PktLoginSyncPerkInformation` | Perk tree UI blank on join |

**Status:** ❌ Core gameplay loops (perks, progression, teleportation, attunement) broken.

---

## SYSTEM 4 — Perk System

### Findings
Core perk allocation/deallocation is functional. The port made intentional simplifications, plus has genuine gaps.

**Intentional simplifications (NOT gaps):**
- Hard-coded `PerkTreeData.buildTree()` instead of JSON-driven `PerkTreeLoader` (faster startup)
- Single generic `PerkAttributeType` instead of 25 specialized attribute type classes
- No `ModifierSource` registry (only perks contribute modifiers)
- No `PerkConverter` chain (direct modifiers only)

**Genuine gaps:**
| Gap | Impact |
|-----|--------|
| `PerkAttributeLimiter` missing | No caps on attribute values (health/armor can reach unrealistic values) |
| Equipment modifier pipeline missing (`EquipmentSourceProvider`, `EquipmentModifierSource`, `EquipmentAttributeModifierProvider`) | Vanilla armor/weapons don't get perk-based bonuses |
| `PerkConverter` / `PerkAttributeMap.convertModifier()` missing | Complex balance mechanics (e.g., "convert 50% armor → health") impossible |
| Per-perk NBT state persistence missing | Perks can't track custom state across restarts |
| `CooldownPerk` / `PerkCooldownHelper` missing | Cooldown-based perk abilities must be manually tracked per perk |
| `ProgressGatedPerk` missing | Works inline currently; loses extensibility |

**Status:** ⚠️ Functional for basic use; equipment integration and attribute limits incomplete.

---

## SYSTEM 5 — Blocks, Items, Crafting, Constellation Effects

### Blocks — 🔄 Intentionally Simplified (~90% ported)
- Marble, BlackMarble, InfusedWood: consolidated from per-variant subclasses → single class registered multiple times. Template base classes removed.
- Altar: 4 subclasses (BlockAltarDiscovery etc.) → single `BlockAltar` with `AltarType` enum. Cleaner.
- `BlockCelestialGateway`, `BlockIlluminator`: port has them under different paths; verify registration.
- Missing block property helpers (`PropertiesMarble`, `PropertiesWood`, `PropertiesMisc`) — handled inline; not a functional gap.

### Items — ⚠️ ~95% ported; 4 missing
- `ItemDazzlingGem`, `ItemDazzlingFrame` (quality gem system) — not in port
- `ItemPerkGemDay`, `ItemPerkGemNight`, `ItemPerkGemSky` — port only has base `ItemPerkGem` (no day/night/sky variants)
- Colored Lens: port's `ItemColoredLens` carries LensColor enum; actual per-lens behavior (`blockInBeam()`, `entityInBeam()`) must be externally delegated
- Shifting Stars consolidated (intentional)

### Crafting (Altar Recipe Effects) — ❌ Completely removed
- 19 `AltarRecipeEffect` files removed from port
- `AltarRecipeEffectsAS` lib constants absent
- `ActiveSimpleAltarRecipe` has no effect hooks
- Altars will use no per-recipe visual customization (fallback to tile entity global behavior or nothing)

### Constellation Effects — 🔄 Intentionally Simplified (~100% logic ported)
- 1.16 `ConstellationEffect` base (500+ lines with client rendering, NBT, chunk tracking) → port's `ConstellationEffectProvider` (~70 lines, stateless)
- All 12 constellation effects ported with equivalent logic (healing, crop growth, etc.)
- Client-side `playClientEffect()` removed — visual effects now external/declarative
- No chunk-load tracking or pedestal NBT in port's base

**Status (crafting):** ❌ Altar visual effects system absent. All other blocks/items/constellations ⚠️/🔄.

---

## SYSTEM 6 — Data, Config, and Sync

### Config — ⚠️ Heavily reduced
- Port has `ConfigManager`, `ConfigRegistration`, `ClientConfig`, `CommonConfig` (flat structure, ForgeConfigSpec)
- **Missing:** `ServerConfig` + 9 entry classes (GeneralConfig, CraftingConfig, EntityConfig, LightNetworkConfig, MachineryConfig, PerkConfig, ToolsConfig, WandsConfig, WorldGenConfig)
- **Missing:** 8 config registry classes (AmuletEnchantmentRegistry, EntityTransmutationRegistry, FluidRarityRegistry, OreBlockRarityRegistry, OreItemRarityRegistry, TechnicalEntityRegistry, TileAccelerationBlacklistRegistry, WeightedPerkAttributeRegistry) + 8 set classes

### Data Sync — ❌ Entirely missing
The entire `common/data/sync/` system is absent from port:
- `SyncDataHolder`, `SyncDataRegistry` (core infrastructure)
- `AbstractData`, `AbstractDataProvider`, `ClientData`, `ClientDataReader` (base classes)
- Client sync: `ClientLightBlockEndpoints`, `ClientLightConnections`, `ClientPatreonFlares`, `ClientTimeFreezeEffects`, `ClientTimeFreezeEntities`
- Server data: `DataLightBlockEndpoints`, `DataLightConnections`, `DataPatreonFlares`, `DataTimeFreezeEffects`, `DataTimeFreezeEntities`

**Impact:** Light network visualization, time-freeze effects, patron effects cannot sync to client.

### Data World Persistence — ❌ Partially missing
- Missing: `GatewayCache`, `LightNetworkBuffer`, `RockCrystalBuffer`, `StorageNetworkBuffer`
- Port has `GatewayHandler`, `ChunkFluidEntry`, `BaseWorldData`, `GlobalWorldData`, `SectionWorldData`, `WorldCacheDomain`, `WorldSection` — good foundation
- Missing `DataAS` lib constants to wire the save keys

**Status:** ❌ Sync system absent; config entries incomplete.

---

## SYSTEM 7 — Client VFX System

### Findings
**Complete architectural replacement** — intentional and reasonable.

**1.16:** Three-layer inheritance (EntityComplexFX → EntityDynamicFX → EntityVisualFX) + specialized render contexts per effect type + VFX function objects.

**Port:** Single `EntityVisualFX` base with composition (Consumer lambdas for tick/color/alpha/scale/motion) + `EffectManager` (singleton, RenderType batching) + `EffectHelper` (static factories) + 13 concrete FX classes.

**What is lost:**
- `EffectType` registry pattern (reduces extensibility for mod hooks)
- Specialized render context optimizations per effect
- `FXSource` / `FXOrbital` source system (6 orbital effect files in 1.16 have no port equivalents in the new architecture)
- `EffectHandler`, `EffectRegistrar`, `EffectUpdater` modular handler pipeline

**What is gained:**
- Dramatically simpler codebase
- Composition over inheritance (easier to add behavior)
- Consistent RenderType batching

**Status:** 🔄 Intentional rewrite. Functionally sufficient for all visual effects in port's scope. Orbital source effects may need manual porting.

---

## SYSTEM 8 — Client Screens

### Findings

**Altar screens:** ✅ Ported. ScreenAltarDiscovery/Attunement/Constellation/Radiance use GuiGraphics (1.20 API). `ScreenContainerBaseAS` replaces 1.16's multi-level hierarchy cleanly.

**Perk tree rendering:** ❌ NOT PORTED — critical gap.
Missing:
- `journal/perk/group/PerkPointRenderGroup` — renders perk point halos and connection lines
- `journal/perk/group/PerkRenderGroup` — groups perks for batch rendering
- `journal/perk/group/PerkPointHaloRenderGroup` — halo glow around allocated perks
- `journal/overlay/ScreenJournalOverlay` — overlay showing perk stats/description on hover
- `journal/overlay/ScreenJournalOverlayPerkStatistics` — perk statistics breakdown panel
- `journal/perk/BatchPerkContext`, `DynamicPerkRender`, `PerkRender` — core rendering pipeline
- Port only has `journal/perk/PerkTreeSizeHandler` (layout math only, no drawing)

**Other missing screens:**
- `ScreenConstellationPaper` — constellation discovery paper item GUI
- `ScreenHandTelescope` — hand telescope item GUI
- `client/screen/base/SkyScreen` — base for sky-view screens (telescope/observatory)
- `client/screen/base/TileConstellationDiscoveryScreen` — tile-backed constellation discovery base
- `client/screen/base/TileEntityScreen` — tile-backed screen base

**Status:** ⚠️ Altar screens working; perk tree UI non-renderable; 5+ screens missing.

---

## SYSTEM 9 — World Generation

### Findings

**Features:** ✅ Rock crystal, aquamarine, marble vein, glow flower features ported. 1.20.1 data-driven approach (JSON configured/placed features) is correct.

**Missing components:**
| Missing | Impact |
|---------|--------|
| `ChancePlacement`, `RiverbedPlacement`, `WorldFilteredPlacement` | Sophisticated placement logic lost; dimension-aware filtering gone |
| `WorldFilterConfig`, `ChanceConfig` | Config for above |
| `MarkerManagerAS` | Shrine chest + crystal seeding during structure generation |
| `FeatureAncientShrineStructure` / `FeatureDesertShrineStructure` / `FeatureSmallShrineStructure` | 1.16 wrapper classes (port may handle inline, needs verification) |
| `StructureGenerationConfig`, `FeatureGenerationConfig` | Distance/frequency tuning for structures/features |
| JSON configured/placed feature datapacks | Port may lack the JSON side of 1.20 feature placement |

**Status:** ⚠️ Basic features work; shrine marker seeding absent; custom placement logic missing.

---

## SYSTEM 10 — Entities, Events, Commands

### Entities — ⚠️ Partial
Port has top-level entity classes (`EntityCelestialCrystal`, `EntityStarling`, `EntityLiquidSpark`, etc.)  
**Missing in 1.16 structure (not necessarily absent from port):**
- `entity/goal/` — 4 AI goals (SpectralToolBreakBlockGoal, SpectralToolBreakLogGoal, SpectralToolGoal, SpectralToolMeleeAttackGoal) for spectral tool entity
- `entity/item/EntityCustomItemReplacement` — custom item entity replacement
- `entity/InteractableEntity` — base for interactable entities

### Events — ⚠️ Partial
Port has: EventHandlerBlockStorage, EventHandlerCelestial, EventHandlerEffects, EventHandlerEnchantmentTick, EventHandlerInteract, EventHandlerMantleTick, EventHandlerMining, EventHandlerMisc, EventHandlerPerkCombat, EventHandlerPerkEffects, EventHandlerServerTick.  
**Missing from 1.16 style:** `event/handler/EventHandlerAutoLink`, `event/handler/EventHandlerCache`, plus event classes: `ASRegistryEvents`, `AttributeEvent`, `EventFlags`, `PlayerAffectionFlags`, `StarlightNetworkEvent`.

### Commands — ❌ Missing
7 sub-command files absent: CommandAttune, CommandConstellation, CommandExp, CommandMaximizeAll, CommandProgress, CommandReset, CommandSerialize.  
`ArgumentTypeConstellation` also missing.

**Status:** ⚠️ Events mostly ported; ❌ Commands entirely absent; entities partial.

---

## SYSTEM 11 — Containers/Menus

### Findings
Port has `ContainerAltarRadiance` and menu type constants in `MenuTypesAS`.  
**Missing:**
- `ContainerAltarTrait`, `ContainerObservatory`, `ContainerTileEntity`, `ContainerTome`
- 7 container factory files: `ContainerAltarAttunementProvider`, `ContainerAltarConstellationProvider`, `ContainerAltarDiscoveryProvider`, `ContainerAltarRadianceProvider`, `ContainerObservatoryProvider`, `ContainerTomeProvider`, `CustomContainerProvider`
- `SlotConstellationFocus` (custom slot type)

**Status:** ❌ Most containers unimplemented; server-side GUI opening (`PktOpenGui`) also absent.

---

## SYSTEM 12 — Integration (JEI, CraftTweaker, Curios, Botania)

### JEI — ⚠️ Partial
Port has: `AstralSorceryJEIPlugin` + 5 category classes (Altar, Infuser, LiquidInteraction, Transmutation, Well).  
**Missing from 1.16:** `TieredAltarRecipeTransferHandler`, `JEICategory` base, recipe interaction JEI handlers (`JEIHandlerDropItem`, `JEIHandlerSpawnEntity`, `JEIInteractionResultHandler`, `JEIInteractionResultRegistry`).

### CraftTweaker — ❌ Missing (5 files)
`AltarManager`, `BlockTransmutationManager`, `InfusionManager`, `LiquidInteractionManager`, `WellManager` — entire CraftTweaker integration absent.

### Curios / Botania — ❌ Missing
`IntegrationCurios`, `IntegrationBotania` — both absent. Amulet enchantment system requires Curios.

**Status:** ⚠️ JEI partial; ❌ CraftTweaker/Curios/Botania fully absent.

---

## SYSTEM 13 — Loot, Advancement, Patreon

### Loot — ⚠️ Partial
Port has `GlobalLootModifierAS`. Missing: `CopyGatewayColor` loot function, `LootModifierPerkVoidTrash`, `LootModifierScorchingHeat` (global loot modifiers for perk effects).

### Advancements — ❌ Missing
`AdvancementsAS` constants absent; `ListenerCriterionTrigger` absent; `RegistryAdvancements` absent. No advancement integration.

### Patreon System — ❌ Entirely missing (35 files)
`PatreonData`, `PatreonEffect`, `PatreonEffectHelper`, all type classes (TypeBlockRing, TypeCelestialWings, etc.), all providers, managers, and entities. Entire system absent. (Lower priority — patreon is cosmetic).

**Status:** ⚠️ Loot partial; ❌ Advancements + Patreon absent.

---

## SYSTEM 14 — Mixins

### Findings
Port has: MixinCooldownTracker, MixinWorld, MixinEntity, MixinClientWorld, ServerItemCooldownsAccessor, LevelSkyDarkenAccessor.

**Missing from 1.16 (may be intentional drops):**
- `MixinAttributeModifierManager` — needed for perk attribute modifier injection into vanilla attribute system
- `MixinForgeHooks` — hooks into Forge event system at low level
- `MixinItemPredicate` — custom item predicate matching (advancements)
- `MixinModifiableAttributeInstance` — needed for perk modifiers to work correctly on AttributeInstance
- `MixinServerPlayNetHandler` — server network handler hooks
- `MixinVoxelShapeSpliterator` — collision/physics optimization
- `MixinGameRenderer` — client camera/render hooks
- `MixinItemStack` — item stack NBT hooks
- `MixinParticleManager` — particle system integration

**Status:** ❌ Critical mixins for perk attribute system (`MixinAttributeModifierManager`, `MixinModifiableAttributeInstance`) and item stack hooks absent.

---

## SYSTEM 15 — Utility Classes

### Client Utility — ❌ Multiple subsystems missing
- `client/util/camera/` — 13 files: full camera management system (CameraPath, CameraTransformer, ClientCameraManager, etc.) — needed for attunement ceremonies and observatory camera
- `client/util/obj/` — 6 files: WavefrontObject OBJ model parser (Face, Vertex, GroupObject, etc.) — needed for custom 3D models (telescope, refraction table, etc.)
- `client/util/color/` — ColorThief, ColorUtil, MMCQ — color extraction utilities
- `client/util/draw/` — BufferBatchHelper, BufferContext, RenderInfo — rendering helpers
- `client/util/image/` — ImageTemplate, ImageTemplates, SkyImageGenerator — for sky rendering
- `client/util/word/` — RandomWordGenerator + English/Chinese variants — for random text generation (journal)

### Common Utility — ⚠️ Mostly ported; some gaps
- `common/util/time/` — TimeStopController, TimeStopEffectHelper, TimeStopZone — Horologium time-freeze system (partially in port as `TimeStopController` in common/auxiliary/)
- `common/util/loot/` — LootCollector, LootUtil — loot helper utilities
- `common/util/reflection/` — ReflectionException, ReflectionHelper — reflection utilities
- `common/util/tick/TimeoutListContainer` — timeout tracking utility
- `common/util/dispenser/FluidContainerDispenseBehavior` — fluid dispenser behavior
- `common/util/ASMHookEndpoint` — ASM hook integration

**Status:** ❌ Camera system, OBJ parser, image/draw utilities missing.

---

## SYSTEM 16 — Storage Network

### Findings
1.16 had a complete `common/storage/` package (IStorageNetworkTile, StorageCache, StorageKey, StorageNetwork, StorageNetworkHandler, StoredItemStack).  
Port has `StorageNetworkHelper` in common/auxiliary/ as a simplified replacement.

**Status:** 🔄 Intentionally simplified (per memory: replaced by vanilla capability approach). May need validation that the helper is actually wired to the capability system.

---

## SYSTEM 17 — Crystal Source System

### Findings
1.16 had `common/crystal/source/` with Crystal, Ritual, AttunedSourceInstance, TraitedSourceInstance.  
Port has `CrystalProperties.java` and the property/calc system but crystal source abstractions are absent.

**Status:** ❌ Crystal source system partially missing (may affect collector crystal → ritual pedestal → altar flow).

---

## SYSTEM 18 — Gateway System

### Findings
Port has: `GatewayHandler` (world data), `BlockEntityGateway`, `BlockGateway`, `GatewayHelper`.  
**Missing:** `CelestialGatewayFilter`, `CelestialGatewayHandler` in `common/auxiliary/gateway/` — the actual teleportation filter/handler logic.

**Status:** ❌ Gateway teleportation filtering and handling logic missing.

---

## SUMMARY TABLE

| System | Status | Severity |
|--------|--------|----------|
| Lib Constants / Registry | ❌ 37 registry classes absent | CRITICAL |
| Starlight Network Flow | ❌ Zero flow physics | CRITICAL |
| Network Packets | ❌ 24 packets missing | CRITICAL |
| Container/Menu System | ❌ Most containers absent | CRITICAL |
| Client Screens — Perk Tree | ❌ Rendering system absent | HIGH |
| Data Sync System | ❌ Entire sync/ missing | HIGH |
| Mixins (perk-critical) | ❌ 3 critical mixins absent | HIGH |
| Config Entries | ⚠️ 9 entry classes missing | HIGH |
| Perk Attr Limiter / Equipment | ⚠️ Missing | HIGH |
| Commands | ❌ All 7 sub-commands absent | MEDIUM |
| World Gen Placement | ⚠️ Custom placement missing | MEDIUM |
| Camera Utility System | ❌ 13 files missing | MEDIUM |
| OBJ Parser | ❌ 6 files missing | MEDIUM |
| Client Screens (misc) | ⚠️ 5 screens missing | MEDIUM |
| Altar Recipe Effects | ❌ Removed (19 files) | MEDIUM |
| Gateway Handler | ❌ Filter/handler missing | MEDIUM |
| Entity AI Goals | ❌ 4 spectral tool goals | MEDIUM |
| JEI Integration | ⚠️ Partial | LOW |
| CraftTweaker Integration | ❌ 5 files missing | LOW |
| Curios / Botania | ❌ Both absent | LOW |
| Advancement System | ❌ Missing | LOW |
| Loot Modifiers | ⚠️ 3 missing | LOW |
| Patreon System | ❌ 35 files missing | COSMETIC |
| Blocks | 🔄 ~90% (consolidated) | DONE |
| Items | ⚠️ ~95% (4 missing) | LOW |
| Constellation Effects | 🔄 ~100% logic (simplified) | DONE |
| VFX System | 🔄 Complete rewrite | DONE |
| Perk Core Logic | ⚠️ Functional (gaps noted) | MEDIUM |
| Tile Entity Naming | ✅ Complete | DONE |
| Altar Screens | ✅ Complete | DONE |
