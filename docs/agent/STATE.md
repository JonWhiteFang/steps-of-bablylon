# Project State

## Current objective
- Plan 23 (Notifications & Widget) is complete. Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–23 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen, notifications & widget complete.
- Plan 23 complete: Enhanced persistent notification (Workshop/Battle action buttons, live balance), 2×2 home screen widget, smart reminders (upgrade proximity via StepSyncWorker), milestone alert notifications (best wave, step milestones), notification preferences (4 toggles), settings screen.
- DB version 6: 11 entities, PlayerProfile has lifetime currency counters + battle stats.
- Domain layer unit tests: 206 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon (all channels).
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 6 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- hiltViewModel() deprecation warnings — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed (Plan 26).
- Supply drop step burst trigger deferred.
- Milestone cosmetic rewards no-op visually.
- Widget shows 0 for balance (DailyStepManager doesn't have balance context).
- Lifetime currency counters start from 0 (no retroactive backfill).

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 25: Anti-cheat hardening (unblocked)
3. Plan 26: Monetization & Ads (unblocked — depends on Plan 17 ✓)
4. Plan 24: Accessibility (unblocked)
5. Plan 28: Balancing & Tuning (after Plan 27)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 26 is ready (depends on Plan 17 ✓). Monetization.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 33 use cases stable.
- `data/local/AppDatabase.kt` — 11 entities, version 6.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards + widget updates.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-09 (Plan 23 implementation)
