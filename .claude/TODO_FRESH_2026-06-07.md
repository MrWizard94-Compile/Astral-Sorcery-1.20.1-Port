# Astral Sorcery Port — Fresh Audit TODO
*Generated: 2026-06-07. Based on direct code reading, not the 2026-06-03 audit.*

---

## Issues Confirmed FIXED (verified by reading source)

| ID | System | Fix Verified |
|----|--------|-------------|
| F01 | `BlockEntitySynchronized.handleUpdateTag()` — skips super, calls only `readCustomNBT()` | ✅ line 114 |
| F02 | `NBTHelper.readEnum()` — bounds check with `constants[0]` fallback | ✅ line 253 |
| F03 | `ByteBufUtils.readEnumValue()` — bounds check with `constants[0]` fallback | ✅ line 264 |
| F04 | `CrystalGenerator` infinite loop — `canAddAnyProperty()` guard before while loop | ✅ lines 81,127,134,145 |
| F05 | `EntitySpectralTool` owner tracking via `SynchedEntityData` | ✅ |
| F06 | `AstralSorcery.java` — `DistExecutor.safeRunForDist()` (not deprecated unsafe) | ✅ |
| F07 | `BlockEntityRitualPedestal.ritualActive` — in `readCustomNBT/writeCustomNBT` | ✅ |
| F08 | `BlockEntityAttunementAltar.isAttuning` — in `readCustomNBT/writeCustomNBT` | ✅ |
| F09 | `AstralBlockTagProvider` — all 19 missing blocks now in pickaxe tag | ✅ |
| F10 | `AstralItemTagProvider` — gem, crystal, beacon payment tags complete | ✅ |
| F11 | `data/minecraft/tags/blocks/mineable/pickaxe.json` — all 19 blocks present | ✅ |
| F12 | `KeySpawnLights` — eviction cap of 32, full cleanup on `onDeallocate()` | ✅ |
| F13 | `CEffectHorologium` — uses `getTicker()` to accelerate BEs, not `randomTick()` | ✅ |
| F14 | `AlignmentChargeHandler.onPlayerLogout()` — clears both charge maps on logout | ✅ |
| F15 | `LinkHandler` — `clearSession()` called on logout, `clearAll()` on server stop | ✅ |
| F16 | `WorldCacheDomain.getData()` — uses underscore separators (no colon in filename) | ✅ |
| F17 | `SimpleAltarRecipe.fromNetwork()` — reads `copyProperties` flag, restores `NBTCopyRecipe` subtype | ✅ |
| F18 | `BlockEntityAltar.gatherSkyStarlight()` — guarded by `doesSeeSky()` → `level.canSeeSky()` | ✅ |
| F19 | `NBTCopyRecipe` — correctly scans inventory, maps attribute tiers to CrystalProperties | ✅ |

---

## Open Issues

### HIGH — Must fix before release

**H1 — AstralLootTableProvider missing blocks — FIXED ✅ (2026-06-07)**
- All 5 custom loot functions (`CopyCrystalProperties`, `CopyConstellation`, `CopyGatewayColor`,
  `RandomCrystalProperty`, `LinearLuckBonus`) now have `create()` factory methods via `simpleBuilder`.
- `AstralLootTableProvider.ASBlockLoot.generate()` now covers all 63 registered blocks:
  - 44 `dropSelf` (marble, black marble, infused wood variants, starmetal, functional blocks)
  - 3 slab tables (`createSlabItemTable`)
  - 6 `noDrop` (flare_light, structural, vanishing, translucent_block, fluid_liquid_starlight, tree_beacon_component)
  - Aquamarine ore: silk-touch dispatch, 1-3 drops with `LinearLuckBonus` + explosion decay
  - Rock crystal ore: 2-5 rolls of crystal with `RandomCrystalProperty` + explosion decay
  - Glow flower: shears dispatch, 2-4 glowstone dust with `LinearLuckBonus`
  - Lens/Prism: drop self + `CopyCrystalProperties`
  - Gateway: drop self + `CopyGatewayColor`
  - Collector/Celestial collector: drop self + `CopyCrystalProperties` + `CopyConstellation`
  - Celestial crystal cluster: 5 staged pools + table-level `CopyCrystalProperties` + `ApplyExplosionDecay`
  - Gem crystal cluster: 3 pools (DAY_2/NIGHT_2/SKY_2) + table-level `ApplyExplosionDecay`
- Datagen validation now passes (all blocks in `getKnownBlocks()` are covered by `generate()`).
- Build: clean compile, 514 tests pass.

**H2 — CommonConfig dead fields — FIXED ✅ (2026-06-07)**
- Confirmed wired (found in production code via full field-name grep):
  - `altarStarlightDiscovery/Attunement/Constellation/Radiance` → `BlockEntityAltar.updateCapacityFromTier()`
  - `maxNetworkRange`, `baseCollectorOutput`, `ritualMaxRange`, `maxPerkLevel`, `perkExpMultiplier`,
    `perkEffectMultiplier`, `mobSpawningDenyAllTypes`, `altarCraftTimeMultiplier`, `dayLength`, `debugLogging`
- Newly wired (2026-06-07):
  - `daytimeStarlightFraction` + `allowDaytimeCollection` → `CelestialHandler.getTimeOfDayFactor()`
  - `rainStarlightPenalty` + `thunderStarlightPenalty` → `CelestialHandler.getWeatherFactor()`
  - `fullMoonBonus` → `CelestialHandler.getMoonPhaseFactor()` (near-full phases scale proportionally)
  - `gatewayMaxRange` → `PktGatewayTeleport.performTeleport()` (range check, 0 = unlimited)
  - `gatewayCrossDimensional` → `PktGatewayTeleport.performTeleport()` (deny cross-dim if false)
