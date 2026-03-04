# Project State

## Current objective
- Plan 07 (Workshop Screen & Upgrades) is complete. Next: Plan 08 (Battle Renderer) — CRITICAL PATH.

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme.
- Plan 01 complete: All domain models, enums, cost calculation engine.
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7) and Room-backed implementations (7). Hilt modules wired.
- Plan 04 complete: Step Counter Service — foreground service, sensor, rate limiter, daily ceiling, boot receiver, WorkManager sync.
- Plan 05 complete: Health Connect Integration — cross-validation, escrow system, gap-filling, Activity Minute Parity.
- Plan 06 complete: Home Screen & Navigation — NavHost, bottom nav, HomeViewModel, real dashboard.
- Plan 07 complete: Workshop Screen & Upgrades — 3-tab layout (Attack/Defense/Utility), 23 upgrades, tap-to-buy, PurchaseUpgrade + QuickInvest use cases, WorkshopViewModel, UpgradeCard composable.

## Known issues / debt
- Notification uses placeholder system icon — replace with custom app icon when assets are created.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map in StepSyncWorker (deferred).
- GDD, architecture.md, database-schema.md still have some Google Fit references (cosmetic, non-blocking).

## Top priorities (next 5)
1. Plan 08: Battle Renderer — Game Loop & Ziggurat (CRITICAL PATH)
2. Plan 09: Battle System — Enemies & Waves (after Plan 08)
3. Plan 16: Labs System (after Plan 07 ✓ — now unblocked)
4. Plan 17: Cards System (after Plan 07 ✓ — now unblocked)
5. Plan 22: Stats & History Screen (after Plan 06 ✓ — unblocked)

## Next actions (explicit order)
1. Implement Plan 08 (Battle Renderer) — on critical path.
2. Read `docs/plans/plan-08-battle-renderer.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 08 is ready (depends on Plan 06 ✓). CRITICAL PATH. Unlocks Plan 09.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable.
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `data/sensor/StepRateLimiter.kt` — anti-cheat rate limiter.
- `presentation/navigation/` — nav graph and bottom bar established.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-04 (Plan 07 implementation)
