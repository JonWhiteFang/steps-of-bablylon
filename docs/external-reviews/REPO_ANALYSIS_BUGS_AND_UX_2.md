# Repository Analysis: Bugs and UX

## 1. Executive Summary
- **What the project appears to do**  
  This repository is an Android game, **Steps of Babylon**, built around a walking-driven progression loop. Real-world steps and exercise sessions feed an in-game economy, which then powers upgrades, battles, labs, missions, cosmetics, and other idle/progression systems.

- **Overall codebase quality impression**  
  The codebase is well-structured at a high level: modern Kotlin, Compose, Hilt, Room, WorkManager, and a reasonably clear separation between data, domain, presentation, and service layers. There is also a meaningful JVM/unit test suite. The main problems are not “messy code” so much as **logic inconsistencies across subsystems**, **state-handling shortcuts that can become fragile**, and several **UX mismatches where the UI promises behavior the implementation does not actually deliver**.

- **Biggest bug risks**  
  The highest-risk area is the step/exercise ingestion pipeline. The code appears to handle raw sensor steps, Health Connect steps, activity-minute conversion, worker catch-up, and reward systems through partially separate paths. That split creates credible risks of **double-crediting**, **missing progression updates**, and **UI/stats inconsistency**.

- **Biggest UX weaknesses**  
  The biggest UX issues are trust-eroding mismatches: controls that do not do exactly what they say, labels that imply one destination but navigate somewhere else, purchasable content marked as “coming soon,” and several flows that silently no-op instead of explaining why the action failed.

- **Top 5 priorities**
  1. Make exercise/activity-minute crediting idempotent and delta-based.
  2. Route all credited movement through one canonical reward/progression pipeline.
  3. Remove `stateIn(viewModelScope).value` one-shot reads from action handlers.
  4. Fix misleading settings/navigation flows (`Return to Workshop`, persistent notification toggle).
  5. Replace destructive Room migration before any real user data matters.

## 2. Repository Understanding
- **Tech stack**  
  Kotlin, Jetpack Compose, Hilt, Room, WorkManager, SQLCipher, Android foreground service step tracking, Health Connect integration, and a custom battle renderer. The build targets Android 14+ (`minSdk 34`) and uses Gradle Kotlin DSL.

- **Architecture overview**  
  The repo broadly follows a clean-ish layering model:
  - `data/`: Room entities/DAOs, repository implementations, Health Connect integration, sensor and notification plumbing
  - `domain/`: models, repository interfaces, and use cases
  - `presentation/`: Compose screens and ViewModels
  - `service/`: foreground step service, boot receiver, sync workers, reminder/notification components

- **Main user flows inferred from the repo**
  - Install/open app → initialize profile/upgrades/research/missions on Home
  - Foreground step service records steps and updates balance/notification
  - Background worker catches up missing steps and imports Health Connect exercise sessions
  - User spends Steps/Gems/Power Stones in Workshop, Labs, Store, Cards, Ultimate Weapon
  - User enters battles and receives post-round rewards
  - User claims missions, milestones, weekly rewards, and supply drops
  - User adjusts settings around notifications/sound

- **Important modules/systems reviewed**
  - Step ingestion and sync: `service/StepCounterService.kt`, `service/StepSyncWorker.kt`, `data/sensor/DailyStepManager.kt`, `data/repository/StepRepositoryImpl.kt`
  - Health Connect pipeline: `data/healthconnect/*`
  - Navigation and battle UX: `presentation/MainActivity.kt`, `presentation/battle/*`
  - Store / billing / cosmetics: `presentation/store/*`, `data/billing/StubBillingManager.kt`
  - Labs, Cards, Workshop action handlers and state management
  - Settings/notifications
  - Room database configuration and schema persistence behavior
  - Tests and README/onboarding docs

- **Any parts that were unclear or insufficiently documented**
  - I could not validate runtime behavior through a full Gradle test/build run because the Gradle wrapper points to `gradle-9.3.1-bin.zip` on `services.gradle.org`, and the container environment does not have network access.  
  - README documents a `run-gradle.sh` helper for non-TTY environments, but that script is not committed; it is described as gitignored and must be recreated manually. That adds friction for reproducible CI/CLI validation.

## 3. High-Priority Findings

