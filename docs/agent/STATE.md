# Project State

## Current objective
- Plan 26 (Monetization & Ads) is complete (stub implementation). Next: Plan 27 (Polish & VFX) — critical path.

## What works
- Plans 01–23 + 10b + 18 + 25 + 26: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen, notifications & widget, anti-cheat hardening, monetization (stub) complete.
- Plan 26 complete: Stub billing (3 Gem packs, ad removal, Season Pass), stub reward ads (post-round Gem, double PS, free Card Pack), cosmetic store (7 placeholder items), Season Pass daily Gem bonus + free Lab rush, all ad UI gated on adRemoved flag.
- DB version 7: 12 entities, PlayerProfile has monetization fields.
- Domain layer unit tests: 222 JVM tests. All green.

## Known issues / debt
- Billing and ads use stub implementations (StubBillingManager, StubRewardAdManager) — real SDK integration deferred.
- Cosmetic visual application not implemented (equip/unequip tracked but no visual change in battle).
- Notification uses placeholder system icon (all channels).
- Room DB version 7 with fallbackToDestructiveMigration (dev only).
- Biome transition overlay is simple styled screen — animation polish deferred to Plan 27.
- UW visual effects are simple geometric — polish deferred to Plan 27.
- hiltViewModel() deprecation warnings — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed.
- Supply drop step burst trigger deferred.
- Milestone cosmetic rewards no-op visually.
- Widget shows 0 for balance (DailyStepManager doesn't have balance context).
- Lifetime currency counters start from 0 (no retroactive backfill).

## Top priorities (next 5)
1. Plan 27: Polish & VFX (CRITICAL PATH — depends on Plan 18 ✓)
2. Plan 28: Balancing & Tuning (after Plan 27)
3. Plan 29: Testing & QA (after Plan 28)
4. Plan 30: Release Prep (after Plan 29)
5. Real SDK integration for billing/ads (post-release or pre-release)

## Next actions (explicit order)
1. Plan 27 (Polish & VFX) — all gameplay systems in place, polish everything in one pass.
2. Plan 28 (Balancing) after polish.

## Parallelizable branches (after dependencies met)
- Plan 27 is ready (depends on Plan 18 ✓). CRITICAL PATH. Unlocks Plan 28.
- Plan 24: DEFERRED to post-v1.0.
- Real billing/ads SDK integration: can be done anytime by swapping DI bindings.

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 34 use cases stable.
- `data/local/AppDatabase.kt` — 12 entities, version 7.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards + widget updates + anti-cheat.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/` — GameEngine, CollisionSystem, WaveSpawner, EnemyScaler.
- `data/sensor/StepVelocityAnalyzer.kt` — anti-cheat velocity analysis.
- `data/healthconnect/StepCrossValidator.kt` — graduated cross-validation.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-09 (Plan 26 implementation)
