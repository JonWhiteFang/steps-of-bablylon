# Project State

## Current objective
- Plan 05 (Health Connect Integration) is complete. Next: Plan 06 (Home Screen & Navigation) — on critical path.

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme, Home placeholder.
- Plan 01 complete: All domain models, enums, cost calculation engine.
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7) and Room-backed implementations (7). Hilt modules wired.
- Plan 04 complete: Step Counter Service — foreground service, sensor, rate limiter, daily ceiling, boot receiver, WorkManager sync.
- Plan 05 complete: Health Connect Integration — cross-validation, escrow system, gap-filling, Activity Minute Parity, exercise session reading, double-counting prevention.

## Known issues / debt
- Notification uses placeholder system icon — replace with custom app icon when assets are created.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map in StepSyncWorker — needs real sensor data for full double-counting prevention (requires per-minute sensor tracking, deferred).
- GDD, architecture.md, database-schema.md, and some plan files still have Google Fit references (cosmetic, non-blocking).

## Top priorities (next 5)
1. Plan 06: Home Screen & Navigation (Compose nav graph, dashboard, bottom nav) — CRITICAL PATH
2. Plan 07: Workshop Screen & Upgrades (after Plan 06)
3. Plan 08: Battle Renderer — Game Loop & Ziggurat (after Plan 06)
4. Plan 19: Walking Encounters & Supply Drops (unblocked)
5. Plan 20: Power Stone & Gem Economy (unblocked)

## Next actions (explicit order)
1. Implement Plan 06 (Home Screen & Navigation) — on critical path.
2. Read `docs/plans/plan-06-home-navigation.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 06 is ready (depends on Plan 03 ✓). Unlocks Plans 07, 08, 22.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat validation.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable.
- `data/local/AppDatabase.kt` — 7 entities, version 1. Schema changed (escrow fields added via destructive fallback).
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `data/sensor/StepRateLimiter.kt` — anti-cheat rate limiter.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-04 (Plan 05 implementation)