### Finding 1: Exercise-session crediting appears non-idempotent and can likely re-credit the same sessions
- **Category:** Bug
- **Severity:** Critical
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepSyncWorker.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/healthconnect/ActivityMinuteConverter.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/repository/StepRepositoryImpl.kt`
- **Why this matters**  
  This is the most important production risk in the repo. If the same Health Connect sessions are processed more than once in a day, users may be awarded duplicate Steps and progression. In a progression/economy game, that undermines balance, stats integrity, and trust.
- **Evidence from the repo**  
  `StepSyncWorker` fetches **all sessions for today**, validates them, converts them, and then calls `dailyStepManager.recordActivityMinutes(...)`. `recordActivityMinutes(...)` credits the passed value directly and persists it through `stepRepository.updateActivityMinutes(...)`. `StepRepositoryImpl.updateActivityMinutes(...)` stores `activityMinutes` and `stepEquivalents`, but there is no obvious persisted marker indicating which sessions were already processed or what delta has already been applied.
- **How it could fail in practice**  
  WorkManager can run repeatedly on the same date. If the user has the same exercise sessions in Health Connect and the worker processes the full-day set again, the same step-equivalent total can plausibly be re-applied.
- **Recommended fix**  
  Make exercise import **delta-based and idempotent**. Persist a stable import watermark or processed session IDs/hash set, or store cumulative raw activity-minute totals and only credit the delta since the last successful import. Add targeted tests that run the worker twice against the same session set and assert no double-credit occurs.

### Finding 2: Activity-minute crediting bypasses the normal progression/reward pipeline, causing inconsistent state and UX
- **Category:** Reliability Risk
- **Severity:** High
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/repository/StepRepositoryImpl.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/stats/StatsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/missions/MissionsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/economy/CurrencyDashboardViewModel.kt`
- **Why this matters**  
  Users may earn Steps from activity minutes and see their balance change, but not get the same secondary effects as normal steps: mission progress, widgets/notifications, drop generation, or consistent stats. That creates “the game gave me currency but the rest of the app didn’t catch up” behavior.
- **Evidence from the repo**  
  `DailyStepManager.recordSteps(...)` performs a broader set of follow-on behavior, including economy updates, mission updates, and related side effects. By contrast, `recordActivityMinutes(...)` increments `dailyCreditedTotal`, calls `stepRepository.updateActivityMinutes(...)`, and `playerRepository.addSteps(credited)`, but does not mirror the same reward/progression path. `StepRepositoryImpl.updateActivityMinutes(...)` also writes `stepEquivalents` without updating `creditedSteps`.
- **How it could fail in practice**  
  - Balance increases but Home/stats still show a lower “today steps” number.
  - Walking missions lag behind exercise-credit earnings.
  - Supply drop generation and weekly/daily reward logic do not behave consistently.
  - Users perceive step counting as unreliable or unfair.
- **Recommended fix**  
  Consolidate all crediting through a single canonical method that updates daily credited totals, mission progression, milestones/rewards, drop generation, stats, widgets, and notifications consistently. If exercise-derived steps are intentionally distinct, expose that distinction clearly in the UI and data model instead of partially merging them.

### Finding 3: Several ViewModels create new hot Flow collectors during button actions by calling `stateIn(viewModelScope).value`
- **Category:** Likely Bug
- **Severity:** High
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/workshop/WorkshopViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/cards/CardsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/labs/LabsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/store/StoreViewModel.kt`
- **Why this matters**  
  This is a subtle but important state-management smell. Using `observeX().stateIn(viewModelScope).value` inside click handlers creates a new hot `StateFlow` tied to the ViewModel scope rather than doing a one-time read. Repeated user interactions can accumulate unnecessary collectors and create stale or misleading reads.
- **Evidence from the repo**  
  This pattern appears in multiple action methods such as workshop purchases, card upgrades, lab actions, and cosmetic purchasing.
- **How it could fail in practice**  
  - Higher memory/work over time from needless collectors
  - Actions using stale values if the flow has not emitted as expected
  - More difficult debugging of racey UI state
- **Recommended fix**  
  Replace these one-off reads with `first()` / `firstOrNull()` for repository flows, or derive from the already-held `uiState` where appropriate. Avoid creating new hot flows in event handlers.

### Finding 4: “Return to Workshop” does not guarantee a return to the Workshop screen
- **Category:** UX Issue
- **Severity:** High
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/ui/PostRoundOverlay.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/MainActivity.kt`
- **Why this matters**  
  Navigation labels must be exact. If a button says “Return to Workshop,” users expect deterministic navigation to Workshop.
