# Project State

## Current objective
- (1) Implement Plan 04 (Step Counter Service) or Plan 06 (Home Screen & Navigation) — both are unblocked.

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme, Home placeholder.
- Plan 01 complete: All domain models, enums, cost calculation engine (34 files in `domain/model/`, 2 use cases).
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7 in `domain/repository/`) and Room-backed implementations (7 in `data/repository/`). Hilt modules wired.

## Known issues / debt
- (none tracked yet)

## Top priorities (next 5)
1. Plan 04: Step Counter Service (foreground service, sensor, WorkManager, anti-cheat)
2. Plan 06: Home Screen & Navigation (Compose nav graph, dashboard, bottom nav)
3. Plan 07: Workshop Screen & Upgrades (after Plan 06)
4. Plan 08: Battle Renderer — Game Loop & Ziggurat (after Plan 06)
5. Plan 05: Google Fit Integration (after Plan 04)

## Next actions (explicit order)
1. Pick Plan 04 or Plan 06 (both ready — Plan 03 is complete).
2. Read the chosen plan file in `docs/plans/`.
3. Implement per the plan's task list.

## Parallelizable branches (after dependencies met)
- Plans 04 + 06 can run in parallel (both depend only on Plan 03).
- Plans 07, 08, 22 unlock after Plan 06.
- Plans 05, 19, 20, 23 unlock after Plan 04.

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable. Only modify if a later plan explicitly requires it.
- `data/local/AppDatabase.kt` — 7 entities registered, schema version 1. Changes require migration planning.
- `gradle/libs.versions.toml` — single source for all dependency versions.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-04 (initial memory system setup)
