# Project State

## Current objective
- Plan R (Remediation) — fix bugs and UX issues identified by external code review before production release. Tier 1 (R01–R05) blocks Plan 31.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, full round lifecycle, tier/biome progression, all progression systems, notifications & widget, anti-cheat, monetization (stub), polish & VFX, balancing, testing, release prep complete.
- DB version 7: 12 entities. 347 JVM tests, all green. Release APK builds (26MB).

## Known issues / debt (from external review)
- **Critical:** Step double-crediting between StepCounterService and StepSyncWorker (R01).
- **Critical:** Health Connect escrow doesn't actually withhold steps; can double-award (R02).
- **High:** Battle engine gets `emptyMap()` for workshop utility levels — CASH_BONUS/CASH_PER_WAVE/INTEREST broken (R03).
- **High:** STEP_MULTIPLIER and RECOVERY_PACKAGES purchasable but unimplemented (R04).
- **High:** Encrypted DB backup/restore can crash on new device; no Room migrations (R05).
- **High:** Widget balance always 0, click target broken (R06).
- **High:** Walking missions only update on screen open (R07).
- **Medium:** Persistent notification setting misleading; lastActiveAt never updated (R08).
- **Medium:** Deep-link fails when app open; premium state inconsistent; adRemoved lost on replay (R09).
- **Medium:** Silent action failures, no double-tap guards, midnight date staleness (R10).
- **Medium:** Symbol-only labels, placeholder contact emails, README inaccuracies (R11).
- Billing/ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented.
- Sound assets are placeholder sine wave tones.
- No app icon resources.

## Top priorities (next 5)
1. R01: Step Ingestion Unification (Critical — blocks R02)
2. R02: Escrow Redesign (Critical — blocks release)
3. R03: Battle Workshop Wiring (High — quick fix)
4. R04: Dead Upgrade Cleanup (High — quick fix)
5. R05: Database Safety (High — blocks release)

## Next actions (explicit order)
1. R01 → R02 (sequential — step integrity critical path)
2. R03, R04, R05, R06, R07 (parallel — all independent)
3. R08, R09 (parallel — Tier 2)
4. R10, R11 (parallel — Tier 3)
5. R12: Integration test coverage (after R01–R11)
6. Plan 31: Play Console & Store Publication (after R Tier 1 complete)

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
- Critical path: 01→…→30→R (Tier 1)→31
- Last run: 2026-03-11 (Remediation plan creation)
