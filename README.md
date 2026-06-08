# Astral Sorcery — Forge 1.20.1 Port

[![Build](https://github.com/MrWizard94-Compile/Astral-Sorcery-1.20.1-Port/actions/workflows/build.yml/badge.svg)](https://github.com/MrWizard94-Compile/Astral-Sorcery-1.20.1-Port/actions/workflows/build.yml)

A community port of [Astral Sorcery](https://github.com/HellFirePvP/AstralSorcery) by HellFirePvP to **Minecraft 1.20.1 with Forge**. The original mod stopped development at 1.16.5; this port brings it forward with clean, maintainable code targeting long-term support.

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.3.0 or later |
| Java | 17 |
| JEI | 15.3.0.4 (optional, for recipe viewing) |
| Curios API | 5.14.1+ (optional, for amulet slot) |

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/MrWizard94-Compile/Astral-Sorcery-1.20.1-Port.git
cd Astral-Sorcery-1.20.1-Port

# Build the mod jar
./gradlew build

# Run all static analysis checks (Checkstyle + SpotBugs) and unit tests
./gradlew check test

# Launch the game client for development
./gradlew runClient
```

The built jar is placed in `build/libs/`.

---

## Development Setup

1. Clone the repository.
2. Run `./gradlew genIntellijRuns` (IntelliJ) or `./gradlew genEclipseRuns` (Eclipse).
3. Open the project in your IDE — run configurations are created automatically.
4. Use the `runClient` configuration to launch the game with the mod loaded.

The project uses official Mojang mappings. No additional mapping setup is required.

---

## Static Analysis

The build enforces zero violations on both tools:

- **Checkstyle 10.17.0** — code style rules defined in `config/checkstyle/checkstyle.xml`
- **SpotBugs 4.8.6** — bug pattern analysis with exclusions in `config/spotbugs/exclude.xml`

`./gradlew check` will fail the build if either tool finds a violation.

---

## Current Status

Core systems ported and functional:

- Starlight transmission network (collector crystals, lenses, prisms, relays)
- All 12 constellation ritual effects
- Starlight Crafting Altar (all 4 tiers)
- Attunement Altar (player and crystal attunement)
- Lightwell, Starlight Infuser, Ritual Pedestal, Chalice
- Celestial Gateway (cross-dimension teleportation)
- Perk tree system
- Research and progression journal
- JEI recipe integration

Known limitations:

- Datagen providers exist but output is not yet merged into the resource pack. Handwritten JSONs are used directly. Once providers are fully validated, run `./gradlew runData` and merge the output.
- Curios integration is compile-time only due to a mixin incompatibility in Curios 5.14.1 on Forge 47.3.0. All call sites are guarded and the mod runs correctly without Curios present.

---

## Contributing

Pull requests are welcome. Before submitting:

1. Run `./gradlew check test` and confirm it passes with zero violations.
2. Keep commits focused — one logical change per commit.
3. Match the existing code style (Checkstyle will enforce this automatically).

This project targets long-term maintainability. Simple, readable code is preferred over clever solutions.

---

## Attribution

Original mod by [HellFirePvP](https://github.com/HellFirePvP/AstralSorcery), licensed under All Rights Reserved.
This port is an independent community project and is not affiliated with the original author.
