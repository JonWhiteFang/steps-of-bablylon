# Project State

## Current objective
- **Plan 31 Phase G in progress.** v2 AAB uploaded to Play Console Internal testing 2026-05-15. SKUs created (5 lowercase IDs: `gem_pack_small`, `gem_pack_medium`, `gem_pack_large`, `ad_removal`, `season_pass`) and license testers added. `ad_removal` priced at $3.99 (matches the in-app `BillingProduct.AD_REMOVAL.priceDisplay` constant; user briefly tried $9.99 then reverted in Play Console). Native-debug-symbols `ndk { debugSymbolLevel = "FULL" }` added to `app/build.gradle.kts` after the v2 upload triggered the standard "no debug symbols" warning — turns out warning is unfixable from our side because SQLCipher + androidx.graphics.path .so files ship pre-stripped. Config kept as good hygiene + future-proofing. Warning is informational, not a release blocker; v2 is fine to roll out.
- **versionCode now at 3 in the build script** (forward-only) for the next legitimate upload. v2 is the AAB sitting in the Play Console internal-track draft pending review-and-rollout.
- **Next external step:** Click "Review and roll out release" on the v2 internal-track draft → wait for Google quick review (5–30 min) → grab the opt-in URL → install on a test device → run the smoke checklist (Gem packs credit 50/300/700, Ad Removal sets the flag, Season Pass sets 30-day expiry + +10 Gems/day, reward ad plays).

## What works
- Plans 01–30 + 10b + R (R01–R12) + R2 (R2-01–R2-12) complete.
- Battle Step Rewards (ADR-0003): per-enemy flat reward, 2k/day cap, partial credit, capped-kill FloatingText suppression (A.7).
- DB version 9: 13 entities (billing_receipt added in C.5 PR 1), Room Migrations v7→8 and v8→9 registered. DB-file wipe recovery on decrypt failure (A.3).
- Phase A foundation, Phase B.1 (TimeProvider seam), Phase B.2 PRs 1–5 (RO-02 atomic transactions, 5/5 sites complete), Phase B.3 PRs 1–2 (RO-03 resilient endRound complete), Phase C.2 PRs 1+2+3+3b+3c + ensureSeedData fix (RO-07 cosmetic renderer override pipeline complete), Phase C.4 (`ClaimMilestone` UnknownCosmetic detection), Phase C.5 PRs 1+2 (real Play Billing v8 BillingManagerImpl + flag-gated Hilt binding + lifecycle wiring + reconcile hook), Phase C.6 PRs 1+2+3 (real AdMob RewardAdManagerImpl + UMP consent + flag-gated Hilt binding + MainActivity consent prefetch + StubRewardAdManager deletion).
- Fresh-install first-kill crash hotfix landed (2026-05-12) — `DailyStepDao.incrementBattleSteps` UPSERT INSERT half now supplies all 9 NOT NULL columns.
- App launcher icon (vector adaptive icon) shipped 2026-05-12; Play Store 512×512 hi-res PNG + 1024×500 feature graphic rendered + landed 2026-05-13.
- Play Console: developer account verified, app `com.whitefang.stepsofbabylon` created in Draft state with package-name registered via Android Developer Verification (debug keystore path). Listing populated end-to-end except for SKUs and AAB rollout.
- Production upload keystore generated, signing config wired, signed AAB builds successfully with BILLING permission and AdMob production IDs.
- **Lowercase SKU wire format** — `BillingProduct.skuId()` returns `name.lowercase()` end-to-end; `fromSkuIdOrNull` matches via `it.skuId()`. Play Console SKU creation in Phase G is now unblocked.
- **527 JVM tests** green (no test count change this session — only fixture string updates).

