# Repository Analysis: Bugs and UX

## 1. Executive Summary

- **What the project appears to do**  
  `Steps of Babylon` is an Android walking-driven idle tower defense game. Real-world steps feed progression, currencies, upgrades, daily missions, labs, cards, battle rounds, notifications, widgets, and some Health Connect integrations.

- **Overall codebase quality impression**  
  The repo is well-structured and much better than a typical prototype: clear package boundaries, a sizeable unit-test suite, domain/use-case separation, and generally readable Kotlin. The main problem is not code organization. The problem is that several high-impact gameplay and UX flows are internally inconsistent. Some systems look complete in the UI but are either partially wired, wired incorrectly, or only safe in the happy path.

- **Biggest bug risks**  
  The most serious risks are in step accounting and progression integrity. The periodic worker and the foreground step service can both credit the same sensor data. The Health Connect “escrow” path does not actually escrow steps and can add them twice. Several utility upgrades are sold but do not function as advertised. Battle cash bonuses are also not reaching the engine correctly.

- **Biggest UX weaknesses**  
  The repo frequently prefers silent failure over user feedback. Permission handling, purchase flows, mission progression, and notification settings all lack clear feedback states. Some features present as live and user-facing, but are effectively stubbed, partial, or misleading: store purchases, cosmetic application, widget behavior, and “persistent notification” settings are the clearest examples.

- **Top 5 priorities**
  1. Fix duplicate step crediting between `StepCounterService` and `StepSyncWorker`.
  2. Redesign Health Connect escrow so suspicious steps are actually withheld, reconciled, or removed correctly.
  3. Pass real workshop utility levels into battle and remove or implement currently dead upgrades.
  4. Fix backup/migration safety for the encrypted Room database.
  5. Fix user-facing trust failures: broken widget, stale missions, misleading notification setting, and missing action feedback.

## 2. Repository Understanding

- **Tech stack**  
  Kotlin, Android, Jetpack Compose, Hilt, Room, WorkManager, SQLCipher, Health Connect, and a custom `SurfaceView` battle renderer.

- **Architecture overview**  
  The repo follows a fairly clean split:
  - `data/`: Room entities, DAOs, repository implementations, preferences, Health Connect, sensor handling
  - `domain/`: models, repository interfaces, use cases, progression/economy logic
  - `presentation/`: Compose screens, ViewModels, battle renderer/UI
  - `service/`: foreground step service, WorkManager sync, notifications, widget
  - `di/`: Hilt wiring

- **Main user flows inferred from the repo**
  1. First launch -> request activity recognition / notifications / optionally Health Connect
  2. Foreground step service starts -> steps are credited -> player balance updates
  3. Home screen -> tier selection, battle entry, missions, supplies, economy, store, settings
  4. Battle -> wave progression -> rewards, milestones, ads, replay
  5. Workshop / Labs / Cards / Weapons -> spend currencies to progress power systems
  6. Stats / widget / notifications -> supporting progress visibility and reminders

- **Important modules/systems reviewed**
  - Step tracking: `StepCounterService`, `StepSyncWorker`, `DailyStepManager`, `StepSensorDataSource`, Health Connect readers/validators
  - Persistence/security: `AppDatabase`, `DatabaseModule`, `DatabaseKeyManager`, Room DAOs/entities
  - Battle flow: `BattleViewModel`, `BattleScreen`, `GameSurfaceView`, `GameEngine`
  - Progression: workshop, labs, cards, ultimate weapons, missions, milestones
  - User-facing support systems: widget, notification managers, settings, README/setup docs

- **Any parts that were unclear or insufficiently documented**
  - I could not execute Gradle tasks in this environment because the wrapper tries to download Gradle from the network, which is unavailable here. This means the review is based on static code inspection rather than a compiled run.
  - Billing and ads are explicitly stubbed in the repo/docs, but the store UI presents them in a user-facing way that still creates product risk.

## 3. High-Priority Findings

