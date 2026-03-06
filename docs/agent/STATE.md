# Project State

## Current objective
- Plan 11 (In-Round Upgrades & Cash Economy) is complete. Next: Plan 10b (Advanced Combat) or Plan 12 (Round Lifecycle) — both on critical path.

## What works
- Plans 01–10: All foundation layers complete.
- Plan 11 complete: Full cash economy (tier-scaled kill rewards, wave completion bonus, interest), in-round upgrade menu (3-tab Compose overlay, always accessible), purchase flow (affordability → free chance → deduct → re-resolve stats → push to engine), spendCash/updateZigguratStats on engine.

## Known issues / debt
- Notification uses placeholder system icon.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- GDD/docs still have some Google Fit references (cosmetic).
- Orbs, Multishot, Bounce Shot deferred to Plan 10b (mini-plan created).

## Top priorities (next 5)
1. Plan 10b: Advanced Combat Mechanics — Orbs, Multishot, Bounce Shot (parallelizable)
2. Plan 12: Round Lifecycle & Post-Round (CRITICAL PATH)
3. Plan 16: Labs System (unblocked)
4. Plan 17: Cards System (unblocked)
5. Plan 22: Stats & History Screen (unblocked)

## Next actions (explicit order)
1. Implement Plan 10b or Plan 12 — both are ready.
2. Read the relevant plan file before starting.

## Parallelizable branches (after dependencies met)
- Plan 10b is ready (depends on Plan 10 ✓). Orbs/Multishot/Bounce.
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
- Last run: 2026-03-06 (Plan 11 implementation)
