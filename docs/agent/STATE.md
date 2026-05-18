# Project State

## Current objective
- **Promote internal v4 → closed testing in Play Console.** Closed-track 14-day clock has not started yet. RO-08 + RO-09 (the two pre-closed-test bundles) are both green and pushed to `origin/main`. Next: bump `versionCode 3 → 4`, run `./run-gradle.sh bundleRelease`, re-upload to internal track for one final smoke check, then promote internal v4 → closed. Recruit ≥12 testers. Wait ≥14 calendar days. Then apply for production access.
- **Previous objective (RO-09, complete):** 3-fix bundle for pre-closed-test audit findings. **#1 CHRONO_FIELD UW** now actually slows enemies via `CHRONO_SLOW_FACTOR=0.10f` per-entity `deltaTime` scaling (was render-overlay-only, 75 PS for zero gameplay benefit). **#2 GOLDEN_ZIGGURAT × overdrive `fortuneMultiplier` stacking** — 3 symmetric edits in `GameEngine.kt` (FORTUNE activate `coerceAtLeast(3.0)`, `expireOverdrive` `if (goldenZigActive) 5.0 else 1.0`, GOLDEN expire `if (activeOverdrive == FORTUNE) 3.0 else 1.0`) close the 5.0×-leak-across-overdrive-expiry exploit. **#7 LabsScreen dead expression** — drive-by delete. Commits `fcb282e` … `fdc34d3` on `main`. Test count 565 → 572. v1.x deferred: #3 STEP_MULTIPLIER × CV unit mismatch, #4 lifetime-counter desync, #5 TOCTOU on gem/PS spend, #6 per-kill credit on `viewModelScope` — all bounded-impact, no closed-test exposure.
- **Previous objective (RO-08, complete):** 4-fix bundle for upgrade wiring (STEP_MULTIPLIER + RECOVERY_PACKAGES + ZigguratEntity stale stats + ResolveStats coverage + STEP_SURGE gem multiplier). Commits `5c2baca` … `b7b8824` on `main`. Test count 535 → 565.

## What works
- Plans 01–30 + 10b + R (R01–R12) + R2 (R2-01–R2-12) complete.
- Battle Step Rewards (ADR-0003): per-enemy flat reward, 2k/day cap, partial credit, capped-kill FloatingText suppression (A.7).
- DB version 9: 13 entities (billing_receipt added in C.5 PR 1), Room Migrations v7→8 and v8→9 registered. DB-file wipe recovery on decrypt failure (A.3).
- Phase A foundation, Phase B.1 (TimeProvider seam), Phase B.2 PRs 1–5 (RO-02 atomic transactions, 5/5 sites complete), Phase B.3 PRs 1–2 (RO-03 resilient endRound complete), Phase C.2 PRs 1+2+3+3b+3c + ensureSeedData fix (RO-07 cosmetic renderer override pipeline complete), Phase C.4 (`ClaimMilestone` UnknownCosmetic detection), Phase C.5 PRs 1+2+3 complete (real Play Billing v8 BillingManagerImpl + lifecycle wiring + reconcile hook + `StubBillingManager` deletion after on-device PASS), Phase C.6 PRs 1+2+3 (real AdMob RewardAdManagerImpl + UMP consent + `StubRewardAdManager` deletion).
- Fresh-install first-kill crash hotfix landed (2026-05-12).
- App launcher icon + Play Store 512×512 hi-res PNG + 1024×500 feature graphic + 5 phone screenshots all landed.
- Play Console: developer account verified, app `com.whitefang.stepsofbabylon` created in Draft, package registered via ADV (debug-keystore path). Listing populated end-to-end. Internal track v3 (versionCode 3) live, on-device-verified. 5 SKUs created and active.
- Real Play Billing v8 + AdMob v25 + UMP v4 wired end-to-end and verified on a real device.
- **Pre-closed-testing UX polish (PRs A + B):** Ad-failure modes surface as snackbars in Battle + Cards; Store screen displays live Play-Console prices via `ProductDetails.priceDisplay` with static-constant fallback. Walkthrough doc reflects the lessons learned during the live walk-through.
- **572 JVM tests** green (565 pre-RO-09 → 572 post-RO-09; +7 new tests for CHRONO_FIELD enemy-slow propagation and GOLDEN_ZIGGURAT × overdrive fortuneMultiplier stacking).