### Finding 1
- **Title**: Step sync worker can double-credit steps already credited by the live foreground service
- **Category**: Bug
- **Severity**: Critical
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepCounterService.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepSyncWorker.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepSyncScheduler.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/StepsOfBabylonApp.kt`
- **Why this matters**:  
  This is a progression-integrity failure. It can inflate steps, currency, milestones, missions, and downstream anti-cheat signals.
- **Evidence from the repo**:  
  `StepCounterService.onCreate()` continuously collects `sensorDataSource.stepDeltas` and calls `dailyStepManager.recordSteps(...)`. Separately, `StepSyncWorker.doWork()` always calls `sensorCatchUp()`, which reads `TYPE_STEP_COUNTER`, compares it to its own persisted `last_counter_value`, and again calls `dailyStepManager.recordSteps(delta, ...)`. There is no coordination between the service and worker state.
- **How it could fail in practice**:  
  A user walking normally while the foreground service is active can still have the 15-minute worker add the same interval again. That will over-credit step balance and distort all step-derived systems.
- **Recommended fix**:  
  Use one authoritative ingestion path. Either:
  - disable `sensorCatchUp()` whenever the foreground service is healthy, or
  - persist/compare against the same authoritative cumulative sensor baseline used by the service, or
  - move worker recovery to a true gap-recovery path only, not a second live credit path.

### Finding 2
- **Title**: Health Connect escrow logic does not actually escrow suspicious steps and can add them twice
- **Category**: Bug
- **Severity**: Critical
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/healthconnect/StepCrossValidator.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/repository/StepRepositoryImpl.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/DailyStepDao.kt`
  - `app/src/test/java/com/whitefang/stepsofbabylon/data/healthconnect/StepCrossValidatorTest.kt`
- **Why this matters**:  
  This undermines the anti-cheat system and corrupts player progression. The current code can both fail to withhold suspicious steps and later add them again.
- **Evidence from the repo**:  
  `DailyStepManager.recordSteps()` immediately calls `playerRepository.addSteps(credited)`. Later, `StepCrossValidator.validate()` detects discrepancy and only writes escrow metadata via `stepRepository.updateEscrow(...)`. It does not subtract those steps from the player balance. On reconciliation, `StepCrossValidator` calls `playerRepository.addSteps(record.escrowSteps)` and then `releaseEscrow()`, while `StepRepositoryImpl.releaseEscrow()` only clears DB escrow fields.
- **How it could fail in practice**:  
  Suspicious steps remain usable immediately. If Health Connect later “resolves” the discrepancy, the user receives the escrow amount again. If it is discarded, the player still kept the original credit because nothing was ever removed.
- **Recommended fix**:  
  Redesign escrow as an actual withheld balance state. Either:
  - do not add suspicious steps to the player balance until validated, or
  - add them to a separate pending balance and release/deduct atomically, or
  - store and reverse the original over-credit when a discrepancy is found.
  Add end-to-end tests around player balance, daily record, escrow release, and discard.

### Finding 3
- **Title**: Battle engine is initialized with empty workshop utility levels, so cash-related upgrades do not work
- **Category**: Bug
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/BattleScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/BattleViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/GameSurfaceView.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/engine/GameEngine.kt`
- **Why this matters**:  
  Players can invest in workshop utility upgrades and not receive their advertised battle economy benefits.
- **Evidence from the repo**:  
  `BattleScreen` calls `surfaceView.configure(viewModel.resolvedStats, viewModel.tier, emptyMap())`. `BattleViewModel.playAgain()` does the same. `GameEngine.handleEnemyDeath()` and `handleWaveComplete()` rely on `wsLevel(UpgradeType.CASH_BONUS)`, `CASH_PER_WAVE`, and `INTEREST`, but the engine’s `workshopLevels` map therefore remains empty.
- **How it could fail in practice**:  
  Kill cash, end-of-wave bonus cash, and interest will behave as if the player never bought those workshop upgrades.
- **Recommended fix**:  
  Pass the real workshop level map into `GameSurfaceView.configure(...)` and keep the engine’s workshop state synchronized with the ViewModel.

### Finding 4
- **Title**: `STEP_MULTIPLIER` and `RECOVERY_PACKAGES` are purchasable upgrades with no implementation
- **Category**: Bug
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/domain/model/UpgradeType.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/workshop/WorkshopViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/engine/GameEngine.kt`
- **Why this matters**:  
  The game lets users spend scarce progression currency on upgrades that do nothing. That is both a bug and a direct trust hit.
- **Evidence from the repo**:  
  `UpgradeType` defines both upgrades with descriptions and costs. A repo-wide search shows no implementation usage for either upgrade outside config/enum definitions.
- **How it could fail in practice**:  
  Users buy an upgrade expecting more walking steps or recovery packages in battle and get no effect. The UI never warns that those upgrades are inactive.
- **Recommended fix**:  
  Either implement both upgrades immediately or hide/disable them until implemented. Do not sell dead upgrades.