- **Evidence from the repo**  
  The battle overlay button text says `Return to Workshop`, but `MainActivity` wires battle exit to `navController.popBackStack()`. That returns to the previous screen, not necessarily Workshop.
- **How it could fail in practice**  
  If battle was opened from Home, a notification action, or some future alternate route, the button label becomes false and disorienting.
- **Recommended fix**  
  Either rename the button to something accurate like `Back` / `Return`, or navigate explicitly to `Screen.Workshop.route` with a clear back-stack policy.

### Finding 5: “Step Count Updates” toggle does not actually stop the foreground notification from existing
- **Category:** Documentation Mismatch
- **Severity:** High
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepCounterService.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepNotificationManager.kt`
- **Why this matters**  
  This is a trust issue. The setting text implies the user can stop the visible ongoing notification, but the service still starts foreground mode with a notification regardless.
- **Evidence from the repo**  
  The settings screen describes the toggle as `Show step count and balance in the notification`. However, `StepCounterService.onCreate()` always calls `startForeground(...)` with a built notification. `StepNotificationManager.updateNotification(...)` respects the preference only when refreshing content, not when deciding whether the foreground notification exists at all.
- **How it could fail in practice**  
  The user disables the setting and still sees an ongoing notification. From their perspective, the setting is broken.
- **Recommended fix**  
  Align the setting with actual behavior. Either:
  - make the toggle control the notification content style only and rename it accordingly, or
  - stop using a persistent foreground notification for this feature path, which may not be feasible on Android, or
  - present a separate explanation that foreground step tracking requires a service notification.

### Finding 6: Room is configured with destructive migration fallback
- **Category:** Reliability Risk
- **Severity:** High
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/di/DatabaseModule.kt`
- **Why this matters**  
  This app stores progression, economy, missions, cosmetics, battle state, and tracking history. Destructive migration means a schema change can wipe user progress.
- **Evidence from the repo**  
  `DatabaseModule` uses `.fallbackToDestructiveMigration()`.
- **How it could fail in practice**  
  A release with a schema bump can silently reset a player’s account state, damaging retention and trust.
- **Recommended fix**  
  Define explicit Room migrations before production use. If destructive migration is temporarily unavoidable for dev builds, isolate it to debug-only configuration.

