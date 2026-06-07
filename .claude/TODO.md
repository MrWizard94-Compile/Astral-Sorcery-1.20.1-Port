# Astral Sorcery Port — Master TODO

Derived from AUDIT_FINDINGS.md (212 issues, 9 sessions).  
Severity: `[HIGH]` → `[MED]` → `[LOW]` → `[WARN]`  
Issues are grouped into fix clusters — related changes share a root cause or fix location.

---

## CLUSTER A — Root NBT Sync Fix *(unblocks all rendering bugs)*

**A1** `[HIGH]` `BlockEntitySynchronized.handleUpdateTag()` calls `super.handleUpdateTag(tag)` which invokes `load()` → `readSaveNBT()` with an incomplete update tag, zeroing all disk-only fields on chunk load for every block entity.  
**Fix:** Override to skip `super`; only call `readCustomNBT(tag)`.  
**File:** `common/tile/base/BlockEntitySynchronized.java:111`

---

## CLUSTER B — Altar Rendering & GUI *(depends on A1)*

**B1** `[HIGH]` `BlockEntityAltar`: `storedStarlight`, `starlightCapacity`, `isCrafting`, `recipeTick`, `receivedConstellation`, `activeRecipeId` are in `writeSaveNBT` only — never sent to client. Altar crafting VFX and starlight gauge never work client-side.  
**Fix:** Move these 6 fields to `writeCustomNBT`/`readCustomNBT` (keep in `writeSaveNBT` too for disk persistence).  
**File:** `common/tile/BlockEntityAltar.java:659–693`

**B2** `[HIGH]` `ContainerAltarBase` has no `ContainerData`/`addDataSlots()`. Even after B1, dynamic values (starlight, crafting progress) won't update live while the GUI is open.  
**Fix:** Add `ContainerData` for `storedStarlight` (scaled int), `recipeTick`, `isCrafting`. Call `addDataSlots()` in the constructor.  
**File:** `common/container/ContainerAltarBase.java`

**B3** `[HIGH]` `RenderAltar.render()` reads `altar.isCrafting()` — always false on client before B1+B2. Craft VFX overlay never shows.  
*(Resolved by B1 — no separate code change needed once B1 is done.)*  
**File:** `client/render/tile/RenderAltar.java:71`

---

## CLUSTER C — Block Entity Rendering — Sync Missing Fields *(depends on A1)*

**C1** `[HIGH]` `BlockEntityRitualPedestal.ritualActive` is in `readSaveNBT` but NOT `readCustomNBT` → `RenderRitualPedestal` always reads `false` → ritual VFX never shows.  
**Fix:** Add `ritualActive` to `writeCustomNBT`/`readCustomNBT`.  
**File:** `common/tile/BlockEntityRitualPedestal.java:303–316`

**C2** `[HIGH]` `BlockEntityAttunementAltar.isAttuning` in `readSaveNBT` only → attunement VFX and sound loop never start on client.  
**Fix:** Add `isAttuning` to `writeCustomNBT`/`readCustomNBT`.  
**File:** `common/tile/BlockEntityAttunementAltar.java`

**C3** `[MED]` `BlockEntityCollectorCrystal.starlightCollected` in `readSaveNBT` only → `RenderCollectorCrystal.isCollecting()` always `false` → upward starlight beam never renders.  
**Fix:** Add `starlightCollected` to `writeCustomNBT`/`readCustomNBT`.  
**File:** `common/tile/BlockEntityCollectorCrystal.java`

**C4** `[MED]` `BlockEntityFountain.structureValid` in `readSaveNBT` only → `RenderFountain.isStructureValid()` always `false` → fountain fluid stream and halo never render.  
**Fix:** Add `structureValid` to `writeCustomNBT`/`readCustomNBT`.  
**File:** `common/tile/BlockEntityFountain.java`

**C5** `[MED]` `BlockEntityInfuser.craftingProgress` in `readSaveNBT` only → `RenderInfuser.isInfusing()` always returns `false` → infuser swirl effect never renders.  
**Fix:** Add `craftingProgress` to `writeCustomNBT`/`readCustomNBT`.  
**File:** `common/tile/BlockEntityInfuser.java`

---

## CLUSTER D — Block Tags / Mining

