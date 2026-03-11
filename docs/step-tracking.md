# Step Tracking

## Sensor Priority

| Priority | Source | Purpose |
|---|---|---|
| Primary | `TYPE_STEP_COUNTER` | Cumulative hardware step count. Battery efficient. Persists across reboots. |
| Secondary | Health Connect SDK | Cross-validation, gap-filling, Activity Minute Parity |
| Tertiary | `TYPE_STEP_DETECTOR` | *(Deferred — not implemented in v1.0)* Real-time per-step events for notification/widget updates |

`TYPE_STEP_COUNTER` returns a cumulative count since last reboot. Track deltas between readings to compute steps per interval.

## Background Service Architecture

### Foreground Service

- Persistent notification showing daily step count, spendable balance, and Workshop/Battle action buttons
- Registers `TYPE_STEP_COUNTER` sensor listener
- Runs continuously while the app is installed
- Tap notification → open app; action buttons → Workshop or Battle screen

### WorkManager

- Periodic sync every 15 minutes
- Checks service heartbeat before reading sensor — skips catch-up if service is alive (heartbeat within 2 minutes)
- Uses Room `sensorSteps` as authoritative baseline — only credits the uncredited gap (prevents double-crediting)
- Reconciles local step count with Health Connect
- Catches up on missed steps if the foreground service was killed
- Constraint: requires Health Connect availability

### Service ↔ Worker Coordination

The foreground service and WorkManager worker share step ingestion responsibility. To prevent double-crediting:

- **Heartbeat**: Service writes a timestamp to `StepIngestionPreferences` on every step credit. Worker checks this — if the heartbeat is within 2 minutes, the service is alive and the worker skips sensor catch-up.
- **Room baseline**: Worker uses the current `sensorSteps` value in Room as the authoritative baseline. It reads the hardware counter, computes the gap (`counter - dayStartCounter - sensorSteps`), and only credits the uncredited portion.
- **Day-start counter**: Whichever path (service or worker) reads the sensor first today records the hardware counter value as the day-start baseline in SharedPreferences.

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
| Step velocity analysis | Constant rate (CV < 5%) or instant jump (0→150/min) | Penalty multiplier (0.5× or 0.0×) |
| Daily ceiling | 50,000 steps/day | Hard cap, no more steps credited |
| Health Connect cross-validation | >20% discrepancy, graduated by offense count | Level 0: escrow; Level 1: faster discard; Level 2: cap at HC; Level 3: cap at HC −10% |
| Activity minute validation | >4hr sessions, <2min micro-sessions, >5 types/day | Truncate, discard, or reject |
| Overlap deduction | Sensor ≥50 steps/min during activity period | Credit only sensor steps, not activity minutes |

### Rate Limiting Implementation

- Track steps per rolling 1-minute window
- If delta > 200 in any window, cap at 200
- Running bursts (up to 250) allowed for short windows (<5 min)
- Discarded steps logged to AntiCheatPreferences

### Step Velocity Analysis

- Rolling 15-minute window of (timestamp, stepDelta) entries
- Two detection heuristics:
  - **Instant jump**: Rate goes from <20 to >150 steps/min with no ramp-up
  - **Constant rate**: Coefficient of variation <5% over 10-minute window (phone shakers)
- Returns penalty multiplier: 1.0 (normal), 0.5 (one flag), 0.0 (both flags)
- Minimum 5 entries required before analysis activates

### Escrow System (Graduated Response)

Cross-validation offense count tracked in SharedPreferences, decays by 1 after 7 days without offense.

| Offense Level | Count | Behavior |
|---|---|---|
| Level 0 | 0 | Escrow excess (deducts from balance), release on reconciliation (3 syncs) |
| Level 1 | 1–2 | Escrow with faster discard (2 syncs), deducts from balance |
| Level 2 | 3–5 | Cap credited steps at Health Connect value |
| Level 3 | 6+ | Cap at Health Connect value minus 10% penalty |

When discrepancy resolves cleanly, escrow releases (restores deducted steps) and offense count decays. If escrow is discarded, the deduction remains — the player loses the suspicious steps.

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

Step-equivalents from Activity Minutes are only credited when the step sensor records <50 steps/min during that period. Per-minute sensor step counts are tracked in `DailyStepManager` and passed to `ActivityMinuteConverter` for minute-level overlap deduction.

### Activity Minute Validation

Sessions are filtered by `ActivityMinuteValidator` before conversion:
- Sessions <2 minutes discarded (noise/gaming)
- Sessions >4 hours truncated to 240 minutes
- More than 5 distinct activity types per day: extras rejected

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
  → Foreground Service (delta calculation, writes heartbeat)
    → StepRateLimiter (200/min cap)
      → StepVelocityAnalyzer (shaker/spoof penalty)
        → Daily ceiling (50k cap)
          → Room (DailyStepRecord update) + per-minute tracking
            → WorkManager (checks heartbeat → skips if service alive)
              → Gap recovery (Room baseline, not private counter)
              → HC gap-fill, cross-validation, activity minutes
                → ActivityMinuteValidator (gaming prevention)
                  → ActivityMinuteConverter (overlap deduction via per-minute data)
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
