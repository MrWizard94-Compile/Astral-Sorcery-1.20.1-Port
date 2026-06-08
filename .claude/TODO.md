# Astral Sorcery Port — Master TODO
*Last updated: 2026-06-08*

---

## Port State Summary

All 70 gameplay systems have code present (blocks, items, tile entities, renderers, recipes, network packets registered and wired). The 212-issue infrastructure and code-quality audit is fully resolved. All 19 CommonConfig fields are confirmed wired to their respective systems (verified 2026-06-08). Direct inspection of tick() and logic methods confirmed that most core gameplay systems have substantive server-side logic implemented.

What remains is: a handful of partial/delegated systems that need verification, known gameplay bugs discovered during testing, and full end-to-end progression testing.

---

## 1 — Config Fields — ALL WIRED ✓

All 19 fields confirmed wired as of 2026-06-08 inspection:

| Field | Wired In |
|---|---|
| `maxNodeConnections` | `WorldNetworkHandler.addLink()` |
| `transmissionLossPerBlock` | `WorldNetworkHandler.distributeFromSource()` |
| `maxCrystalSize` | `CrystalProperties.grow()` |
| `crystalGrowthChance` | `BlockEntityCelestialCrystals.grow()` |
| `celestialPurityThreshold` | `FormCelestialCrystalClusterRecipe.matches()` |
| `perksWorkInAllDimensions` | `PerkAttributeHelper.collectModifiers()` |
| `infusionCostMultiplier` | `BlockEntityInfuser.tryFindInfusionRecipe()` |
| `infusionConsumesLiquid` | `BlockEntityInfuser.tickInfusion()` + `completeInfusion()` |
| `wellBaseProduction` | `BlockEntityWell.tick()` |
| `wellMaxStorage` | `BlockEntityWell.onFirstTick()` |
| `wellCatalystConsumptionChance` | `BlockEntityWell.tick()` |
| `rockCrystalVeinSize` | `RockCrystalFeature.place()` |
| `rockCrystalMinY` | `AstralConfigHeightPlacement.getPositions()` |
| `rockCrystalMaxY` | `AstralConfigHeightPlacement.getPositions()` |
| `rockCrystalAttemptsPerChunk` | `AstralConfigCountPlacement.getPositions()` |
| `marbleVeinSize` | `MarbleVeinFeature.place()` |
| `aquamarineFrequency` | `AstralConfigCountPlacement.getPositions()` |
| `generateShrines` | `AncientShrineStructure`, `DesertShrineStructure`, `SmallShrineStructure.findGenerationPoint()` |
| `gatewayCostPerBlock` | `PktGatewayTeleport.performTeleport()` |

---

## 2 — Known Gameplay Bugs

Bugs discovered through `runClient` testing. Fix before calling any system "done".

| # | Bug | Root Cause | Status |
|---|-----|-----------|--------|
| B1 | Attuned crystal variants missing from creative menu and ancient shrines | Model JSONs had no `tintindex` on `#texture` faces → color handlers never fired | **FIXED 2026-06-08** |
| B2 | Resonating Wand: linking non-functional, no ore particles, shrine→altar broken | EventHandlerInteract cancelled event on client; Block.use() intercepted before Item.useOn() | **FIXED 2026-06-08** |
| B3 | Collector crystal starlight beam invisible | `starlightCollected` only in `writeSaveNBT`, never synced to client → `isCollecting()` always false | **FIXED** (prior session) |
| B4 | Fosic Resonator non-functional | Starlight mode particles need night-time + ~5s seed round-trip. Needs re-test in `runClient` | **Needs verification** |
| B5 | Crystal tinting shows on baked model only; BESR crystal body renders plain | `CrystalModelRenderer` uses procedural geometry without constellation tint | **Known/accepted** — glow and beam ARE constellation-colored; body tint would require geometry changes |

---

## 3 — Partial / Delegated Systems Needing Verification

Systems where the block entity logic is intentionally delegated to a handler or entity, but the full path has not been tested end-to-end.

### 3.1 Prism — Beam Splitting
- `BlockEntityPrism` stores efficiency and participates in the starlight network, but multi-target beam distribution is handled by `WorldNetworkHandler`
- **Verify:** Place prism with lens, confirm beam splits and hits multiple targets with correct efficiency penalty
- **Files:** `common/tile/BlockEntityPrism.java`, `common/starlight/WorldNetworkHandler.java`

### 3.2 Observatory — Constellation Recording
- `BlockEntityObservatory` manages `EntityObservatoryHelper` lifecycle; actual sky scanning and constellation-position recording lives in the entity
- **Verify:** Player enters observatory, observes sky, constellation positions are recorded and persist
- **Files:** `common/tile/BlockEntityObservatory.java`, `common/entity/EntityObservatoryHelper.java`