### Finding 5
- **Title**: Encrypted database backup/restore path can crash restored installs
- **Category**: Reliability Risk
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/DatabaseKeyManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/di/DatabaseModule.kt`
- **Why this matters**:  
  This can brick real user installs after device restore or cloud restore.
- **Evidence from the repo**:  
  The manifest sets `android:allowBackup="true"`. `DatabaseKeyManager` stores an encrypted passphrase blob and IV in SharedPreferences, protected by an Android Keystore key. On restore to a different device, the prefs/database may restore but the keystore key will not. `getPassphrase()` assumes the stored blob is decryptable and does not have a recovery path.
- **How it could fail in practice**:  
  On restored install, app startup can fail while constructing the Room database because decrypting the old passphrase with a new or missing keystore key will throw.
- **Recommended fix**:  
  Either disable backup for the encrypted DB/passphrase materials, or implement backup exclusion rules / restore detection with safe key regeneration and DB reset messaging.

### Finding 6
- **Title**: Room schema version is already at 7, but the app defines no migrations
- **Category**: Likely Bug
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/AppDatabase.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/di/DatabaseModule.kt`
  - `app/schemas/com.whitefang.stepsofbabylon.data.local.AppDatabase/`
- **Why this matters**:  
  Any existing install created on an older schema is likely to fail at startup after update.
- **Evidence from the repo**:  
  `AppDatabase` is `version = 7`. `DatabaseModule` builds Room with no `addMigrations(...)` and no fallback behavior. The inline comment says future schema changes must provide migrations, but the schema version is already multiple revisions in.
- **How it could fail in practice**:  
  Updating from earlier internal/beta builds or QA installs can cause Room migration exceptions and startup failure.
- **Recommended fix**:  
  Add explicit migrations for every supported upgrade path and add migration tests. If older schemas are intentionally unsupported, document that clearly and handle destructive reset intentionally rather than implicitly crashing.

### Finding 7
- **Title**: Widget behavior is broken and misleading
- **Category**: UX Issue
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/WidgetUpdateHelper.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepWidgetProvider.kt`
  - `app/src/main/res/layout/widget_step_counter.xml`
- **Why this matters**:  
  Widgets are low-trust surfaces. If they show wrong data or don’t open reliably, users stop trusting the app’s progress tracking.
- **Evidence from the repo**:  
  `DailyStepManager.recordSteps()` updates the widget with `widgetUpdateHelper.update(dailyCreditedTotal, 0)`, so the widget balance is always written as `0`. `StepWidgetProvider.updateAllWidgets()` sets the click handler on `android.R.id.background`, but that ID does not exist in `widget_step_counter.xml`.
- **How it could fail in practice**:  
  The widget can show a permanently wrong balance and may not respond to taps at all.
- **Recommended fix**:  
  Pass the real balance when updating widget data, and attach the click `PendingIntent` to a real view ID defined in the widget layout.

### Finding 8
- **Title**: Walking mission progress only updates when the Missions screen is opened
- **Category**: Bug
- **Severity**: High
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/missions/MissionsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/home/HomeViewModel.kt`
- **Why this matters**:  
  Daily mission systems need to feel live. Stale mission progress creates confusion and missed rewards.
- **Evidence from the repo**:  
  `MissionsViewModel.init` calls `updateWalkingMissionProgress()` once. A repo-wide search shows no walking mission updates from step ingestion in `DailyStepManager` or elsewhere.
- **How it could fail in practice**:  
  A user can walk enough steps to complete a mission, but the mission will not update until they open the Missions screen. Home badges and claimability can lag behind reality.
- **Recommended fix**:  
  Update walking mission progress as part of the step credit pipeline, or observe daily step totals reactively in mission logic.

### Finding 9
- **Title**: “Persistent notification” setting does not actually disable the persistent foreground notification
- **Category**: Documentation Mismatch
- **Severity**: Medium
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepCounterService.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepNotificationManager.kt`
- **Why this matters**:  
  This is a direct trust problem. The setting promises one thing and the runtime behavior does another.
- **Evidence from the repo**:  
  The settings screen exposes a `Step Counter` toggle described as “Persistent notification with daily steps.” But `StepCounterService.onCreate()` always calls `startForeground(...)` with a notification before checking preferences. `StepNotificationManager.updateNotification()` checks the preference only for later updates.
- **How it could fail in practice**:  
  Users turn the setting off and still keep seeing the ongoing notification, or keep the service running with stale notification content.
- **Recommended fix**:  
  Either rename the setting to reflect what it actually controls, or make the service/notification lifecycle respect the preference end-to-end.

### Finding 10
- **Title**: Smart reminder inactivity logic is based on a timestamp that is never updated
- **Category**: Likely Bug
- **Severity**: Medium
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/SmartReminderManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/PlayerProfileDao.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/repository/PlayerRepositoryImpl.kt`
- **Why this matters**:  
  Reminder quality depends on respecting recent activity. If the inactivity clock is wrong, reminders feel spammy or arbitrary.
