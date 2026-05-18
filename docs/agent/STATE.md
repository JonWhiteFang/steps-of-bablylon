# Project State

## Current objective
- **RO-09 \u2014 pre-closed-test audit findings (2026-05-18 evening, post-RO-08).** Self-audit found 7 issues, plan documented at `docs/plans/plan-RO-09-pre-closed-test-fixes.md`. Three to fix before closed test, four deferred to v1.x:
  - **#1 CRITICAL \u2014 CHRONO_FIELD UW does nothing functional.** Description says "Slows all enemies to 10 % speed for duration" but the only consumer of `chronoActive` is the rendering overlay; `EnemyEntity.update` reads raw `speed` and `GameEngine.update` passes raw `deltaTime`. 75 Power-Stone unlock → purple tint, zero gameplay benefit. Fix shape: scale `deltaTime` for `EnemyEntity` instances when `chronoActive` true.
  - **#2 MODERATE \u2014 GOLDEN_ZIGGURAT \u00d7 overdrive `fortuneMultiplier` leak.** Single shared field used by FORTUNE (3.0\u00d7) and GOLDEN_ZIGGURAT (5.0\u00d7). GOLDEN expire path's `if (activeOverdrive == null) fortune = 1.0` only resets when no overdrive active; if ASSAULT/FORTRESS/SURGE is active, the 5.0\u00d7 multiplier leaks for the remainder of that overdrive (up to ~50 s 5\u00d7 cash exploit). Fix: `fortuneMultiplier = if (FORTUNE active) 3.0 else 1.0` symmetrical fix in 3 sites.
  - **#7 COSMETIC \u2014 dead `total` expression in `LabsScreen.kt:106`.** Algebraically `2 \u00d7 info.remainingMs`, never read. Drive-by delete.
  - **Deferred to v1.x:** #3 STEP_MULTIPLIER \u00d7 cross-validator unit mismatch, #4 currency lifetime counter desync, #5 TOCTOU race on gem/PS spend, #6 per-kill battle-step credit on `viewModelScope`. All bounded-impact, no closed-test exposure.
- **Tests target post-RO-09:** 565 \u2192 ~572 (+7: 3 chrono, 4 fortune-stacking).
- **Acceptance:** all three fix-before-CT findings landed; tests green; bundleRelease green; versionCode 3 \u2192 4; re-upload internal track; promote internal v4 \u2192 closed.
- **Previous objective (RO-08, complete):** 4-fix bundle for upgrade wiring (STEP_MULTIPLIER + RECOVERY_PACKAGES + ZigguratEntity stale stats + ResolveStats coverage + STEP_SURGE gem multiplier). Commits `5c2baca` \u2026 `b7b8824` on `main`. Test count 535 \u2192 565.
- **Next external step:** Promote internal v4 (after RO-09 lands) \u2192 closed testing in Play Console. Recruit \u226512 testers. Wait \u226514 calendar days. Then apply for production access.

## What works
- Plans 01\u201330 + 10b + R (R01\u2013R12) + R2 (R2-01\u2013R2-12) complete.
- Battle Step Rewards (ADR-0003): per-enemy flat reward, 2k/day cap, partial credit, capped-kill FloatingText suppression (A.7).
- DB version 9: 13 entities (billing_receipt added in C.5 PR 1), Room Migrations v7\u21928 and v8\u21929 registered. DB-file wipe recovery on decrypt failure (A.3).
- Phase A foundation, Phase B.1 (TimeProvider seam), Phase B.2 PRs 1\u20135 (RO-02 atomic transactions, 5/5 sites complete), Phase B.3 PRs 1\u20132 (RO-03 resilient endRound complete), Phase C.2 PRs 1+2+3+3b+3c + ensureSeedData fix (RO-07 cosmetic renderer override pipeline complete), Phase C.4 (`ClaimMilestone` UnknownCosmetic detection), Phase C.5 PRs 1+2+3 complete (real Play Billing v8 BillingManagerImpl + lifecycle wiring + reconcile hook + `StubBillingManager` deletion after on-device PASS), Phase C.6 PRs 1+2+3 (real AdMob RewardAdManagerImpl + UMP consent + `StubRewardAdManager` deletion).
- Fresh-install first-kill crash hotfix landed (2026-05-12).
- App launcher icon + Play Store 512\u00d7512 hi-res PNG + 1024\u00d7500 feature graphic + 5 phone screenshots all landed.
- Play Console: developer account verified, app `com.whitefang.stepsofbabylon` created in Draft, package registered via ADV (debug-keystore path). Listing populated end-to-end. Internal track v3 (versionCode 3) live, on-device-verified. 5 SKUs created and active.
- Real Play Billing v8 + AdMob v25 + UMP v4 wired end-to-end and verified on a real device.
- **Pre-closed-testing UX polish (PRs A + B):** Ad-failure modes surface as snackbars in Battle + Cards; Store screen displays live Play-Console prices via `ProductDetails.priceDisplay` with static-constant fallback. Walkthrough doc reflects the lessons learned during the live walk-through.
- **565 JVM tests** green (535 pre-RO-08 \u2192 565 post-RO-08; +30 new tests for upgrade wiring, ResolveStats coverage, GameEngine overdrive/recovery, STEP_SURGE gem multiplier, STEP_MULTIPLIER walking credit).

