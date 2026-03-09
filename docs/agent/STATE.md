# Project State

## Current objective
- Plan 27 (Polish & VFX) is complete. Next: Plan 28 (Balancing & Tuning) — critical path.

## What works
- Plans 01–27 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen, notifications & widget, anti-cheat hardening, monetization (stub), polish & VFX complete.
- Plan 27 complete: Pooled particle system (200 particles), projectile trail effects, enemy death animations (6 types), floating cash text, screen shake system, enhanced UW visual effects (6 particle-based spectacles), overdrive aura particle emissions (4 types), wave announcements + boss warnings, SoundManager with 7 placeholder audio assets, sound mute toggle in settings, minimal UI animations (screen transitions, purchase pulse, step counter animateContentSize), reduced motion support via system ANIMATOR_DURATION_SCALE.
- DB version 7: 12 entities, PlayerProfile has monetization fields.
- Domain layer unit tests: 244 JVM tests. All green.

## Known issues / debt
- Billing and ads use stub implementations (StubBillingManager, StubRewardAdManager) — real SDK integration deferred.
- Cosmetic visual application not implemented (equip/unequip tracked but no visual change in battle).
- Notification uses placeholder system icon (all channels).
- Room DB version 7 with fallbackToDestructiveMigration (dev only).
- hiltViewModel() deprecation warnings — cosmetic, works fine.
- Step Surge gemMultiplier tracked but not yet consumed.
- Supply drop step burst trigger deferred.
- Milestone cosmetic rewards no-op visually.
- Widget shows 0 for balance (DailyStepManager doesn't have balance context).
- Lifetime currency counters start from 0 (no retroactive backfill).
- Sound assets are placeholder sine wave tones — replace with real royalty-free audio.
- UWVisualEffect.kt and GameEngine.kt have minor redundant conversion warnings.

## Top priorities (next 5)
1. Plan 28: Balancing & Tuning (CRITICAL PATH — depends on Plan 27 ✓)
2. Plan 29: Testing & QA (after Plan 28)
3. Plan 30: Release Prep (after Plan 29)
4. Real SDK integration for billing/ads (post-release or pre-release)
5. Replace placeholder audio with real sound effects

## Next actions (explicit order)
1. Plan 28 (Balancing & Tuning) — all systems polished, tune numbers.
2. Plan 29 (Testing & QA) after balancing.

## Parallelizable branches (after dependencies met)
- Plan 28 is ready (depends on Plan 27 ✓). CRITICAL PATH. Unlocks Plan 29.
- Plan 24: DEFERRED to post-v1.0.
- Real billing/ads SDK integration: can be done anytime by swapping DI bindings.
- Real audio assets: can be swapped in anytime by replacing res/raw/ files.

## Do-not-touch / fragile zones
- `domain/model/` — stable.
- `domain/usecase/` — all 34 use cases stable.
- `data/local/AppDatabase.kt` — 12 entities, version 7.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards + widget updates + anti-cheat.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/GameEngine.kt` — now integrates EffectEngine, SoundManager, all trigger points.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `data/sensor/StepVelocityAnalyzer.kt` — anti-cheat velocity analysis.
- `data/healthconnect/StepCrossValidator.kt` — graduated cross-validation.

## References
- Master plan: docs/plans/master-plan.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30
- Last run: 2026-03-09 (Plan 27 implementation)