- **Evidence from the repo**:  
  `SmartReminderManager` suppresses reminders if `now - profile.lastActiveAt < INACTIVITY_MS`. `PlayerProfileDao` defines `updateLastActiveAt(...)`, but a repo-wide search shows no production call site using it.
- **How it could fail in practice**:  
  After initial profile creation, reminders can start qualifying based on a stale timestamp even when the user is actively opening and using the app.
- **Recommended fix**:  
  Update `lastActiveAt` on app foreground, significant navigation, or gameplay interactions, then cover this with tests.

### Finding 11
- **Title**: Supply notification deep-linking is fragile when the app is already open
- **Category**: Likely Bug
- **Severity**: Medium
- **Confidence**: Medium
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/SupplyDropNotificationManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/MainActivity.kt`
- **Why this matters**:  
  Notifications must take users to the promised destination reliably.
- **Evidence from the repo**:  
  `SupplyDropNotificationManager` uses `FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP`, but `MainActivity` only reads `intent?.getStringExtra("navigate_to")` inside a one-time `LaunchedEffect(Unit)` and does not override `onNewIntent()`.
- **How it could fail in practice**:  
  If the activity is already alive, tapping a supply notification can bring the app forward without navigating to supplies.
- **Recommended fix**:  
  Handle `onNewIntent()` and route deep-link extras into Compose navigation consistently.

### Finding 12
- **Title**: Store and replay state handling are inconsistent around ads and season pass status
- **Category**: Bug
- **Severity**: Medium
- **Confidence**: High
- **Affected files**:
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/store/StoreViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/billing/StubBillingManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/BattleViewModel.kt`
- **Why this matters**:  
  Purchase state needs to be consistent everywhere or users will mistrust premium systems immediately.
- **Evidence from the repo**:  
  `StoreViewModel` uses `profile.seasonPassActive` directly and does not check `seasonPassExpiry`, while `HomeViewModel` does check expiry. `StubBillingManager.isSeasonPassActive()` contains expiry cleanup logic, but nothing calls it from the UI path. Separately, `BattleViewModel.playAgain()` rebuilds `BattleUiState(...)` without preserving `adRemoved`, so the next post-round overlay can revert to ad-enabled UI.
- **How it could fail in practice**:  
  An expired season pass may still look active in Store and block re-subscribe flows. An ad-free user can see ad CTA state again after replaying a round.
- **Recommended fix**:  
  Centralize premium entitlement resolution and reuse it across screens. Preserve premium flags when resetting battle UI state.

## 4. Full Findings List

### 4.1 Bugs and Logic Risks

- **Worker/service step double-counting**  
  **Severity:** Critical  
  **File(s):** `service/StepCounterService.kt`, `service/StepSyncWorker.kt`, `data/sensor/DailyStepManager.kt`  
  **Explanation:** Two independent step-ingestion paths can credit the same physical movement.  
  **Suggested fix:** Use one authoritative baseline and gate catch-up logic.

- **Escrow flow does not escrow and can double-award**  
  **Severity:** Critical  
  **File(s):** `data/healthconnect/StepCrossValidator.kt`, `data/repository/StepRepositoryImpl.kt`, `data/sensor/DailyStepManager.kt`  
  **Explanation:** Suspicious excess is recorded, not withheld; reconciliation adds again.  
  **Suggested fix:** Make escrow a real balance state with atomic release/deduction.

- **Battle cash utility upgrades are not applied**  
  **Severity:** High  
  **File(s):** `presentation/battle/BattleScreen.kt`, `presentation/battle/BattleViewModel.kt`, `presentation/battle/engine/GameEngine.kt`  
  **Explanation:** Engine gets `emptyMap()` for workshop levels, so `CASH_BONUS`, `CASH_PER_WAVE`, and `INTEREST` do not function.  
  **Suggested fix:** Pass actual workshop levels into the engine.

- **Dead purchasable upgrades: `STEP_MULTIPLIER` and `RECOVERY_PACKAGES`**  
  **Severity:** High  
  **File(s):** `domain/model/UpgradeType.kt`  
  **Explanation:** These upgrades are sold but have no usage in gameplay or step crediting.  
  **Suggested fix:** Implement or remove from purchase flow.

- **Walking missions update only on Missions screen open**  
  **Severity:** High  
  **File(s):** `presentation/missions/MissionsViewModel.kt`  
  **Explanation:** Step progress is not updated in the step-ingestion path.  
  **Suggested fix:** Push walking mission progress updates into `DailyStepManager` or reactive mission observers.

