# Project State

## Current objective
- Plan 30 (Release Prep) is complete. Next: Plan 31 (Play Console & Store Publication) — final critical path step.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, stats engine, cash economy, in-round upgrades, all combat mechanics, full round lifecycle, tier system, biome progression, step overdrive, ultimate weapons, labs system, cards system, walking encounters, premium currency economy, milestones & daily missions, stats & history screen, notifications & widget, anti-cheat hardening, monetization (stub), polish & VFX, balancing & tuning, testing & QA, release prep complete.
- Plan 30 complete: R8 rules hardened, signing config wired, fallbackToDestructiveMigration removed, version 1.0.0, privacy policy, Play Store listing text, CHANGELOG, release APK builds successfully (26MB unsigned).
- DB version 7: 12 entities, PlayerProfile has monetization fields.
- Unit tests: 347 JVM tests. All green.
- Release build: assembleRelease succeeds with R8 minification, no R8 errors.

## Known issues / debt
- Billing and ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented.
- Notification uses placeholder system icon.
- Sound assets are placeholder sine wave tones.
- hiltViewModel() deprecation warnings (6 screens).
- Step Surge gemMultiplier tracked but not yet consumed.
- Supply drop step burst trigger deferred.
- Milestone cosmetic rewards no-op visually.
- Widget shows 0 for balance.
- Lifetime currency counters start from 0.
- First UW unlock takes ~3 weeks (not 2) — acceptable for mid-game reward.
- Interest at max level is 59% of kill income — borderline but requires significant investment.
- LabsViewModel and MissionsViewModel tests use use-case-level testing (not ViewModel-level) due to infinite ticker loops.
- No instrumented tests (Room DAO, Compose UI) — deferred to post-release.
- No app icon resources (using default Android icon) — deferred to Plan 31.
- Upload keystore not yet generated — developer must run keytool manually (see docs/release/signing-guide.md).
- Privacy policy needs hosting at public URL (GitHub Pages recommended).
- Contact email placeholder in privacy policy and store listing.

## Top priorities (next 5)
1. Plan 31: Play Console & Store Publication (CRITICAL PATH — depends on Plan 30 ✓)
2. Generate upload keystore and create keystore.properties
3. Host privacy policy at public URL (GitHub Pages)
4. Create visual assets (app icon, screenshots, feature graphic)
5. Real SDK integration for billing/ads

## Next actions (explicit order)
1. Generate upload keystore (manual: `keytool` command in docs/release/signing-guide.md)
2. Plan 31 (Play Console) — Console setup, store listing upload, IAP/ad SDK integration, test tracks, publication.
3. Replace placeholder audio with real sound effects.
4. Plan 24: Accessibility (post-v1.0).

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests.
- `domain/usecase/` — all 32 use cases stable.
- `data/local/AppDatabase.kt` — 12 entities, version 7. No destructive migration.
- `data/sensor/DailyStepManager.kt` — integrates supply drops + economy rewards + widget updates + anti-cheat.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `presentation/battle/engine/GameEngine.kt` — integrates EffectEngine, SoundManager, all trigger points.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.
- `app/proguard-rules.pro` — hardened R8 rules for all libraries.
- `app/build.gradle.kts` — signing config, version 1.0.0.

## References
- Master plan: docs/plans/master-plan.md
- Balance report: docs/balance/balance-report.md
- Release docs: docs/release/ (privacy policy, store listing, signing guide, checklist)
- Critical path: 01→02→03→06→08→09→10→11→12→13→18→27→28→29→30→31
- Last run: 2026-03-10 (Plan 30 implementation)