## Known issues / debt
- **Closed-testing prerequisite for production launch (new Google policy).** Dashboard mandates ≥12 testers opted-in, ≥14 days of closed testing, before production access can be applied for. Adds ~14 days to launch timeline. Plan 31 walk-through doc didn't anticipate this.
- **Pre-existing UX gap (now slightly more visible in debug post-C.6 PR 3):** `CardsViewModel.watchFreePackAd`, `BattleViewModel.watchGemAd`, `BattleViewModel.watchPsAd` all silently swallow `AdResult.Error` and `AdResult.Cancelled`. Worth a snackbar plumbing pass before public launch — mirror the `userMessage: StateFlow<String?>` pattern from `MissionsViewModel`. Affects 3 call sites. Not a release-blocker.
- `StubBillingManager` still ships in release builds even though the `@Binds` swap in C.5 PR 2 moved the release binding to `BillingManagerImpl`. Deletion is the C.5 PR 3 gate, which is itself gated on internal-track on-device verification of a real Play Billing test purchase (which itself requires SKUs created in Play Console — i.e. unblocked by the lowercase fix landing this session, but still gated on the external Phase G work).
- Cosmetic visual application plumbed end-to-end for 4 cosmetics; 3 non-milestone ziggurat skins (zig_obsidian, zig_crystal, zig_golden) + 4 non-ziggurat seeds (proj_fire, proj_lightning, enemy_shadow, enemy_neon) still show "Coming Soon" in the Store pending their visual content.
- Sound assets are placeholder sine wave tones.
- Phase B debt remaining: B.4 FollowOnPipeline extraction + B.5 UpdateMissionProgress use case. Not blockers.
- `BuildConfig.USE_REAL_BILLING` + `USE_REAL_ADS` branches still not covered by JVM tests. C.5 PR 2 covered via `BillingManagerParityTest`; C.6 PR 2 device-verified 2026-05-12. Post-C.6 PR 3 the `USE_REAL_ADS` Provider-switch branch is gone.
- Plan 31 walk-through doc (`docs/release/plan-31-walkthrough.md`) pre-dates Android Developer Verification + the closed-testing-before-production policy. Worth a docs revision pass once Plan 31 lands cleanly.

## Top priorities (next 5)
1. **Plan 31 Phase G — internal track AAB upload + 5 SKU creation in Play Console.** AAB at `app/build/outputs/bundle/release/app-release.aab` was rebuilt this session with the lowercase wire format; just upload to Internal Testing → save release. Then create 5 SKUs (`gem_pack_small`, `gem_pack_medium`, `gem_pack_large`, `ad_removal`, `season_pass`). Add license testers (Gmail addresses), roll out internal release.
2. **Internal-track on-device verification.** Install via the internal-track opt-in URL on a real device. Smoke test: launcher icon, step counting, battle, real Play Billing test purchase credits the wallet (the gate for C.5 PR 3), reward ads, screenshots if any need re-capture. Unblocks both C.5 PR 3 and Phase H/I.
3. **C.5 PR 3** — delete `StubBillingManager` + collapse `BillingModule` Provider-switch to `@Binds BillingManagerImpl`. Mechanically identical to C.6 PR 3.
4. **Plan 31 Phase G2 — closed testing track.** Recruit ≥12 testers, run closed track for ≥14 days. Required before Phase I (production rollout). Can be partially set up while internal verification is happening.
5. **Plan 31 Phases H+I — apply for production access; promote closed → production.** Google review 1–7 days. Final external step before public launch.

