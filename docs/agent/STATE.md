# Project State

## Current objective
- Plan R2: Second remediation pass — 12 sub-plans from second external review.
- Plan 31: Play Console & Store Publication — blocked until R2 Tier 1 complete.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, full round lifecycle, tier/biome progression, all progression systems, notifications & widget, anti-cheat, monetization (stub), polish & VFX, balancing, testing, release prep complete.
- Plan R (Remediation): All 12 sub-plans (R01–R12) complete. All bugs and UX issues from external review resolved.
- R2-01 (Activity-Minute Idempotency): Complete. Delta-based crediting, shared ensureInitialized(), combined 50k ceiling.
- DB version 7: 12 entities. 397 JVM tests, all green. Release APK builds (26MB).

## Known issues / debt
- Billing/ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented (R2-11 gates purchases).
- Sound assets are placeholder sine wave tones.
- No app icon resources.
- Activity-minute pipeline bypasses widget/mission/drop/economy updates (R2-02).
- 12 `stateIn(viewModelScope).value` occurrences in action handlers (R2-03).
- `.fallbackToDestructiveMigration()` still in production DB config (R2-06).

## Top priorities (next 5)
1. R2-02: Activity-Minute Pipeline Unification (High)
2. R2-06: Destructive Migration Removal (High)
3. R2-03: Hot Flow Cleanup (High)
4. R2-04, R2-05, R2-07: Quick UX/observability fixes (High)
5. R2-12: Activity-Minute Test Coverage (High, depends on R2-02)

## Next actions (explicit order)
1. Implement R2-02 (unify activity-minute pipeline with recordSteps follow-ons)
2. Implement R2-06 (replace fallbackToDestructiveMigration)
3. Implement R2-03 (replace stateIn().value with first())
4. Implement R2-04, R2-05, R2-07 (quick UX/observability fixes)
5. Implement R2-12 (activity-minute tests)
6. Implement R2-08 through R2-11 (Tier 3 polish)
7. Plan 31: Play Console & Store Publication

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests.
- `domain/usecase/` — all 32 use cases stable.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `app/proguard-rules.pro` — hardened R8 rules.
- `app/build.gradle.kts` — signing config, version 1.0.0.

## References
- Remediation plan (1st review): docs/plans/plan-R-remediation.md
- Remediation plan (2nd review): docs/plans/plan-R2-remediation.md
- External review (1st): docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX.md
- External review (2nd): docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX_2.md
- Master plan: docs/plans/master-plan.md
- Balance report: docs/balance/balance-report.md
- Release docs: docs/release/
- Critical path: 01→…→30→R→R2→31
- Last run: 2026-03-13 (R2-01 Activity-Minute Idempotency)
