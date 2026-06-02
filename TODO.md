# Astral Sorcery 1.20.1 Port — Master TODO
**Generated:** 2026-06-01  
**Source:** AUDIT.md (full system-by-system comparison against 1.16 reference)

Dependency notation: `[depends: X]` means X must be done first.  
Priority: 🔴 Critical (blocks play) · 🟠 High (major feature broken) · 🟡 Medium · 🟢 Low · ⚪ Cosmetic

---

## PHASE A — Foundation (must be done before almost everything else)

### A1 — Lib Constant Files 🔴
These are referenced by everything else. Add missing constants to existing or new lib files.

- [ ] **A1.1** Add `CapabilitiesAS` — CHUNK_FLUID_KEY + Capability<ChunkFluidEntry> field
- [ ] **A1.2** Add `ConstellationEffectsAS` — 12 ConstellationEffectProvider constants
- [ ] **A1.3** Add `ContainerTypesAS` — 6 ContainerType/MenuType constants (or extend `MenuTypesAS`)
- [ ] **A1.4** Add `DataAS` — DOMAIN_AS + 4 SaveKey constants
- [ ] **A1.5** Add `DataSerializersAS` — 3 EntityDataSerializer constants (LONG, VECTOR, FLUID)
- [ ] **A1.6** Add `GameRulesAS` — IGNORE_SKYLIGHT_CHECK_RULE
- [ ] **A1.7** Add `IngredientSerializersAS` — FLUID_SERIALIZER + CRYSTAL_SERIALIZER
- [ ] **A1.8** Add `MantleEffectsAS` — 12 MantleEffect constants
- [ ] **A1.9** Add `PerkNamesAS` — 54 perk attribute name string constants
- [ ] **A1.10** Add `PerkConvertersAS` + `PerkCustomModifiersAS` — converter/modifier constants
- [ ] **A1.11** Add `StructureTypesAS` — 11 StructureType constants (or port's equivalent pattern type IDs)
- [ ] **A1.12** Add `AdvancementsAS` — 5 advancement trigger constants (once advancement system is ready)
- [ ] **A1.13** Add `WorldGenerationAS` constants (features, placements, configs)

### A2 — Registry Wiring 🔴
The port uses DeferredRegister objects but has no class calling `.register()` for most systems.

- [ ] **A2.1** Create/complete `RegistryBlocks` — call `.register()` for all deferred blocks in `BlocksAS` + verify all ~80 blocks are in the DeferredRegister
- [ ] **A2.2** Create/complete `RegistryItems` — call `.register()` for all deferred items in `ItemsAS`; link block items
- [ ] **A2.3** Create `RegistryEntities` — register 9 entity types, wire renderers (client-only), set attributes for FLARE + SPECTRAL_TOOL `[depends: A1]`
- [ ] **A2.4** Create `RegistryCapabilities` — initialize chunk fluid capability via CapabilityManager, attach to Chunk `[depends: A1.1]`
- [ ] **A2.5** Create `RegistryContainerTypes` — register 6 menu types + screen handlers (client-only) `[depends: A1.3]`
- [ ] **A2.6** Create `RegistryData` — create world cache domain, populate DataAS SaveKey constants `[depends: A1.4]`
- [ ] **A2.7** Create `RegistryPerkAttributeTypes` — register 24 attribute types with PerkAttributeLimiter caps `[depends: B1.1]`
- [ ] **A2.8** Create `RegistryPerks` — call register on all PerkTree nodes
- [ ] **A2.9** Create `RegistryConstellationEffects` — link 12 ConstellationEffectProvider constants to constellations `[depends: A1.2]`
- [ ] **A2.10** Create `RegistryMantleEffects` — link 12 mantle effects to constellations `[depends: A1.8]`
- [ ] **A2.11** Create `RegistryLoot` — register global loot modifiers
- [ ] **A2.12** Create `RegistryGameRules` — register IGNORE_SKYLIGHT_CHECK_RULE `[depends: A1.6]`
- [ ] **A2.13** Create `RegistryIngredientTypes` — register custom ingredient serializers `[depends: A1.7]`
- [ ] **A2.14** Create `RegistryRecipeSerializers` + `RegistryRecipeTypes` — verify all recipe types wired
- [ ] **A2.15** Create `RegistryConstellations` — register all constellation objects
- [ ] **A2.16** Create `RegistryCrystalProperties` + `RegistryCrystalPropertyUsages`
- [ ] **A2.17** Create `RegistrySounds`, `RegistryEffects` (potion effects), `RegistryEngravingEffects`
- [ ] **A2.18** Create `RegistryWorldGeneration` — wire configured/placed features and structure types `[depends: A1.13, C3]`
- [ ] **A2.19** Create `RegistryStructures` + `RegistryStructureTypes`
- [ ] **A2.20** Wire all registries into `AstralSorcery.java` mod constructor / FMLCommonSetupEvent / FMLClientSetupEvent

---

## PHASE B — Perk System Completion

### B1 — Perk Attribute Infrastructure 🟠

- [ ] **B1.1** Port `PerkAttributeLimiter` — value clamping system; register caps for dodge (0.75), elemental resist (0.6), life leech (0.2), cooldown reduction (0.8)
- [ ] **B1.2** Port `PerkConverter` base interface + `PerkAttributeMap.convertModifier()` — needed for FOCUS_ALCARA, FOCUS_GELU converters
- [ ] **B1.3** Port `PerkConvertersAS` constants (IDENTITY, FOCUS_ALCARA, FOCUS_GELU) `[depends: B1.2]`
- [ ] **B1.4** Port `PerkCustomModifiersAS` modifier constants (FOCUS_GELU, FOCUS_ULTERIA, FOCUS_VORUX)
- [ ] **B1.5** Port `CooldownPerk` interface + `PerkCooldownHelper` — cooldown tracking for ability perks
- [ ] **B1.6** Port `ProgressGatedPerk` interface for tier-gated perks

### B2 — Equipment Modifier Pipeline 🟡

- [ ] **B2.1** Port `ModifierSource` interface + `ModifierSourceProvider` registry interface
- [ ] **B2.2** Port `EquipmentModifierSource`, `EquipmentSourceProvider`, `EquipmentAttributeModifierProvider`
- [ ] **B2.3** Port `AttributeConverterProvider` + `AttributeModifierProvider` `[depends: B1.2]`

### B3 — Perk State Persistence 🟡

- [ ] **B3.1** Add per-perk NBT serialization to `AbstractPerk.saveData()` / `loadData()`
- [ ] **B3.2** Verify `PlayerPerkData` stores and restores perk-specific state across relog

### B4 — Perk Attribute Type Specialization 🟡

- [ ] **B4.1** Evaluate whether 25 individual AttributeType classes need porting or whether the single generic `PerkAttributeType` can be extended to cover special behaviors (UUID stability, on-apply hooks)
- [ ] **B4.2** Port `VanillaAttributeType` / `VanillaPerkAttributeType` if generic type is insufficient for vanilla attribute backing

---

## PHASE C — Starlight Network (CRITICAL — blocks all altar/collector/ritual gameplay)

### C1 — Transmission Node Hierarchy 🔴

- [ ] **C1.1** Port `IPrismTransmissionNode` interface (throughput, consumption, link notifications, crystal attributes)
- [ ] **C1.2** Port `ITransmissionNode`, `ITransmissionReceiver`, `ITransmissionSource` sub-interfaces `[depends: C1.1]`
- [ ] **C1.3** Port `SimplePrismTransmissionNode` (concrete base impl) `[depends: C1.1]`
- [ ] **C1.4** Port `SimpleTransmissionNode`, `SimpleTransmissionReceiver`, `SimpleTransmissionSourceNode` `[depends: C1.3]`
- [ ] **C1.5** Port `CrystalTransmissionNode`, `CrystalPrismTransmissionNode` (crystal-property-driven throughput) `[depends: C1.3]`
- [ ] **C1.6** Port `IndependentCrystalSource` (collector crystal standalone source) `[depends: C1.5]`

### C2 — Registry & Factory 🔴 `[depends: C1]`

- [ ] **C2.1** Port `TransmissionProvider` — factory interface for creating node instances from block type
- [ ] **C2.2** Port `TransmissionClassRegistry` — maps Block → transmission node class
- [ ] **C2.3** Port `SourceClassRegistry` — maps Block → source class

### C3 — Network Computation Engine 🔴 `[depends: C1, C2]`

- [ ] **C3.1** Port `TransmissionChain` — recursive starlight propagation; loss multipliers; source→endpoint resolution
- [ ] **C3.2** Port `TransmissionWorldHandler` — per-dimension chain management + source-to-chain mapping
- [ ] **C3.3** Port `StarlightTransmissionHandler` — top-level dimension→TransmissionWorldHandler map
- [ ] **C3.4** Port `StarlightUpdateHandler` — queue of nodes needing recalculation
- [ ] **C3.5** Port `TransmissionChunkTracker` — chunk load/unload listeners that pause/resume network segments
- [ ] **C3.6** Port `BlockTransmutationHandler` in `starlight/network/handler/` — starlight-driven block conversion delivery

### C4 — Tile Entity Base Classes 🔴 `[depends: C1]`

- [ ] **C4.1** Port `BlockEntityNetwork<T>` — base for all network-participating tile entities; first-tick registration; node lifecycle
- [ ] **C4.2** Port `BlockEntitySourceBase<T>` extends BlockEntityNetwork — IStarlightSource impl; outgoing link NBT persistence `[depends: C4.1]`
- [ ] **C4.3** Port `BlockEntityReceiverBase<T>` extends BlockEntityNetwork — IStarlightReceiver impl `[depends: C4.1]`
- [ ] **C4.4** Port `BlockEntityTransmissionBase` — base for lens/prism tiles `[depends: C4.1]`
- [ ] **C4.5** Port `TileAreaOfInfluence` — base for area-effect tiles
- [ ] **C4.6** Port `TileOwned` — ownership (player UUID) tracking
- [ ] **C4.7** Port `TileRequiresMultiblock` — multiblock validation base

### C5 — Refactor Concrete Tile Entities to Use Base Classes 🟠 `[depends: C4]`

- [ ] **C5.1** Refactor `BlockEntityCollectorCrystal` to extend `BlockEntitySourceBase`
- [ ] **C5.2** Refactor `BlockEntityAltar` to extend `BlockEntityReceiverBase`
- [ ] **C5.3** Refactor `BlockEntityPrism` to extend `BlockEntityTransmissionBase`
- [ ] **C5.4** Refactor `BlockEntityLens`, `BlockEntityRelay`, `BlockEntitySpectralRelay` similarly
- [ ] **C5.5** Refactor `BlockEntityRitualPedestal`, `BlockEntityWell`, `BlockEntityTreeBeacon` to extend `BlockEntityReceiverBase`
- [ ] **C5.6** Add `TileOwned` to tile entities that track player ownership (Observatory, Chalice, etc.)

---

## PHASE D — Network Packets

All packets follow the same port pattern. Implement in pairs (handler + registration in channel).

### D1 — Login Packets 🔴 (needed on join)

- [ ] **D1.1** Port `ASLoginPacket` base class (abstract, with acknowledgment handshake index tracking)
- [ ] **D1.2** Port `PktLoginAcknowledge` client→server handshake `[depends: D1.1]`
- [ ] **D1.3** Port `PktLoginSyncPerkInformation` — full perk tree JSON on join `[depends: D1.1]`
- [ ] **D1.4** Port `PktLoginSyncGateway` — all gateway positions on join `[depends: D1.1]`
- [ ] **D1.5** Port `PktLoginSyncDataHolder` — all sync data entries as NBT on join `[depends: D1.1, E1]`
- [ ] **D1.6** Register all login packets in the channel / mod setup `[depends: D1.1–D1.5]`

### D2 — Client→Server Play Packets 🔴

- [ ] **D2.1** Port `PktUnlockPerk` — perk allocation request + validation
- [ ] **D2.2** Port `PktAttunePlayerConstellation` — attunement ceremony completion
- [ ] **D2.3** Port `PktRequestTeleport` — gateway teleport request with validation `[depends: F1]`
- [ ] **D2.4** Port `PktRotateTelescope` — telescope rotation (clockwise/CCW at position)
- [ ] **D2.5** Port `PktPerkGemModification` — gem socket insert/drop
- [ ] **D2.6** Port `PktRequestPerkSealAction` — seal creation/breaking on perks
- [ ] **D2.7** Port `PktRevokeGatewayAccess` — owner removes player UUID from gateway
- [ ] **D2.8** Port `PktToggleClientOption` — toggles like "disable perk abilities"

### D3 — Server→Client Play Packets 🔴

- [ ] **D3.1** Port `PktOpenGui` — server-opens client GUI with menu type + NBT `[depends: G2]`
- [ ] **D3.2** Port `PktSyncKnowledge` — full knowledge state sync (constellations, research, perks, attunement, tier)
- [ ] **D3.3** Port `PktSyncPerkActivity` — perk activate/deactivate/clearall events
- [ ] **D3.4** Port `PktSyncModifierSource` — perk modifier source add/remove/update
- [ ] **D3.5** Port `PktSyncCharge` — alignment charge max/current values
- [ ] **D3.6** Port `PktUpdateGateways` — all gateway cache entries per dimension `[depends: F1]`
- [ ] **D3.7** Port `PktProgressionUpdate` — tier advancement notification + journal refresh
- [ ] **D3.8** Port `PktSyncStepAssist` — player step height for perk climb boost
- [ ] **D3.9** Port `PktSyncData` — differential NBT sync across ResourceLocation keys `[depends: E1]`
- [ ] **D3.10** Port `PktOreScan` — ore scan visual effects at block positions
- [ ] **D3.11** Port `PktShootEntity` — apply motion vector to entity
- [ ] **D3.12** Register all new packets in the channel

---

## PHASE E — Data Sync System 🟠

- [ ] **E1.1** Port `SyncDataHolder` + `SyncDataRegistry` — core runtime data sync infrastructure
- [ ] **E1.2** Port `AbstractData`, `AbstractDataProvider`, `ClientData`, `ClientDataReader` base classes `[depends: E1.1]`
- [ ] **E1.3** Port `DataLightBlockEndpoints`, `DataLightConnections` (server data) `[depends: E1.2]`
- [ ] **E1.4** Port `ClientLightBlockEndpoints`, `ClientLightConnections` (client readers) `[depends: E1.2]`
- [ ] **E1.5** Port `DataTimeFreezeEffects`, `DataTimeFreezeEntities` + client equivalents `[depends: E1.2]`
- [ ] **E1.6** Wire SyncDataRegistry into mod setup; populate DataAS with save domain + keys `[depends: A1.4, E1.1]`

---

## PHASE F — Gateway System 🟠

- [ ] **F1.1** Port `CelestialGatewayFilter` — filters valid teleport destinations
- [ ] **F1.2** Port `CelestialGatewayHandler` — manages teleport execution, cooldowns, access lists `[depends: F1.1]`
- [ ] **F1.3** Verify `GatewayHandler` world data is wired to `DataAS` SaveKey `[depends: A1.4, A2.6]`
- [ ] **F1.4** Wire `PktRequestTeleport` handler to use CelestialGatewayHandler `[depends: D2.3, F1.2]`

---

## PHASE G — Container/Menu System 🔴

- [ ] **G1.1** Port `ContainerTileEntity` — base container linked to a tile entity position
- [ ] **G1.2** Port `ContainerAltarTrait` extends ContainerTileEntity — for constellation/radiance altar `[depends: G1.1]`
- [ ] **G1.3** Port `ContainerObservatory` `[depends: G1.1]`
- [ ] **G1.4** Port `ContainerTome` (knowledge book) `[depends: G1.1]`
- [ ] **G1.5** Port `SlotConstellationFocus` custom slot `[depends: G1.2]`
- [ ] **G2.1** Port 7 container factory/provider classes (ContainerAltarAttunementProvider, ContainerAltarConstellationProvider, ContainerAltarDiscoveryProvider, ContainerAltarRadianceProvider, ContainerObservatoryProvider, ContainerTomeProvider, CustomContainerProvider) `[depends: G1]`
- [ ] **G3.1** Register all container types in `RegistryContainerTypes` and `MenuTypesAS` `[depends: A2.5, G1, G2]`

---

## PHASE H — Client Screens & Rendering 🟠

### H1 — Perk Tree Rendering

- [ ] **H1.1** Port `journal/perk/PerkRender` — per-perk render logic (icon, connection lines, state)
- [ ] **H1.2** Port `journal/perk/BatchPerkContext` — batch draw context for perk tree rendering `[depends: H1.1]`
- [ ] **H1.3** Port `journal/perk/DynamicPerkRender` — animated perk state rendering `[depends: H1.1]`
- [ ] **H1.4** Port `journal/perk/group/PerkRenderGroup` — groups perks for batch rendering `[depends: H1.2]`
- [ ] **H1.5** Port `journal/perk/group/PerkPointRenderGroup` — renders perk point connection nodes `[depends: H1.4]`
- [ ] **H1.6** Port `journal/perk/group/PerkPointHaloRenderGroup` — halo glow for allocated perks `[depends: H1.4]`
- [ ] **H1.7** Port `journal/overlay/ScreenJournalOverlay` — hover overlay for perk stats/description `[depends: H1.1]`
- [ ] **H1.8** Port `journal/overlay/ScreenJournalOverlayPerkStatistics` — full perk statistics panel `[depends: H1.7]`
- [ ] **H1.9** Wire perk rendering into `ScreenPerkTree.java` (currently only has `PerkTreeSizeHandler`) `[depends: H1.1–H1.8]`

### H2 — Missing Screens 🟡

- [ ] **H2.1** Port `ScreenConstellationPaper` — constellation paper item GUI
- [ ] **H2.2** Port `ScreenHandTelescope` — hand telescope GUI
- [ ] **H2.3** Port `client/screen/base/SkyScreen` — base for sky-view screens
- [ ] **H2.4** Port `client/screen/base/TileConstellationDiscoveryScreen` — tile-backed constellation discovery
- [ ] **H2.5** Port `client/screen/base/TileEntityScreen` — tile-backed screen base

### H3 — Camera System 🟠 (needed for attunement ceremony, observatory)

- [ ] **H3.1** Port `ClientCameraManager` singleton + `ICameraTransformer` interface
- [ ] **H3.2** Port `CameraPath` + `CameraPathBuilder` — smooth camera movement paths `[depends: H3.1]`
- [ ] **H3.3** Port `CameraTransformerPlayerFocus`, `CameraTransformerSettingsCache` `[depends: H3.1]`
- [ ] **H3.4** Port `EntityCameraRenderView`, `EntityClientReplacement` — camera entity faking `[depends: H3.1]`
- [ ] **H3.5** Port `ICameraTickListener`, `ICameraStopListener`, `ICameraPersistencyFunction` interfaces
- [ ] **H3.6** Port `CameraEventHelper`, `ClientCameraUtil` `[depends: H3.1]`

### H4 — OBJ Model System 🟡 (needed for telescope, refraction table custom 3D models)

- [ ] **H4.1** Port `WavefrontObject` OBJ parser + helper classes (Face, Vertex, GroupObject, TextureCoordinate, ModelFormatException)
- [ ] **H4.2** Wire OBJ parser into custom model rendering for telescope, refraction table

### H5 — Remaining Client Utilities 🟡

- [ ] **H5.1** Port `client/util/draw/` (BufferBatchHelper, BufferContext, RenderInfo)
- [ ] **H5.2** Port `client/util/image/` (ImageTemplate, ImageTemplates, SkyImageGenerator) — for sky rendering
- [ ] **H5.3** Port `client/util/color/` (ColorThief, ColorUtil, MMCQ) — color extraction
- [ ] **H5.4** Port `client/util/word/` (RandomWordGenerator, WordGeneratorEnglish, WordGeneratorChinese) — for journal random text
- [ ] **H5.5** Port `GatewayUI` + `AreaOfInfluencePreview` client utility helpers

---

## PHASE I — Config Entries 🟠

- [ ] **I1.1** Port `ServerConfig` class skeleton (extends ForgeConfigSpec equivalent, replaces 1.16 BaseConfiguration)
- [ ] **I1.2** Port `GeneralConfig` entry (game-wide toggles: playerInteractions, dimensionWhitelist, etc.)
- [ ] **I1.3** Port `MachineryConfig` entry (altar, collector, relay range/efficiency values)
- [ ] **I1.4** Port `PerkConfig` entry (perk point cap, experience multiplier, effect scale)
- [ ] **I1.5** Port `LightNetworkConfig` entry (transmission loss, relay behavior)
- [ ] **I1.6** Port `CraftingConfig` entry (altar crafting toggles)
- [ ] **I1.7** Port `EntityConfig` entry (entity drop toggles, spawn rates)
- [ ] **I1.8** Port `ToolsConfig` + `WandsConfig` entries
- [ ] **I1.9** Port `WorldGenConfig` entry (shrine distance, ore frequency)
- [ ] **I1.10** Port 8 config registry classes (AmuletEnchantmentRegistry, EntityTransmutationRegistry, etc.) + their 8 set/entry classes
- [ ] **I1.11** Wire all config entries into `ConfigManager` / `ConfigRegistration` `[depends: I1.1–I1.10]`

---

## PHASE J — World Generation Completion 🟡

- [ ] **J1.1** Port `ChancePlacement` + `ChanceConfig` — frequency-tuned placement decorator
- [ ] **J1.2** Port `RiverbedPlacement` — placement along river biomes/waterways
- [ ] **J1.3** Port `WorldFilteredPlacement` + `WorldFilterConfig` — dimension whitelist placement
- [ ] **J1.4** Port `MarkerManagerAS` — shrine chest + crystal seeding during structure generation
- [ ] **J1.5** Port `StructureGenerationConfig` + `FeatureGenerationConfig` — distance/frequency tuning classes
- [ ] **J1.6** Create JSON datapack files for PlacedFeature/ConfiguredFeature (1.20 data-driven): rock crystal, aquamarine, marble vein, glow flower, shrines `[depends: A1.13, A2.18]`
- [ ] **J1.7** Verify shrine structure piece generation (AncientShrine, DesertShrine, SmallShrine) includes chest/crystal placement via MarkerManagerAS `[depends: J1.4]`

---

## PHASE K — Mixins 🟠

- [ ] **K1.1** Port `MixinAttributeModifierManager` — perk modifier injection into vanilla attribute system (critical for perk stats to apply)
- [ ] **K1.2** Port `MixinModifiableAttributeInstance` — perk modifier correctness on AttributeInstance `[depends: K1.1]`
- [ ] **K2.1** Port `MixinItemPredicate` — custom item predicate matching (needed for advancement triggers)
- [ ] **K2.2** Port `MixinItemStack` — item stack NBT hooks for crystal/perk item behaviors
- [ ] **K3.1** Port `MixinServerPlayNetHandler` — server network handler hooks
- [ ] **K3.2** Port `MixinGameRenderer` — camera/render hooks (needed for H3) `[depends: H3.1]`
- [ ] **K3.3** Port `MixinParticleManager` — particle system integration
- [ ] **K4.1** Evaluate `MixinForgeHooks`, `MixinVoxelShapeSpliterator` — port if needed for specific behaviors
- [ ] **K5.1** Register all new mixins in the mixin JSON config

---

## PHASE L — Missing Items 🟡

- [ ] **L1.1** Port `ItemDazzlingGem` + `ItemDazzlingFrame` (quality gem items in `common/item/quality/`)
- [ ] **L1.2** Port `ItemPerkGemDay`, `ItemPerkGemNight`, `ItemPerkGemSky` as subclasses of `ItemPerkGem` with time-of-day behavior
- [ ] **L1.3** Implement per-lens behavior in colored lenses (`blockInBeam()`, `entityInBeam()` per LensColor value) — currently behavior placeholder only
- [ ] **L1.4** Register new items in `ItemsAS` DeferredRegister `[depends: L1.1, L1.2]`

---

## PHASE M — Commands 🟡

- [ ] **M1.1** Port `ArgumentTypeConstellation` — custom command argument for constellation names
- [ ] **M1.2** Port `CommandAttune` — `/as attune <player> <constellation>`
- [ ] **M1.3** Port `CommandConstellation` — constellation discovery commands
- [ ] **M1.4** Port `CommandExp` — perk experience manipulation
- [ ] **M1.5** Port `CommandProgress` — research progress query/set
- [ ] **M1.6** Port `CommandReset` — reset player progress
- [ ] **M1.7** Port `CommandMaximizeAll` — dev command to max all progress
- [ ] **M1.8** Port `CommandSerialize` — debug serialization command
- [ ] **M1.9** Register commands in `CommandsAS` (or equivalent) and wire to `RegisterCommandsEvent`

---

## PHASE N — Entity Completion 🟡

- [ ] **N1.1** Port `SpectralToolGoal`, `SpectralToolMeleeAttackGoal`, `SpectralToolBreakBlockGoal`, `SpectralToolBreakLogGoal` — AI goals for spectral tool entity
- [ ] **N1.2** Wire goals into spectral tool entity's `registerGoals()`
- [ ] **N1.3** Port `InteractableEntity` base interface
- [ ] **N1.4** Port `EntityCustomItemReplacement` — custom item entity replacement logic
- [ ] **N1.5** Verify all 9 entity types are registered in `RegistryEntities` `[depends: A2.3]`

---

## PHASE O — Event Handlers & Events 🟡

- [ ] **O1.1** Port `ASRegistryEvents` — custom registry event definitions
- [ ] **O1.2** Port `AttributeEvent` — custom perk attribute event
- [ ] **O1.3** Port `EventFlags` + `PlayerAffectionFlags` — flags for event suppression/tracking
- [ ] **O1.4** Port `StarlightNetworkEvent` — events for starlight network changes
- [ ] **O2.1** Port `EventHandlerAutoLink` — auto-links relays/lenses in network
- [ ] **O2.2** Port `EventHandlerCache` — caches event-related data
- [ ] **O2.3** Port `EventHelperEnchantmentTick` + `EventHelperEntityFreeze` — event helper utilities
- [ ] **O2.4** Register new event handlers in mod setup

---

## PHASE P — Loot & Advancements 🟡

- [ ] **P1.1** Port `CopyGatewayColor` loot function (NBT copy for gateway block drops)
- [ ] **P1.2** Port `LootModifierPerkVoidTrash` — perk effect: void some drops
- [ ] **P1.3** Port `LootModifierScorchingHeat` — perk effect: smelt drops in-place
- [ ] **P1.4** Register loot modifiers in `RegistryLoot` `[depends: A2.11]`
- [ ] **P2.1** Port `ListenerCriterionTrigger` — custom advancement trigger base class
- [ ] **P2.2** Create advancement trigger instances for: ALTAR_CRAFT, ATTUNE_CRYSTAL, ATTUNE_SELF, DISCOVER_CONSTELLATION, PERK_LEVEL `[depends: P2.1]`
- [ ] **P2.3** Register advancement triggers in `RegistryAdvancements` and populate `AdvancementsAS` `[depends: A1.12, P2.2]`
- [ ] **P2.4** Create advancement JSON files (or data gen providers) for Astral Sorcery progression advancements

---

## PHASE Q — Integration 🟢

### Q1 — JEI Completion
- [ ] **Q1.1** Port `TieredAltarRecipeTransferHandler` — recipe transfer for tiered altars in JEI
- [ ] **Q1.2** Port `JEIHandlerDropItem`, `JEIHandlerSpawnEntity`, `JEIInteractionResultHandler`, `JEIInteractionResultRegistry` — interaction recipe result display in JEI

### Q2 — Curios Integration
- [ ] **Q2.1** Port `IntegrationCurios` — wire Curios amulet slot detection into CuriosAmuletHelper
- [ ] **Q2.2** Wire amulet enchantment registry to Curios `[depends: I1.10]`

### Q3 — CraftTweaker
- [ ] **Q3.1** Port 5 CraftTweaker manager files (AltarManager, BlockTransmutationManager, InfusionManager, LiquidInteractionManager, WellManager)
- [ ] **Q3.2** Port `IntegrationCraftTweaker` entry point

### Q4 — Botania
- [ ] **Q4.1** Port `IntegrationBotania` if Botania compatibility is desired

---

## PHASE R — Data Generation 🟢

- [ ] **R1.1** Port or update `AstralDataGenerator` main data gen entry
- [ ] **R1.2** Port `AstralBlockStateMappingProvider`
- [ ] **R1.3** Port `AstralAdvancementProvider` `[depends: P2]`
- [ ] **R1.4** Port block/chunk loot table providers (BlockLootTableProvider, ChestLootTableProvider, EntityLootTableProvider, GameplayLootTableProvider)
- [ ] **R1.5** Port `AstralPerkTreeProvider` — generates perk tree data files
- [ ] **R1.6** Port altar recipe providers (Discovery, Attunement, Celestial, Radiance)
- [ ] **R1.7** Port remaining recipe providers (InfuserRecipeProvider, InteractionRecipeProvider, BlockTransmutationRecipeProvider, VanillaTypedRecipeProvider, LightwellRecipeProvider)
- [ ] **R1.8** Port tag providers (AstralBlockTagsProvider, AstralItemTagsProvider)

---

## PHASE S — Patreon System ⚪ (cosmetic, defer)

- [ ] **S1** Port PatreonData, PatreonEffect, PatreonEffectHelper
- [ ] **S2** Port all PatreonEffect types (TypeBlockRing, TypeCelestialWings, etc.)
- [ ] **S3** Port PatreonManager, PatreonManagerClient
- [ ] **S4** Port patron entity types (PatreonFlare, PatreonCrystalFlare, etc.)
- [ ] **S5** Wire data sync for patron effects `[depends: E1]`

---

## DEPENDENCY GRAPH (critical path)

```
A1 (lib constants)
  └─ A2 (registry wiring)
       ├─ C1→C2→C3 (starlight network)
       │    └─ C4→C5 (tile base classes + refactor)
       ├─ D1→D2→D3 (network packets)
       │    └─ G1→G2→G3 (containers)
       │         └─ H1 (perk tree rendering)
       ├─ E1 (data sync)
       │    └─ D1.5, D3.9
       ├─ F1 (gateway)
       │    └─ D2.3, D3.6
       ├─ B1 (perk attr limits)
       │    └─ A2.7
       ├─ I1 (config entries)
       ├─ J1 (worldgen)
       └─ K1 (mixins — perk attrs)

H3 (camera)  ──────────────────── K3.2 (MixinGameRenderer)
H4 (OBJ)    — standalone

M (commands) — standalone after A
N (entities) — standalone after A2.3
O (events)   — standalone after core systems
P (loot/advancements) — standalone
Q (integrations) — after core
```

---

## QUICK REFERENCE: What will crash on first runClient

1. **NullPointerException on registry** — most lib constants missing → fix A1 first
2. **Missing entity registration** — entity types not registered → A2.3
3. **Starlight network NPE** — WorldNetworkHandler references missing node interfaces → C1-C3
4. **GUI open crash** — PktOpenGui missing, container factories missing → D3.1, G2
5. **Perk allocation freeze** — PktUnlockPerk missing → D2.1
6. **Login crash** — no login packet infrastructure → D1.1
