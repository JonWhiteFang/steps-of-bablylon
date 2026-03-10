# Plan 31 — Play Console & Store Publication

**Status:** Not Started
**Dependencies:** Plan 30 (Release Prep)
**Layer:** Store publication

---

## Objective

Upload the signed AAB to Google Play Console, configure the store listing, set up test tracks, and complete all Play Console requirements for publication.

---

## Task Breakdown

### Task 1: Play Console Setup

Configure Google Play Console:
- Create app listing
- Set up internal testing track
- Configure pricing: Free (with IAP)
- Set target countries/regions
- Complete content rating questionnaire
- Complete data safety section
- Link privacy policy URL (hosted from Plan 30)

---

### Task 2: Store Listing Upload

Upload all assets created in Plan 30:
- App icon (512×512 PNG)
- Feature graphic (1024×500 PNG)
- Screenshots (phone + tablet)
- Short description and full description
- Category: Games → Strategy
- Contact email

---

### Task 3: AAB Upload & Test Tracks

- Upload signed AAB from Plan 30 to internal testing track
- Verify AAB with `bundletool` if not already done
- Test universal APK from AAB on device
- Set up closed/open testing tracks as needed
- Configure tester groups

---

### Task 4: IAP & Ad Verification

- Configure IAP products in Play Console (Gem packs, Ad Removal, Season Pass)
- Test IAPs via licensed test accounts on internal track
- Integrate real Google Play Billing Library (replace StubBillingManager)
- Integrate real AdMob SDK (replace StubRewardAdManager)
- Verify reward ads load and grant rewards
- Verify purchase flows complete end-to-end

---

### Task 5: Pre-Launch Report

- Enable Firebase Test Lab pre-launch report
- Review automated crawl results for crashes/ANRs
- Fix any critical issues found
- Re-upload if needed

---

### Task 6: Production Release

- Promote from internal → closed → open → production track
- Monitor crash reports and vitals
- Tag final release in version control

---

## Completion Criteria

- App listed on Google Play Console with all assets
- Internal testing track functional
- IAPs configured and testable
- Pre-launch report shows no critical issues
- Ready for production rollout
