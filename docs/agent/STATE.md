# Project State

## Current objective
- Plan 06 (Home Screen & Navigation) is complete. Next: Plan 07 (Workshop Screen) or Plan 08 (Battle Renderer) — both on critical path.

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme.
- Plan 01 complete: All domain models, enums, cost calculation engine.
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7) and Room-backed implementations (7). Hilt modules wired.
- Plan 04 complete: Step Counter Service — foreground service, sensor, rate limiter, daily ceiling, boot receiver, WorkManager sync.
- Plan 05 complete: Health Connect Integration — cross-validation, escrow system, gap-filling, Activity Minute Parity.
- Plan 06 complete: Home Screen & Navigation — Compose NavHost with 5 routes, bottom nav bar, HomeViewModel with live data from Room, real Home dashboard (steps, balance, tier, biome, best wave, battle button), placeholder screens for Workshop/Battle/Labs/Stats.

## Known issues / debt
- Notification uses placeholder system icon — replace with custom app icon when assets are created.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map in StepSyncWorker (deferred).
- GDD, architecture.md, database-schema.md still have some Google Fit references (cosmetic, non-blocking).

## Top priorities (next 5)
1. Plan 08: Battle Renderer — Game Loop & Ziggurat (CRITICAL PATH)
2. Plan 07: Workshop Screen & Upgrades (after Plan 06 ✓)
3. Plan 09: Battle System — Enemies & Waves (after Plan 08)
4. Plan 22: Stats & History Screen (after Plan 06 ✓)
5. Plan 19: Walking Encounters & Supply Drops (unblocked)

## Next actions (explicit order)
1. Pick Plan 07 or Plan 08 (both ready — Plan 06 complete).
2. Plan 08 is on the critical path (08→09→10→11→12→13→18→27→28→29→30).
3. Read the chosen plan file in `docs/plans/` before starting.

## Parallelizable branches (after dependencies met)
- Plan 07 is ready (depends on Plan 06 ✓). Unlocks Plans 16, 17.
- Plan 08 is ready (depends on Plan 06 ✓). Unlocks Plan 09. CRITICAL PATH.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable.
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `data/sensor/StepRateLimiter.kt` — anti-cheat rate limiter.
- `presentation/navigation/` — nav graph and bottom bar established. Add routes, don't restructure.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-04 (Plan 06 implementation)