**D1** `[HIGH]` 19 blocks have `requiresCorrectToolForDrops()` but are absent from `data/minecraft/tags/blocks/mineable/pickaxe.json`. Players cannot mine them for drops with any tool.  
Affected: all 8 black marble non-raw variants, `starmetal_ore`, `starmetal`, `celestial_crystal_cluster`, `gem_crystal_cluster`, `fountain_prime_liquid/ore/vortex`, `spectral_relay`, `refraction_table`, `ritual_link`, `tree_beacon_component`.  
**Fix:** Add all 19 to `data/minecraft/tags/blocks/mineable/pickaxe.json`.  
**File:** `src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`

**D2** `[HIGH]` `AstralBlockTagProvider.addTags()` is incomplete — root cause of D1. Running `./gradlew runData` regenerates the broken tag file and erases hand-edits.  
**Fix:** Complete `addTags()` with all missing pickaxe, slab, stair, and tool-tier entries.  
**File:** `common/datagen/AstralBlockTagProvider.java`

**D3** `[MED]` `#minecraft:slabs` missing `black_marble_slab`, `infused_wood_slab`. `#minecraft:stairs` missing `black_marble_stairs`, `infused_wood_stairs`.  
**Fix:** Add to the respective JSON tag files and to `AstralBlockTagProvider`.  
**Files:** `data/minecraft/tags/blocks/slabs.json`, `data/minecraft/tags/blocks/stairs.json`

**D4** `[MED]` `starmetal_ore` and `starmetal` have no tool-tier tag — any pickaxe mines them once D1 is fixed. Verify intended harvest level vs 1.16 reference and add `needs_iron_tool` or `needs_stone_tool` as appropriate.  
**File:** `data/minecraft/tags/blocks/mineable/`

**D5** `[LOW]` `infused_wood_slab`, `infused_wood_stairs` missing from `mineable/axe.json`.  
**Fix:** Add to `data/minecraft/tags/blocks/mineable/axe.json` and `AstralBlockTagProvider`.

---

## CLUSTER E — Loot Tables

**E1** `[HIGH]` `AstralLootTableProvider` is dangerously incomplete and registered in datagen with `generate = true`. Running `./gradlew runData` overwrites hand-crafted loot tables that contain critical custom functions (`copy_crystal_properties`, `copy_constellation`, `copy_gateway_color`, `random_crystal_property`), silently breaking crystal attribute/constellation transfer on block break.  
**Fix (short-term):** Remove `AstralLootTableProvider` from `AstralDataGenerator` or set `generate = false` until complete.  
**Fix (long-term):** Complete the provider to cover all blocks with their custom loot functions.  
**File:** `common/datagen/AstralLootTableProvider.java`, `common/datagen/AstralDataGenerator.java:37`

**E2** `[MED]` `rock_crystal_ore.json` loot table rolls 2–5 crystals with no Fortune scaling. Fortune should increase yield via `minecraft:apply_bonus`.  
**File:** `src/main/resources/data/astralsorcery/loot_tables/blocks/rock_crystal_ore.json`

---

## CLUSTER F — Datagen Safety

**F1** `[HIGH]` `build.gradle:84` adds `src/generated/resources/` to `sourceSets.main.resources`. Generated files shadow handwritten ones when the same path exists in both. `src/generated/` is currently empty — do not run datagen until all providers are complete.  
**Fix:** Document this prominently. Complete all providers before running datagen. Consider removing the srcDir line until then.  
**File:** `build.gradle:84`

**F2** `[HIGH]` `AstralItemTagProvider.addTags()` is completely empty — generates no item tags at all. Running datagen produces empty item tag output.  
**Fix:** Populate with `forge:crystals`, `forge:gems/aquamarine`, `minecraft:beacon_payment_items` (starmetal ingot), and any custom perk gem tags.  
**File:** `common/datagen/AstralItemTagProvider.java`

**F3** `[MED]` `AstralBlockStateProvider.registerStatesAndModels()` covers only 16 simple blocks out of 60+. Running datagen generates incomplete blockstate JSONs that shadow handwritten ones.  
**Fix:** Complete to cover all registered blocks, or remove from datagen until ready.  
**File:** `common/datagen/AstralBlockStateProvider.java`

**F4** `[MED]` `AstralRecipeProvider.buildRecipes()` only defines 8 vanilla crafting recipes out of the full set. Running datagen shadows those 8 handwritten recipe files.  
**Fix:** Complete to cover all vanilla crafting recipes, or remove from datagen until ready.  
**File:** `common/datagen/AstralRecipeProvider.java`