### 3.3 Telescope — Passive Discovery
- `BlockEntityTelescope` has no tick override; player interaction handled via `ScreenTelescope` and right-click
- **Verify:** Player uses telescope at night, can discover/track constellations visible in current sky
- **Files:** `common/tile/BlockEntityTelescope.java`, `client/screen/ScreenTelescope.java`

### 3.4 Gateway — Teleportation
- `BlockEntityGateway` validates multiblock structure; actual teleportation logic lives in `PktGatewayTeleport` + `GatewayHandler`
- **Verify:** Two gateways placed and linked, player teleports between them, starlight consumed
- **Files:** `common/tile/BlockEntityGateway.java`, `common/network/play/client/PktGatewayTeleport.java`, `common/data/world/GatewayHandler.java`

### 3.5 Chalice — Liquid Interaction Recipes
- `BlockEntityChalice` handles fluid draw and balancing; liquid interaction recipe triggering (item dropped in chalice) needs end-to-end test
- **Verify:** Drop a valid item into liquid starlight chalice, recipe triggers, output appears
- **Files:** `common/tile/BlockEntityChalice.java`

### 3.6 AltarRecipeTypeHandler
- `init()` is called from `CommonProxy.onCommonSetup()` per the fix log, but verify all 11 constellation recipe subtypes are actually reachable and fire correctly in-game
- **Verify:** Craft a constellation-specific altar recipe (e.g., ConstellationCopyStatsRecipe), confirm it executes
- **Files:** `common/crafting/recipe/altar/AltarRecipeTypeHandler.java`

---

## 4 — Full Progression Path — End-to-End Testing

Not yet fully tested. Each step gates the next. Test in order.

### 4.1 World Generation
- [ ] Ancient shrines spawn correctly; crystal is attuned to a major constellation
- [ ] Rock crystal ore generates at the correct Y range with correct vein size
- [ ] Marble veins generate in correct biomes with correct size
- [ ] Aquamarine ore generates in correct underwater locations
- [ ] Desert shrines and small shrines spawn and have correct loot

### 4.2 Early Game — Getting Started
- [ ] Resonating Wand: right-click rock crystal ore shows white END_ROD particles at night
- [ ] Resonating Wand: right-click crafting table under shrine crystal → Discovery Altar placed
- [ ] Discovery Altar: GUI opens, starlight gauge fills, basic recipe executes
- [ ] Hand Telescope: equip at night, look at sky → constellations visible
- [ ] Constellation Paper: crafted and drawn using hand telescope
- [ ] Journal: pages unlock as player discovers constellations and crafts items

### 4.3 Mid Game — Progression
- [ ] Starlight Infusion table (Tier 2 altar): upgrade from Discovery → Attunement
- [ ] Collector Crystal: placed under open sky, collects starlight, beam visible at night
- [ ] Linking Tool: links collector crystal to altar → altar receives starlight
- [ ] Spectral Relay: placed in relay chain, boosts altar starlight
- [ ] Attunement Altar: structure built correctly, player attuned to a constellation
- [ ] Telescope (block): placed and used for detailed sky observation
- [ ] Refraction Table: crystal engraved with constellation, glass consumed

### 4.4 Ritual System
- [ ] Ritual Pedestal: multiblock built, attuned crystal placed, ritual activates
- [ ] Each of the 5 major constellation rituals produces expected effects (Aevitas heals, Discidia boosts damage, etc.)
- [ ] Ritual range scales correctly with crystal attributes

### 4.5 Infuser, Well, Fountain
- [ ] Infuser: multiblock built, liquid starlight consumed, item infused
- [ ] Lightwell: placed under sky, produces liquid starlight, catalyst degradation works
- [ ] Chalice: fills from lightwell, balances between adjacent chalices
- [ ] Fountain: fountain structure built, fountain effect executes

### 4.6 Wands
- [ ] Blink Wand: right-click teleports player to aimed location, alignment charge consumed
- [ ] Architect Wand: selects fill/copy region, places blocks
- [ ] Exchange Wand: swaps blocks in region with held block
- [ ] Grapple Wand: hook fires, player pulled toward target
- [ ] Illumination Wand: places illuminator blocks through solid walls

### 4.7 Late Game
- [ ] Perks: player gains constellation XP, allocates perk tree points, effects apply
- [ ] Mantle armor: crafted, attuned, passive bonuses active
- [ ] Shifting Star: crafted, de-allocates constellation, XP refunded
- [ ] Celestial Gateway: two gateways built, linked, teleportation works
- [ ] Crystal tools: crafted with rock crystal, correct tier, fortune/efficiency from crystal properties
- [ ] Spectral Tool entity: spawned via Pelotrio ritual, mines autonomously

### 4.8 Constellation Effects
- [ ] All 12 constellation effects fire when corresponding ritual is active and sky condition met
- [ ] Potency scales with crystal quality
- [ ] Effects respect biome/player range limits

---

