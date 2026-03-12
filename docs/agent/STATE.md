# Project State

## Current objective
- Plan 31: Play Console & Store Publication — final step before production release.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, full round lifecycle, tier/biome progression, all progression systems, notifications & widget, anti-cheat, monetization (stub), polish & VFX, balancing, testing, release prep complete.
- Plan R (Remediation): All 12 sub-plans (R01–R12) complete. All bugs and UX issues from external review resolved.
- DB version 7: 12 entities. 399 JVM tests, all green. Release APK builds (26MB).

## Known issues / debt
- Billing/ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented.
- Sound assets are placeholder sine wave tones.
- No app icon resources.

## Top priorities (next 5)
1. Plan 31: Play Console & Store Publication

## Next actions (explicit order)
1. Plan 31: Play Console & Store Publication

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests.
- `domain/usecase/` — all 32 use cases stable.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `app/proguard-rules.pro` — hardened R8 rules.
- `app/build.gradle.kts` — signing config, version 1.0.0.

## References
- Remediation plan: docs/plans/plan-R-remediation.md
- External review: docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX.md
- Master plan: docs/plans/master-plan.md
- Balance report: docs/balance/balance-report.md
- Release docs: docs/release/
- Critical path: 01→…→30→R→31
- Last run: 2026-03-12 (R12 Integration Test Coverage)
