# Project State

## Current objective
- Plan 10b (Advanced Combat) is complete. Next: Plan 12 (Round Lifecycle & Post-Round) — CRITICAL PATH.

## What works
- Plans 01–11 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, and all combat mechanics complete.
- Plan 10b complete: Orbs (orbiting projectiles with per-enemy cooldown), Multishot (fire at N targets simultaneously), Bounce Shot (projectile chaining between enemies). All three respond to in-round upgrade changes.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- GDD/docs still have some Google Fit references (cosmetic).

## Top priorities (next 5)
1. Plan 12: Round Lifecycle & Post-Round (CRITICAL PATH)
2. Plan 13: Tier System & Progression (after Plan 12)
3. Plan 16: Labs System (unblocked)
4. Plan 17: Cards System (unblocked)
5. Plan 22: Stats & History Screen (unblocked)

## Next actions (explicit order)
1. Implement Plan 12 (Round Lifecycle & Post-Round) — on critical path.
2. Read `docs/plans/plan-12-round-lifecycle.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 12 is ready (depends on Plan 11 ✓). CRITICAL PATH. Unlocks Plans 13, 14, 15.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — ResolveStats, CalculateDamage, CalculateDefense, CalculateUpgradeCost.
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 10b implementation)