## 5 — Datagen Providers (Disabled — Complete Before Re-enabling)

All datagen providers are disabled to prevent overwriting handwritten JSONs. Complete before re-enabling `src/generated/resources` in `build.gradle`.

| Provider | Status | What's missing |
|---|---|---|
| `AstralBlockStateProvider` | Disabled | Covers 16 of 60+ blocks — needs all complex multi-state blocks |
| `AstralRecipeProvider` | Disabled | Covers 8 of full vanilla recipe set |
| `AstralItemModelProvider` | Disabled | Covers 27 of 142+ items |
| `AstralLootTableProvider` | Disabled | Was previously dangerously incomplete; hand-written tables are correct — complete provider to match |

**Re-enable procedure:** Run `./gradlew runData`, diff `src/generated/` against `src/main/resources/`, verify correctness, remove superseded handwritten files, uncomment the `srcDir` line in `build.gradle`.

---

## 6 — Code Quality Items Still Open

Items from the old audit not yet closed, or discovered since.

| # | Severity | Description | File |
|---|---|---|---|
| CQ1 | ~~MED~~ | ~~BlockEntityTelescope no tick~~ | **CLOSED 2026-06-08** — `ScreenTelescope.attemptDiscovery()` sends `PktDiscoverConstellation` C→S; server validates night/sky/visible/progress and calls `ConstellationDiscoveryHandler.grantDiscovery` |
| CQ2 | ~~MED~~ | ~~`AmuletRandomizeHelper` static Random~~ | **FIXED 2026-06-08** — switched to `ThreadLocalRandom.current()` |
| CQ3 | ~~LOW~~ | ~~BOM in rock_crystal_ore.json~~ | **CLOSED 2026-06-08** — file starts with `7B 0A`, no BOM present |
| CQ4 | ~~LOW~~ | ~~BlockEntityPrism NPE risk in recalculateEfficiency~~ | **CLOSED 2026-06-08** — `heldCrystal` is `@Nonnull` initialized to `ItemStack.EMPTY`; isEmpty() check at top of method |
| CQ5 | ~~LOW~~ | ~~GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE~~ | **CLOSED 2026-06-08** — rule registered in `CommonProxy.onCommonSetup()`; `doesSeeSky()` checks `GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE != null && level.getGameRules().getBoolean(...)` — null guard handles pre-setup calls |
| CQ6 | ~~WARN~~ | ~~ItemWand doInteract/useOn duplication~~ | **CLOSED 2026-06-08** — comment block at line 155 documents the intentional pattern: `doInteract` runs via `EventHandlerInteract` first (cancels block GUI); `useOn` is the explicit fallback when event is skipped |

---

## 7 — JEI Integration

JEI plugin classes exist (`client/jei/`). Verify each category renders correctly with current recipe data.

- [ ] Altar recipes (all 4 tiers) show correctly in JEI with constellation requirements
- [ ] Liquid infusion recipes show fluid + item + output
- [ ] Liquid interaction recipes show
- [ ] Block transmutation recipes show
- [ ] Well liquefaction recipes show
- [ ] Starlight crafting recipes (InfusedWood, crystal growth, merging) show

---

## 8 — Lang / Localization Gaps — CLEAN ✓

Verified 2026-06-08: all previously-flagged keys are present in `en_us.json`:
- `astralsorcery.observatory.observing` ✓ (line 156)
- `container.astralsorcery.altar_discovery` ✓
- All JEI category keys (`jei.category.astralsorcery.*`) ✓

No missing keys known at this time.

---

## 9 — Known Design Decisions (Not Bugs)

Things that look incomplete but are intentional.

| Item | Decision |
|---|---|
| Crystal BESR body color not tinted to constellation | Body color changes would require geometry modification of `CrystalModelRenderer`. Glow ring and starlight beam ARE constellation-colored. Accepted as-is. |
| `ItemResonator` AREA_SIZE mode is a no-op | The "Area of Influence" visualization interface was not ported from 1.16. Documented with comment. |
| `BlockEntityCelestialCrystals` reverts star metal to iron ore | Original 1.16 target was configurable. 1.20 port hardcodes iron ore for simplicity. Documented. |
| `CollisionManager.register()` has no callers | Shell is load-bearing (referenced by `CollisionHelper`). Kept as placeholder for a future system. |

---

## Priority Order for Next Sessions

1. ~~**Wire the 19 config fields**~~ — **DONE** (all 19 confirmed wired 2026-06-08)
2. **Verify Fosic Resonator** (B4) — confirmed partially broken, needs `runClient` test
3. **End-to-end progression test** (Section 4) — in order, find what's actually broken vs what works
4. **Close partial systems** (Section 3) — Prism beam splitting, Observatory recording, Gateway teleport
5. **JEI integration** (Section 7) — visual verification
6. **Datagen providers** (Section 5) — last, after everything else is stable