## Next actions (explicit order)
1. **(Commit, immediate)** Commit `feat(billing): lowercase SKU wire format` on `main`. 7 files changed (3 prod + 4 tests + CHANGELOG/AGENTS/source-files/structure docs).
2. **(External)** Upload `app/build/outputs/bundle/release/app-release.aab` to Play Console → Test and release → Internal testing → Create new release. Save (don't roll out yet). Wait for AAB processing (1–3 min).
3. **(External)** Create 5 SKUs in Play Console → Monetize with Play → Products → In-app products + Subscriptions, with lowercase IDs matching the new wire format (`gem_pack_small`, `gem_pack_medium`, `gem_pack_large`, `ad_removal`, `season_pass`). Activate each.
4. **(External)** Add license testers to internal track. Roll out the release. Install on a real device via the opt-in URL.
5. **(Verification)** Real Play Billing test purchase end-to-end. If wallet credits correctly: land C.5 PR 3 (mechanical deletion).
6. **(External)** Promote internal → closed track. Recruit ≥12 testers. Wait ≥14 days.
7. **(External)** Apply for production access. Promote closed → production. Google review 1–7 days.
8. **(Optional, opportunistic)** Ad-error UX snackbar fix (3 call sites, mirror `MissionsViewModel` pattern). Add release upload keystore as additional ADV key. B.4/B.5 debt cleanup.

## Do-not-touch / fragile zones
- `domain/model/` — stable, all constants validated by balance tests. `BillingProduct.skuId()` is now a public method; treat as a stable public API for any reverse-lookup helpers.
- `domain/usecase/` — all 32 use cases stable.
- Balance constants in UpgradeType, TierConfig, EnemyScaler, EnemyType — validated by 39 regression tests.
- `presentation/battle/effects/` — particle pool, effect engine, all visual effects.
- `gradle/libs.versions.toml` — single source for all dependency versions.
- `app/proguard-rules.pro` — hardened R8 rules.
- `app/build.gradle.kts` — signing config, version 1.0.0, AdMob production-ID wiring (don't break the test-ID fallback path).
- `Screen.items by lazy` + `argumentFreeRoutes by lazy` — both guard against sealed-class init-order NPE (commit 1872af9).
- `release/` directory contents — all gitignored, all locally-significant. Don't delete or move without backing up first. `release/upload-keystore.jks` is irreplaceable; losing it before Play App Signing enrollment = no app updates ever.

## References
- ADR-0003 (Battle Step Rewards): docs/agent/DECISIONS/ADR-0003-battle-step-rewards.md
- ADR-0005 (Billing SDK, Accepted; decision #6 refined to lowercase wire format 2026-05-14): docs/agent/DECISIONS/ADR-0005-billing-sdk.md
- ADR-0006 (Ad SDK, Accepted): docs/agent/DECISIONS/ADR-0006-ad-sdk.md
- ADR-0007 (ADV via debug keystore, Accepted): docs/agent/DECISIONS/ADR-0007-adv-debug-keystore.md
- Plan 31 walk-through: docs/release/plan-31-walkthrough.md
- Privacy policy (canonical, in repo): docs/release/privacy-policy.md
- Privacy policy (hosted, GitHub Pages): docs/index.md → https://jonwhitefang.github.io/steps-of-bablylon/
- Delete-data URL (referenced in Play Console data-safety form): https://jonwhitefang.github.io/steps-of-bablylon/#delete-data
- Play Store listing copy: docs/release/play-store-listing.md
- Master plan: docs/plans/master-plan.md
- Critical path: 01→…→30→R→R2→ Battle Step Rewards → Phase A done → B.1 done → B.2 done (RO-02 complete) → B.3 done (RO-03 complete) → B.4–B.5 → C.2 PRs done → C.4 done → C.5 PRs 1+2 done → C.6 PRs 1+2+3 done → battle-step-credit hotfix done → Plan 31 (Phases A–F + ADV done; Phase F unblocker `feat(billing): lowercase SKU wire format` lands 2026-05-14) → Phase G internal track + SKUs → C.5 PR 3 → Phase G2 closed track → Phases H+I production → D
- Last run: 2026-05-15 (Phase G upload session — v2 AAB uploaded to internal track, 5 lowercase SKUs created, license testers added; `ndk { debugSymbolLevel = "FULL" }` config added but Play Console symbol warning is unfixable because dependencies are pre-stripped — informational warning only, not a blocker; versionCode 2 → 3 in build script; next is roll-out + on-device smoke test).