## Known issues / debt
- **Closed-testing prerequisite for production launch (new Google policy).** Dashboard mandates ≥12 testers opted-in, ≥14 days of closed testing, before production access can be applied for. Adds ≥14 days to launch timeline.
- Cosmetic visual application plumbed end-to-end for 4 cosmetics; 3 non-milestone ziggurat skins (zig_obsidian, zig_crystal, zig_golden) + 4 non-ziggurat seeds (proj_fire, proj_lightning, enemy_shadow, enemy_neon) still show "Coming Soon" in the Store pending their visual content.
- Sound assets are placeholder sine wave tones.
- Phase B debt remaining: B.4 FollowOnPipeline extraction + B.5 UpdateMissionProgress use case. Per ADR-0004 this is a 4-PR / ~1-week refactor with zero user-visible benefit — deferred to post-launch.
- `BuildConfig.USE_REAL_ADS` branch (release-only consent prefetch in MainActivity) is not covered by JVM tests — device-verified 2026-05-12 + 2026-05-18.
- Live-price feature has two intentional v1.x deferrals (PR B): no refresh on app resume / locale change; no retry on transient network failure. Static `BillingProduct.priceDisplay` fallback covers both for v1.
- The Play Console "no debug symbols" warning will persist on every upload (SQLCipher + androidx.graphics.path .so files ship pre-stripped). Informational, not a release blocker. Documented in walkthrough.
- **RO-09 deferred findings (v1.x patch backlog):** #3 STEP_MULTIPLIER × cross-validator unit mismatch (needs schema migration to track multiplier-bonus separately); #4 currency lifetime counter desync (display-only drift on crash); #5 TOCTOU race on gem/PS spend (lifetime drift, wallet stays correct); #6 per-kill battle-step credit on `viewModelScope` (≤1 step per pending callback lost on mid-round nav-away).

## Top priorities (next 5)
1. **Plan 31 Phase G2 — closed testing track.** Bump `versionCode 3 → 4`, `bundleRelease`, upload to internal track for one final smoke check. Then promote internal v4 → closed. Recruit ≥12 testers. Wait ≥14 calendar days while collecting feedback.
2. **Plan 31 Phase H — Pre-launch report review.** Auto-runs Firebase Test Lab on every internal-track AAB upload. Review + fix anything critical (bump versionCode → bundleRelease → re-upload to closed track).
3. **Plan 31 Phase I — production access application + rollout.** After ≥14 days closed testing with ≥12 testers, apply for production access. Google review 1–3 days. Then promote closed → production with staged rollout (5–10 % → 100 %).
4. **Tag v1.0.0 in git** post-production rollout. Update STATE + RUN_LOG. Then start v1.x patch backlog: deferred RO-09 findings #3 – #6, B.4 + B.5 refactor.
5. **(v1.x patch backlog)** RO-09 deferred findings #3–#6; B.4 FollowOnPipeline extraction + B.5 UpdateMissionProgress use case; live-price retry-on-failure.

