# Audit Progress Tracker

**Status: COMPLETE — 9 sessions, 212 total findings**
**Final tally: 0 CRIT · 15 HIGH · 70 MED · 115 LOW · 12 WARN**

---

## System 1: Build / Configuration ✅
- [x] build.gradle
- [x] gradle.properties
- [x] mods.toml
- [x] astralsorcery.mixins.json
- [x] pack.mcmeta (pack_format 15 verified correct for 1.20.1)

## System 2: Mod Entry / Proxies ✅
- [x] AstralSorcery.java
- [x] common/CommonProxy.java
- [x] client/ClientProxy.java

## System 3: Registry / Lib ✅
- [x] common/lib/BlocksAS.java
- [x] common/lib/ItemsAS.java
- [x] common/lib/BlockEntityTypesAS.java
- [x] common/lib/MenuTypesAS.java
- [x] common/lib/EntityTypesAS.java
- [x] common/lib/FluidsAS.java / FluidTypesAS.java
- [x] common/lib/EnchantmentsAS.java
- [x] common/lib/EffectsAS.java
- [x] common/lib/RecipeTypesAS.java / RecipeSerializersAS.java
- [x] common/lib/SoundsAS.java
- [x] common/lib/ParticleTypesAS.java
- [x] common/lib/StructuresAS.java
- [x] common/lib/CreativeTabsAS.java
- [x] common/lib/LootAS.java
- [x] common/lib/ConstellationsAS.java / ConstellationEffectsAS.java / MantleEffectsAS.java
- [x] common/lib/CrystalPropertiesAS.java / DataSerializersAS.java / DamageTypesAS.java
- [x] common/lib/DataAS.java / TagsAS.java / ColorsAS.java / GameRulesAS.java
- [x] common/lib/PerkAttributeTypesAS.java / PerkCustomModifiersAS.java / PerkNamesAS.java
- [x] common/lib/IngredientSerializersAS.java / RegistriesAS.java / CapabilitiesAS.java
- [x] common/lib/structure/ (BlockArray, PatternBlockArray, MatchableState, SimpleMatchableBlock, StructureValidator)
- [x] common/registry/RegistryResearch.java

## System 4: Blocks ✅
- [x] common/block/tile/ (BlockAltar, BlockAttunementAltar, BlockCollectorCrystal, BlockRitualPedestal, BlockTelescope, BlockGateway, BlockInfuser, BlockWell, BlockFountain, BlockChalice, BlockPrism, BlockLens, BlockRelay, BlockSpectralRelay, BlockObservatory, BlockTreeBeacon, BlockTreeBeaconComponent, BlockVanishing, BlockRitualLink, BlockFlareLight, BlockTranslucentBlock, BlockRefractionTable, BlockCelestialCrystalCluster, BlockGemCrystalCluster + fountain primes)
- [x] common/block/marble/ (BlockMarble, BlockBlackMarble variants, BlockMarblePillar, BlockBlackMarblePillar, slabs, stairs)
- [x] common/block/ore/ (BlockAquamarineOre, BlockRockCrystalOre, BlockStarmetal, BlockStarmetalOre)
- [x] common/block/foliage/ (BlockGlowFlower)
- [x] common/block/base/ (BlockAS, BlockDynamicColor, BlockEntityBlock, CustomItemBlock, LiquidStarlightOwned)
- [x] common/block/ (BlockIlluminator, BlockInfusedWood, BlockInfusedWoodSlab, BlockInfusedWoodStairs, BlackMarble)

