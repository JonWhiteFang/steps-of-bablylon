# Project State

## Current objective
- Plan 18 (Narrative Biome Progression) is complete. Next: Plan 14 (Step Overdrive) or Plan 27 (Polish & VFX) — critical path continues at Plan 27.

## What works
- Plans 01–13 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, and biome progression complete.
- Plan 18 complete: 5 biome color palettes (sky gradient, ground, ziggurat layers, enemy tint, particles), BackgroundRenderer with ambient particle system, biome-themed ziggurat and enemies, biome transition overlay on first entry, home screen biome gradient, SharedPreferences-based first-seen tracking.
- Domain layer unit tests: 97 JVM tests covering all use cases, domain models, battle condition effects, tier unlock logic, biome themes, EnemyScaler, and StepRateLimiter. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Plan 02/03 docs still reference `googleFitSteps` column name (cosmetic).
- Room DB version 2 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 14: Step Overdrive (after Plan 12 ✓)
3. Plan 15: Ultimate Weapons (after Plan 12 ✓)
4. Plan 16: Labs System (unblocked)
5. Plan 17: Cards System (unblocked)

## Next actions (explicit order)
1. Plan 27 is next on critical path but depends on Plan 18 ✓ — ready now.
2. Alternatively, tackle gameplay systems: Plans 14/15 (combat), 16/17 (progression).

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 14 is ready (depends on Plan 12 ✓). Step Overdrive.
- Plan 15 is ready (depends on Plan 12 ✓). Ultimate Weapons.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable (includes BattleConditionEffects, TierConfig, Biome).
- `domain/usecase/` — all 9 use cases stable.
- `data/local/AppDatabase.kt` — 7 entities, version 2.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 18 implementation)
