# Project State

## Current objective
- Plan 19 (Walking Encounters & Supply Drops) is complete. Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–19 + 10b + 18: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, and walking encounters complete.
- Plan 19 complete: Supply drop generation (seeded random, 4 trigger types), ClaimSupplyDrop use case, SupplyDropNotificationManager (dedicated channel), DailyStepManager integration (drops generated during walks), UnclaimedSuppliesScreen + ViewModel, inbox badge on HomeScreen, notification deep-link to supplies screen, inbox cap (max 10, oldest discarded), FakeWalkingEncounterRepository.
- Decisions: No GPS triggers (step-based only), no free Overdrive charges (deferred), no Card Pack rewards (Card Dust instead), milestone gives 5 Gems.
- Domain layer unit tests: 170 JVM tests. All green.

## Known issues / debt
- Notification uses placeholder system icon (step counter + supply drops).
- ActivityMinuteConverter passes empty sensorStepsPerMinute map (deferred).
- Room DB version 3 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- LabsScreen/CardsScreen hiltViewModel() deprecation warning (moved package) — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed (no Gem earning in battle yet — Plan 20).
- Supply drop step burst trigger deferred — needs step velocity tracking in DailyStepManager.
- 10k milestone gives only Gems (5); Power Stones (3) deferred to second drop or combined reward.

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 22: Stats & History (unblocked)
3. Plans 20/21: Premium currencies, milestones (unblocked)
4. Plan 25: Anti-cheat hardening (unblocked)
5. Plan 26: Monetization & Ads (unblocked — depends on Plan 17 ✓)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 26 is ready (depends on Plan 17 ✓). Monetization.
- Plan 24 is ready (depends on Plan 18 ✓). Accessibility.
- Plan 22 is ready (depends on Plan 06 ✓). Stats & History.
- Plan 25 is ready (depends on Plan 05 ✓). Anti-cheat.
- Plans 20, 23 are ready (depend on Plan 04 ✓).

## Do-not-touch / fragile zones
- `domain/model/` — stable (includes SupplyDropTrigger, SupplyDropReward, DropGeneratorState).
- `domain/usecase/` — all 25 use cases stable (added GenerateSupplyDrop, ClaimSupplyDrop).
- `data/local/AppDatabase.kt` — 7 entities, version 3.
- `data/sensor/DailyStepManager.kt` — now integrates supply drop generation.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine (overdrive + UW + Second Wind + card cash bonus), CollisionSystem, WaveSpawner, EnemyScaler.
- `presentation/battle/biome/` — BiomeTheme, BackgroundRenderer.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-06 (Plan 19 implementation)
