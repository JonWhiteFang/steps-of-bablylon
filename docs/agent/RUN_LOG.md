# Run Log

## 2026-03-04 — Project Memory System Setup
- Goal: Implement repo-backed project memory system for Kiro CLI default agent.
- Plan: Create steering files (10-project-memory.md, 11-agent-protocol.md), living memory docs (START_HERE, STATE, CONSTRAINTS, RUN_LOG, ADR template), update AGENTS.md.
- Changes made:
  - Created `.kiro/steering/10-project-memory.md` (always-on memory source declarations)
  - Created `.kiro/steering/11-agent-protocol.md` (preflight + end-of-run protocol)
  - Created `docs/agent/START_HERE.md` (agent contract)
  - Created `docs/agent/STATE.md` (current project snapshot)
  - Created `docs/agent/CONSTRAINTS.md` (invariants and rules)
  - Created `docs/agent/RUN_LOG.md` (this file)
  - Created `docs/agent/DECISIONS/ADR-0001-template.md`
  - Created `docs/agent/state.json`
  - Updated `AGENTS.md` with memory spine section
- Commands/tests run: N/A (documentation-only change)
- Open questions / blockers: None.
- Follow-ups created: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-04 — Plan 04: Step Counter Service
- Goal: Implement background step counting with foreground service, anti-cheat, and WorkManager sync.
- Changes made:
  - Added `hilt-work:1.3.0` and `hilt-androidx-compiler:1.3.0` to version catalog + build.gradle.kts
  - Created `data/sensor/StepRateLimiter.kt` — rolling 1-min window, 200/min cap (250 burst)
  - Created `data/sensor/DailyStepManager.kt` — orchestrates rate limit → 50k ceiling → Room persist
  - Created `data/sensor/StepSensorDataSource.kt` — TYPE_STEP_COUNTER wrapper, emits deltas via callbackFlow
  - Created `service/StepNotificationManager.kt` — notification channel + builder, 30s throttle
  - Created `service/StepCounterService.kt` — foreground service (health type), START_STICKY
  - Created `service/BootReceiver.kt` — BOOT_COMPLETED → restart service
  - Created `service/StepSyncWorker.kt` — @HiltWorker CoroutineWorker, 15-min periodic catch-up
  - Created `service/StepSyncScheduler.kt` — enqueues periodic work request
  - Created `di/StepModule.kt` — provides SensorManager via Hilt
  - Updated `StepsOfBabylonApp.kt` — implements Configuration.Provider, injects HiltWorkerFactory
  - Updated `AndroidManifest.xml` — 5 permissions, service + receiver declarations, disabled default WorkManager init
  - Updated `MainActivity.kt` — runtime permission requests for ACTIVITY_RECOGNITION + POST_NOTIFICATIONS
  - Added `getDailyRecord()` to StepRepository interface + StepRepositoryImpl
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers: None.
- Follow-ups created:
  - Replace placeholder notification icon with custom app icon (when assets exist)
  - Notification balance could show live wallet balance via Flow observation
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-04 — Plan 05: Health Connect Integration
- Goal: Implement Health Connect (replacing deprecated Google Fit) for step cross-validation, gap-filling, and Activity Minute Parity.
- Key decision: ADR-worthy — used Health Connect instead of Google Fit (Google Fit APIs deprecated, shutting down 2026). See docs/agent/DECISIONS/ for ADR.
- Changes made:
  - Added `health-connect-client:1.2.0-alpha02` to version catalog + build.gradle.kts
  - Created `data/healthconnect/HealthConnectClientWrapper.kt` — client setup, availability, permissions
  - Created `data/healthconnect/HealthConnectStepReader.kt` — aggregated step reading
  - Created `data/healthconnect/StepCrossValidator.kt` — escrow system (>20% discrepancy, 3-sync lifecycle)
  - Created `data/healthconnect/StepGapFiller.kt` — recovers missed steps from HC
  - Created `data/healthconnect/ExerciseSessionReader.kt` — reads exercise sessions
  - Created `data/healthconnect/ActivityMinuteConverter.kt` — conversion table with per-activity caps + double-counting prevention
  - Created `di/HealthConnectModule.kt` — organizational Hilt module
  - Created `presentation/HealthConnectPermissionActivity.kt` — privacy policy stub
  - Updated `DailyStepRecordEntity.kt` — renamed googleFitSteps→healthConnectSteps, added escrowSteps + escrowSyncCount
  - Updated `DailyStepSummary.kt` — matching field changes
  - Updated `StepRepository.kt` — renamed method, added escrow methods
  - Updated `StepRepositoryImpl.kt` — implemented escrow methods
  - Updated `DailyStepDao.kt` — added clearEscrow query
  - Updated `DailyStepManager.kt` — added recordActivityMinutes()
  - Updated `StepSyncWorker.kt` — integrated HC gap-fill, cross-validation, activity minutes
  - Updated `MainActivity.kt` — HC permission request via PermissionController
  - Updated `AndroidManifest.xml` — HC permissions, privacy policy activity + activity-alias
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers:
  - StepSyncWorker passes empty sensorStepsPerMinute map to ActivityMinuteConverter (full per-minute tracking deferred)
- Follow-ups created:
  - Update GDD/step-tracking docs to reference Health Connect instead of Google Fit
  - Create ADR for Google Fit → Health Connect decision
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-04 — Plan 06: Home Screen & Navigation
- Goal: Build Compose navigation graph, bottom nav bar, and real Home dashboard with live data.
- Changes made:
  - Added `hilt-navigation-compose:1.3.0` and `compose-material-icons-core` to version catalog + build.gradle.kts
  - Created `presentation/navigation/Screen.kt` — sealed class with 5 routes (Home, Workshop, Battle, Labs, Stats)
  - Created `presentation/navigation/BottomNavBar.kt` — NavigationBar with 5 items, route highlighting
  - Created `presentation/home/HomeUiState.kt` — UI state data class
  - Created `presentation/home/HomeViewModel.kt` — @HiltViewModel combining PlayerRepository + StepRepository flows
  - Rewrote `presentation/home/HomeScreen.kt` — real dashboard (tier/biome header, step card, currency row, best wave, battle button)
  - Updated `presentation/MainActivity.kt` — Scaffold + NavHost + BottomNavBar, preserved permission logic
  - HomeViewModel calls `ensureProfileExists()` in init to seed default profile
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers: None.
- Follow-ups created: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-04 — Plan 07: Workshop Screen & Upgrades
- Goal: Build Workshop screen with 3-tab layout, 23 upgrades, tap-to-buy, Quick Invest.
- Changes made:
  - Created `domain/usecase/PurchaseUpgrade.kt` — checks affordability, deducts Steps, increments level
  - Created `domain/usecase/QuickInvest.kt` — recommends cheapest affordable upgrade
  - Created `presentation/workshop/WorkshopUiState.kt` — UpgradeDisplayInfo + WorkshopUiState
  - Created `presentation/workshop/WorkshopViewModel.kt` — @HiltViewModel, combines upgrades + wallet flows
  - Created `presentation/workshop/UpgradeCard.kt` — reusable card with 3 visual states
  - Created `presentation/workshop/WorkshopScreen.kt` — PrimaryTabRow, LazyColumn, Quick Invest FAB
  - Updated `presentation/home/HomeViewModel.kt` — added workshopRepository.ensureUpgradesExist() in init
  - Updated `presentation/MainActivity.kt` — replaced Workshop placeholder with WorkshopScreen()
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers: None.
- Follow-ups created: None.
- Memory updated: STATE ✅ / RUN_LOG ✅
