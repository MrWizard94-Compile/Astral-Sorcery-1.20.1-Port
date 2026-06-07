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

**M1 — `BlockEntitySynchronized.rand` — static shared Random**
- File: `src/main/java/hellfirepvp/astralsorcery/common/tile/base/BlockEntitySynchronized.java:46`
  ```java
  protected static final Random rand = new Random();
  ```
- Problem: Shared mutable state across all BE instances. Not thread-safe. Produces correlated RNG sequences when multiple BEs tick in the same tick.
- Fix: Use `level.getRandom()` in subclasses (preferred for server-side), or `ThreadLocalRandom.current()`. Remove the static field.

**M2 — `BlockEntityAltar.structureValid` — dead serialized state**
- File: `src/main/java/hellfirepvp/astralsorcery/common/tile/BlockEntityAltar.java`
- Problem: The field is always `false` (port removed multiblock requirement per comment at line 294). Still serialized in `readSaveNBT/writeSaveNBT`, and `isStructureValid()`/`setStructureValid()` accessors still exist. Future devs will waste time wondering why it's always false.
- Fix: Remove the dead field, its serialization (lines 700,723), and its accessors (lines 521,525).

**M3 — `receivedConstellation` and `activeRecipeId` not synced to client**
- File: `src/main/java/hellfirepvp/astralsorcery/common/tile/BlockEntityAltar.java:673-732`
- Problem: Both fields are serialized only in `readSaveNBT/writeSaveNBT` (disk-only). After a server restart with a craft in progress, these restore from disk but are never sent to the client. The altar GUI cannot display the active recipe or required constellation after chunk reload.
- Fix: Move both fields into `readCustomNBT/writeCustomNBT` (they can still remain in the save section too for persistence).

**M4 — `CrystalGenerator.addRandomProperty()` busy-wait on near-maxed crystals**
- File: `src/main/java/hellfirepvp/astralsorcery/common/crystal/CrystalGenerator.java:159-175`
- Problem: The outer `canAddAnyProperty()` guard prevents the true infinite loop, but the inner `while (!addRandomProperty())` loop still busy-waits. If 9 of 10 properties are at max tier, `addRandomProperty()` has a ~90% chance of returning false per call (it picks randomly from all properties, then checks if the picked one is below max).
- Fix: Pre-filter the `properties` argument to only include entries below max tier before entering the while loop. This makes the selection deterministic and eliminates the busy-wait.
  ```java
  // Before the while loop:
  List<CrystalProperty> eligible = properties.stream()
      .filter(p -> builder.getPropertyLvl(p, 0) < p.getMaxTier())
      .collect(Collectors.toList());
  if (!eligible.isEmpty()) {
      CrystalProperty chosen = MiscUtils.getRandomEntry(eligible, random);
      builder.addProperty(chosen, 1);
  }
  ```

---

### LOW — Polish / regression prevention

**L1 — `KeySpawnLights.placedLights` — static unsynchronized HashMap**
- File: `src/main/java/hellfirepvp/astralsorcery/common/perk/node/key/KeySpawnLights.java:35`
- Problem: `HashMap` is not thread-safe. Player ticks are server-thread sequential in vanilla, but future changes (or tick parallelism mods) could cause ConcurrentModificationException.
- Fix: `Collections.synchronizedMap(new HashMap<>())` or `ConcurrentHashMap`.

**L2 — `CrystalGenerator.RAND` — shared static Random**
- File: `src/main/java/hellfirepvp/astralsorcery/common/crystal/CrystalGenerator.java:51`
- Problem: Same issue as M1. Single `static final Random RAND` is not thread-safe and produces correlated sequences.
- Fix: Accept the level's `RandomSource` as a parameter at call sites rather than using a static field.

**L3 — CommonConfig dead fields lack user-visible documentation**
- Server admins who set `rainStarlightPenalty = 0.5` get no effect and no feedback.
- Fix: Add `// NOT YET WIRED - pending <system> port` comments on each dead field in `CommonConfig`.

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