- **Ad-free flag lost on battle replay**  
  **Severity:** Medium  
  **File(s):** `presentation/battle/BattleViewModel.kt`  
  **Explanation:** `playAgain()` rebuilds `BattleUiState` without preserving `adRemoved`.  
  **Suggested fix:** Carry forward premium flags on reset.

- **Expired season pass can still appear active in Store**  
  **Severity:** Medium  
  **File(s):** `presentation/store/StoreViewModel.kt`, `data/billing/StubBillingManager.kt`  
  **Explanation:** Store uses raw profile flag; Home uses expiry-aware logic.  
  **Suggested fix:** Derive entitlement from one shared source of truth.

- **Widget balance is always zero**  
  **Severity:** High  
  **File(s):** `data/sensor/DailyStepManager.kt`, `service/WidgetUpdateHelper.kt`  
  **Explanation:** Widget update always passes `balance = 0`.  
  **Suggested fix:** Fetch and send real balance.

### 4.2 Error Handling and Resilience Gaps

- **No migration path for schema version 7**  
  **Severity:** High  
  **File(s):** `data/local/AppDatabase.kt`, `di/DatabaseModule.kt`  
  **Explanation:** Any supported upgrade path from earlier schemas is unsafe.  
  **Suggested fix:** Add migrations and migration tests.

- **Backup/restore can break encrypted DB startup**  
  **Severity:** High  
  **File(s):** `AndroidManifest.xml`, `data/local/DatabaseKeyManager.kt`  
  **Explanation:** Restored encrypted DB/passphrase material may not match restored keystore state.  
  **Suggested fix:** Exclude from backup or implement restore-safe recovery.

- **Action failures are mostly silent in ViewModels**  
  **Severity:** Medium  
  **File(s):** `presentation/labs/LabsViewModel.kt`, `presentation/cards/CardsViewModel.kt`, `presentation/store/StoreViewModel.kt`, `presentation/workshop/WorkshopViewModel.kt`  
  **Explanation:** Use cases return detailed results, but many UI actions ignore them and show no error/success feedback.  
  **Suggested fix:** Add user-visible snackbars/dialog states for insufficient currency, maxed state, already active, etc.

- **No deep-link handling for `onNewIntent()`**  
  **Severity:** Medium  
  **File(s):** `presentation/MainActivity.kt`  
  **Explanation:** Notification navigation is one-shot on initial composition only.  
  **Suggested fix:** Handle new intents explicitly.

### 4.3 Validation and Edge Case Gaps

- **Blind balance decrements can go negative under concurrent actions**  
  **Severity:** Medium  
  **File(s):** `data/local/PlayerProfileDao.kt`, `presentation/store/StoreViewModel.kt`, `presentation/cards/CardsViewModel.kt`, `presentation/labs/LabsViewModel.kt`, `presentation/workshop/WorkshopViewModel.kt`  
  **Explanation:** DAO decrement queries do not clamp or transact against current state; several screens do not disable repeat taps while work is in progress.  
  **Suggested fix:** Use transactional spend methods with non-negative guards at the repository/DB level.

- **Purchase/ad actions have no in-progress guard**  
  **Severity:** Medium  
  **File(s):** `presentation/store/StoreViewModel.kt`, `presentation/battle/BattleViewModel.kt`, `presentation/cards/CardsViewModel.kt`  
  **Explanation:** Multiple taps can launch overlapping purchase/ad reward coroutines. `StoreUiState.isPurchasing` exists but is unused.  
  **Suggested fix:** Disable buttons while work is active and make reward paths idempotent.

- **Date-sensitive screens can become stale across midnight**  
  **Severity:** Medium  
  **File(s):** `presentation/home/HomeViewModel.kt`, `presentation/missions/MissionsViewModel.kt`, `presentation/stats/StatsViewModel.kt`  
  **Explanation:** `LocalDate.now()` is captured once and not rolled forward in long-lived ViewModels.  
  **Suggested fix:** Recompute/observe date boundaries or refresh on midnight transition.

### 4.4 UX and Usability Problems

- **Persistent notification setting is misleading**  
  **Severity:** Medium  
  **File(s):** `presentation/settings/NotificationSettingsScreen.kt`, `service/StepCounterService.kt`  
  **Explanation:** Users are told they can disable the persistent notification, but the service always starts foreground with one.  
  **Suggested fix:** Align runtime behavior with copy.

