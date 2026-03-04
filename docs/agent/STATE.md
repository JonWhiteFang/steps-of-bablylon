# Project State

## Current objective
- Plan 04 (Step Counter Service) is complete. Next: Plan 06 (Home Screen & Navigation) or Plan 05 (Google Fit Integration).

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme, Home placeholder.
- Plan 01 complete: All domain models, enums, cost calculation engine (34 files in `domain/model/`, 2 use cases).
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7 in `domain/repository/`) and Room-backed implementations (7 in `data/repository/`). Hilt modules wired.
- Plan 04 complete: Step Counter Service — foreground service with TYPE_STEP_COUNTER sensor, rate limiter (200/min, 250 burst), daily ceiling (50k), boot receiver, WorkManager 15-min sync, Hilt-WorkManager integration, runtime permissions in MainActivity.

## Known issues / debt
- Notification uses placeholder system icon (`android.R.drawable.ic_menu_directions`) — replace with custom app icon when assets are created.
- StepCounterService notification balance field shows 0 — could observe PlayerRepository wallet flow for live balance updates (low priority, cosmetic).

## Top priorities (next 5)
1. Plan 06: Home Screen & Navigation (Compose nav graph, dashboard, bottom nav) — on critical path
2. Plan 05: Google Fit Integration (cross-validation, Activity Minute Parity, escrow)
3. Plan 07: Workshop Screen & Upgrades (after Plan 06)
4. Plan 08: Battle Renderer — Game Loop & Ziggurat (after Plan 06)
5. Plan 19: Walking Encounters & Supply Drops (after Plan 04 — now unblocked)

## Next actions (explicit order)
1. Pick Plan 06 (critical path) or Plan 05 (Google Fit, now unblocked).
2. Read the chosen plan file in `docs/plans/`.
3. Implement per the plan's task list.

## Parallelizable branches (after dependencies met)
- Plan 06 is ready (depends on Plan 03 ✓). Unlocks Plans 07, 08, 22.
- Plan 05 is ready (depends on Plan 04 ✓). Unlocks Plan 25.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable. Only modify if a later plan explicitly requires it.
- `data/local/AppDatabase.kt` — 7 entities registered, schema version 1. Changes require migration planning.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `data/sensor/StepRateLimiter.kt` — anti-cheat rate limiter. Changes require careful review.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-04 (Plan 04 implementation)