- Documented NOT YET WIRED (19 fields with inline comments):
  - `maxNodeConnections`, `transmissionLossPerBlock` — starlight network link system
  - `maxCrystalSize`, `crystalGrowthChance`, `celestialPurityThreshold` — crystal growth
  - `perksWorkInAllDimensions` — perk dimension check
  - `infusionCostMultiplier`, `infusionConsumesLiquid` — infusion recipes
  - `wellBaseProduction`, `wellMaxStorage`, `wellCatalystConsumptionChance` — lightwell
  - `rockCrystalVeinSize/MinY/MaxY/Attempts`, `marbleVeinSize`, `aquamarineFrequency`, `generateShrines` — worldgen
  - `gatewayCostPerBlock` — gateway starlight drain

**H3 — build.gradle datagen shadow risk — FIXED ✅ (2026-06-07)**
- `sourceSets.main.resources { srcDir 'src/generated/resources' }` is now commented out.
- Comment explains the re-enable procedure: run `./gradlew runData`, review generated files against
  handwritten JSONs in `src/main/resources/`, then uncomment and remove superseded handwritten files.
- `src/generated/resources/` remains empty; no runtime impact.

---

### MEDIUM — Should fix for a quality release

**M1 — `BlockEntitySynchronized.rand` — static shared Random — FIXED ✅ (2026-06-07)**
- Removed `protected static final Random rand` from `BlockEntitySynchronized`.
- `BlockEntityCelestialCrystals`: 2 call sites replaced with `level.getRandom()`.
- `BlockEntityGemCrystals`: 2 call sites replaced with `worldLevel.getRandom()`.
- Build: clean compile, 514 tests pass.

**M2 — `BlockEntityAltar.structureValid` — dead serialized state — FIXED ✅ (2026-06-07)**
- Removed field declaration, `isStructureValid()`/`setStructureValid()` accessors, and both
  serialization lines (`readSaveNBT` + `writeSaveNBT`).
- Comment at `tryFindRecipe()` line 293 explains the removal.
- Build: clean compile, 514 tests pass.

**M3 — `receivedConstellation` and `activeRecipeId` not synced to client — FIXED ✅ (2026-06-07)**
- `activeRecipeId` added to `readCustomNBT/writeCustomNBT` (was disk-only).
- `readSaveNBT/writeSaveNBT` cleared entirely — all fields were already in `readCustomNBT/writeCustomNBT`.
- Both fields now sync to client on `markForUpdate()` and survive chunk reload.
- Build: clean compile, 514 tests pass.

**M4 — `CrystalGenerator.addRandomProperty()` busy-wait on near-maxed crystals — FIXED ✅ (2026-06-07)**
- Replaced `addRandomProperty()` (returns bool, causes busy-wait) with `addEligibleRandomProperty()`
  that pre-filters the candidate list to properties below maxTier before picking.
- All 4 `while (!addRandomProperty(...)) {}` call sites replaced with direct calls.
- Selection now deterministic: 85% pick from existing properties (pre-filtered), 15% any eligible.
- Build: clean compile, 514 tests pass.

---

### LOW — Polish / regression prevention

**L1 — `KeySpawnLights.placedLights` — static unsynchronized HashMap — FIXED ✅ (2026-06-07)**
- Changed `new HashMap<>()` to `new ConcurrentHashMap<>()`.
- Build: clean compile, 514 tests pass.

**L2 — `CrystalGenerator.RAND` — shared static Random — FIXED ✅ (2026-06-07)**
- Removed `private static final Random RAND` field.
- No-arg wrappers (`upgradeProperties`, `generateNewAttributes`, `getRandomProperty`) now delegate
  to `ThreadLocalRandom.current()` — thread-safe, no shared state.
- Build: clean compile, 514 tests pass.

**L3 — CommonConfig dead fields lack user-visible documentation — FIXED ✅ (2026-06-07)**
- 19 `// NOT YET WIRED — pending <system> port` comments added (done as part of H2 fix).
- Verified: 19 occurrences of "NOT YET WIRED" in CommonConfig.java.

---

## New Tests Written (2026-06-07)

The following test files were created to cover previously-untested critical systems:

| File | What it tests |
|------|--------------|
| `common/util/NBTBoundsTest.java` | `NBTHelper.readEnum()` and `ByteBufUtils.readEnumValue()` with out-of-range, negative, and corrupt ordinals — verifies the bounds-check fixes don't regress |
| `common/auxiliary/charge/AlignmentChargeHandlerMathTest.java` | Regen rate formula (surface/underground/day factors), drain/fill math, fill-percentage clamping |
| `common/crafting/recipe/CrystalAttributeMappingTest.java` | `NBTCopyRecipe.tierToRange()` proportional mapping math: full, zero, half, clamp, and single-tier cases |

---

## Test Coverage Gaps (not yet written)

- `CrystalGenerator.upgradeProperties()` with a fully-maxed crystal (requires `CrystalPropertyRegistry` — needs MC bootstrap)
- `BlockEntityAltar` crafting lifecycle (startCrafting → tick → completeCrafting) — requires MC bootstrap
- `ContainerData` sync correctness (altar starlight meter reaches GUI) — requires MC bootstrap
- `CommonConfig` field wiring (verify `.get()` callers receive correct values) — requires Forge config bootstrap