- **Permissions flow gives little explanation or fallback guidance**  
  **Severity:** Medium  
  **File(s):** `presentation/MainActivity.kt`  
  **Explanation:** Denied permissions do not lead to clear in-app explanation, degraded mode messaging, or route to settings. Health Connect permission result is ignored.  
  **Suggested fix:** Add explicit permission states and recovery UI.

- **Store sells cosmetics while admitting visual application is not implemented**  
  **Severity:** Medium  
  **File(s):** `presentation/store/StoreScreen.kt`  
  **Explanation:** The screen says “Visual application coming soon” but still allows purchase/equip/unequip.  
  **Suggested fix:** Hide purchasability until visuals exist, or label as preview/test content outside production UI.

- **Mission and progression feedback can lag behind reality**  
  **Severity:** High  
  **File(s):** `presentation/missions/MissionsViewModel.kt`, `presentation/home/HomeViewModel.kt`  
  **Explanation:** Step-driven completions do not update live unless the user visits the relevant screen.  
  **Suggested fix:** Make progression updates event-driven.

- **README suggests a helper script that is not included**  
  **Severity:** Low  
  **File(s):** `README.md`  
  **Explanation:** `run-gradle.sh` is recommended but not shipped.  
  **Suggested fix:** Include it or remove the recommendation.

### 4.5 Accessibility Concerns

- **Battle controls rely on symbol-only labels**  
  **Severity:** Medium  
  **File(s):** `presentation/battle/BattleScreen.kt`, `presentation/battle/ui/PostRoundOverlay.kt`, `presentation/home/HomeScreen.kt`  
  **Explanation:** Controls such as `▶`, `⏸`, `⬆`, `⚡`, emoji-only labels, and icon-heavy buttons are not very screen-reader friendly.  
  **Suggested fix:** Add meaningful text labels and semantics/content descriptions.

- **Widget/system notification icons are placeholders**  
  **Severity:** Low  
  **File(s):** `service/StepNotificationManager.kt`, `service/SupplyDropNotificationManager.kt`, `service/MilestoneNotificationManager.kt`  
  **Explanation:** Generic system icons make the app feel unfinished and can reduce recognition/trust.  
  **Suggested fix:** Use app-specific notification assets.

### 4.6 Documentation and Onboarding Issues

- **Privacy policy/contact details still contain placeholders**  
  **Severity:** Medium  
  **File(s):** `presentation/HealthConnectPermissionActivity.kt`, `docs/release/privacy-policy.md`, `docs/release/play-store-listing.md`  
  **Explanation:** `<contact-email>` remains in user-facing/compliance-facing content.  
  **Suggested fix:** Replace all placeholders before any release.

- **README references instrumented tests, but repo has no `androidTest` tree**  
  **Severity:** Low  
  **File(s):** `README.md`, `app/src/`  
  **Explanation:** The setup doc implies a testing layer that is not actually present.  
  **Suggested fix:** Either add instrumented tests or document current test scope accurately.

### 4.7 Test Coverage Gaps

- **No tests for services, notifications, widget, or deep-link handling**  
  **Severity:** High  
  **File(s):** `app/src/test/java/...`  
  **Explanation:** The most failure-prone lifecycle-driven Android code is largely untested.  
  **Suggested fix:** Add instrumentation or Robolectric coverage for service startup, worker behavior, notification routing, and widget updates.

- **No migration tests for the database**  
  **Severity:** High  
  **File(s):** `data/local/AppDatabase.kt`, `app/schemas/...`  
  **Explanation:** Schema evolution risk is currently unchecked.  
  **Suggested fix:** Add `MigrationTestHelper` coverage for every migration.

- **Anti-cheat tests assert escrow metadata, not real balance correctness**  
  **Severity:** Medium  
  **File(s):** `data/healthconnect/StepCrossValidatorTest.kt`  
  **Explanation:** The tests do not verify the actual player-balance semantics that are currently wrong.  
  **Suggested fix:** Add end-to-end tests that assert credited balance before discrepancy, after escrow, after discard, and after release.

## 5. User Journey Review

### First launch and permission grant
The app immediately pushes users into activity recognition and notification permissions from `MainActivity`, and may also request Health Connect permissions. The repo does not provide meaningful explanation, fallback guidance, or degraded-mode messaging when users deny access. The Health Connect permission result callback is empty, so the user receives no confirmation about what changed.

### Passive walking / background tracking
This is the core trust journey, and it is the weakest technically. The step service and sync worker can both feed the same progress system. That means a user can see inflated steps, then later inconsistent anti-cheat behavior, mission state, or notifications. Once users suspect the app’s step totals are “off,” the product loses its core credibility.