### Finding 7: Labs “free rush” can silently no-op with no user feedback
- **Category:** UX Issue
- **Severity:** Medium
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/labs/LabsViewModel.kt`
- **Why this matters**  
  Silent failure is one of the fastest ways to make a feature feel broken.
- **Evidence from the repo**  
  `freeRush(...)` returns early if the season pass is inactive/expired, the daily free rush is already used, or there is no active research matching the requested type. These branches return without setting `_userMessage`.
- **How it could fail in practice**  
  The user taps the action and nothing visible happens; they have no idea whether they misread the rules, tapped the wrong item, or hit a bug.
- **Recommended fix**  
  Return explicit messages for each blocked path: `Season Pass required`, `Free rush already used today`, `No active research to rush`, etc.

### Finding 8: Milestone claims are protected mostly by UI flow, not by domain validation
- **Category:** Validation Gap
- **Severity:** Medium
- **Confidence:** High
- **Affected files:**
  - `app/src/main/java/com/whitefang/stepsofbabylon/domain/usecase/ClaimMilestone.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/missions/MissionsViewModel.kt`
- **Why this matters**  
  Critical reward paths should be enforced in domain logic, not only via UI assumptions.
- **Evidence from the repo**  
  `ClaimMilestone` checks whether a milestone was already claimed, then awards rewards and marks it claimed. It does not verify that the required steps threshold has actually been reached.
- **How it could fail in practice**  
  Any future code path, bug, dev tool, or alternate UI route that invokes the use case too early could award milestone rewards incorrectly.
- **Recommended fix**  
  Make `ClaimMilestone` validate the player’s current step total against `milestone.requiredSteps` before awarding anything.

## 4. Full Findings List

### 4.1 Bugs and Logic Risks
- **Exercise-session crediting can be applied more than once**  
  **Severity:** Critical  
  **File(s):** `service/StepSyncWorker.kt`, `data/sensor/DailyStepManager.kt`, `data/repository/StepRepositoryImpl.kt`, `data/healthconnect/ActivityMinuteConverter.kt`  
  **Explanation:** Today’s full session set is reprocessed without a durable processed-session/delta guard.  
  **Suggested fix:** Persist import checkpoints or processed session IDs and only apply deltas.

- **Exercise-derived steps are not persisted as canonical credited daily steps**  
  **Severity:** High  
  **File(s):** `data/sensor/DailyStepManager.kt`, `data/repository/StepRepositoryImpl.kt`  
  **Explanation:** `updateActivityMinutes(...)` writes `stepEquivalents` but not `creditedSteps`, so different UI and progression systems can disagree about “today’s credited movement.”  
  **Suggested fix:** Unify on one source of truth for credited steps.

- **Action handlers create new hot flows via `stateIn(viewModelScope).value`**  
  **Severity:** High  
  **File(s):** `presentation/workshop/WorkshopViewModel.kt`, `presentation/cards/CardsViewModel.kt`, `presentation/labs/LabsViewModel.kt`, `presentation/store/StoreViewModel.kt`  
  **Explanation:** This is likely to create unnecessary collectors and fragile action-time state reads.  
  **Suggested fix:** Replace with `first()` or use existing `uiState` snapshots.

- **Battle exit label does not match actual navigation behavior**  
  **Severity:** High  
  **File(s):** `presentation/battle/ui/PostRoundOverlay.kt`, `presentation/MainActivity.kt`  
  **Explanation:** “Return to Workshop” uses `popBackStack()` rather than explicit Workshop navigation.  
  **Suggested fix:** Rename or navigate explicitly.

- **Milestone reward path lacks server/domain-style eligibility validation**  
  **Severity:** Medium  
  **File(s):** `domain/usecase/ClaimMilestone.kt`  
  **Explanation:** Use case only blocks duplicate claims, not premature ones.  
  **Suggested fix:** Check the user’s actual progress before awarding.

- **Milestone achieved notification may repeat on app re-entry until the user claims it**  
  **Severity:** Medium  
  **File(s):** `presentation/home/HomeViewModel.kt`, `domain/usecase/CheckMilestones.kt`  
  **Explanation:** Home init notifies the first achievable unclaimed milestone each time, with no “already notified” tracking.  
  **Suggested fix:** Persist a per-milestone notification state or throttle repeated notifications.

- **Currency dashboard appears snapshot-based rather than truly reactive**  
  **Severity:** Medium  
  **File(s):** `presentation/economy/CurrencyDashboardViewModel.kt`  
  **Explanation:** The view model reads one-shot values rather than building from live flows, so the screen can become stale during use.  
  **Suggested fix:** Convert to a combined reactive state flow.

### 4.2 Error Handling and Resilience Gaps
- **Health Connect worker swallows broad exceptions with no observability**  
  **Severity:** High  
  **File(s):** `service/StepSyncWorker.kt`  
  **Explanation:** Best-effort handling is reasonable, but a full silent catch makes diagnosis difficult when credits do not appear.  
  **Suggested fix:** Log structured error events and track skipped imports/retries.

- **Foreground step flow catches exceptions into fallback values instead of surfacing degraded state**  
  **Severity:** Medium  
  **File(s):** `service/StepCounterService.kt`  
  **Explanation:** Failures in balance reads collapse to zero for notification purposes, which can mislead users without signaling trouble.  
  **Suggested fix:** Log failures and consider displaying a degraded-state message instead of silently showing `0`.

- **Multiple feature paths use silent early returns instead of user-visible outcomes**  
  **Severity:** Medium  
  **File(s):** `presentation/labs/LabsViewModel.kt`, likely other action ViewModels  
  **Explanation:** Silent no-op behavior makes legitimate restrictions look like bugs.  
  **Suggested fix:** Standardize on explicit action results and user messaging.

- **Boot/service recovery behavior is difficult to audit from the UI**  
  **Severity:** Medium  
  **File(s):** `service/BootReceiver.kt`, `service/StepCounterService.kt`, `service/StepSyncWorker.kt`  
  **Explanation:** There is little visible indication to the user when tracking has stopped, fallen back to worker mode, or recovered.  
  **Suggested fix:** Add a tracking-status surface in Settings/Home.

### 4.3 Validation and Edge Case Gaps
- **Settings text overpromises control over the ongoing foreground notification**  
  **Severity:** High  
  **File(s):** `presentation/settings/NotificationSettingsScreen.kt`, `service/StepCounterService.kt`, `service/StepNotificationManager.kt`  
  **Explanation:** The toggle wording does not match Android foreground-service reality.  
  **Suggested fix:** Rename and explain constraints.

- **Store allows cosmetic purchase/equip while same screen says visual application is “coming soon”**  
  **Severity:** Medium  
  **File(s):** `presentation/store/StoreScreen.kt`, `presentation/store/StoreViewModel.kt`  
  **Explanation:** The user can spend premium currency and equip items without seeing the promised visual effect.  
  **Suggested fix:** Gate purchases behind actual implementation, or clearly state they are collectible placeholders only.

- **Action guards are often UI-local rather than domain-enforced**  
  **Severity:** Medium  
  **File(s):** `domain/usecase/ClaimMilestone.kt`, other purchase/upgrade flows  
  **Explanation:** Several restrictions appear to rely on current UI routing/visibility assumptions.  
  **Suggested fix:** Put invariant checks inside domain use cases and repositories.

- **No instrumented/UI test layer for permission, lifecycle, service, and navigation edge cases**  
  **Severity:** Medium  
  **File(s):** `README.md`, `app/src/androidTest` (absent)  
  **Explanation:** The riskiest behaviors are Android-specific and not well-covered by JVM tests.  
  **Suggested fix:** Add instrumented tests for permissions, service lifecycle, notifications, and navigation.

### 4.4 UX and Usability Problems
- **“Return to Workshop” is misleading**  
  **Severity:** High  
  **File(s):** `presentation/battle/ui/PostRoundOverlay.kt`, `presentation/MainActivity.kt`  
  **Explanation:** Button label implies a deterministic destination that is not guaranteed.  
  **Suggested fix:** Rename or change navigation behavior.

- **Notification toggle likely feels broken to users**  
  **Severity:** High  
  **File(s):** `presentation/settings/NotificationSettingsScreen.kt`, `service/StepCounterService.kt`  
  **Explanation:** Users can disable “Step Count Updates” and still see a service notification.  
  **Suggested fix:** Align wording and behavior.

- **Free lab rush can fail silently**  
  **Severity:** Medium  
  **File(s):** `presentation/labs/LabsViewModel.kt`  
  **Explanation:** No user-facing reason is shown when the action is blocked.  
  **Suggested fix:** Always surface a reason.

- **Cosmetics flow undermines trust**  
  **Severity:** Medium  
  **File(s):** `presentation/store/StoreScreen.kt`  
  **Explanation:** The screen says visual application is not ready, yet still invites purchase/equip actions.  
  **Suggested fix:** Disable purchase/equip or show a very explicit placeholder explanation.

- **Likely inconsistent “today steps” messaging across screens**  
  **Severity:** Medium  
  **File(s):** `presentation/home/HomeViewModel.kt`, `presentation/stats/StatsViewModel.kt`, `presentation/economy/CurrencyDashboardViewModel.kt`, `data/sensor/DailyStepManager.kt`  
  **Explanation:** Sensor-step and activity-minute crediting are not obviously represented the same way across all screens.  
  **Suggested fix:** Define one user-facing metric model and use it consistently.

- **User feedback for background-sync/tracking health is weak**  
  **Severity:** Medium  
  **File(s):** step service/worker/settings/home flows  
  **Explanation:** Users may not know whether steps are actively tracking, delayed, denied by permission, or partially synced.  
  **Suggested fix:** Expose clear status, last-sync time, and remediation guidance.

### 4.5 Accessibility Concerns
- **Reliance on color and emoji-style affordances for status/reward communication**  
  **Severity:** Medium  
  **File(s):** multiple Compose screens, including battle/store/economy screens  
  **Explanation:** Several status indicators rely heavily on color or decorative emoji/glyph cues.  
  **Suggested fix:** Pair color with explicit text/state labels and verify contrast.

- **No evidence of accessibility-focused UI test coverage**  
  **Severity:** Medium  
  **File(s):** `app/src/androidTest` absent  
  **Explanation:** Compose screens may be semantically reasonable in places, but the repo lacks evidence of regression protection for screen-reader or focus behavior.  
  **Suggested fix:** Add Compose UI tests for semantics, labels, and navigation focus.

- **Potentially misleading action text without contextual explanation**  
  **Severity:** Low  
  **File(s):** `presentation/settings/NotificationSettingsScreen.kt`, `presentation/store/StoreScreen.kt`  
  **Explanation:** Ambiguous wording harms comprehension for all users, and especially those relying on concise literal labels.  
  **Suggested fix:** Rewrite labels to be behaviorally exact.

### 4.6 Documentation and Onboarding Issues
- **README requires a helper script that is not actually committed**  
  **Severity:** Medium  
  **File(s):** `README.md`  
  **Explanation:** The repo documents `./run-gradle.sh` for non-TTY environments but expects the reader to recreate it manually. That is onboarding friction and a reproducibility problem.  
  **Suggested fix:** Commit the helper or remove the instruction.

- **Build/test validation is harder than README implies in isolated environments**  
  **Severity:** Medium  
  **File(s):** `README.md`, `gradle/wrapper/gradle-wrapper.properties`  
  **Explanation:** The wrapper requires fetching Gradle 9.3.1; in restricted environments, tests cannot be executed without prior caching.  
  **Suggested fix:** Document offline expectations more clearly or provide a dev-container/CI bootstrap path.

- **Instrumented tests are called out as “planned,” but core Android-specific flows already depend on them**  
  **Severity:** Medium  
  **File(s):** `README.md`  
  **Explanation:** For this kind of app, service lifecycle, permissions, Health Connect, and navigation behavior are not secondary concerns.  
  **Suggested fix:** Treat Android/instrumented coverage as part of the baseline, not a future enhancement.

### 4.7 Test Coverage Gaps
- **No tests found for `recordActivityMinutes(...)` risk path**  
  **Severity:** High  
  **File(s):** `app/src/test/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManagerTest.kt`  
  **Explanation:** Existing tests exercise `recordSteps(...)`, but the riskiest exercise-session crediting path is not meaningfully protected.  
  **Suggested fix:** Add tests for duplicate worker runs, daily resets, ceilings, mission updates, and UI-visible totals.

- **No test proving activity-minute import is idempotent**  
  **Severity:** High  
  **File(s):** Health Connect / worker / repository tests  
  **Explanation:** This is the highest-risk logic path and should have a dedicated regression test.  
  **Suggested fix:** Add a worker-level test that processes identical session sets multiple times.

- **No UI/instrumented tests for navigation text vs actual destination**  
  **Severity:** Medium  
  **File(s):** `app/src/androidTest` absent  
  **Explanation:** The `Return to Workshop` mismatch would be easy to catch with a navigation test.  
  **Suggested fix:** Add a simple Compose navigation assertion.

- **No instrumentation around notification settings and service behavior**  
  **Severity:** Medium  
  **File(s):** `app/src/androidTest` absent  
  **Explanation:** The current setting/foreground-service mismatch is exactly the kind of issue unit tests will miss.  
  **Suggested fix:** Add device/emulator tests around settings changes and notification visibility.

## 5. User Journey Review

### Journey A: First launch → Home screen → start progressing
- **Likely experience:** Reasonable initial setup. `HomeViewModel` ensures the player profile, upgrades, research, daily login, and daily missions exist.
- **Where confusion can occur:**  
  The app has several intertwined progression systems, but there is limited evidence of a clear “tracking health/status” surface. Users may not understand whether steps are currently coming from the live sensor service, worker catch-up, or Health Connect.
- **Concrete implementation tie-in:**  
  The code performs a lot of initialization automatically in `HomeViewModel`, but the user-facing state appears more focused on balances and counts than on system readiness.

### Journey B: Walking/exercising → expecting steps and rewards to appear
- **Likely experience:** This is the highest-risk journey in the app.
- **Potential friction / failure points:**
  - Exercise sessions may be reprocessed.
  - Exercise-derived steps may not update the same surfaces as live sensor steps.
  - A user can see balance/progression mismatches between Home, Stats, Missions, and economy screens.
  - Silent exception handling in the worker makes problems hard to diagnose.
- **Concrete implementation tie-in:**  
  `StepSyncWorker` and `DailyStepManager.recordActivityMinutes(...)` appear to operate differently from `recordSteps(...)`, and `StepRepositoryImpl.updateActivityMinutes(...)` does not maintain `creditedSteps` as the canonical daily total.

### Journey C: Open battle → finish round → leave battle
- **Likely experience:** The post-round overlay gives the user clear next-step buttons.
- **Where it breaks UX expectations:**  
  The `Return to Workshop` label is too specific for a generic `popBackStack()` implementation.
- **Concrete implementation tie-in:**  
  `PostRoundOverlay` text and `MainActivity` navigation behavior do not line up.

### Journey D: Settings → disable step notification
- **Likely experience:** The user believes they are disabling the visible step-count notification.
- **Where trust breaks:**  
  The foreground service still needs a notification, so the visible result is not likely to match the label.
- **Concrete implementation tie-in:**  
  `NotificationSettingsScreen` wording is stronger than what `NotificationSettingsViewModel` and `StepNotificationManager` actually control.

### Journey E: Store → buy/equip cosmetics
- **Likely experience:** The user sees a monetized cosmetic system.
- **Where trust breaks:**  
  The same screen says `Visual application coming soon`, which implies the cosmetic effect is not implemented even though purchase/equip is available.
- **Concrete implementation tie-in:**  
  `StoreScreen` renders active Buy/Equip buttons directly under a message that the visual feature is not ready.

### Journey F: Labs → use free rush
- **Likely experience:** The user taps a premium-perk action expecting instant feedback.
- **Where they hit a dead end:**  
  Several blocked branches simply return without feedback.
- **Concrete implementation tie-in:**  
  `LabsViewModel.freeRush(...)` has multiple silent exits.

## 6. Most Likely Real-World Failures
- **Trigger:** Health Connect worker runs multiple times on the same day with unchanged sessions  
  **Likely symptom:** User receives duplicate Steps / inflated progression  
  **Root cause in code:** No durable idempotency/delta guard in the exercise-session import path  
  **Suggested mitigation:** Persist processed-session watermark/IDs and test repeated worker runs.

- **Trigger:** User earns movement credit through exercise sessions rather than raw sensor deltas  
  **Likely symptom:** Balance changes but mission/stats/home numbers disagree  
  **Root cause in code:** `recordActivityMinutes(...)` does not appear to feed the same canonical daily-credit pipeline as `recordSteps(...)`  
  **Suggested mitigation:** Unify crediting paths and reconcile the data model.

- **Trigger:** Heavy repeated use of upgrade/purchase/research actions during a session  
  **Likely symptom:** Increasingly fragile or stale action-time state reads, possible unnecessary flow retention  
  **Root cause in code:** Repeated `stateIn(viewModelScope).value` inside event handlers  
  **Suggested mitigation:** Replace with one-shot reads (`first()`) or existing UI state.

- **Trigger:** User disables “Step Count Updates” expecting the persistent notification to disappear  
  **Likely symptom:** User concludes the settings screen is broken  
  **Root cause in code:** Foreground service still starts with a notification; setting only affects update behavior  
  **Suggested mitigation:** Rewrite the setting and explain Android limitations.

- **Trigger:** App update changes Room schema  
  **Likely symptom:** Player loses progress/data  
  **Root cause in code:** `.fallbackToDestructiveMigration()`  
  **Suggested mitigation:** Ship explicit migrations and keep destructive fallback out of production.

- **Trigger:** User taps free lab rush while ineligible or with no matching active research  
  **Likely symptom:** Nothing happens; user assumes bug  
  **Root cause in code:** Silent early returns in `LabsViewModel.freeRush(...)`  
  **Suggested mitigation:** Always emit a user-visible result message.

- **Trigger:** Battle opened from a non-Workshop route  
  **Likely symptom:** “Return to Workshop” returns somewhere else  
  **Root cause in code:** Label/behavior mismatch between overlay and `popBackStack()`  
  **Suggested mitigation:** Rename or navigate explicitly to Workshop.

## 7. Quick Wins
- Replace all `stateIn(viewModelScope).value` reads in action handlers with `first()` or `uiState` snapshots.
- Rename the battle exit button to match actual behavior, or explicitly navigate to Workshop.
- Add explicit user messages for all blocked `freeRush(...)` outcomes.
- Rewrite the “Step Count Updates” setting label to accurately describe what is and is not controlled.
- Disable cosmetic purchase/equip until visible application exists, or clearly mark cosmetics as placeholder unlocks.
- Add structured logging around worker import failures instead of fully silent catches.

## 8. Strategic Improvements
- Build a **single movement-credit pipeline** that all sources use: sensor deltas, worker catch-up, Health Connect steps, and activity-minute conversions.
- Introduce a **tracking health/status model** for the UI: permissions granted, service running, last sensor update, last worker sync, Health Connect status, and last import result.
- Harden domain invariants so reward-granting paths validate eligibility independently of the UI.
- Replace snapshot-heavy screens with reactive state models where the user expects live updates.
- Add Android instrumentation coverage for service lifecycle, navigation, notifications, permissions, and Health Connect–adjacent flows.

## 9. Suggested Remediation Plan
- **Phase 1: urgent fixes**
  - Make activity-minute import idempotent.
  - Unify exercise crediting with canonical daily credited step/progression logic.
  - Remove `stateIn(viewModelScope).value` from action handlers.
  - Fix misleading navigation/setting labels.
  - Add logs and diagnostics for worker/sync failures.

- **Phase 2: stability and UX improvements**
  - Add domain-level validation to milestone and similar reward paths.
  - Add user-facing tracking health/status surfaces.
  - Standardize error/blocked-action feedback across all screens.
  - Rework stale/snapshot screens to use live combined state.

- **Phase 3: polish and maintainability**
  - Replace destructive migration with explicit Room migrations.
  - Add Android/instrumented tests for navigation, notifications, permissions, and service behavior.
  - Clean up README/onboarding friction by committing helper scripts or documenting an official CI/bootstrap path.
  - Review monetization/cosmetics UX to ensure every purchasable item has a visible, testable outcome.

## 10. Appendix
- **Notable files reviewed**
  - `README.md`
  - `app/build.gradle.kts`
  - `gradle/wrapper/gradle-wrapper.properties`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/MainActivity.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/home/HomeViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/battle/ui/PostRoundOverlay.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/store/StoreScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/store/StoreViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/labs/LabsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/workshop/WorkshopViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/cards/CardsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/economy/CurrencyDashboardViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsScreen.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/presentation/settings/NotificationSettingsViewModel.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/NotificationPreferences.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepCounterService.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepNotificationManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/service/StepSyncWorker.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/di/DatabaseModule.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/repository/StepRepositoryImpl.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/domain/usecase/ClaimMilestone.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/billing/StubBillingManager.kt`
  - `app/src/main/java/com/whitefang/stepsofbabylon/data/ads/StubRewardAdManager.kt`
  - `app/src/test/java/com/whitefang/stepsofbabylon/data/sensor/DailyStepManagerTest.kt`

