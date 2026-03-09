# Project State

## Current objective
- Plan 21 (Milestones & Daily Missions) is complete. Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–21 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions complete.
- Plan 21 complete: 6 walking milestones (1k→5M steps, Gem/PS/cosmetic rewards), 3 daily missions (walking/battle/upgrade, midnight refresh), progress tracking from battle/workshop/lab events, Missions screen with claim flow, home screen badge.
- DB version 5: 11 entities (added MilestoneEntity, DailyMissionEntity).
- Domain layer unit tests: 206 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon (step counter + supply drops).
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 5 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- LabsScreen/CardsScreen/MissionsScreen hiltViewModel() deprecation warning (moved package) — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed (no Gem earning in battle yet — Plan 26).
- Supply drop step burst trigger deferred — needs step velocity tracking in DailyStepManager.
- Long-distance walking Gem bonuses deferred to Plan 21 (milestones) — now covered by milestone Gem rewards.
- TrackWeeklyChallenge/TrackDailyLogin use cases depend on DAOs directly (not via repository) — acceptable for data-layer integration.
- Milestone cosmetic rewards stored as claimed but no-op visually (cosmetics system doesn't exist yet).
- Card Pack milestone rewards awarded as equivalent Gems (50/150/500) — player buys packs themselves.

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 22: Stats & History (unblocked)
3. Plan 25: Anti-cheat hardening (unblocked)
4. Plan 26: Monetization & Ads (unblocked — depends on Plan 17 ✓)
5. Plan 23: Notifications & Widget (unblocked)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 26 is ready (depends on Plan 17 ✓). Monetization.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plan 23 is ready (depends on Plan 04 ✓). Notifications & Widget.

## Do-not-touch / fragile zones
- `domain/model/` — stable (includes Milestone, MilestoneReward, DailyMissionType, MissionCategory).
- `domain/usecase/` — all 33 use cases stable.
- `data/local/AppDatabase.kt` — 11 entities, version 5.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-09 (Plan 21 implementation)