### Home screen and mission awareness
Home shows balances, badges, and progression cues, but walking mission completion is not updated in the step-ingestion path. A user can walk enough to complete a mission and still not see the expected claim state unless they visit the Missions screen. That feels laggy and confusing.

### Workshop and progression spending
Workshop is easy to navigate, but it currently lets users invest in upgrades that are partially or fully disconnected from actual runtime behavior. The worst case is spending on `STEP_MULTIPLIER` or `RECOVERY_PACKAGES`, which appear real but do nothing. Even where the upgrade exists, battle cash bonuses are not reaching the engine correctly.

### Battle flow
The battle experience is visually richer than the rest of the app, but the post-round and replay loop has state inconsistencies. Premium/ad state is not preserved cleanly on replay, symbol-heavy controls are not very accessible, and battle rewards are affected by the engine wiring issue for utility upgrades.

### Store and premium trust
The Store is risky from a trust standpoint. Billing is stubbed, purchases do not expose much feedback, cosmetics can be bought while the screen admits visual application is not implemented, and season pass state is resolved inconsistently across screens. This is the kind of UI that users can interpret as deceptive even if it began as a development stub.

### Notifications and widget
Notifications are important because this app is built around passive engagement. Here, the deep-link path is fragile, the “persistent notification” setting is misleading, the widget balance is wrong, and the widget tap target is likely broken. These are not cosmetic issues; they directly damage daily usability.

## 6. Most Likely Real-World Failures

1. **Trigger:** User walks normally for several hours with the foreground step service running.  
   **Likely symptom:** Step total and balance grow too quickly.  
   **Root cause in code:** `StepCounterService` and `StepSyncWorker.sensorCatchUp()` both credit steps into `DailyStepManager`.  
   **Suggested mitigation:** Disable worker catch-up when live sensor ingestion is healthy.

2. **Trigger:** Health Connect later reports lower totals than the app credited.  
   **Likely symptom:** “Escrow” does not visibly reduce balance, and later reconciliation may add extra steps again.  
   **Root cause in code:** `StepCrossValidator` records escrow metadata without reversing player balance; `releaseEscrow` then adds steps again.  
   **Suggested mitigation:** Convert escrow to a true pending-balance mechanism.

3. **Trigger:** User purchases utility upgrades expecting better battle income.  
   **Likely symptom:** Cash rewards feel unchanged or lower than expected.  
   **Root cause in code:** Battle engine receives `emptyMap()` for workshop levels.  
   **Suggested mitigation:** Pass and test actual workshop levels inside battle initialization.

4. **Trigger:** User buys `STEP_MULTIPLIER` or `RECOVERY_PACKAGES`.  
   **Likely symptom:** No visible effect at all.  
   **Root cause in code:** No implementation exists outside enum/config definitions.  
   **Suggested mitigation:** Hide or implement the upgrades.

5. **Trigger:** User restores the app on a new device from backup.  
   **Likely symptom:** App crashes during startup / DB open.  
   **Root cause in code:** Backed-up encrypted DB/passphrase blob no longer matches Android Keystore state.  
   **Suggested mitigation:** Exclude DB/passphrase from backup or add restore recovery.

6. **Trigger:** Existing tester/beta user updates from an older schema.  
   **Likely symptom:** App fails to open with a Room migration exception.  
   **Root cause in code:** `AppDatabase` is at version 7 with no migrations configured.  
   **Suggested mitigation:** Add migration objects and migration tests.

7. **Trigger:** User completes a walking mission without opening Missions.  
   **Likely symptom:** Mission badge/progress/claim state stays stale.  
   **Root cause in code:** Walking progress update only happens in `MissionsViewModel.init`.  
   **Suggested mitigation:** Update mission progress inside the step pipeline.

8. **Trigger:** User disables the persistent notification.  
   **Likely symptom:** Ongoing notification still appears.  
   **Root cause in code:** `StepCounterService` always calls `startForeground(...)`.  
   **Suggested mitigation:** Respect the preference in service lifecycle or rename the setting.

9. **Trigger:** User taps the home screen widget.  
   **Likely symptom:** Nothing happens, and the shown balance is wrong.  
   **Root cause in code:** Invalid click view ID and balance hard-coded to zero during updates.  
   **Suggested mitigation:** Bind the click handler to a real widget view and pass real data.

10. **Trigger:** User taps a supply notification while the app is already open.  
    **Likely symptom:** App opens but does not navigate to Supplies.  
    **Root cause in code:** `MainActivity` has no `onNewIntent()` routing path.  
    **Suggested mitigation:** Handle new intents and route to Compose navigation consistently.

