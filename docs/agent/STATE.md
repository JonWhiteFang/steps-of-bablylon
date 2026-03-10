# Project State

## Current objective
- Plan 29 (Testing & QA) is complete. Next: Plan 30 (Release Prep) — critical path.

## What works
- Plans 01–29 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen, notifications & widget, anti-cheat hardening, monetization (stub), polish & VFX, balancing & tuning, testing & QA complete.
- Plan 29 complete: 64 new JVM tests — ViewModel tests (10 VMs), TrackDailyLogin/TrackWeeklyChallenge use case tests, 7 new fakes + 6 new DAO fakes.
- DB version 7: 12 entities, PlayerProfile has monetization fields.
- Unit tests: 347 JVM tests. All green.

## Known issues / debt
- Billing and ads use stub implementations — real SDK integration deferred.
- Cosmetic visual application not implemented.
- Notification uses placeholder system icon.
- Room DB version 7 with fallbackToDestructiveMigration (dev only).
- Sound assets are placeholder sine wave tones.
- hiltViewModel() deprecation warnings.
- Step Surge gemMultiplier tracked but not yet consumed.
- Supply drop step burst trigger deferred.
- Milestone cosmetic rewards no-op visually.
- Widget shows 0 for balance.
- Lifetime currency counters start from 0.
- First UW unlock takes ~3 weeks (not 2) — acceptable for mid-game reward.
- Interest at max level is 59% of kill income — borderline but requires significant investment.
- LabsViewModel and MissionsViewModel tests use use-case-level testing (not ViewModel-level) due to infinite ticker loops.
- No instrumented tests (Room DAO, Compose UI) — deferred to post-release.

## Top priorities (next 5)
1. Plan 30: Release Prep (CRITICAL PATH — depends on Plan 29 ✓)
2. Plan 31: Play Console & Store Publication (depends on Plan 30)
3. Real SDK integration for billing/ads (part of Plan 31)
4. Replace placeholder audio with real sound effects
5. Plan 24: Accessibility (post-v1.0)

## Next actions (explicit order)
1. Plan 30 (Release Prep) — ProGuard/R8, signing, version, privacy policy, listing assets, build verification, AAB.
2. Plan 31 (Play Console) — Console setup, store listing upload, IAP/ad SDK integration, test tracks, publication.

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests.
- `domain/usecase/` — all 32 use cases stable.
- `data/local/AppDatabase.kt` — 12 entities, version 7.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards + widget updates + anti-cheat.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/GameEngine.kt` — integrates EffectEngine, SoundManager, all trigger points.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.

## References
- Master plan: docs/plans/master-plan.md
- Balance report: docs/balance/balance-report.md
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30→31
- Last run: 2026-03-10 (Plan 29 implementation)