- **Assumptions made**
  - I treated the uploaded ZIP as the authoritative project state.
  - I inferred intended UX from labels, screen structure, repository naming, and use case boundaries.
  - Where runtime execution was not possible, I based findings on static analysis of control flow, state flow usage, and persistence behavior.

- **Areas of uncertainty**
  - I could not fully validate some runtime-only Android behaviors (service lifecycle, Health Connect permission behavior, exact notification behavior on device, navigation edge cases under every entry path) because there are no instrumented tests in the repo and I could not run the app here.
  - Some risks are labeled “likely” where the code strongly suggests a defect but runtime confirmation would still be useful.

- **Anything that should be manually tested by a human**
  - Repeated worker execution against the same Health Connect session data
  - Cross-screen consistency after exercise-session crediting
  - Persistent notification behavior after toggling notification settings
  - Battle exit behavior when battle is entered from Home, Workshop, notifications, and deep-link style entry points
  - Cosmetic purchase/equip behavior and whether any visible effect exists
  - Lab free-rush blocked cases and user feedback quality
  - Upgrade compatibility across a Room schema change

## Top 10 Fixes
1. Make Health Connect exercise import idempotent and delta-based.
2. Unify all movement crediting into one canonical progression pipeline.
3. Replace `stateIn(viewModelScope).value` action-time reads with one-shot reads or `uiState` snapshots.
4. Remove `.fallbackToDestructiveMigration()` from production database configuration.
5. Fix the “Step Count Updates” setting so wording matches actual notification behavior.
6. Fix the battle exit UX by aligning button text with navigation behavior.
7. Add explicit user feedback for all blocked/silent action paths, starting with Labs free rush.
8. Enforce milestone eligibility in domain logic, not only in the UI path.
9. Gate or clearly redefine cosmetics until visible application is implemented.
10. Add instrumentation tests for step tracking, notifications, navigation, and permission-driven flows.