**F5** `[MED]` `AstralItemModelProvider.registerModels()` covers 27 of 142+ items. Running datagen replaces those 27 handwritten item models (some 3D) with flat `simpleItem` templates.  
**Fix:** Complete, or remove from datagen until ready.  
**File:** `common/datagen/AstralItemModelProvider.java`

---

## CLUSTER G — Entity SpectralTool

**G1** `[HIGH]` `EntitySpectralTool` tracks owner with `int` entity ID — not persistent across server restarts. After a restart the tool follows the wrong entity or despawns.  
**Fix:** Store owner UUID; resolve via `ServerLevel.getEntity(UUID)`.  
**File:** `common/entity/EntitySpectralTool.java`

**G2** `[HIGH]` `EntitySpectralTool.performMine()` calls `level().destroyBlock(targetBlock, true)` — drops items without applying Fortune/Silk Touch from the spectral tool's enchantments.  
**Fix:** Use `Block.dropResources(state, level, targetBlock, null, null, getToolItem())`.  
**File:** `common/entity/EntitySpectralTool.java`

**G3** `[MED]` `EntitySpectralTool.isValidMineTarget()` calls `state.getDestroySpeed(level(), blockPosition())` using the **tool's own position** instead of the target block position.  
**Fix:** Pass `targetBlock` as the second argument.  
**File:** `common/entity/EntitySpectralTool.java`

---

## CLUSTER H — CommonConfig Dead Fields

**H1** `[HIGH]` 32 of 40 `CommonConfig` fields are defined but never read by any system. Players who configure world gen rates, altar starlight, network range, ritual range, crystal growth, etc. get no effect.  
**Fix:** Wire each field to its respective system, or remove the dead entries and document the hardcoded values.  
**File:** `common/data/config/CommonConfig.java` + every system that should read these values

---

## CLUSTER I — Starlight Charge & Scheduler

**I1** `[MED]` `AlignmentChargeHandler.maximumCharge` and `currentCharge` static maps keyed by UUID are never cleared on player logout. Long-running servers accumulate entries indefinitely.  
**Fix:** Subscribe to `PlayerEvent.PlayerLoggedOutEvent`; remove the UUID from both maps.  
**File:** `common/auxiliary/charge/AlignmentChargeHandler.java`

**I2** `[LOW]` `AlignmentChargeHandler` calls `PerkTree.getPerk(AstralSorcery.key("key_charge_balancing"))` every server tick per player. Cache this in a static field after first lookup.  
**File:** `common/auxiliary/charge/AlignmentChargeHandler.java`

**I3** `[MED]` `CommonScheduler.waiting.clear()` at line 58 is outside the `synchronized(lock)` block. A concurrent thread can add a task between lock release and `clear()`, silently dropping it.  
**Fix:** Move `waiting.clear()` inside the `synchronized(lock)` block.  
**File:** `common/CommonScheduler.java:58`

---

## CLUSTER J — Link System Memory Leaks

**J1** `[MED]` `LinkHandler.activeSessions` static map is never cleared on player logout or server stop. Mid-link sessions accumulate forever.  
**Fix:** Subscribe to `PlayerEvent.PlayerLoggedOutEvent` and call `clearSession(uuid)`. Call `clearAll()` on `ServerStoppingEvent`.  
**File:** `common/auxiliary/link/LinkHandler.java`

---

## CLUSTER K — NBT / Network Safety

**K1** `[MED]` `NBTHelper.readEnum()` and `ByteBufUtils.readEnumValue()` do not bounds-check the array index. Out-of-range ordinal from saved data or malformed packet throws `ArrayIndexOutOfBoundsException`.  
**Fix:** Add `if (index < 0 || index >= constants.length) return defaultValue` guard.  
**Files:** `common/util/nbt/NBTHelper.java:251`, `common/util/data/ByteBufUtils.java:267`

**K2** `[LOW]` `ByteBufUtils.writeNBTTag()` silently swallows its `IOException`. A failed write leaves the buffer in a partial state; the receiver then crashes with `IllegalStateException`.  
**Fix:** Rethrow as `RuntimeException`, or at minimum log the failure before the buffer is corrupted.  
**File:** `common/util/data/ByteBufUtils.java:413`