## System 5: Items ✅
- [x] common/item/crystal/ (ItemCrystalBase, ItemRockCrystalSimple, ItemAttunedRockCrystal, ItemCelestialCrystal, ItemAttunedCelestialCrystal)
- [x] common/item/armor/ (ItemMantle + all 12 constellation subclasses)
- [x] common/item/tool/ (CrystalToolTier, ItemCrystalPickaxe/Axe/Shovel/Sword, ItemInfusedCrystalPickaxe/Axe/Shovel/Sword)
- [x] common/item/lens/ (ItemColoredLens)
- [x] common/item/gem/ (ItemAquamarine, ItemPerkGem, GemAttributeHelper)
- [x] common/item/wand/ (ItemWand, ItemBlinkWand, ItemArchitectWand, ItemExchangeWand, ItemGrappleWand, ItemIlluminationWand)
- [x] common/item/useeffect/ (ItemShiftingStar + 5 constellation subclasses, ItemShiftingStone)
- [x] common/item/quality/ (GemQuality, ItemDazzlingGem, ItemDazzlingFrame)
- [x] common/item/base/ (ItemAS, ItemBlockAS, AlignmentChargeConsumer, AlignmentChargeRevealer, OverrideInteractItem, IConstellationFocus, PerkExperienceRevealer, ItemBlockStorage, ItemHeldRender, ItemOverlayRender)
- [x] common/item/block/ (ItemBlockAltar, ItemBlockCollectorCrystal)
- [x] common/item/ (ItemChisel, ItemConstellationPaper, ItemEnchantmentAmulet, ItemFormationStone, ItemGlassLens, ItemHandTelescope, ItemIlluminationPowder, ItemInfusedGlass, ItemKnowledgeFragment, ItemKnowledgeShare, ItemLinkingTool, ItemNocturnalPowder, ItemParchment, ItemPerkSeal, ItemResonatingGem, ItemResonator, ItemStardust, ItemStarmetalDust, ItemStarmetalIngot, ItemTome)

## System 6: Tile Entities ✅
- [x] BlockEntityAltar.java
- [x] BlockEntityAttunementAltar.java
- [x] BlockEntityCollectorCrystal.java
- [x] BlockEntityRitualPedestal.java
- [x] BlockEntityTelescope.java
- [x] BlockEntityGateway.java
- [x] BlockEntityInfuser.java
- [x] BlockEntityWell.java
- [x] BlockEntityFountain.java
- [x] BlockEntityChalice.java
- [x] BlockEntityPrism.java
- [x] BlockEntityLens.java
- [x] BlockEntityRelay.java
- [x] BlockEntitySpectralRelay.java
- [x] BlockEntityObservatory.java
- [x] BlockEntityTreeBeacon.java
- [x] BlockEntityTreeBeaconComponent.java
- [x] BlockEntityVanishing.java
- [x] BlockEntityRitualLink.java
- [x] BlockEntityCelestialCrystals.java
- [x] BlockEntityGemCrystals.java
- [x] BlockEntityIlluminator.java
- [x] BlockEntityTranslucentBlock.java
- [x] BlockEntityRefractionTable.java
- [x] common/tile/base/ (BlockEntitySynchronized, BlockEntityTick, BlockEntityFakedState)
- [x] common/container/ (ContainerAltarBase + all 4 tier subclasses, SlotConstellationPaper, SlotUnclickable)

