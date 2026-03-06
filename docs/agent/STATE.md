# Project State

## Current objective
- Plan 15 (Ultimate Weapons) is complete. Next: Plan 16 (Labs) or Plan 17 (Cards).

## What works
- Plans 01–15 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, and ultimate weapons complete.
- Plan 15 complete: 6 UW types with unlock/upgrade/equip, loadout (max 3), battle activation with cooldowns, 6 gameplay effects (Death Wave, Chain Lightning, Black Hole, Chrono Field, Poison Swamp, Golden Ziggurat), simple geometric visual effects, UW management screen accessible from Workshop, SURGE overdrive wired to reset cooldowns.
- Domain layer unit tests: 108 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 2 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.

## Top priorities (next 5)
1. Plan 16: Labs System (unblocked — depends on Plan 07 ✓)
2. Plan 17: Cards System (unblocked — depends on Plan 07 ✓)
3. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
4. Plan 22: Stats & History (unblocked)
5. Plans 19/20/21: Walking features, currencies, milestones (unblocked)

## Next actions (explicit order)
1. Plan 16 (Labs) or Plan 17 (Cards) — new progression systems.
2. Plan 27 (Polish) when all gameplay systems are in place.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 12 use cases stable.
- `data/local/AppDatabase.kt` — 7 entities, version 2.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine (overdrive + UW system), CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 15 implementation)
