# CLAUDE.md — AI Coding Assistant Guidelines

This file defines strict expectations for AI coding assistants (Claude, Grok, etc.) when working on this project and other coding projects. These rules exist to maintain high code quality, avoid technical debt, and ensure reliable, maintainable results.

**How I Work With AI (Important Context for Assistants):**
- I primarily operate in a **supervisory / director role**. I expect the AI to handle implementation while I review, direct, and request changes.
- The strict rules in this file exist so the AI can produce consistent, high-quality work with less back-and-forth on fundamentals.
- When I request changes or corrections, treat them as high priority.
- I value complete, production-ready code over rapid but incomplete progress.

---



## 1. Core Philosophy: Complete, Production-Ready Code Only

- **No deferrals, no stubs, no placeholders, no TODOs, no "implement later".**
  - Deliver **full, complete, wired-up, working code** every time.
  - If a feature, class, or method is requested, implement it fully — including all necessary supporting code, registrations, initializations, and integrations.

- **Dependency-first implementation**
  - If code you are writing depends on classes, methods, or systems that do not yet exist, **build those dependencies first** before continuing with the original request.
  - Never leave broken references or "we'll add this later" gaps.

- **One-shot completeness**
  - Prefer solutions that can be dropped in and work immediately over incremental "we'll build it up over multiple steps" approaches (unless explicitly asked for an incremental plan).

---

## 2. Error & Warning Policy — Zero Tolerance

- **No error or warning is harmless.**
  - Every compiler error, warning, lint error, static analysis issue, or runtime warning must be investigated and fixed.
  - Do **not** suppress warnings with annotations, `@SuppressWarnings`, or configuration changes unless there is an extremely strong, documented reason.
  - Fix the root cause rather than hiding the symptom.

- Treat warnings as errors during development. A clean build with zero warnings is the expected state.

---

## 3. Testing Philosophy

- **Tests are written against intended behavior**, not against current (possibly incorrect) implementation.
- When a test fails:
  1. Investigate why the code does not match the intended behavior.
  2. Fix the **code**, not the test.
- Only modify tests when the intended behavior itself has legitimately changed (and that change has been explicitly approved).

- Prefer meaningful, intention-revealing tests over high coverage numbers achieved through shallow tests.

---

## 4. Version & Project Appropriateness

- **Always use code, APIs, and patterns appropriate for the target project and version.**
  - Example: Minecraft Forge 1.20.1 projects must use Forge 1.20.1 APIs, mappings, event systems, and idioms. Do not use NeoForge-only patterns or 1.21+ APIs.
  - Never backport modern APIs or forward-port old patterns unless that is an explicit, intentional part of the task.

- When porting mods, preserve original behavior unless changes are deliberate and documented.

- Stay consistent with the project's existing architecture, coding style, and conventions unless improving them is part of the explicit task.

---

## 5. Research-First When Uncertain

- **If you are not 100% certain about something, do research first.**
  - Search the web, official documentation, existing code in the project, mappings, or reliable sources.
  - Do not guess or make assumptions about APIs, behavior, or best practices.
  - When in doubt, ask the user for clarification rather than proceeding with uncertain code.

- Prefer verified information over "it probably works like this."

---

## 6. Code Quality Standards

- Maintain strict null safety (use proper `@Nullable` / `@NotNull` annotations consistently).
- No raw types.
- Follow project linting, formatting, and static analysis rules (Checkstyle, SpotBugs, etc.).
- Write clean, readable, maintainable code with clear naming and structure.
- Avoid unnecessary complexity. Simple, correct solutions are preferred over clever ones.
- Document non-obvious behavior, especially in complex systems (e.g., starlight networks, rituals, custom registries).

---

## 7. Documentation Standards

- Document all public APIs, complex internal logic, and non-obvious behavior with clear Javadoc (or equivalent).
- Documentation should explain **why** something exists and how it is intended to be used, not just repeat what the code does.
- Keep documentation up to date when behavior or implementation changes.
- In Minecraft mods, prioritize documentation for:
  - Custom registries and registration logic
  - Capability implementations and usage
  - Custom networking packets and data formats
  - Complex systems (e.g., starlight transmission, ritual logic, rendering pipelines)