**K3** `[LOW]` Multiple `new ResourceLocation(string)` calls that should be `ResourceLocation.tryParse(string)`. Malformed save data or packets throw unchecked `ResourceLocationException` instead of failing gracefully.  
Affected locations:
- `NBTHelper.getResourceLocation()` — line 366
- `ByteBufUtils.readResourceLocation()` — line 252
- `CrystalAttributes.Attribute.deserialize()` — line 395
- `BlockEntityAltar.readSaveNBT()` — lines 668, 672 (`receivedConstellation`, `activeRecipeId`)
- `BlockEntityRitualPedestal.readCustomNBT()` — line 283 (`attunedConstellation`)
- `SimpleSingleFluidTank.readNBT()` — line 212
- `PrecisionSingleFluidTank.readNBT()` — line 214
- `AltarRecipeInstance.deserialize()` — line 102
- `EntityFlare.getConstellation()` — from NBT
- `AbstractPerk.readFromNBT()` — requiredConstellation
- `DynamicEnchantmentHelper.getNewEnchantmentLevel()` — line 25
- `SimpleAltarRecipe.Serializer.fromJson()` — constellation parsing

---

## CLUSTER L — Recipe System

**L1** `[MED]` `SimpleAltarRecipe.Serializer.fromNetwork()` always creates a plain `SimpleAltarRecipe`, discarding the `NBTCopyRecipe` subtype for recipes loaded with `copy_crystal_properties: true`. Client-side JEI loses the crystal-copy indicator.  
**Fix:** Encode the `copy_crystal_properties` flag in `toNetwork()` and decode it in `fromNetwork()` to reconstruct `NBTCopyRecipe` on the client.  
**File:** `common/crafting/recipe/SimpleAltarRecipe.java`

**L2** `[MED]` `AltarRecipeTypeHandler.init()` is never called — `CONVERTER_MAP` is always empty. Eight constellation-specific recipe subclasses (`ConstellationItemRecipe`, `ConstellationCopyStatsRecipe`, etc.) are never instantiated.  
**Fix:** Either call `init()` in `CommonProxy.onCommonSetup()` and wire it to the recipe system, or remove the dead class.  
**File:** `common/crafting/recipe/altar/AltarRecipeTypeHandler.java`

---

## CLUSTER M — Item Bugs

**M1** `[MED]` `ItemIlluminationWand`: `state.getShape(level, pos).equals(Shapes.block())` uses object identity. Blocks that construct an equivalent VoxelShape won't be recognized as full blocks.  
**Fix:** Replace with `Block.isShapeFullBlock(state.getShape(level, pos))`.  
**File:** `common/item/wand/ItemIlluminationWand.java:86`

**M2** `[MED]` `ItemColoredLens` FIRE lens: `level.getRecipeManager().getRecipes().stream().filter(...)` scans ALL recipes on every beam tick.  
**Fix:** Use `level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)` and cache the result.  
**File:** `common/item/lens/ItemColoredLens.java` — `FIRE.findSmelt()`

**M3** `[MED]` `ItemColoredLens` PUSH lens: `level.getServer().isPvpAllowed()` with no `level.isClientSide()` guard — `getServer()` returns null on client. Latent NPE if the call site ever runs client-side.  
**Fix:** Add `if (level.isClientSide()) return` guard before accessing `level.getServer()`.  
**File:** `common/item/lens/ItemColoredLens.java` — `PUSH.entityInBeam()`

**M4** `[LOW]` `ItemResonator` AREA_SIZE upgrade mode is an empty switch case — the area visualization feature was not ported.  
**Fix:** Implement or explicitly mark as not-yet-implemented with a comment.  
**File:** `common/item/ItemResonator.java`

**M5** `[LOW]` `ItemIlluminationPowder.use()` places `BlockIlluminator` without calling `setPlayerPlaced(true)`. The placed block emits passive light but the tile entity won't actively illuminate caves.  
**Fix:** After `level.setBlock()`, get the tile entity and call `be.setPlayerPlaced(true)`.  
**File:** `common/item/ItemIlluminationPowder.java`

---

## CLUSTER N — Perk Bugs

**N1** `[MED]` `KeySpawnLights` places `Blocks.LIGHT` permanently — invisible, indestructible vanilla debug blocks accumulate throughout the world. `onDeallocate()` is not overridden.  
**Fix:** Track placed positions in a per-player set and remove them in `onDeallocate()`, or use `BlockEntityIlluminator` with a timer.  
**File:** `common/perk/node/key/KeySpawnLights.java`