## System 7: Crafting / Recipes ✅
- [x] common/crafting/recipe/ (SimpleAltarRecipe, AltarUpgradeRecipe, LiquidInfusion, LiquidInteraction, BlockTransmutation, WellLiquefaction, RecipeDyeableChangeColor, GatedRecipe, context classes, interaction result classes)
- [x] common/crafting/recipe/altar/ (AltarCraftingProgress, AltarRecipeTypeHandler, AltarUpgradeRecipe interface, builtin/* — AttunementUpgrade, ConstellationUpgrade, TraitUpgrade, NBTCopy, etc., effect/* — all AltarRecipeEffect classes)
- [x] common/crafting/recipe/infusion/ (ActiveLiquidInfusionRecipe)
- [x] common/crafting/nojson/ (LiquidStarlightCraftingRegistry, WorldFreezingRegistry, WorldMeltableRegistry, FountainEffectRegistry + all recipe subtypes)
- [x] common/crafting/helper/ (CraftingFocusStack, DefaultWrapperContext, IHandlerRecipe, RecipeCraftingContext, RecipeHelper)

## System 8: Network ✅
- [x] common/network/PacketChannel.java (28 packets, sequential IDs)
- [x] common/network/play/server/ (all 16 server→client packets)
- [x] common/network/play/client/ (all 12 client→server packets)

## System 9: Rendering / Client ✅
- [x] client/render/tile/ (all 18 tile renderers)
- [x] client/render/entity/ (RenderEntityCelestialCrystal, RenderEntityFlare, RenderEntityGrapplingHook, RenderEntityIlluminationSpark, RenderEntityItemHighlighted, RenderEntityLiquidSpark, RenderEntityNocturnalSpark, RenderEntityObservatoryHelper, RenderEntityShootingStar, RenderEntitySpectralTool, RenderEntityStarling)
- [x] client/render/overlay/ (OverlayAlignmentCharge, OverlayItemEffects, OverlayPerkExperience, OverlayStarlightGauge)
- [x] client/render/layer/ (LayerStarryGlow)
- [x] client/render/ (CrystalModelRenderer)
- [x] client/effect/ (EffectHelper, EffectManager, EffectProperties, EntityVisualFX, FX* classes, AttunementCameraEffect)
- [x] client/model/ (CustomArmorModel, ModelArmorMantle, ModelAttunementAltar, ModelObservatory)
- [x] client/sky/ (AstralSkyRenderer)
- [x] client/screen/ (ScreenAltarDiscovery + 3 tier screens, ScreenBaseAS, ScreenContainerBaseAS, ScreenConstellationPaper, ScreenGateway, ScreenObservatory, ScreenPerkTree, ScreenRefractionTable, ScreenTelescope + base/helper/journal subtypes)
- [x] client/lib/ (RenderTypesAS, SpritesAS, TexturesAS)
- [x] client/registry/ (RegistrySprites)
- [x] client/event/ (ClientRenderEventHandler, EventHandlerClientMantleTick, ItemHeldEffectRenderer, LightbeamRenderHelper)
- [x] client/resource/ (AbstractRenderableTexture, AssetLibrary, AssetLoader, BindableResource, GeneratedResource, ReloadableResource, SpriteSheetResource, SpriteQuery, TextureQuery)
- [x] client/util/ (Blending, BufferDecoratorBuilder, RenderTypeDecorator, RenderingConstellationUtils, RenderingDrawUtils, RenderingUtils, ScreenTextEntry, WandRenderHelper, sound/*, word/*)
- [x] client/ (ClientPerkReaderRegistry, ClientProxy, ClientScheduler, ClientStarlightCache)

## System 10: Constellations ✅
- [x] common/constellation/effect/ (all 12 CEffect* classes + ConstellationEffectProperties/Provider/Registry/Status)
- [x] common/constellation/mantle/ (MantleEffect base, MantleEffectRegistry, all 12 MantleEffect* subclasses)
- [x] common/constellation/engraving/ (EngravedStarMap, EngravingEffect, EngravingEffectRegistry + EngravingEffectsAS)
- [x] common/constellation/star/ (StarConnection, StarLocation)
- [x] common/constellation/world/ (ActiveCelestialsHandler, CelestialEventHandler, CelestialHandler, ConstellationDiscoveryHandler, ConstellationHandler, DayTimeHelper, DistributionHandler, WorldContext, event/*)
- [x] common/constellation/ (BaseConstellation, Constellation, ConstellationBaseItem/Item, ConstellationRegistry, ConstellationTile, DrawnConstellation, IConstellation/IMajor/IMinor/IWeak, IConstellationSpecialShowup, SkyHandler)

## System 11: Perk System ✅
- [x] common/perk/node/ (AbstractPerk, AllocationStatus, CooldownPerk, DynamicModifierHelper, PerkAttributeLimiter, PerkConverter, PerkCooldownHelper, PerkLevelManager, PerkTree, PerkTreeData, PerkTreeManager, PerkTreePoint, ProgressGatedPerk)
- [x] common/perk/node/key/ (all 38 Key* classes)
- [x] common/perk/node/focus/ (FocusAevitas/Armara/Discidia/Evorsio/Vicio)
- [x] common/perk/node/root/ (RootAevitas/Armara/Discidia/Evorsio/Vicio)
- [x] common/perk/node/socket/ (GemSocketItem, GemSocketMajorPerk, IPerkGem)
- [x] common/perk/node/ (FocusPerk, GemSocketPerk, KeyPerk, MajorPerk, RootPerk, SmallPerk)
- [x] common/perk/effect/ (PerkAttributeHelper, PerkEffectHelper)
- [x] common/perk/modifier/ (ModifierType, PerkAttributeModifier)
- [x] common/perk/reader/ (PerkAttributeInterpreter, PerkAttributeReader, PerkStatistic, Reader* classes)
- [x] common/perk/tree/ (PerkTreeConstellation, PerkTreeGem, PerkTreeMajor)
- [x] common/perk/type/ (AttributeTypeRegistry, PerkAttributeType)

## System 12: World Generation ✅
- [x] common/world/feature/ (AquamarineFeature, GlowFlowerFeature, MarbleVeinFeature, RockCrystalFeature)
- [x] common/world/structure/ (AncientShrineStructure, DesertShrineStructure, SmallShrineStructure, ShrineMarkers, StructureTypesAS)
- [x] common/world/WorldGenerationAS.java
- [x] common/data/world/ (GatewayHandler, WorldNetworkHandler, LightNetworkBuffer, StorageNetworkBuffer, RockCrystalBuffer, ChunkFluidEntry, base/*)

## System 13: Data / Research / Capabilities ✅
- [x] common/data/research/ (PlayerProgress, PlayerProgressManager, ProgressionTier, ResearchHelper, ResearchManager, ResearchNode, ResearchProgression)
- [x] common/data/journal/ (JournalPage, JournalPageBlockTransmutation, JournalPageEmpty, JournalPageLiquidInfusion, JournalPageRecipe, JournalPageStructure, JournalPageText, RenderablePage)
- [x] common/data/config/ (ClientConfig, CommonConfig, ConfigManager, ConfigRegistration, ConfiguredBlockStateList, LogConfig, AmuletEnchantmentRegistry, EntityTransmutationRegistry, FluidRarityRegistry + entry classes)
- [x] common/capability/ (CapabilitySetup, ChunkFluidCapabilityProvider, PlayerCapabilityProvider, PlayerProgressHelper)
- [x] common/data/ (KnowledgeFragmentManager)

## System 14: Events ✅
- [x] common/event/ (BlockChangeNotifier, CooldownSetEvent, DynamicEnchantmentEvent, EventHandlerBlockStorage, EventHandlerCelestial, EventHandlerEffects, EventHandlerEnchantmentTick, EventHandlerInteract, EventHandlerMantleTick, EventHandlerMining, EventHandlerMisc, EventHandlerPerkCombat, EventHandlerPerkEffects, EventHandlerServerTick)
- [x] common/event/helper/ (EventHelperDamageCancelling, EventHelperInvulnerability, EventHelperSpawnDeny, EventHelperTemporaryFlight)

## System 15: Entities ✅
- [x] common/entity/ (EntityCelestialCrystal, EntityCrystal, EntityDazzlingGem, EntityFlare, EntityGrapplingHook, EntityIlluminationSpark, EntityItemExplosionResistant, EntityItemHighlighted, EntityLiquidSpark, EntityNocturnalSpark, EntityObservatoryHelper, EntityShootingStar, EntitySpectralTool, EntityStarling, EntityStarmetal)

## System 16: Resources (JSON) ✅
- [x] lang/en_us.json (verified; 5 missing keys found + 7 GemQuality keys to verify)
- [x] blockstates/ (4 orphaned files found: celestial_gateway, rock_collector_crystal, liquid_starlight, 4 individual altar tier files)
- [x] models/ (all 142+ item models confirmed present)
- [x] recipes/ (altar, infusion, change_color JSON format verified)
- [x] loot_tables/ (crystal property/constellation copy functions verified; AstralLootTableProvider dangerously incomplete)
- [x] advancements/ (AstralAdvancementTriggers verified; placeholder icon finding)
- [x] tags/ (19 blocks missing from pickaxe tag; missing slab/stair/axe tag entries)
- [x] data/damage_type/ (UTF-8 BOM in multiple files)
- [x] data/worldgen/ (all ConfiguredFeature, PlacedFeature, BiomeModifier JSON verified)

## System 17: Mixins ✅
- [x] mixin/ (all 9 classes: MixinCooldownTracker, MixinEnchantmentHelper, MixinEntity, MixinItemStack, MixinLivingEntity, MixinWorld, MixinClientWorld, ServerItemCooldownsAccessor, LevelSkyDarkenAccessor)

---

## Session Notes (chronological)

**Session 1 (2026-06-03):** Build/config, AstralSorcery.java, CommonProxy.java, ClientProxy.java, all 9 mixins, lib files (BlocksAS, ItemsAS, BlockEntityTypesAS, EntityTypesAS, CreativeTabsAS, LootAS, SoundsAS, EffectsAS, EnchantmentsAS, FluidsAS, FluidTypesAS, MenuTypesAS), BlockAltar, ItemBlockAltar, BlockEntitySynchronized (HIGH: handleUpdateTag bug), BlockEntityTick, BlockEntityAltar (HIGH: sync bugs), BlockEntityGateway, PacketChannel, PktAltarCraftingUpdate, PktSyncBlockEntity, ContainerAltarBase + ContainerAltarDiscovery (HIGH: no ContainerData), lang/sounds/loot/blockstates resources (orphaned blockstate files found). *Running total: 44*

**Session 2 (2026-06-03):** ConstellationsAS/ConstellationRegistry, CEffectLucerna/CEffectBootes, PerkAttributeHelper/ProgressGatedPerk, EventHandlerMining, WorldGenerationAS + all worldgen JSONs, StructureTypesAS, ConfigRegistration, CapabilitySetup/PlayerCapabilityProvider/PlayerProgress, BlockEntityCollectorCrystal/RitualPedestal/AttunementAltar/TreeBeacon/Chalice, ItemCrystalBase/ItemMantle/ItemBlockCollectorCrystal/ItemRockCrystalSimple, CommandAstralSorcery, RenderAltar/RenderRitualPedestal (HIGH: isCrafting/ritualActive never synced), ALL tags/blocks/*.json (HIGH: 19 blocks missing pickaxe tag), loot tables, AstralBlockTagProvider (root cause of tag gaps), AstralLootTableProvider (HIGH: dangerous incompleteness), JEI plugin, ClientRenderEventHandler (dynamic enchant tooltip bug), RecipeTypesAS/RecipeSerializersAS, recipe JSONs, celestial event handlers. *Running total: 78*

**Session 3 (2026-06-03):** CrystalToolTier + crystal tool items, ItemWand/ItemBlinkWand, MantleEffectArmara/MantleEffect base/EventHandlerMantleTick, CEffectAevitas, AstralSkyRenderer, FluidLiquidStarlight/FluidTypeLiquidStarlight, AstralAdvancementTriggers, GlobalLootModifierAS, damage_type JSONs (BOM), loot_modifiers JSONs, PktAttunePlayer/PktPerkAllocate security, JEI categories (3 missing lang keys), WorldNetworkHandler.registerSource idempotency, PatternBlockArray/StructuresAS, TagsAS/DamageTypesAS, all 142+ item models confirmed, AstralBlockTagProvider root cause confirmed, EventHandlerPerkEffects tree-felling (Fortune missing, isLogBlock fragile), ScreenJournalConstellationDetail, pack.mcmeta verified. *Running total: 96*

**Session 4 (2026-06-03):** All 4 EventHelper classes, all 12 CEffect* classes (Horologium randomTick vs BE tick mismatch found), TimeStopController, CollisionHelper, AttunementCraftingRegistry, SimpleAltarRecipe + Serializer, PerkTreeData (duplicate import), PerkAttributeTypesAS, WorldCacheDomain (latent Windows colon bug), LightNetworkBuffer/StorageNetworkBuffer/RockCrystalBuffer (dead code), GatewayHandler/WorldNetworkHandler save IDs safe, lang file comprehensive scan (5 missing keys total), RegistryResearch DistExecutor.unsafe, screen class hierarchy. *Running total: 111*

**Session 5 (2026-06-03):** All 18 tile renderers (RenderCollectorCrystal/RenderFountain/RenderInfuser never render — readSaveNBT victims), ScreenAltarDiscovery (starlight meter always 0 confirmed), ScreenContainerBaseAS/ClientScreenHandler, LiquidStarlightCraftingRegistry (5 recipes), GrowCrystalSizeRecipe, WorldFreezingRegistry/WorldMeltableRegistry/FountainEffectRegistry, all 3 enchantments + EventHandlerEnchantmentTick, EntityShootingStar/EntityGrapplingHook, EffectBleed/DamageUtil, EventHandlerMisc/EventHandlerInteract/StarlightNetworkRegistry, AmuletEnchantmentHelper/Curios guard, PerkTreeData completeness (815 lines, 45 key perks). *Running total: 119*

**Session 6 (2026-06-03):** All 16 server→client packets, all 12 client→server packets, RenderLens/RenderPrism/RenderRefractionTable/RenderWell (isTransmitting via readCustomNBT confirmed), DynamicEnchantmentHelper, EngravingEffectsAS, BlockBreakHelper, StorageNetworkHelper/TransmutationHelper/LiquidInteractionHandler, SkyHandler/PlayerProgressManager/ResearchManager, CrystalPropertiesAS (PROPERTY_RITUAL_RANGE overcap in creative tab), journal page renderers, PerkTreeData (complete). *Running total: 124*

**Session 7 (2026-06-03):** All 24 tile entities (BlockEntityVanishing, RitualLink, Observatory, Telescope, SpectralRelay, CelestialCrystals, GemCrystals, Illuminator, TranslucentBlock, TreeBeaconComponent — TreeBeaconComponent self-destructs when beacon chunk unloaded; Illuminator O(n) spiral scan), all non-tile blocks (all marble/infused variants, ores, foliage, structural blocks confirmed), all remaining items (ItemTome, ItemChisel, ItemConstellationPaper, ItemHandTelescope, ItemResonator — AREA_SIZE mode missing, ItemIlluminationPowder passive-only, ItemNocturnalPowder, ItemLinkingTool, ItemShiftingStar + subclasses, ItemPerkGem, ItemKnowledgeShare/Fragment, ItemFormationStone, ItemPerkSeal), CommonConfig (32 of 40 fields never read — dead configs). *Running total: 138*

**Session 8 (2026-06-03):** All remaining ~300 unreviewed files. EntitySpectralTool (entity ID owner tracking — HIGH; destroyBlock ignores enchantments — HIGH; wrong block pos for destroy speed), AltarRecipeTypeHandler.init() never called (dead), SimpleAltarRecipe.fromNetwork() drops NBTCopyRecipe subtype, ItemIlluminationWand VoxelShape identity check fragile, ItemColoredLens FIRE scans all recipes per beam tick, ItemColoredLens PUSH null dereference risk, KeySpawnLights places permanent Blocks.LIGHT (never cleaned up), BlockStructureObserver/ObserverHelper/ChangeSubscriber system completely disconnected, BlockChangeNotifier no production listeners, AlignmentChargeHandler static maps never cleared on logout, CommonScheduler.waiting.clear() outside sync block, GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE registered but never read, AbstractPerk.readFromNBT() new ResourceLocation() not tryParse(), EntityFlare.getConstellation() same, KeyMineralis CAVE_THRESHOLD_Y=60 wrong for 1.20, ScreenJournalProgression static fields not cleared, wand/gem/mantle/entity/perk sweep complete. *Running total: 171*

**SYSTEM 5 fill (2026-06-03):** ItemAS/base interfaces all clean. ItemParchment uses `new Properties()` not `defaultProperties()` (WARN). GemAttributeHelper static Random (LOW). ItemDazzlingGem/Frame ordinal() storage (LOW). GemQuality 7 lang keys to verify (LOW). All 13 mantle subclasses reviewed — 5 with inline passives, 7 thin delegation; design asymmetry noted (LOW). *Running total: 176*

---

**Session 9 (2026-06-03):** Full second pass — all ~906 files cross-referenced vs prior session notes. Physically read every previously un-individually-reviewed file. New areas covered: `NBTHelper.java` (enum read no bounds check, ResourceLocation unsafe), `ByteBufUtils.java` (enum read no bounds check, IOException swallowed, ResourceLocation unsafe), `MiscUtils.java` (clean), `TileInventory.java` (clean), `CrystalAttributes.java` (static Random, Attribute.deserialize ResourceLocation unsafe), `CrystalCalculations.java` (3 dead methods), `CrystalGenerator.java` (infinite loop in upgradeProperties; static Random), `CrystalPropertyRegistry.java` (clean), `LinkHandler.java` (activeSessions never cleared), `BlockDiscoverer.java` (O(n²) visited list), `NodeConnection.java` (O(n) addConnection), `CollisionManager.java` (dead), `TreeDiscoverer.java` (correct but unused by perk), `EventHandlerPerkEffects.floodFillLogs` (same-block-type only), `SkyCollectionHelper.java` (sharedRand thread-unsafe), `AstralItemTagProvider.java` (empty), `AstralBlockStateProvider.java` (16 blocks only), `AstralRecipeProvider.java` (8 recipes only), `AstralItemModelProvider.java` (27 items only), `AstralDataGenerator.java` + `build.gradle:84` (generated shadows main — latent), `GatewayHelper.java` (fully dead code), `SimpleSingleFluidTank.java` (ResourceLocation unsafe), `Vector3.java` (static Random), `BlockUtils.java` (getDrops dead + incorrect Fortune), `DamageSourceAS.java` (clean), `DamageSourceUtil.java` (clean), `StarlightNetworkHelper.java` (clean), `MoonPhase.java` (clean), `RotationHelper.java` (clean), `PlayerAmuletHandler.java` (clean), all 7 cmd/sub commands (clean), `CropHelper.java` (clean), `CopyConstellation/CopyCrystalProperties/LinearLuckBonus/RandomCrystalProperty` (clean), `KeyBindingsAS.java` (clean — wired via method ref), `RecipeHelper.java` (clean). *Running total: 204*

Additional findings (continued session 9): `LiquidInteractionHandler.checkInteraction()` dead code (all callers absent); `AmuletRandomizeHelper` static Random + uncached enchantment pool + hardcoded "config" fields; `EntityUtils.rand` unused field; `CalendarUtils.isAprilFirst()` dead utility; `AltarRecipeInstance.deserialize()` ResourceLocation unsafe; `PrecisionSingleFluidTank.readNBT()` ResourceLocation unsafe; `BlockLiquidStarlight` clean; `TransmissionLink` clean; `PropertyConstellation` clean; `CrystalAttributeItem/Tile` clean; `VoxelUtils` clean; `NBTComparator` clean; `WRItemObject` clean; `AltarCraftTrigger` clean; `ServerLifecycleListener` clean; `TokenMap` clean.

**FINAL TOTAL: 212 issues (0 CRIT · 15 HIGH · 70 MED · 115 LOW · 12 WARN)**
