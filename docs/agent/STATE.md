# Project State

## Current objective
- Plan 16 (Labs System) is complete. Next: Plan 17 (Cards) or Plan 27 (Polish & VFX).

## What works
- Plans 01–16 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, and labs system complete.
- Plan 16 complete: 10 research types with cost scaling (1.15^level) and time scaling (1.10^level), 1–4 lab slots (200 Gems each), Gem rush (50–200 linear interpolation), auto-completion on app launch, real-time countdown UI, full Labs screen replacing placeholder.
- Domain layer unit tests: 133 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 3 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- LabsScreen hiltViewModel() deprecation warning (moved package) — cosmetic, works fine.

## Top priorities (next 5)
1. Plan 17: Cards System (unblocked — depends on Plan 07 ✓)
2. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
3. Plan 22: Stats & History (unblocked)
4. Plans 19/20/21: Walking features, currencies, milestones (unblocked)
5. Plan 25: Anti-cheat hardening (unblocked)

## Next actions (explicit order)
1. Plan 17 (Cards) — last major progression system before polish.
2. Plan 27 (Polish) when all gameplay systems are in place.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 19 use cases stable.
- `data/local/AppDatabase.kt` — 7 entities, version 3.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine (overdrive + UW system), CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 16 implementation)