**N2** `[LOW]` `KeyMineralis.CAVE_THRESHOLD_Y = 60` is a 1.16-era underground boundary. In 1.20.1, mountains exceed Y=60 and caves extend to Y=−64.  
**Fix:** Replace with `level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) > player.getBlockY()`.  
**File:** `common/perk/node/key/KeyMineralis.java`

**N3** `[MED]` `EventHandlerPerkEffects.isLogBlock()` uses fragile `getDescriptionId().contains("log/wood/stem")` string match. The correct `TreeDiscoverer.isLog()` using `BlockTags.LOGS` already exists but is not used.  
**Fix:** Replace `isLogBlock()` and `floodFillLogs()` with `TreeDiscoverer.findConnectedLogs()`.  
**File:** `common/event/EventHandlerPerkEffects.java:456–495`

**N4** `[LOW]` `EventHandlerPerkEffects` tree-felling: `level.destroyBlock(logPos, true, player)` does not pass the player's tool, so Fortune doesn't apply to felled log drops.  
**Fix:** Use `Block.dropResources(state, level, pos, blockEntity, player, tool)` with the player's held item.  
**File:** `common/event/EventHandlerPerkEffects.java`

**N5** `[LOW]` `EventHandlerPerkEffects.isLogBlock()` secondary issue: `floodFillLogs()` matches only `getBlock() == logType` (exact same block class). Multi-wood trees partially fell.  
*(Resolved by N3)*

---

## CLUSTER O — Celestial / Sky

**O1** `[MED]` `ClientRenderEventHandler.onItemTooltip` guards dynamic enchantment display with `!stack.isEnchanted()`. `MixinItemStack` makes `isEnchanted()` return `true` for items with only dynamic enchantments, causing those enchantments to be skipped from the tooltip entirely.  
**Fix:** Change `!stack.isEnchanted()` to `stack.getEnchantmentTags().isEmpty()` (checks only actual NBT enchantments).  
**File:** `client/event/ClientRenderEventHandler.java:108`

**O2** `[MED]` `CEffectHorologium.accelerateBlockEntities()` calls `state.randomTick()` on blocks, not the block entity's actual ticker. Despite the method name, it only accelerates random-tick blocks, not machines.  
**Fix:** Invoke the block entity's ticker directly, or rename and document the actual behavior.  
**File:** `common/constellation/effect/CEffectHorologium.java`

**O3** `[MED]` `MixinCooldownTracker`: `Math.max(event.getResultCooldown(), 1)` forces a minimum 1-tick cooldown, preventing Horologium from fully clearing cooldowns to 0.  
**Fix:** Change to `Math.max(event.getResultCooldown(), 0)` so Horologium can remove cooldowns entirely.  
**File:** `mixin/MixinCooldownTracker.java`

---

## CLUSTER P — World Data & Saves

**P1** `[HIGH]` `WorldCacheDomain` constructs save IDs containing a colon (e.g., `"astralsorcery:as_domain.starlight_network"`) — illegal in Windows file names. Latent crash on any Windows server if this code path is ever reached.  
**Fix:** Replace `:` with `_` in the ID construction: `namespace + "_" + path + "_" + key`.  
**Note:** The three `DataAS` keys using this domain are currently dead code (never read), so the crash path is not active.  
**File:** `common/data/world/base/WorldCacheDomain.java`

**P2** `[MED]` `BlockEntityTreeBeaconComponent` calls `removeSelf()` when the tree beacon chunk is not loaded, destroying beacon structure components on unload.  
**Fix:** Check `level.getChunkSource().hasChunk()` for the beacon's chunk before self-destructing.  
**File:** `common/tile/BlockEntityTreeBeaconComponent.java`

---

## CLUSTER Q — Performance

**Q1** `[MED]` `BlockEntityIlluminator.generatePositions()` uses `positions.contains(current)` on an `ArrayList` — O(n) per check during spiral scan. At `SEARCH_RADIUS=64` this is O(n²).  
**Fix:** Use `LinkedHashSet<BlockPos>` for O(1) dedup while preserving order.  
**File:** `common/tile/BlockEntityIlluminator.java`

