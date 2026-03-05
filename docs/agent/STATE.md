# Project State

## Current objective
- Plan 10 (Stats & Combat) is complete. Next: Plan 11 (In-Round Upgrades & Cash Economy) — CRITICAL PATH.

## What works
- Plans 01–09: All foundation layers complete (domain models, database, repositories, step counter, Health Connect, home screen, workshop, battle renderer, enemies & waves).
- Plan 10 complete: Stats resolution engine (ResolveStats), damage calculator (crit + damage/meter), defense calculator (% reduction + flat block), centralized damage pipeline (defense → death defy → thorn), knockback, lifesteal, health regen, death defy. Workshop upgrades now affect all combat stats.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- GDD/docs still have some Google Fit references (cosmetic).
- Orbs, Multishot, Bounce Shot deferred — stats computed in ResolvedStats but not wired to gameplay yet.
- Cash economy simplified (base per type) — Plan 11 adds full formula.

## Top priorities (next 5)
1. Plan 11: In-Round Upgrades & Cash Economy (CRITICAL PATH)
2. Plan 12: Round Lifecycle & Post-Round (after Plan 11)
3. Plan 16: Labs System (unblocked)
4. Plan 17: Cards System (unblocked)
5. Plan 22: Stats & History Screen (unblocked)

## Next actions (explicit order)
1. Implement Plan 11 (In-Round Upgrades & Cash Economy) — on critical path.
2. Read `docs/plans/plan-11-in-round-upgrades.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 11 is ready (depends on Plan 10 ✓). CRITICAL PATH. Unlocks Plan 12.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable. ResolvedStats added (Plan 10).
- `domain/usecase/` — ResolveStats, CalculateDamage, CalculateDefense (Plan 10).
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-05 (Plan 10 implementation)
