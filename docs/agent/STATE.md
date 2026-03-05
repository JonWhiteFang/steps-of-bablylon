# Project State

## Current objective
- Plan 09 (Enemies & Waves) is complete. Next: Plan 10 (Stats & Combat) — CRITICAL PATH.

## What works
- Project scaffold: Gradle 9.3.1, Hilt, Room skeleton, Compose theme.
- Plan 01 complete: All domain models, enums, cost calculation engine.
- Plan 02 complete: All Room entities (7), DAOs (7), TypeConverters, SQLCipher encryption, AppDatabase.
- Plan 03 complete: All repository interfaces (7) and Room-backed implementations (7). Hilt modules wired.
- Plan 04 complete: Step Counter Service — foreground service, sensor, rate limiter, daily ceiling, boot receiver, WorkManager sync.
- Plan 05 complete: Health Connect Integration — cross-validation, escrow system, gap-filling, Activity Minute Parity.
- Plan 06 complete: Home Screen & Navigation — NavHost, bottom nav, HomeViewModel, real dashboard.
- Plan 07 complete: Workshop Screen & Upgrades — 3-tab layout, 23 upgrades, tap-to-buy, PurchaseUpgrade + QuickInvest use cases.
- Plan 08 complete: Battle Renderer — GameSurfaceView, GameLoopThread (60 UPS fixed timestep), GameEngine, Entity system, ZigguratEntity, ProjectileEntity, HealthBarRenderer, BattleScreen, BattleViewModel, speed controls, pause, bottom nav hidden.
- Plan 09 complete: Enemies & Waves — 6 enemy types (Basic/Fast/Tank/Ranged/Boss/Scatter), WaveSpawner (26s+9s), EnemyScaler (1.05^wave), CollisionSystem, EnemyProjectileEntity, nearest-enemy targeting, melee/ranged damage, cash from kills, Scatter splitting, round end on HP=0, live HUD (wave/enemies/cash/phase).

## Known issues / debt
- Notification uses placeholder system icon — replace with custom app icon when assets are created.
- ActivityMinuteConverter passes empty sensorStepsPerMinute map in StepSyncWorker (deferred).
- GDD, architecture.md, database-schema.md still have some Google Fit references (cosmetic, non-blocking).
- BattleViewModel loads tier but doesn't yet apply workshop bonuses to base stats — Plan 10 adds ResolveStats.
- Cash economy is simplified (base cash per type only) — Plan 11 adds full formula with tier multiplier + workshop bonuses.
- EnemyType enum updated to match battle-formulas.md (FAST/RANGED/BOSS corrections).

## Top priorities (next 5)
1. Plan 10: Battle System — Stats & Combat (CRITICAL PATH)
2. Plan 11: In-Round Upgrades & Cash Economy (after Plan 10)
3. Plan 16: Labs System (after Plan 07 ✓ — now unblocked)
4. Plan 17: Cards System (after Plan 07 ✓ — now unblocked)
5. Plan 22: Stats & History Screen (after Plan 06 ✓ — unblocked)

## Next actions (explicit order)
1. Implement Plan 10 (Stats & Combat) — on critical path.
2. Read `docs/plans/plan-10-stats-combat.md` before starting.

## Parallelizable branches (after dependencies met)
- Plan 10 is ready (depends on Plan 09 ✓). CRITICAL PATH. Unlocks Plan 11.
- Plan 16 is ready (depends on Plan 07 ✓). Labs System.
- Plan 17 is ready (depends on Plan 07 ✓). Cards System.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 19, 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — Plan 01 complete, stable. ZigguratBaseStats added (Plan 08). EnemyType corrected (Plan 09).
- `data/local/AppDatabase.kt` — 7 entities, version 1.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `data/sensor/StepRateLimiter.kt` — anti-cheat rate limiter.
- `presentation/navigation/` — nav graph and bottom bar established.
- `presentation/battle/engine/` — GameEngine, Entity, WaveSpawner, CollisionSystem, EnemyScaler (Plan 08+09 foundation).

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-05 (Plan 09 implementation)