**Q2** `[LOW]` `BlockDiscoverer.discoverBlocksWithSameStateAround()` uses `List<BlockPos> visited = new LinkedList<>()` then `visited.contains()` — O(n²) for large flood-fills.  
**Fix:** Replace with `HashSet<BlockPos>`.  
**File:** `common/util/block/BlockDiscoverer.java:182`

**Q3** `[LOW]` `NodeConnection.addConnection()` uses `ArrayList.contains()` — O(n) per add. For large networks this compounds.  
**Fix:** Change `connectedTo` from `ArrayList` to `LinkedHashSet`.  
**File:** `common/starlight/transmission/NodeConnection.java:55`

**Q4** `[LOW]` `AmuletRandomizeHelper.getRandomEnchant()` allocates a new list of all enchantments on every amulet roll. Cache on first use.  
**File:** `common/enchantment/amulet/AmuletRandomizeHelper.java:51`

---

## CLUSTER R — Missing Lang Keys

**R1** `[LOW]` Five translation keys are used in code but absent from `en_us.json`:
1. `item.astralsorcery.celestial_crystal.fracture_proof` — `ItemCelestialCrystal.java:31`
2. `container.astralsorcery.altar_discovery` — JEI altar category title
3. `jei.astralsorcery.liquid_interaction` — JEI liquid interaction category
4. `jei.astralsorcery.transmutation` — JEI transmutation category
5. `astralsorcery.observatory.observing` — `ScreenObservatory.java:199`

**File:** `src/main/resources/assets/astralsorcery/lang/en_us.json`

**R2** `[LOW]` 7 `GemQuality` tier keys need verification: `item.astralsorcery.gem_quality.broken` through `item.astralsorcery.gem_quality.flawless`.

---

## CLUSTER S — CrystalGenerator Infinite Loop

**S1** `[MED]` `CrystalGenerator.upgradeProperties()` and `generateNewAttributes()` contain `while (!addRandomProperty(...)) {}` with no escape condition. If the builder has all properties at max tier, the loop never terminates.  
**Fix:** Add a maximum-iteration guard (e.g., break after N consecutive failures), or pre-check that at least one property is below max tier before entering the loop.  
**File:** `common/crystal/CrystalGenerator.java:81, 125, 132, 142`

---

## CLUSTER T — Dead Code Removal

**T1** `[MED]` `GatewayHelper` entire class — only referenced by a comment. Never called. Actual gateway persistence handled by `GatewayHandler`.  
**Fix:** Remove the class.  
**File:** `common/auxiliary/GatewayHelper.java`

**T2** `[MED]` `LiquidInteractionHandler.checkInteraction()` — zero callers. Chalice handles its own recipe lookup internally.  
**Fix:** Remove the class.  
**File:** `common/auxiliary/LiquidInteractionHandler.java`

**T3** `[MED]` `CollisionManager.register()` — zero callers. The custom collision system is dead infrastructure (placeholder for "Phase 13").  
**Fix:** Remove or leave with a clear TODO comment.  
**File:** `common/util/collision/CollisionManager.java`

**T4** `[MED]` `BlockStructureObserver` / `ObserverHelper` / `ChangeSubscriber` / `ObserverProvider` — entire system disconnected. `observeArea()` has zero production callers. Structure validation uses direct `PatternBlockArray.matches()` calls.  
**Fix:** Remove all 6 classes.  
**Files:** `common/structure/observer/`

**T5** `[LOW]` `BlockChangeNotifier.addListener()` only called from test code. `notifyChange()` fires to an empty listener list on every block change.  
**Fix:** Remove production call to `notifyChange()` in `EventHandlerMisc`, or remove the class.  
**File:** `common/event/BlockChangeNotifier.java`

**T6** `[LOW]` `GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE` registered but never read by any code. Players can set it via `/gamerule` with no effect.  
**Fix:** Either wire it to `BlockEntityAltar.doesSeeSky()` and `BlockEntityCollectorCrystal`, or remove it.  
**File:** `common/lib/GameRulesAS.java`

**T7** `[MED]` `CrystalCalculations.getCollectionRate()`, `getRitualRange()`, `getAltarSpeedMultiplier()` — zero callers. Systems that should use them use hardcoded values instead.  
**Fix:** Wire these to the appropriate block entities, or remove and document the hardcoded formulas.  
**File:** `common/crystal/CrystalCalculations.java`

