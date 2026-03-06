# Project State

## Current objective
- Plan 14 (Step Overdrive) is complete. Next: Plan 15 (Ultimate Weapons) or Plan 27 (Polish & VFX).

## What works
- Plans 01–14 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, and step overdrive complete.
- Plan 14 complete: 4 overdrive types (Assault/Fortress/Fortune/Surge), Step cost deduction, once-per-round limit, 60s timer at game speed, stat modification (damage/attack speed/regen/defense/cash multiplier), pulsing aura + timer bar on ziggurat, overdrive selection menu, SURGE stubbed for Plan 15.
- Domain layer unit tests: 101 JVM tests covering all use cases, domain models, battle condition effects, tier unlock logic, biome themes, overdrive activation, EnemyScaler, and StepRateLimiter. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Plan 02/03 docs still reference `googleFitSteps` column name (cosmetic).
- Room DB version 2 with fallbackToDestructiveMigration (dev only).
- SURGE overdrive is a no-op until Plan 15 adds Ultimate Weapons.
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.

## Top priorities (next 5)
1. Plan 15: Ultimate Weapons (after Plan 12 ✓)
2. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
3. Plan 16: Labs System (unblocked)
4. Plan 17: Cards System (unblocked)
5. Plan 22: Stats & History (unblocked)

## Next actions (explicit order)
1. Plan 15 (Ultimate Weapons) — adds strategic depth, wires up SURGE overdrive.
2. Or Plan 16/17 (Labs/Cards) — new progression systems.
3. Plan 27 (Polish) when all gameplay systems are in place.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 15 is ready (depends on Plan 12 ✓). Ultimate Weapons.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 10 use cases stable.
- `data/local/AppDatabase.kt` — 7 entities, version 2.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine (now with overdrive), CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 14 implementation)
