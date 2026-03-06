# Project State

## Current objective
- Plan 13 (Tier System & Progression) is complete. Next: Plan 18 (Biome Progression) — CRITICAL PATH.

## What works
- Plans 01–13 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, and tier system complete.
- Plan 13 complete: Tier unlock via wave milestones (GDD §6.1), tier selector on home screen, battle conditions at Tier 6+ (enemy speed, orb/knockback/thorn resistance, armored enemies, boss frequency, enemy attack speed), post-round tier unlock notification, highestUnlockedTier persisted separately from selected play tier.
- Domain layer unit tests: 93 JVM tests covering all use cases, domain models, battle condition effects, tier unlock logic, EnemyScaler, and StepRateLimiter. All green.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Plan 02/03 docs still reference `googleFitSteps` column name (cosmetic — completed plans, actual code is correct).
- Room DB version bumped to 2 with fallbackToDestructiveMigration (dev only — proper migration needed before release).

## Top priorities (next 5)
1. Plan 18: Biome Progression (CRITICAL PATH — depends on Plan 13 ✓)
2. Plan 14: Step Overdrive (after Plan 12 ✓)
3. Plan 15: Ultimate Weapons (after Plan 12 ✓)
4. Plan 16: Labs System (unblocked)
5. Plan 17: Cards System (unblocked)

## Next actions (explicit order)
1. Implement Plan 18 (Narrative Biome Progression) — on critical path.
2. Read `docs/plans/plan-18-biome-progression.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 18 is ready (depends on Plan 13 ✓). CRITICAL PATH. Unlocks Plans 24, 27.
- Plan 14 is ready (depends on Plan 12 ✓). Step Overdrive.
- Plan 15 is ready (depends on Plan 12 ✓). Ultimate Weapons.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable (includes BattleConditionEffects, TierConfig).
- `domain/usecase/` — ResolveStats, CalculateDamage, CalculateDefense, CalculateUpgradeCost, UpdateBestWave, CheckTierUnlock.
- `data/local/AppDatabase.kt` — 7 entities, version 2.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 13 implementation)