**T8** `[LOW]` `BlockUtils.getDrops(…, int harvestFortune)` — zero callers. Also uses `withLuck()` which does not apply Fortune in the vanilla `minecraft:apply_bonus` way.  
**Fix:** Remove the method.  
**File:** `common/util/block/BlockUtils.java`

**T9** `[WARN]` `CalendarUtils.isAprilFirst()` — zero callers.  
**Fix:** Remove, or implement the Easter-egg feature it was intended for.  
**File:** `common/util/CalendarUtils.java`

---

## CLUSTER U — Resource / JSON Orphans

**U1** `[MED]` 4 orphaned blockstate JSON files reference non-existent blocks:
- `blockstates/celestial_gateway.json` — block is `"gateway"`, not `"celestial_gateway"`
- `blockstates/rock_collector_crystal.json` — no such block in 1.20.1 port
- `blockstates/liquid_starlight.json` — block is `"fluid_liquid_starlight"` (duplicate; `fluid_liquid_starlight.json` is correct)
- `blockstates/altar_attunement.json`, `altar_constellation.json`, `altar_discovery.json`, `altar_radiance.json` — altar is a single block with `altar_type` property; these reference non-existent per-tier blocks

**Fix:** Delete the 7 orphaned files.  
**Files:** `src/main/resources/assets/astralsorcery/blockstates/`

**U2** `[WARN]` Multiple JSON files have UTF-8 BOM prefix (`﻿`). Affected: `configured_feature/rock_crystal_ore.json`, `ancient_shrine.json`, `ancient_shrine/starts.json`, `desert_shrine/starts.json`, `small_shrine/starts.json`, `damage_type/bypasses_armor.json`, `damage_type/is_magic.json`, `loot_modifiers/enderman_stardust.json`.  
**Fix:** Re-save each file without BOM (UTF-8 without BOM encoding).

---

## CLUSTER V — Rendering / Client Misc

**V1** `[LOW]` `ClientProxy.java`: `ItemProperties.register(ItemsAS.RESONATOR.get(), new ResourceLocation("upgrade"), ...)` — creates `minecraft:upgrade` instead of `astralsorcery:upgrade`. Must match the predicate key in the resonator item model JSON.  
**Fix:** Change to `AstralSorcery.key("upgrade")` and verify the model JSON predicate key matches.  
**File:** `client/ClientProxy.java:48`

**V2** `[LOW]` `first_altar_craft.json` advancement uses `"item": "minecraft:crafting_table"` as its icon — placeholder.  
**Fix:** Change to `"item": "astralsorcery:altar_discovery"` or similar.  
**File:** `src/main/resources/data/astralsorcery/advancements/first_altar_craft.json`

**V3** `[LOW]` `BlockEntityPrism.heldCrystal` is serialized in both `readCustomNBT` AND `readSaveNBT` — redundant double-read on disk load.  
**Fix:** Remove the `readSaveNBT` copy; `readCustomNBT` is sufficient.  
**File:** `common/tile/BlockEntityPrism.java`

**V4** `[LOW]` `BlockEntityTick.ticksOnClient()` / `ticksOnServer()` methods exist but are never checked in the tick dispatcher. Subclasses overriding them get no effect.  
**Fix:** Either remove the dead API or implement the flags check in `tickStatic()`.  
**File:** `common/tile/base/BlockEntityTick.java`

**V5** `[LOW]` `BlockEntityCelestialCrystals` starmetal revert is hardcoded to `Blocks.IRON_ORE`. In 1.16 this was configurable.  
**Note:** Track as design decision if intentional.  
**File:** `common/tile/BlockEntityCelestialCrystals.java`

---

## CLUSTER W — Misc Code Quality

**W1** `[LOW]` `ProgressGatedPerk`: `(ServerPlayer) player` cast without `instanceof` guard — FakePlayers from automation mods throw `ClassCastException`.  
**Fix:** Add `if (!(player instanceof ServerPlayer sp)) return;`  
**File:** `common/perk/ProgressGatedPerk.java:80`

**W2** `[LOW]` `EventHandlerMining.hasFornaxPerk()` uses `key.getPath().contains("key_fornax")` — fragile string match.  
**Fix:** Compare against a constant from the perk registry.  
**File:** `common/event/EventHandlerMining.java`

**W3** `[WARN]` Duplicate `import net.minecraft.resources.ResourceLocation` in `PerkTreeData.java` (lines 16 and 61).  
**Fix:** Remove the duplicate.  
**File:** `common/perk/PerkTreeData.java`