## Known issues / debt
- **Closed-testing prerequisite for production launch (new Google policy).** Dashboard mandates \u226512 testers opted-in, \u226514 days of closed testing, before production access can be applied for. Adds \u226514 days to launch timeline.
- Cosmetic visual application plumbed end-to-end for 4 cosmetics; 3 non-milestone ziggurat skins (zig_obsidian, zig_crystal, zig_golden) + 4 non-ziggurat seeds (proj_fire, proj_lightning, enemy_shadow, enemy_neon) still show "Coming Soon" in the Store pending their visual content.
- Sound assets are placeholder sine wave tones.
- Phase B debt remaining: B.4 FollowOnPipeline extraction + B.5 UpdateMissionProgress use case. Per ADR-0004 this is a 4-PR / ~1-week refactor with zero user-visible benefit \u2014 deferred to post-launch.
- `BuildConfig.USE_REAL_ADS` branch (release-only consent prefetch in MainActivity) is not covered by JVM tests \u2014 device-verified 2026-05-12 + 2026-05-18.
- Live-price feature has two intentional v1.x deferrals (PR B): no refresh on app resume / locale change; no retry on transient network failure. Static `BillingProduct.priceDisplay` fallback covers both for v1.
- The Play Console "no debug symbols" warning will persist on every upload (SQLCipher + androidx.graphics.path .so files ship pre-stripped). Informational, not a release blocker. Documented in walkthrough.