- Avoid over-documenting trivial code; focus documentation where it adds real value.

---

## 8. Performance Considerations

- Be mindful of performance implications, especially in hot paths (rendering, tick methods, event handlers, network processing).
- Avoid unnecessary object allocations, boxing/unboxing, and expensive operations inside frequently executed code.
- Reason about performance before optimizing — do not prematurely optimize without evidence or clear need.
- In Minecraft contexts:
  - Distinguish between client-side and server-side performance concerns.
  - Be aware of chunk loading, entity counts, and world generation impact.
  - Prefer efficient data structures and algorithms for frequently accessed systems (e.g., starlight networks, ritual lookups).
- When performance work is needed, measure or profile where possible rather than guessing.

---

## 9. Security Mindset

- Treat all external input (network packets, commands, configuration, saved data) as potentially untrusted.
- Validate and sanitize inputs, especially when deserializing custom data or handling player-provided values.
- Avoid common pitfalls such as:
  - Unsafe reflection or class loading based on untrusted data
  - Improper capability or data attachment handling that could be exploited
  - Logging sensitive information
- Even in primarily single-player or cooperative mods, good security hygiene prevents future vulnerabilities and makes the mod more robust for modpack and server use.
- When in doubt about the safety of an approach, research secure alternatives or ask for guidance.

---

## 10. Handling Large Refactors

- For large or complex refactors, first outline a clear plan (unless explicitly told to proceed directly).
- Break large refactors into logical, incremental, and testable steps whenever practical.
- After completing a significant refactor:
  - Ensure the project builds cleanly with zero warnings.
  - Verify that relevant tests still pass (or update them only if intended behavior changed).
  - Confirm core functionality behaves as expected.
- Preserve existing behavior unless the refactor explicitly includes intentional changes (document those changes).
- Use the "complete code" and "dependency-first" rules even during refactors — do not leave the codebase in a broken intermediate state.

---

## 11. Collaboration & Communication Style

- **Default to complete file replacements.**
  - When modifying existing code, provide the **full updated file** rather than diffs, patches, or "replace this line with that line" instructions.
  - The user can then copy-paste the entire file.

- Be direct and precise. Avoid hedging language when you have researched the topic.

- When making significant changes, briefly explain the reasoning and any trade-offs.

- If requirements are ambiguous, ask clarifying questions instead of guessing.

- Proactively point out potential issues, edge cases, or long-term maintainability concerns.

---

## 12. Minecraft / Modding Specific Rules

- Respect the target loader (Forge vs NeoForge) and version strictly.
- Use correct registration patterns, event buses, and capability systems for the version in use.
- When porting, carefully map old 1.16.5 (or earlier) patterns to the correct 1.20.1 equivalents.
- Preserve the original mod's feel and behavior as much as possible unless changes are intentional.
- Be mindful of performance and threading implications in client/server code.
- Follow established modding best practices for the version (e.g., proper use of `DeferredRegister`, `RegistryObject`, etc. in 1.20.1).

---

## 13. Project-Specific Notes (Astral Sorcery Port)

- This project is a port of Astral Sorcery to **Forge 1.20.1**.
- Long-term goal includes potential NeoForge compatibility and later 1.21 migration, but current focus is a solid, high-quality Forge 1.20.1 release.
- Maintain high code quality from the start — this port may eventually be positioned as a community-maintained version.
- Core systems (starlight, constellations, rituals, rendering, networking) should be implemented cleanly and completely.

---

## 14. General Workflow Expectations

- Start by understanding the full scope of a request before writing code.
- When given a task, think through dependencies and prerequisites first.
- After providing code, be prepared to iterate based on feedback while still following the "complete code" rule.
- Prioritize correctness and long-term maintainability over speed of delivery.

---

These guidelines are non-negotiable for maintaining code quality and project integrity. When in doubt, default to the strictest interpretation of these rules.

---

Always look for and record useful information in the .claude directory and any subdirectories therein.
*Last updated: June 7, 2026*