**W4** `[LOW]` `SimpleAltarRecipe.Serializer.fromJson()`: `BlockAltar.AltarType.valueOf(typeStr.toUpperCase())` throws `IllegalArgumentException` on invalid JSON with no error message.  
**Fix:** Wrap in try/catch with a meaningful `ResourceParseException`.  
**File:** `common/crafting/recipe/SimpleAltarRecipe.java`

**W5** `[LOW]` `BlockEntityGateway.ticksExisted` duplicates `BlockEntityTick.tickCount`. Multiple other TEs have the same duplicate: `BlockEntityObservatory`, `BlockEntitySpectralRelay`, `BlockEntityCollectorCrystal`.  
**Fix:** Remove the local fields; use `super.getTicksExisted()`.

**W6** `[MED]` `ScreenJournalProgression` has two static mutable fields (`progressionRenderer`, `currentInstance`) that hold screen references after the screen closes, preventing GC.  
**Fix:** Clear both on `onClose()` / `removed()`.  
**File:** `client/screen/journal/ScreenJournalProgression.java`

**W7** `[LOW]` `BlockEntityGateway.readSaveNBT`/`writeSaveNBT` both contain empty bodies with just a `super` call — unnecessary boilerplate. Remove them.  
**File:** `common/tile/BlockEntityGateway.java`

**W8** `[LOW]` `BlockEntityGateway.validateStructure()` accepts any non-air block at the 4 cardinal positions — extremely loose. Consider checking against an AS marble block tag.  
**File:** `common/tile/BlockEntityGateway.java`

**W9** `[LOW]` `WorldNetworkHandler.registerSource()` double-registration from `BlockEntityCollectorCrystal.setAttunedConstellation()` updates the source but does not refresh network links. Old links retain the old constellation flavor.  
**Fix:** Refresh links on constellation change.  
**File:** `common/starlight/WorldNetworkHandler.java`

**W10** `[MED]` `BlockEntityRitualPedestal.storedStarlight` not saved to disk. On server restart pedestal always starts empty — `ritualActive` will be true but starlight will be 0, causing an immediate deactivation on tick 1.  
**Fix:** Add `storedStarlight` to `writeSaveNBT`/`readSaveNBT`.  
**File:** `common/tile/BlockEntityRitualPedestal.java`

**W11** `[LOW]` `CreativeTabsAS.maxCrystalStack()` specifies `PROPERTY_RITUAL_RANGE` at tier 3 but `PropertyRitualRange.getMaxTier()` returns 2. Creative tab crystals have ritual range beyond normal max.  
**Fix:** Change to tier 2.  
**File:** `common/lib/CreativeTabsAS.java`

**W12** `[WARN]` `CapabilitySetup`: `@SubscribeEvent` on `attachPlayerCaps` and `attachChunkCaps` is redundant — these are registered via explicit `addGenericListener`. Remove the annotations.  
**File:** `common/capability/CapabilitySetup.java`

**W13** `[LOW]` `ItemKnowledgeShare` stores `ProgressionTier` via `ordinal()` — fragile if tier enum order changes.  
**File:** `common/item/ItemKnowledgeShare.java`

**W14** `[LOW]` `CommandAstralSorcery` registers the root node twice (once standalone, once inside redirect lambda), potentially creating two Brigadier command entries.  
**Fix:** Capture first `register()` result, pass to `redirect()`.  
**File:** `common/cmd/CommandAstralSorcery.java`

---

## CLUSTER X — Deprecated API Migration

**X1** `[LOW]` 13 files use `DistExecutor.unsafeRunForDist` / `unsafeRunWhenOn` (deprecated in Forge 47.x). Migrate to `safeRunForDist` / `safeRunWhenOn` with `() -> ClassName::new` method references.  
Affected files: `AstralSorcery.java`, `EventHandlerBlockStorage.java`, `EventHandlerMisc.java`, `ItemResonator.java`, `PktAttunementActive.java`, `PktParticleEvent.java`, `PktPlayEffect.java`, `PktSyncBlockEntity.java`, `PktSyncConstellation.java`, `PktSyncPlayerProgress.java`, `PktSyncSeed.java`, `PktSyncStarlightNetwork.java`, `RegistryResearch.java`

---

*Total: 212 issues across 15 HIGH · 70 MED · 115 LOW · 12 WARN*