## Top priorities (next 5)
1. **Implement RO-09 fix bundle (#1 + #2 + #7)** per `docs/plans/plan-RO-09-pre-closed-test-fixes.md`. Single PR, 4 commits (chrono fix, fortune-stacking fix, dead-code cleanup, doc sync). Bump versionCode 3 \u2192 4. ~1\u20132 hour task.
2. **Plan 31 Phase G2 \u2014 closed testing track.** After RO-09 lands and v4 is uploaded to internal, promote internal v4 \u2192 closed. Recruit \u226512 testers. Wait \u226514 calendar days while collecting feedback.
3. **Plan 31 Phase H \u2014 Pre-launch report review.** Auto-runs Firebase Test Lab on every internal-track AAB upload. Review + fix anything critical (bump versionCode \u2192 bundleRelease \u2192 re-upload to closed track).
4. **Plan 31 Phase I \u2014 production access application + rollout.** After \u226514 days closed testing with \u226512 testers, apply for production access. Google review 1\u20133 days. Then promote closed \u2192 production with staged rollout (5\u201310 % \u2192 100 %).
5. **Tag v1.0.0 in git** post-production rollout. Update STATE + RUN_LOG. Then start v1.x patch backlog: deferred RO-09 findings #3 \u2013 #6, B.4 + B.5 refactor.

## Next actions (explicit order)
1. **(Implement, immediate)** RO-09 fix bundle per `docs/plans/plan-RO-09-pre-closed-test-fixes.md`. Single PR with 4 commits. Tests 565 \u2192 ~572.
2. **(Build + upload)** Bump `versionCode 3 \u2192 4`, run `./run-gradle.sh bundleRelease`, upload to internal track. On-device smoke-test.
3. **(External)** Promote internal v4 \u2192 closed testing in Play Console. Recruit \u226512 testers (Gmail addresses), distribute opt-in URL, monitor for \u226514 days.
4. **(External)** Review Pre-launch report on the v4 internal-track AAB. Address any critical findings.
5. **(External)** After \u226514 days closed testing, apply for production access. Google review 1\u20133 days.
6. **(External)** Promote closed \u2192 production with staged rollout. Tag v1.0.0 in git.
7. **(v1.x patch backlog)** Deferred RO-09 findings #3\u2013#6 (cross-validator unit fix, currency lifetime atomicity, TOCTOU spend race, per-kill credit on applicationScope); B.4/B.5 refactor; live-price retry-on-failure.

## Do-not-touch / fragile zones
- `domain/model/` \u2014 stable, all constants validated by balance tests. `BillingProduct.skuId()` is now a public method; treat as a stable public API.
- `domain/usecase/` \u2014 all 32 use cases stable.
- Balance constants \u2014 validated by 39 regression tests.
- `presentation/battle/effects/` \u2014 particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` \u2014 single source for all dependency versions.
- `app/proguard-rules.pro` \u2014 hardened R8 rules.
- `app/build.gradle.kts` \u2014 signing config, AdMob production-ID wiring (don't break the test-ID fallback path), `ndk { debugSymbolLevel = "FULL" }`.
- `Screen.items by lazy` + `argumentFreeRoutes by lazy` \u2014 both guard against sealed-class init-order NPE (commit 1872af9).
- `release/` directory \u2014 all gitignored. `release/upload-keystore.jks` is irreplaceable; losing it before Play App Signing enrollment = no app updates ever (already enrolled, so this is now mostly historical).
- **Live-price wiring (PR B)** \u2014 stable. Don't add price refresh on app resume / locale change without re-deriving the cache invalidation rules; the current "fetch once on Store entry" is intentionally simple for v1.

## References
- ADR-0003 (Battle Step Rewards): docs/agent/DECISIONS/ADR-0003-battle-step-rewards.md
- ADR-0004 (FollowOnPipeline, Proposed \u2014 deferred to post-launch): docs/agent/DECISIONS/ADR-0004-follow-on-pipeline.md
- ADR-0005 (Billing SDK, Accepted; decision #6 refined to lowercase wire format 2026-05-14): docs/agent/DECISIONS/ADR-0005-billing-sdk.md
- ADR-0006 (Ad SDK, Accepted): docs/agent/DECISIONS/ADR-0006-ad-sdk.md
- ADR-0007 (ADV via debug keystore, Accepted): docs/agent/DECISIONS/ADR-0007-adv-debug-keystore.md
- Plan 31 walk-through (revised 2026-05-18): docs/release/plan-31-walkthrough.md
- Privacy policy (canonical, in repo): docs/release/privacy-policy.md
- Privacy policy (hosted, GitHub Pages): docs/index.md \u2192 https://jonwhitefang.github.io/steps-of-bablylon/
- Delete-data URL: https://jonwhitefang.github.io/steps-of-bablylon/#delete-data
- Play Store listing copy: docs/release/play-store-listing.md
- Master plan: docs/plans/master-plan.md
- **Plan RO-09 (active, pre-closed-test fix bundle): docs/plans/plan-RO-09-pre-closed-test-fixes.md**
- Critical path: 01\u2192\u202630\u2192R\u2192R2\u2192 Battle Step Rewards \u2192 Phase A done \u2192 B.1 done \u2192 B.2 done (RO-02 complete) \u2192 B.3 done (RO-03 complete) \u2192 B.4\u2013B.5 (deferred post-launch) \u2192 C.2 PRs done \u2192 C.4 done \u2192 C.5 PRs 1+2+3 done \u2192 C.6 PRs 1+2+3 done \u2192 battle-step-credit hotfix done \u2192 RO-08 done (4-fix upgrade-wiring bundle) \u2192 **RO-09 active** (pre-closed-test self-audit bundle: chrono, fortune stacking, drive-by) \u2192 Plan 31 (Phases A\u2013G done; smoke test PASSED 2026-05-18) \u2192 Phase G2 closed track (\u226514 days, \u226512 testers) \u2192 Phases H+I production \u2192 D
- Last run: 2026-05-18 evening (RO-09 plan documented after deep-scan audit; 7 findings catalogued, 3 selected for fix-before-CT, 4 deferred to v1.x; ready to implement RO-09 fix bundle next session).