## Next actions (explicit order)
1. **(Build + upload, immediate)** Bump `versionCode 3 → 4` in `app/build.gradle.kts`, run `./run-gradle.sh bundleRelease`, upload to internal track. On-device smoke-test (RO-09 #1 + #2 visible-effect spot-check: equip CHRONO_FIELD UW and confirm enemies actually slow; activate ASSAULT then GOLDEN_ZIGGURAT and confirm cash multiplier resets to 1.0× when GOLDEN expires).
2. **(External)** Promote internal v4 → closed testing in Play Console. Recruit ≥12 testers (Gmail addresses), distribute opt-in URL, monitor for ≥14 days.
3. **(External)** Review Pre-launch report on the v4 internal-track AAB. Address any critical findings.
4. **(External)** After ≥14 days closed testing, apply for production access. Google review 1–3 days.
5. **(External)** Promote closed → production with staged rollout. Tag v1.0.0 in git.
6. **(v1.x patch backlog)** Deferred RO-09 findings #3–#6 (cross-validator unit fix, currency lifetime atomicity, TOCTOU spend race, per-kill credit on applicationScope); B.4/B.5 refactor; live-price retry-on-failure.

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests. `BillingProduct.skuId()` is now a public method; treat as a stable public API.
- `domain/usecase/` — all 32 use cases stable.
- Balance constants — validated by 39 regression tests.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `app/proguard-rules.pro` — hardened R8 rules.
- `app/build.gradle.kts` — signing config, AdMob production-ID wiring (don't break the test-ID fallback path), `ndk { debugSymbolLevel = "FULL" }`.
- `Screen.items by lazy` + `argumentFreeRoutes by lazy` — both guard against sealed-class init-order NPE (commit 1872af9).
- `release/` directory — all gitignored. `release/upload-keystore.jks` is irreplaceable; losing it before Play App Signing enrollment = no app updates ever (already enrolled, so this is now mostly historical).
- **Live-price wiring (PR B)** — stable. Don't add price refresh on app resume / locale change without re-deriving the cache invalidation rules; the current "fetch once on Store entry" is intentionally simple for v1.
- **GOLDEN_ZIGGURAT × overdrive `fortuneMultiplier` stacking (RO-09 #2)** — the 3-site invariant ("higher buff wins; lower restores cleanly when one ends") is regression-guarded by 4 GameEngineTest entries. Don't add a fifth fortune source without extending those tests.

## References
- ADR-0003 (Battle Step Rewards): docs/agent/DECISIONS/ADR-0003-battle-step-rewards.md
- ADR-0004 (FollowOnPipeline, Proposed — deferred to post-launch): docs/agent/DECISIONS/ADR-0004-follow-on-pipeline.md
- ADR-0005 (Billing SDK, Accepted; decision #6 refined to lowercase wire format 2026-05-14): docs/agent/DECISIONS/ADR-0005-billing-sdk.md
- ADR-0006 (Ad SDK, Accepted): docs/agent/DECISIONS/ADR-0006-ad-sdk.md
- ADR-0007 (ADV via debug keystore, Accepted): docs/agent/DECISIONS/ADR-0007-adv-debug-keystore.md
- Plan 31 walk-through (revised 2026-05-18): docs/release/plan-31-walkthrough.md
- Privacy policy (canonical, in repo): docs/release/privacy-policy.md
- Privacy policy (hosted, GitHub Pages): docs/index.md → https://jonwhitefang.github.io/steps-of-bablylon/
- Delete-data URL: https://jonwhitefang.github.io/steps-of-bablylon/#delete-data
- Play Store listing copy: docs/release/play-store-listing.md
- Master plan: docs/plans/master-plan.md
- Plan RO-09 (complete, pre-closed-test fix bundle): docs/plans/plan-RO-09-pre-closed-test-fixes.md
- Critical path: 01→…30→R→R2→ Battle Step Rewards → Phase A done → B.1 done → B.2 done (RO-02 complete) → B.3 done (RO-03 complete) → B.4–B.5 (deferred post-launch) → C.2 PRs done → C.4 done → C.5 PRs 1+2+3 done → C.6 PRs 1+2+3 done → battle-step-credit hotfix done → RO-08 done (4-fix upgrade-wiring bundle) → RO-09 done (chrono fix + fortune stacking + drive-by) → Plan 31 (Phases A–G done; smoke test PASSED 2026-05-18) → **Phase G2 closed track active** (≥14 days, ≥12 testers) → Phases H+I production → D
- Last run: 2026-05-18 evening (RO-09 fix bundle landed; commits `fcb282e` … `fdc34d3`; 565 → 572 tests; bundleRelease green; ready to bump versionCode and re-upload to internal track).
