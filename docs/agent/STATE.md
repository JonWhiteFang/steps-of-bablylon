# Project State

## Current objective
- Battle Step Rewards feature complete — kills grant Steps with a 2k/day cap (ADR-0003). DB schema v8.
- Plan 31: Play Console & Store Publication — unblocked, ready to start.

## What works
- Plans 01–30 + 10b: All foundation layers, battle system, full round lifecycle, tier/biome progression, all progression systems, notifications & widget, anti-cheat, monetization (stub), polish & VFX, balancing, testing, release prep complete.
- Plan R (Remediation): All 12 sub-plans (R01–R12) complete.
- Plan R2 (Remediation 2): All 12 sub-plans (R2-01–R2-12) complete.
- Battle Step Rewards (ADR-0003): enemy kills grant flat per-type Steps (BASIC/FAST/SCATTER=1, RANGED=2, TANK=3, BOSS=10), capped at 2,000/day via `AwardBattleSteps` against `DailyStepRecordEntity.battleStepsEarned`. Wired through `GameEngine.onStepReward` callback, surfaced as HUD counter + green '+N Step' FloatingText + Round End line item.
- DB version 8: 12 entities, first explicit Room Migration (v7→v8) registered. **412 JVM tests**, all green. Release APK builds (26MB).

## Known issues / debt
- Billing/ads use stub implementations — real SDK integration deferred to Plan 31.
- Cosmetic visual application not implemented (purchases disabled via R2-11 guard).
- Sound assets are placeholder sine wave tones.
- No app icon resources.

## Top priorities (next 5)
1. Plan 31: Play Console & Store Publication
2. Real billing SDK integration (Google Play Billing Library)
3. Real ad SDK integration (AdMob)
4. App icon and store listing assets
5. Accessibility (Plan 24, deferred)

## Next actions (explicit order)
1. Begin Plan 31: Play Console & Store Publication
2. Set up Google Play Console, create app listing
3. Integrate real billing SDK (replace StubBillingManager)
4. Integrate real ad SDK (replace StubRewardAdManager)
5. Upload AAB to internal test track

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests.
- `domain/usecase/` — all 32 use cases stable.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `app/proguard-rules.pro` — hardened R8 rules.
- `app/build.gradle.kts` — signing config, version 1.0.0.

## References
- ADR-0003 (Battle Step Rewards): docs/agent/DECISIONS/ADR-0003-battle-step-rewards.md
- Remediation plan (1st review): docs/plans/plan-R-remediation.md
- Remediation plan (2nd review): docs/plans/plan-R2-remediation.md
- External review (1st): docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX.md
- External review (2nd): docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX_2.md
- Master plan: docs/plans/master-plan.md
- Balance report: docs/balance/balance-report.md
- Release docs: docs/release/
- Code archaeology (Phase 1, user overview): devdocs/archaeology/small_summary.md
- Code archaeology (Phase 2, architecture + deployment): devdocs/archaeology/intro2codebase.md, intro2deployment.md
- Code archaeology (Phase 3, per-boundary traces): devdocs/archaeology/traces/ (13 traces + README)
- Code archaeology (Phase 4, 5-things improvement list): devdocs/archaeology/5_things_or_not.md
- Code archaeology (Phase 5, concept inventory): devdocs/archaeology/concepts/ (technical, design, business, missing)
- Evolution (Phase 14, Part 1): devdocs/evolution/refactoring_opportunities.md — top-10 highest-ROI refactors (RO-01..RO-10) with current pattern, proposed abstraction, benefits, effort, risk+mitigation, ROI, first safe step, verification, rollback, non-goals; plus a deferred-refactors appendix and cross-reference table to Phases 4/8/10/11/12/13
- Evolution (Phase 14, Part 2): devdocs/evolution/implementation_roadmap.md — phased plan (A Foundation, B Core Refactoring, C Gap Filling, D Integration & Polish); each item has files / dependencies / success criteria / risk / verification / PR size / rollback / owner role; includes aggregate critical path, mermaid dep graph, doc-update table, Non-goals list, memory-update checklist, and source-phase cross-reference table
- Code archaeology (Phase 6, foundations): devdocs/archaeology/foundations/ (project_description, philosophy, known_requirements)
- Doc-inferred foundations (Phase 7): devdocs/foundations/ (project_description, philosophy, known_requirements) — built from docs only, pairs with Phase 6
- Code archaeology (Phase 8, reconstruction): devdocs/archaeology/architecture_analysis.md + module_discovery.md — architectural critique + module-boundary analysis from code
- Code archaeology (Phase 9, concept mappings): devdocs/archaeology/concept_mappings.md — 25-concept map with coverage %, divergence rationale, alternatives, edge cases, tests/config pointers, risks; plus cross-concept risk appendix + coverage roll-up
- Evolution (Phase 10, gap analysis): devdocs/evolution/gap_analysis.md — compares current state to desired state; separates known/inferred gaps, marks release blockers vs incremental improvements; argues no rewrite needed; names cosmetic rendering pipeline as the one structural refactor blocking a shipped-but-disabled feature
- Evolution (Phase 11, gap closure plan): devdocs/evolution/gap_closure_plan.md — phased execution plan (Q1–Q8 quick wins, I1–I7 incremental subsystem work, M1–M4 + MR1 major refactor, §4 rewrites rejected with revisit triggers, §5 explicit non-goals, §6 critical path)
- Smoke tests (Phase 12, baseline): smoke_tests/check_what_is_working/ — README (strategy/commands/prerequisites), test_plan.md (5 areas × 5 cases mapped to existing tests), report.md (live run results: 412 tests pass, lint clean, APK builds; 6 JUnit4 Robolectric tests silently not discovered because no junit-vintage-engine dependency — flagged as "broken but acceptable" with one-line fix path)
- Codebase cleanup inventory (Phase 13): devdocs/archaeology/cleanup_inventory.md — identifies removal/consolidation/quarantine candidates (3 High-conf Low-risk pure removals: PlaceholderScreen, UltimateWeaponLoadout class+test; 4 consolidations: escrow methods, StepCrossValidator branches, 6 SharedPreferences wrappers, GameEngine stat snapshots; 4 quarantines pending product decision: STEP_BURST, STEP_MULTIPLIER+RECOVERY_PACKAGES, MilestoneReward.Cosmetic no-op+ID mismatch, cosmetic purchase UI); Dynamic-risk register §F pins classes invisible to grep (manifest, Hilt/Room/WorkManager codegen, enum-as-string Room columns, notification deep-links). No file modified this phase.
- Critical path: 01→…→30→R→R2→ Battle Step Rewards →31
- Last run: 2026-05-06 (Standard Analysis Phase 14 — wrote devdocs/evolution/refactoring_opportunities.md (Part 1, ~1296 lines) + devdocs/evolution/implementation_roadmap.md (Part 2, ~1319 lines); no build/test runs; no code changes; synthesises Phases 4/8/10/11/12/13 into top-10 ROI-ranked refactors and a release-gated Phase A/B/C/D roadmap; Phase B is optional for v1.0, release-critical subset is A.4 + C.2 PR1-2 + C.5 + C.6 + D)
