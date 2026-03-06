# Project State

## Current objective
- Plan 20 (Power Stone & Gem Economy) is complete. Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–20 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, and premium currency economy complete.
- Plan 20 complete: Weekly step challenge (PS at 50k/75k/100k), daily login PS (1k+ steps), login streak Gems (1–5 over 7 days), wave milestone PS (1/2/5 on new bests), Currency Dashboard screen, DailyStepManager integration, HomeViewModel daily login trigger.
- DB version 4: 9 entities (added WeeklyChallengeEntity, DailyLoginEntity), PlayerProfile has streak fields.
- Domain layer unit tests: 179 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon (step counter + supply drops).
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 4 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- LabsScreen/CardsScreen hiltViewModel() deprecation warning (moved package) — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed (no Gem earning in battle yet — Plan 26).
- Supply drop step burst trigger deferred — needs step velocity tracking in DailyStepManager.
- Long-distance walking Gem bonuses deferred to Plan 21 (milestones).
- TrackWeeklyChallenge/TrackDailyLogin use cases depend on DAOs directly (not via repository) — acceptable for data-layer integration.

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 21: Milestones & Daily Missions (unblocked — depends on Plan 20 ✓)
3. Plan 22: Stats & History (unblocked)
4. Plan 25: Anti-cheat hardening (unblocked)
5. Plan 26: Monetization & Ads (unblocked — depends on Plan 17 ✓)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 21 is ready (depends on Plan 20 ✓). Milestones & Daily Missions.
- Plan 26 is ready (depends on Plan 17 ✓). Monetization.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plan 23 is ready (depends on Plan 04 ✓). Notifications & Widget.

## Do-not-touch / fragile zones
- `domain/model/` — stable (includes SupplyDropTrigger, SupplyDropReward, DropGeneratorState).
- `domain/usecase/` — all 28 use cases stable.
- `data/local/AppDatabase.kt` — 9 entities, version 4.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 20 implementation)
