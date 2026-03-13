# Project State

## Current objective
- Plan R2: Second remediation pass — 12 sub-plans from second external review.
- Plan 31: Play Console & Store Publication — blocked until R2 Tier 1 complete.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, full round lifecycle, tier/biome progression, all progression systems, notifications & widget, anti-cheat, monetization (stub), polish & VFX, balancing, testing, release prep complete.
- Plan R (Remediation): All 12 sub-plans (R01–R12) complete. All bugs and UX issues from external review resolved.
- R2-01 (Activity-Minute Idempotency): Complete. Delta-based crediting, shared ensureInitialized(), combined 50k ceiling.
- R2-02 (Activity-Minute Pipeline Unification): Complete. Extracted runFollowOnPipeline(), called from both recordSteps() and recordActivityMinutes().
- R2-03 (Hot Flow Cleanup): Complete. Replaced 12 stateIn(viewModelScope).value with first() or uiState.value reads.
- R2-04 (Battle Exit Navigation): Complete. Renamed "Return to Workshop" → "Leave Battle", parameter onReturnToWorkshop → onExitBattle.
- R2-05 (Notification Setting Alignment): Complete. Renamed toggle "Step Count Updates" → "Live Step Updates", clarified description, added minimal notification variant for when live updates disabled.
- DB version 7: 12 entities. 397 JVM tests, all green. Release APK builds (26MB).

## Known issues / debt
- Billing/ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented (R2-11 gates purchases).
- Sound assets are placeholder sine wave tones.
- No app icon resources.
- `.fallbackToDestructiveMigration()` still in production DB config (R2-06).

## Top priorities (next 5)
1. R2-06: Destructive Migration Removal (High)
2. R2-07: Worker Error Observability (High)
3. R2-12: Activity-Minute Test Coverage (High, R2-01+R2-02 done)
4. R2-08 through R2-11: Tier 3 polish (Medium)
5. Plan 31: Play Console & Store Publication

## Next actions (explicit order)
1. Implement R2-06 (replace fallbackToDestructiveMigration)
2. Implement R2-07 (worker error observability)
3. Implement R2-12 (activity-minute tests)
4. Implement R2-08 through R2-11 (Tier 3 polish)
5. Plan 31: Play Console & Store Publication

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
- Last run: 2026-03-13 (R2-05 Notification Setting Alignment)
