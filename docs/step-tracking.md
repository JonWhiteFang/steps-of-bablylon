# Step Tracking

## Sensor Priority

| Priority | Source | Purpose |
|---|---|---|
| Primary | `TYPE_STEP_COUNTER` | Cumulative hardware step count. Battery efficient. Persists across reboots. |
| Secondary | Health Connect SDK | Cross-validation, gap-filling, Activity Minute Parity |
| Tertiary | `TYPE_STEP_DETECTOR` | *(Planned)* Real-time per-step events for notification/widget updates |

`TYPE_STEP_COUNTER` returns a cumulative count since last reboot. Track deltas between readings to compute steps per interval.

## Background Service Architecture

### Foreground Service

- Persistent notification showing daily step count, spendable balance, and Workshop/Battle action buttons
- Registers `TYPE_STEP_COUNTER` sensor listener
- Runs continuously while the app is installed
- Tap notification → open app; action buttons → Workshop or Battle screen

### WorkManager

- Periodic sync every 15 minutes
- Reconciles local step count with Health Connect
- Catches up on missed steps if the foreground service was killed
- Constraint: requires Health Connect availability

### Boot Receiver

- `BOOT_COMPLETED` broadcast receiver restarts the foreground service after reboot
- Re-registers sensor listeners

### Battery Optimization

- Request battery optimization whitelist on first launch
- Use `SensorManager.SENSOR_DELAY_NORMAL` (lowest power)
- Minimize wake locks — rely on hardware step counter batching

## Anti-Cheat Rules

| Rule | Threshold | Action |
|---|---|---|
| Rate limit | 200 steps/min (250 burst for running) | Excess steps silently discarded |
| Daily ceiling | 50,000 steps/day | Hard cap, no more steps credited |
| Health Connect cross-validation | >20% discrepancy | Steps held in escrow until reconciled |
| Accelerometer pattern analysis | *(Planned)* Mechanical regularity detected | Suspicious steps rejected |

### Rate Limiting Implementation

- Track steps per rolling 1-minute window
- If delta > 200 in any window, cap at 200
- Running bursts (up to 250) allowed for short windows (<5 min)
- Log discarded steps for analytics

### Escrow System

When Health Connect discrepancy >20%:
1. Steps are credited to an escrow balance (not spendable)
2. Next WorkManager sync re-checks
3. If resolved within 3 syncs, escrow releases to main balance
4. If still discrepant, escrow is discarded

## Activity Minute Parity

For non-ambulatory activities tracked by Health Connect:

| Activity | Conversion | Daily Cap |
|---|---|---|
| Outdoor walking/running | 1:1 native steps | 50,000 |
| Treadmill | 1:1 | 50,000 |
| Stationary cycling | 1 min = 100 Step-eq | 10,000 |
| Rowing | 1 min = 100 Step-eq | 10,000 |
| Swimming | 1 min = 120 Step-eq | 12,000 |
| Wheelchair propulsion | 1 min = 110 Step-eq | 11,000 |
| Yoga / Stretching | 1 min = 50 Step-eq | 5,000 |

### Double-Counting Prevention

Step-equivalents from Activity Minutes are only credited when the step sensor records <50 steps/min during that period. This prevents counting a walk as both steps AND active minutes.

## Health Connect Integration

Health Connect (replacing deprecated Google Fit) is used as the secondary data source:
- `HealthConnectClient.getOrCreate()` — framework module on SDK 34+, always available
- `aggregate()` with `StepsRecord.COUNT_TOTAL` for daily step totals
- `readRecords()` with `ExerciseSessionRecord` for exercise sessions
- Permissions: `android.permission.health.READ_STEPS`, `android.permission.health.READ_EXERCISE`
- No OAuth required — uses standard Android health permissions

## Data Flow

```
Sensor Event
  → Foreground Service (delta calculation, rate limiting)
    → Room (DailyStepRecord update)
      → WorkManager (Health Connect reconciliation, escrow check)
        → Room (final credited steps)
          → PlayerProfile.currentStepBalance update
```

## Permissions

- `ACTIVITY_RECOGNITION` — required for step sensors
- `FOREGROUND_SERVICE` — persistent step counting
- `FOREGROUND_SERVICE_HEALTH` — foreground service type
- `RECEIVE_BOOT_COMPLETED` — restart after reboot
- `POST_NOTIFICATIONS` — step count notification
- `android.permission.health.READ_STEPS` — Health Connect step data
- `android.permission.health.READ_EXERCISE` — Health Connect exercise sessions