## 7. Quick Wins

- Remove or disable the two dead workshop upgrades until they are implemented.
- Pass real workshop levels into battle initialization instead of `emptyMap()`.
- Fix widget balance updates and bind the click `PendingIntent` to a real view ID.
- Preserve `adRemoved` when resetting battle UI state.
- Replace `<contact-email>` placeholders everywhere user-facing.
- Add visible success/failure feedback for store, labs, cards, and workshop actions.
- Update walking mission progress from the step pipeline instead of screen open.
- Either ship `run-gradle.sh` or stop documenting it.

## 8. Strategic Improvements

- Unify all step ingestion under one authoritative event model with explicit recovery semantics.
- Rework anti-cheat into a proper accounting system with pending, credited, and rejected balances.
- Treat premium entitlements as a shared domain state rather than raw per-screen flag checks.
- Add Android lifecycle/integration testing for services, notifications, widget behavior, and intent routing.
- Add migration discipline: every schema change gets a migration and a migration test.
- Add product-state gating so incomplete systems cannot be sold or exposed as finished.

## 9. Suggested Remediation Plan

- **Phase 1: urgent fixes**
  - Fix worker/service double-crediting.
  - Fix escrow semantics so balances are correct under discrepancy, discard, and reconciliation.
  - Pass workshop levels correctly into battle and remove dead upgrades from the UI.
  - Fix widget balance/click behavior.
  - Disable backup for encrypted DB materials or add restore-safe recovery.

- **Phase 2: stability and UX improvements**
  - Add Room migrations and migration tests.
  - Move walking mission progression into the step-ingestion path.
  - Make persistent notification behavior match the setting text.
  - Implement consistent premium entitlement resolution.
  - Add user-visible feedback for denied permissions and failed actions.

- **Phase 3: polish and maintainability**
  - Add integration coverage for services, notifications, widget, and deep links.
  - Clean up stale placeholder content and release docs.
  - Improve accessibility labels and semantics in battle/UI controls.
  - Remove or hide unfinished monetization/cosmetic flows from production-facing UI.

## 10. Appendix

- **Notable files reviewed**
  - `README.md`
  - `app/build.gradle.kts`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/whitefang/stepsofbabylon/StepsOfBabylonApp.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/MainActivity.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepCounterService.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepSyncWorker.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/healthconnect/StepCrossValidator.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/AppDatabase.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/local/DatabaseKeyManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/BattleViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/BattleScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/engine/GameEngine.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/missions/MissionsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/store/StoreViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepWidgetProvider.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepNotificationManager.kt`
  - `app/src/test/java/com/whitefang/stepsofbabylon/data/healthconnect/StepCrossValidatorTest.kt`

- **Assumptions made**
  - Static analysis is representative because the repo is internally consistent enough to trace core flows.
  - Store/billing stubs are intentional for development, but the review treats them as product risk where the UI exposes them to end users.

- **Areas of uncertainty**
  - I could not fully validate runtime behavior by compiling/running Gradle tasks here because the wrapper attempted to fetch Gradle from the network and this environment has no outbound access.
  - I did not validate visual rendering behavior on a device/emulator.

- **Anything that should be manually tested by a human**
  - Real step accumulation over time with both service and worker active
  - Restore/install from backup on a second device
  - Upgrade from older DB schema versions
  - Notification deep links while app is foreground/background/already on stack
  - Widget tap behavior and displayed balances
  - Walking mission completion without visiting Missions
  - Battle reward differences before/after utility workshop upgrades
  - Premium/ad state after replaying rounds and after season pass expiry

## Top 10 Fixes

1. Stop `StepSyncWorker` from double-crediting steps already handled by `StepCounterService`.
2. Replace the current escrow metadata flow with real pending/withheld balance accounting.
3. Pass real workshop levels into `GameEngine`; verify `CASH_BONUS`, `CASH_PER_WAVE`, and `INTEREST` in tests.
4. Remove or implement `STEP_MULTIPLIER` and `RECOVERY_PACKAGES` before allowing purchase.
5. Make encrypted DB restore safe by excluding backup or handling keystore mismatch cleanly.
6. Add Room migrations for schema version 7 and cover them with migration tests.
7. Fix the widget: real balance, real click target, and regression tests.
8. Update walking mission progress from the step-ingestion pipeline, not only from `MissionsViewModel` init.
9. Make the persistent notification setting reflect actual service/notification behavior.
10. Add lifecycle/integration tests for services, notifications, deep links, and premium state transitions.
