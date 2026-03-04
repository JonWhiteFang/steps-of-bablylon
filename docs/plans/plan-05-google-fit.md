# Plan 05 — Health Connect Integration (formerly Google Fit)

**Status:** Complete
**Dependencies:** Plan 04 (Step Counter Service)
**Layer:** `data/` — Google Fit data source

---

## Objective

Integrate Google Fit SDK for step cross-validation, gap-filling when the foreground service is killed, and Activity Minute Parity (crediting indoor workouts as step-equivalents). This plan adds the secondary data source that strengthens step tracking reliability and inclusivity.

Reference: `docs/step-tracking.md` §Activity Minute Parity and §Anti-Cheat.

---

## Task Breakdown

### Task 1: Google Fit Client Setup

Create `data/googlefit/GoogleFitClient.kt`:
- Wraps Google Fit `HistoryClient` and `SessionsClient`
- Handles OAuth sign-in flow for `FITNESS_ACTIVITY_READ` scope
- Provides connection state as `StateFlow<Boolean>`
- Graceful fallback if user declines or Google Play Services unavailable

---

### Task 2: Google Fit Step Reader

Create `data/googlefit/GoogleFitStepReader.kt`:
- Queries Google Fit for step count over a date range
- Returns daily step totals for cross-validation
- Called by `StepSyncWorker` during periodic sync

---

### Task 3: Cross-Validation & Escrow

Create `data/googlefit/StepCrossValidator.kt`:
- Compares local sensor steps vs Google Fit steps for the current day
- If discrepancy >20%: move excess steps to escrow (not spendable)
- Escrow tracked in `DailyStepRecord` (add `escrowSteps: Long` column)
- On next sync: re-check. If resolved within 3 syncs → release escrow to balance
- If still discrepant after 3 syncs → discard escrow

---

### Task 4: Gap-Filling

Create `data/googlefit/StepGapFiller.kt`:
- When foreground service was killed and missed steps, query Google Fit for the gap period
- Credit missed steps (after rate limiting and ceiling checks)
- Called during WorkManager sync when local delta seems too low

---

### Task 5: Activity Minute Reader

Create `data/googlefit/ActivityMinuteReader.kt`:
- Queries Google Fit for Active Minutes by activity type
- Maps activity types to step-equivalent conversions:
  - Stationary cycling: 1 min = 100 Step-eq (cap 10,000/day)
  - Rowing: 1 min = 100 Step-eq (cap 10,000/day)
  - Swimming: 1 min = 120 Step-eq (cap 12,000/day)
  - Wheelchair propulsion: 1 min = 110 Step-eq (cap 11,000/day)
  - Yoga/Stretching: 1 min = 50 Step-eq (cap 5,000/day)

---

### Task 6: Double-Counting Prevention

Create `data/googlefit/ActivityMinuteValidator.kt`:
- Step-equivalents only credited when step sensor records <50 steps/min during that period
- Cross-references sensor step rate with activity minute timestamps
- Prevents counting a walk as both steps AND active minutes

---

### Task 7: Activity Minute Integration

Update `DailyStepManager` (from Plan 04):
- After processing sensor steps, also process activity minute step-equivalents
- Update `DailyStepRecord.activityMinutes` and `DailyStepRecord.stepEquivalents`
- Add step-equivalents to `PlayerProfile.currentStepBalance`
- Enforce per-activity daily caps

---

### Task 8: Google Fit Hilt Module

Create `di/GoogleFitModule.kt`:
- Provides `GoogleFitClient`, `GoogleFitStepReader`, `ActivityMinuteReader`
- Scoped as `@Singleton`

---

### Task 9: Database Migration

Update `AppDatabase`:
- Add `escrowSteps` column to `DailyStepRecordEntity`
- Migration v2 → v3

---

## File Summary

```
data/googlefit/
├── GoogleFitClient.kt          (new)
├── GoogleFitStepReader.kt      (new)
├── StepCrossValidator.kt       (new)
├── StepGapFiller.kt            (new)
├── ActivityMinuteReader.kt     (new)
└── ActivityMinuteValidator.kt  (new)

data/sensor/
└── DailyStepManager.kt         (update)

data/local/
├── DailyStepRecordEntity.kt    (update — add escrowSteps)
└── AppDatabase.kt              (update — migration v2→v3)

di/
└── GoogleFitModule.kt          (new)

service/
└── StepSyncWorker.kt           (update — add cross-validation and gap-filling)
```

## Completion Criteria

- Google Fit OAuth sign-in works and reads step data
- Cross-validation flags >20% discrepancies and uses escrow system
- Gap-filling recovers missed steps when service was killed
- Activity Minute Parity credits indoor workouts at correct conversion rates
- Double-counting prevention works (no overlap between steps and activity minutes)
- Per-activity daily caps enforced
- Escrow releases after reconciliation or discards after 3 failed syncs
