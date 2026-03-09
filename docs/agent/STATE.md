# Project State

## Current objective
- Plan 22 (Stats & History Screen) is complete. Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–22 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen complete.
- Plan 22 complete: Walking history bar chart (7-day/30-day/12-week), battle stats (rounds/kills/cash), all-time stats (lifetime steps, Gems/PS earned/spent, days active, avg daily), today's activity breakdown, Canvas-drawn chart.
- DB version 6: 11 entities, PlayerProfile has lifetime currency counters + battle stats.
- Domain layer unit tests: 206 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon (step counter + supply drops).
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 6 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- LabsScreen/CardsScreen/MissionsScreen/StatsScreen hiltViewModel() deprecation warning — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed (no Gem earning in battle yet — Plan 26).
- Supply drop step burst trigger deferred — needs step velocity tracking in DailyStepManager.
- Milestone cosmetic rewards stored as claimed but no-op visually (cosmetics system doesn't exist yet).
- Card Pack milestone rewards awarded as equivalent Gems (50/150/500).
- Lifetime currency counters start from 0 (no retroactive backfill of pre-Plan 22 earnings).

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 25: Anti-cheat hardening (unblocked)
3. Plan 26: Monetization & Ads (unblocked — depends on Plan 17 ✓)
4. Plan 23: Notifications & Widget (unblocked)
5. Plan 24: Accessibility (unblocked)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 26 is ready (depends on Plan 17 ✓). Monetization.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plan 23 is ready (depends on Plan 04 ✓). Notifications & Widget.

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 33 use cases stable.
- `data/local/AppDatabase.kt` — 11 entities, version 6.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-09 (Plan 22 implementation)
