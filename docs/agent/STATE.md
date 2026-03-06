# Project State

## Current objective
- Plan 12 (Round Lifecycle) is complete. Next: Plan 13 (Tier System & Progression) — CRITICAL PATH.

## What works
- Plans 01–12 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, and full round lifecycle complete.
- Plan 12 complete: Round start flow, post-round summary overlay (wave record, enemies killed, cash earned, time survived), best wave persistence per tier, pause overlay with Resume/Quit, auto-pause on background, Play Again resets engine in-place, Return to Workshop navigates out.
- Domain layer unit tests: 80 JVM tests covering all 8 use cases, 6 domain models, EnemyScaler, and StepRateLimiter. JUnit 5 + coroutines-test. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Plan 02/03 docs still reference `googleFitSteps` column name (cosmetic — completed plans, actual code is correct).

## Top priorities (next 5)
1. Plan 13: Tier System & Progression (CRITICAL PATH)
2. Plan 14: Step Overdrive (after Plan 12 ✓)
3. Plan 15: Ultimate Weapons (after Plan 12 ✓)
4. Plan 16: Labs System (unblocked)
5. Plan 17: Cards System (unblocked)

## Next actions (explicit order)
1. Implement Plan 13 (Tier System & Progression) — on critical path.
2. Read `docs/plans/plan-13-tier-system.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 13 is ready (depends on Plan 12 ✓). CRITICAL PATH. Unlocks Plan 18.
- Plan 14 is ready (depends on Plan 12 ✓). Step Overdrive.
- Plan 15 is ready (depends on Plan 12 ✓). Ultimate Weapons.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — ResolveStats, CalculateDamage, CalculateDefense, CalculateUpgradeCost, UpdateBestWave.
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 12 implementation)
