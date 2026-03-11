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

## 2026-03-04 — Plan 08: Battle Renderer — Game Loop & Ziggurat
- Goal: Build custom SurfaceView battle renderer with game loop, ziggurat entity, projectiles, health bar, and Compose overlay.
- Decisions made:
  - (b) ZigguratBaseStats as domain/model object — proper constants for Plan 10's ResolveStats to consume.
  - (a) Simple geometric ziggurat — 5 stacked rectangles in sandstone tones.
  - (a) Hidden bottom nav during battle — full-screen immersive.
- Changes made:
  - Created `domain/model/ZigguratBaseStats.kt` — base stat constants (HP, damage, attack speed, range, regen, knockback, projectile speed)
  - Created `presentation/battle/engine/Entity.kt` — abstract base class (x, y, width, height, isAlive, update, render)
  - Created `presentation/battle/engine/GameEngine.kt` — entity list, update/render dispatch, HealthBarRenderer integration
  - Created `presentation/battle/entities/ZigguratEntity.kt` — 5-layer ziggurat, auto-fire via callback, HP tracking
  - Created `presentation/battle/entities/ProjectileEntity.kt` — moves toward target, self-destructs on arrival
  - Created `presentation/battle/ui/HealthBarRenderer.kt` — green/yellow/red HP bar with numeric text
  - Created `presentation/battle/GameLoopThread.kt` — fixed timestep (60 UPS), accumulator pattern, speed multiplier, FPS counter
  - Created `presentation/battle/GameSurfaceView.kt` — SurfaceHolder.Callback, manages game loop thread lifecycle
  - Created `presentation/battle/BattleUiState.kt` — UI state for Compose overlay
  - Created `presentation/battle/BattleViewModel.kt` — @HiltViewModel, loads tier, exposes state + BattleEvent
  - Created `presentation/battle/BattleScreen.kt` — Compose wrapper (AndroidView + overlay: wave counter, speed controls, pause, exit)
  - Updated `presentation/MainActivity.kt` — BattleScreen replaces placeholder, bottom nav hidden on Battle route
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers:
  - Ziggurat fires at fixed test target (top-center) — Plan 09 replaces with nearest enemy
  - Workshop bonuses not applied to base stats yet — Plan 10 adds ResolveStats
- Follow-ups created: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-05 — Plan 09: Battle System — Enemies & Waves
- Goal: Add 6 enemy types, wave spawning, enemy scaling, collision, cash, nearest-enemy targeting, round end.
- Decisions made:
  - (b) Enemies spawn from top + left + right edges (converging on ziggurat)
  - (b) Fix EnemyType enum to match battle-formulas.md (FAST dmg 0.5→0.7, RANGED spd 1.0→0.8 + dmg 1.5→1.2, BOSS hp 10→20)
  - (b) Wave scaling: 1.05^wave (gentler curve, tunable in Plan 28)
- Changes made:
  - Updated `domain/model/EnemyType.kt` — corrected multipliers to match balance spec
  - Created `presentation/battle/engine/EnemyScaler.kt` — wave-based stat scaling (1.05^wave), cash rewards per type
  - Created `presentation/battle/entities/EnemyEntity.kt` — 6 types, movement, melee/ranged attack, distinct shapes/colors, mini HP bar
  - Created `presentation/battle/entities/EnemyProjectileEntity.kt` — red projectiles for Ranged enemies
  - Created `presentation/battle/engine/WaveSpawner.kt` — 26s spawn + 9s cooldown, enemy composition by wave, boss every 10 waves
  - Created `presentation/battle/engine/CollisionSystem.kt` — projectile↔enemy and enemy projectile↔ziggurat collision
  - Updated `presentation/battle/engine/GameEngine.kt` — integrated WaveSpawner, CollisionSystem, cash tracking, Scatter splitting, round end detection, findNearestEnemy()
  - Updated `presentation/battle/entities/ZigguratEntity.kt` — targets nearest enemy via lambda, only fires when enemy in range
  - Updated `presentation/battle/BattleUiState.kt` — added enemyCount, wavePhase
  - Updated `presentation/battle/BattleViewModel.kt` — polls engine state every 200ms, detects roundOver
  - Updated `presentation/battle/BattleScreen.kt` — shows enemy count, wave phase, cash in overlay
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers:
  - Cash economy simplified (base per type) — Plan 11 adds full formula
  - Workshop bonuses not applied to stats — Plan 10 adds ResolveStats
- Follow-ups created: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-05 — Plan 10: Battle System — Stats & Combat
- Goal: Stats resolution engine + core combat mechanics (crit, knockback, lifesteal, thorn, regen, death defy, defense).
- Decisions made:
  - (b) Core stats + simple mechanics now; Orbs/Multishot/Bounce deferred
  - (a) GameEngine accepts ResolvedStats in init() — ViewModel resolves on round start
  - (a) Centralized applyDamageToZiggurat() for all damage sources
- Changes made:
  - Created `domain/model/ResolvedStats.kt` — all computed combat stats data class
  - Created `domain/usecase/ResolveStats.kt` — workshop + in-round levels → ResolvedStats
  - Created `domain/usecase/CalculateDamage.kt` — raw damage + crit roll + damage/meter bonus
  - Created `domain/usecase/CalculateDefense.kt` — damage reduction (cap 75%) + flat block
  - Updated `presentation/battle/entities/ZigguratEntity.kt` — uses ResolvedStats for HP, attack speed, range, health regen
  - Updated `presentation/battle/entities/EnemyEntity.kt` — added applyKnockback()
  - Updated `presentation/battle/engine/CollisionSystem.kt` — delegates to engine callbacks
  - Updated `presentation/battle/engine/GameEngine.kt` — centralized damage pipeline (defense → death defy → thorn), knockback, lifesteal
  - Updated `presentation/battle/GameSurfaceView.kt` — accepts ResolvedStats, re-inits engine
  - Updated `presentation/battle/BattleViewModel.kt` — resolves stats from workshop on init
  - Updated `presentation/battle/BattleScreen.kt` — passes resolved stats to surface view
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers:
  - Orbs, Multishot, Bounce Shot computed in ResolvedStats but not wired to gameplay
  - In-round upgrades (Plan 11) will re-resolve stats on purchase
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 11: In-Round Upgrades & Cash Economy
- Goal: Full cash economy + in-round upgrade menu with purchase flow.
- Decisions made:
  - (b) Cash economy + upgrade menu only; Orbs/Multishot/Bounce deferred to mini-plan 10b
  - (a) Upgrade menu always accessible via toggle button
  - (a) onWaveComplete callback added to WaveSpawner
- Changes made:
  - Updated `presentation/battle/engine/WaveSpawner.kt` — added onWaveComplete callback, fires on SPAWNING→COOLDOWN
  - Updated `presentation/battle/engine/GameEngine.kt` — full cash formula (tier × cashBonus), wave cash + interest, spendCash(), updateZigguratStats()
  - Updated `presentation/battle/BattleUiState.kt` — added showUpgradeMenu, inRoundLevels, lastPurchaseFree
  - Updated `presentation/battle/BattleViewModel.kt` — purchase flow, in-round levels, re-resolve stats, free upgrade chance, tier tracking
  - Updated `presentation/battle/GameSurfaceView.kt` — configure() accepts stats + tier + workshopLevels
  - Created `presentation/battle/ui/InRoundUpgradeMenu.kt` — 3-tab Compose overlay, upgrade list, purchase buttons
  - Updated `presentation/battle/BattleScreen.kt` — upgrade toggle button, InRoundUpgradeMenu overlay
  - Created `docs/plans/plan-10b-advanced-combat.md` — mini-plan for Orbs, Multishot, Bounce Shot
  - Updated `docs/plans/plan-11-in-round-upgrades.md` — removed deferred section
  - Updated `docs/plans/master-plan.md` — added Plan 10b entry
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers:
  - Orbs/Multishot/Bounce in Plan 10b (ready to implement anytime)
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 10b: Advanced Combat (Orbs, Multishot, Bounce Shot)
- Goal: Wire the three deferred combat mechanics to gameplay.
- Decisions made:
  - (a) Orbs: damage on contact with 0.5s per-enemy cooldown, 50% resolved damage
  - (a) Bounce: spawn new ProjectileEntity with bouncesRemaining, reuse collision pipeline
  - (a) Multishot: findNearestEnemies(n) lambda, fire one projectile per target
- Changes made:
  - Updated `presentation/battle/entities/ProjectileEntity.kt` — added bouncesRemaining + hitEnemies
  - Created `presentation/battle/entities/OrbEntity.kt` — orbiting entity, per-enemy cooldown, cyan rendering
  - Updated `presentation/battle/entities/ZigguratEntity.kt` — multishot via findNearestEnemies(n) lambda
  - Updated `presentation/battle/engine/GameEngine.kt` — findNearestEnemies(), bounce logic in onProjectileHitEnemy, orb spawn/despawn, onOrbHitEnemy
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Documentation Sweep
- Goal: Full project documentation audit — find and fix stale/incorrect references.
- Changes made:
  - Updated `docs/StepsOfBabylon_GDD.md` — replaced all Google Fit references with Health Connect (§2.1, §11.1–§11.4, §15.1, §17, §19). Fixed anti-cheat rate limit from ">500 steps/min" to "200/min (250 burst)".
  - Updated `docs/database-schema.md` — DailyStepRecord: `googleFitSteps` → `healthConnectSteps`, added `escrowSteps` and `escrowSyncCount` columns.
  - Updated `docs/architecture.md` — layer diagram "Google Fit" → "Health Connect", DI section now lists actual modules (StepModule, HealthConnectModule) instead of "Future modules".
  - Rewrote `docs/plans/plan-05-google-fit.md` — body now reflects actual Health Connect implementation with correct file paths and class names.
  - Updated `docs/plans/plan-25-anti-cheat.md` — all Google Fit references → Health Connect, corrected package paths (`data/healthconnect/` not `data/googlefit/`).
  - Updated `docs/plans/plan-30-release.md` — ProGuard keep rules, privacy policy, and checklist updated for Health Connect.
  - Updated `docs/plans/master-plan.md` — Plan 10 description corrected (orbs/bounce were deferred to 10b).
  - Updated `docs/agent/STATE.md` — removed stale "Google Fit references" known issue.
- Remaining cosmetic issues (not fixed — completed plans, code is correct):
  - `docs/plans/plan-02-database.md` and `plan-03-repositories.md` still reference `googleFitSteps` column name (these are historical plan docs; actual code uses `healthConnectSteps`)
  - `docs/agent/RUN_LOG.md` references are historical records (correct to leave as-is)
  - `docs/agent/DECISIONS/ADR-0002-health-connect.md` references are contextual (explaining the decision)
  - `docs/agent/state.json` is an orphaned file from earlier approach (harmless)
  - `docs/temp/` contains a reference playbook from setup (harmless)
- Commands/tests run: N/A (documentation-only changes)
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 12: Round Lifecycle & Post-Round
- Goal: Full round lifecycle with post-round summary, best wave persistence, pause overlay, auto-pause.
- Decisions made:
  - (b) Post-round as overlay within Battle route (avoids ViewModel re-creation)
  - (a) Engine owns totalEnemiesKilled + elapsedTimeSeconds (single source of truth)
  - (a) Quit Round shows summary and saves best wave (player earned that progress)
- Changes made:
  - Updated `presentation/battle/engine/GameEngine.kt` — added totalEnemiesKilled, elapsedTimeSeconds, totalCashEarned tracking; made roundOver publicly settable for quit flow
  - Created `domain/usecase/UpdateBestWave.kt` — compares wave to stored best, persists if new record, returns Result(isNewRecord, previousBest)
  - Updated `presentation/battle/BattleUiState.kt` — added RoundEndState data class and roundEndState field
  - Rewrote `presentation/battle/BattleViewModel.kt` — endRound(), quitRound(), playAgain(), pause(); removed BattleEvent; tracks surfaceView reference for play-again re-init
  - Created `presentation/battle/ui/PostRoundOverlay.kt` — wave reached, enemies killed, cash earned, time survived, new record banner, Play Again / Return to Workshop buttons
  - Created `presentation/battle/ui/PauseOverlay.kt` — Resume / Quit Round buttons
  - Rewrote `presentation/battle/BattleScreen.kt` — integrated overlays, auto-pause via LifecycleEventObserver, exit button calls quitRound(), controls hidden when round over
- Commands/tests run: `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL, zero warnings
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Domain Layer Unit Testing (Regression Safety Net)
- Goal: Add pure JVM unit tests covering all domain use cases, key domain models, and critical pure-Kotlin logic outside domain.
- Decisions made:
  - JVM-only tests (no instrumented/emulator tests) for speed and simplicity
  - JUnit 5 + kotlinx-coroutines-test as test framework (no Turbine needed yet)
  - Injected `Random` into `CalculateDamage` for deterministic crit testing (default param, zero caller impact)
  - Created fake repositories (FakePlayerRepository, FakeWorkshopRepository) for use case tests
- Changes made:
  - Updated `gradle/libs.versions.toml` — added junit5=5.11.4, coroutinesTest=1.10.1, test library entries
  - Updated `app/build.gradle.kts` — added testImplementation deps, JUnit Platform config, platform launcher
  - Refactored `domain/usecase/CalculateDamage.kt` — injectable Random parameter
  - Created `test/fakes/FakePlayerRepository.kt` — in-memory MutableStateFlow-backed fake
  - Created `test/fakes/FakeWorkshopRepository.kt` — in-memory MutableStateFlow-backed fake
  - Created 15 test classes (80 tests total):
    - `domain/usecase/`: CalculateUpgradeCostTest, CanAffordUpgradeTest, QuickInvestTest, PurchaseUpgradeTest, UpdateBestWaveTest, ResolveStatsTest, CalculateDamageTest, CalculateDefenseTest
    - `domain/model/`: TierConfigTest, BiomeTest, CardLoadoutTest, UltimateWeaponLoadoutTest, UpgradeTypeTest, EnemyTypeTest
    - `presentation/battle/engine/`: EnemyScalerTest
    - `data/sensor/`: StepRateLimiterTest
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — BUILD SUCCESSFUL, 80 tests, 0 failures
- Open questions / blockers: None. ViewModel tests and instrumented tests deferred to Plan 29.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 13: Tier System & Progression
- Goal: Tier unlock logic, tier selector UI, battle conditions at Tier 6+, post-round tier unlock notification.
- Decisions made:
  - (a) Armor as hit counter — enemies block first N hits, then take full damage. Punishes fast-attack/low-damage builds.
  - (a) Minimal tier selector — horizontal chip row on home screen, not a dedicated screen.
  - (b) Notify only on unlock — player stays on current tier, chooses when to advance via selector.
  - Added `highestUnlockedTier` as separate field from `currentTier` (play tier) to support tier selection.
  - DB version bumped to 2 with destructive fallback (dev phase — proper migration before release).
- Changes made:
  - Created `domain/usecase/CheckTierUnlock.kt` — iterates tiers, checks wave milestones against bestWavePerTier
  - Created `domain/model/BattleConditionEffects.kt` — pre-computes numeric modifiers from tier battle conditions
  - Created `presentation/home/TierSelector.kt` — horizontal tier chip row with lock/unlock states, condition summary
  - Updated `data/local/PlayerProfileEntity.kt` — added `highestUnlockedTier` column (default 1)
  - Updated `data/local/PlayerProfileDao.kt` — added `updateHighestUnlockedTier()` query
  - Updated `data/local/AppDatabase.kt` — bumped version to 2
  - Updated `domain/model/PlayerProfile.kt` — added `highestUnlockedTier` field
  - Updated `domain/repository/PlayerRepository.kt` — added `updateHighestUnlockedTier()` method
  - Updated `data/repository/PlayerRepositoryImpl.kt` — implemented new method + entity→domain mapping
  - Updated `presentation/battle/entities/EnemyEntity.kt` — added `armorHits` (blocks first N hits), `attackInterval` param, armor ring visual
  - Updated `presentation/battle/engine/WaveSpawner.kt` — accepts `BattleConditionEffects`, applies speed/attack/armor/boss interval
  - Updated `presentation/battle/engine/GameEngine.kt` — computes conditions from tier, applies orb/knockback/thorn multipliers
  - Updated `presentation/battle/BattleUiState.kt` — added `tierUnlocked` to `RoundEndState`
  - Updated `presentation/battle/BattleViewModel.kt` — checks tier unlock after round end, persists new highest tier
  - Updated `presentation/battle/ui/PostRoundOverlay.kt` — shows "🔓 Tier X Unlocked!" banner with cash multiplier teaser
  - Updated `presentation/home/HomeUiState.kt` — added `highestUnlockedTier`, `bestWavePerTier`
  - Updated `presentation/home/HomeViewModel.kt` — loads unlock data, exposes `selectTier()`
  - Updated `presentation/home/HomeScreen.kt` — replaced static header with TierSelector
  - Updated `test/fakes/FakePlayerRepository.kt` — added `updateHighestUnlockedTier`
  - Created `test/.../CheckTierUnlockTest.kt` — 7 tests for tier unlock logic
  - Created `test/.../BattleConditionEffectsTest.kt` — 6 tests for all tier condition values
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — BUILD SUCCESSFUL, 93 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 18: Narrative Biome Progression
- Goal: 5 biome visual identities, ambient particles, biome transition overlay, home screen theming.
- Decisions made:
  - (a) Simple overlay for biome transition — styled Compose screen, animation deferred to Plan 27.
  - (a) Simple particles — lightweight spawn-drift-recycle, 30-50 per biome, no physics.
  - (a) Derive biome unlock from highestUnlockedTier — no DB change, first-seen via SharedPreferences.
  - Enemy tinting via 30% color blend with base type color (not color filter).
  - Ziggurat colors passed as constructor parameter, paints built dynamically.
- Changes made:
  - Created `presentation/battle/biome/BiomeTheme.kt` — 5 biome palettes (sky, ground, ziggurat, enemy tint, particles)
  - Created `presentation/battle/biome/BackgroundRenderer.kt` — gradient sky + ambient particle system
  - Created `presentation/battle/ui/BiomeTransitionOverlay.kt` — full-screen biome reveal with step count
  - Created `data/BiomePreferences.kt` — SharedPreferences wrapper for first-seen tracking
  - Updated `presentation/battle/engine/GameEngine.kt` — creates BackgroundRenderer, passes biome colors/tint
  - Updated `presentation/battle/entities/ZigguratEntity.kt` — accepts layerColors parameter
  - Updated `presentation/battle/entities/EnemyEntity.kt` — accepts enemyTint, blends with base color
  - Updated `presentation/battle/engine/WaveSpawner.kt` — accepts and passes enemyTint
  - Updated `presentation/battle/BattleUiState.kt` — added biomeTransition field
  - Updated `presentation/battle/BattleViewModel.kt` — injects BiomePreferences, checks first-seen, dismissBiomeTransition()
  - Updated `presentation/battle/BattleScreen.kt` — shows BiomeTransitionOverlay
  - Updated `presentation/home/HomeScreen.kt` — biome gradient background
  - Created `test/.../BiomeThemeTest.kt` — 4 tests
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — 97 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 14: Step Overdrive
- Goal: Mid-battle mechanic to sacrifice Steps for 60s combat buff, once per round.
- Decisions made:
  - (a) Stub SURGE — shows in UI, deducts cost, but UW cooldown reset is no-op until Plan 15.
  - (a) Skip free charges — deferred to Plan 19 (Walking Encounters).
  - (a) Engine-side aura — pulsing circle + timer bar rendered on Canvas, respects game speed.
- Changes made:
  - Created `domain/usecase/ActivateOverdrive.kt` — sealed Result, checks balance + once-per-round
  - Created `presentation/battle/ui/OverdriveMenu.kt` — 4-option selection with cost/affordability
  - Created `test/.../ActivateOverdriveTest.kt` — 4 tests
  - Updated `GameEngine.kt` — overdrive state (timer, fortune multiplier, stat modification), activateOverdrive(), expireOverdrive()
  - Updated `ZigguratEntity.kt` — pulsing aura circle + timer bar, overdriveColor/overdriveProgress fields
  - Updated `BattleUiState.kt` — added overdriveUsed, activeOverdriveType, overdriveTimeRemaining, stepBalance, showOverdriveMenu
  - Updated `BattleViewModel.kt` — activateOverdrive(), toggleOverdriveMenu(), polls engine overdrive state
  - Updated `BattleScreen.kt` — ⚡ button in control bar, OverdriveMenu overlay, active overdrive HUD indicator
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — 101 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 15: Ultimate Weapons
- Goal: 6 UW types with unlock/upgrade/equip, battle activation with cooldowns, visual effects, management screen.
- Decisions made:
  - (a) Simple geometric effects — expanding circles, lines, tints. Polish in Plan 27.
  - (a) Sub-screen of Workshop — "Ultimate Weapons" button navigates to UW management.
  - (a) Simple scaling — upgradeCost = unlockCost * 2 * level, cooldown -5%/level, max level 10.
- Changes made:
  - Updated `domain/model/UltimateWeaponType.kt` — added baseCooldownSeconds, effectDurationSeconds, upgradeCost(), cooldownAtLevel(), MAX_LEVEL
  - Created `domain/usecase/UnlockUltimateWeapon.kt` — checks balance + not owned, deducts Power Stones
  - Created `domain/usecase/UpgradeUltimateWeapon.kt` — cost scaling, max level 10
  - Created `presentation/weapons/UltimateWeaponViewModel.kt` — observes weapons + wallet
  - Created `presentation/weapons/UltimateWeaponScreen.kt` — 6 UW cards with lock/unlock/equip/upgrade
  - Created `presentation/battle/ui/UltimateWeaponBar.kt` — row of 3 UW activation buttons
  - Updated `GameEngine.kt` — UW state management, 6 effect implementations, visual rendering, SURGE wired
  - Updated `BattleUiState.kt` — added UWSlotInfo, uwSlots
  - Updated `BattleViewModel.kt` — injects UltimateWeaponRepository, loads equipped, polls UW state
  - Updated `BattleScreen.kt` — shows UltimateWeaponBar
  - Updated `Screen.kt` — added Weapons route
  - Updated `MainActivity.kt` — added Weapons composable route
  - Updated `WorkshopScreen.kt` — added "Ultimate Weapons" navigation button
  - Created `test/fakes/FakeUltimateWeaponRepository.kt`
  - Created `test/.../UnlockUltimateWeaponTest.kt` — 3 tests
  - Created `test/.../UpgradeUltimateWeaponTest.kt` — 4 tests
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — 108 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 16: Labs System
- Goal: Implement Labs research system — 10 time-gated research projects, lab slots, Gem rush, auto-completion.
- Decisions made:
  - (a) Cost scaling 1.15, time scaling 1.10 — moderate ramp matching Workshop feel.
  - (a) Gem rush: linear interpolation `50 + fraction × 150` (range 50–200 Gems).
  - (a) Per-type scaling fields on ResearchType enum (tunable in Plan 28).
- Changes made:
  - Updated `domain/model/ResearchType.kt` — added `costScaling: Double = 1.15` and `timeScaling: Double = 1.10`
  - Created `domain/usecase/CalculateResearchCost.kt` — `baseCostSteps × costScaling^level`
  - Created `domain/usecase/CalculateResearchTime.kt` — `baseTimeHours × timeScaling^level`
  - Created `domain/usecase/StartResearch.kt` — validates slots, affordability, max level, deducts Steps
  - Created `domain/usecase/CompleteResearch.kt` — gates on timer, increments level
  - Created `domain/usecase/RushResearch.kt` — linear Gem cost, companion `calculateRushCost()`
  - Created `domain/usecase/UnlockLabSlot.kt` — 200 Gems per slot, max 4
  - Created `domain/usecase/CheckResearchCompletion.kt` — auto-completes expired research
  - Updated `data/local/PlayerProfileEntity.kt` — added `labSlotCount` with `@ColumnInfo(defaultValue = "1")`
  - Updated `data/local/PlayerProfileDao.kt` — added `updateLabSlotCount()`
  - Updated `data/local/AppDatabase.kt` — bumped version to 3
  - Updated `domain/model/PlayerProfile.kt` — added `labSlotCount`
  - Updated `domain/repository/PlayerRepository.kt` — added `updateLabSlotCount()`
  - Updated `data/repository/PlayerRepositoryImpl.kt` — implemented + toDomain mapping
  - Updated `domain/repository/LabRepository.kt` — added `getResearchLevel()`, `getActiveResearchCount()`, updated `startResearch()` signature
  - Updated `data/repository/LabRepositoryImpl.kt` — implemented new methods
  - Created `presentation/labs/LabsUiState.kt` — ResearchDisplayInfo + LabsUiState
  - Created `presentation/labs/LabsViewModel.kt` — combines research/wallet/tick flows, 1s countdown
  - Created `presentation/labs/LabsScreen.kt` — full UI with slot indicator, research cards, start/rush/unlock
  - Updated `presentation/MainActivity.kt` — replaced Labs placeholder with LabsScreen
  - Updated `presentation/home/HomeViewModel.kt` — added labRepository.ensureResearchExists() + CheckResearchCompletion
  - Created `test/fakes/FakeLabRepository.kt` — in-memory StateFlow-backed fake
  - Updated `test/fakes/FakePlayerRepository.kt` — added updateLabSlotCount
  - Created 7 test classes (25 new tests):
    - CalculateResearchCostTest (4), CalculateResearchTimeTest (3), StartResearchTest (5), CompleteResearchTest (3), RushResearchTest (4), UnlockLabSlotTest (3), CheckResearchCompletionTest (3)
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — 133 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: None.
- Memory updated: STATE ✅ / RUN_LOG ✅

## 2026-03-06 — Plan 17: Cards System
- Goal: Implement Cards system — 9 card types, 3 rarities, pack opening, Card Dust upgrades, loadout, battle integration.
- Decisions made:
  - (a) Pack distributions: Common 80/18/2, Rare 50/40/10, Epic 20/40/40. Dust from dupes: 5/15/50.
  - (a) Numeric fields on CardType enum with linear interpolation for level scaling.
  - (b) Post-process pattern: ApplyCardEffects modifies ResolvedStats copy, ResolveStats untouched.
- Changes made:
  - Updated `domain/model/CardType.kt` — added valueLv1/valueLv5/secondaryLv1/secondaryLv5, effectAtLevel(), secondaryAtLevel()
  - Updated `domain/model/CardRarity.kt` — added dustValue (5/15/50) and upgradeDustPerLevel (10/25/50)
  - Created `domain/usecase/OpenCardPack.kt` — PackTier enum, CardResult, rarity rolling, duplicate→dust
  - Created `domain/usecase/UpgradeCard.kt` — Card Dust cost scaling by rarity and level
  - Created `domain/usecase/ApplyCardEffects.kt` — CardEffectResult, 9 card effects as post-process on ResolvedStats
  - Created `domain/usecase/ManageCardLoadout.kt` — equip/unequip with max 3 validation
  - Created `presentation/cards/CardsUiState.kt` — CardDisplayInfo, PackOption, CardsUiState
  - Created `presentation/cards/CardsViewModel.kt` — combines cards + wallet, all actions
  - Created `presentation/cards/CardsScreen.kt` — pack buttons, card collection, equip/upgrade, rarity colors
  - Updated `presentation/battle/BattleViewModel.kt` — inject CardRepository, apply card effects at round start + playAgain
  - Updated `presentation/battle/engine/GameEngine.kt` — Second Wind revive, cashBonusPercent in kill rewards
  - Updated `presentation/navigation/Screen.kt` — added Cards route
  - Updated `presentation/MainActivity.kt` — added Cards composable
  - Updated `presentation/workshop/WorkshopScreen.kt` — added "🃏 Cards" navigation button
  - Created `test/fakes/FakeCardRepository.kt` — in-memory StateFlow-backed fake
  - Updated `test/fakes/FakePlayerRepository.kt` — implemented addCardDust/spendCardDust
  - Created 4 test classes (22 new tests):
    - OpenCardPackTest (4), UpgradeCardTest (4), ApplyCardEffectsTest (11), ManageCardLoadoutTest (3)
- Commands/tests run: `./run-gradle.sh testDebugUnitTest` — 155 tests, 0 failures. `./run-gradle.sh assembleDebug` — BUILD SUCCESSFUL.
- Open questions / blockers: Step Surge gemMultiplier tracked but not consumed (no Gem earning in battle — deferred to Plan 20).
- Memory updated: STATE ✅ / RUN_LOG ✅

## Run — 2026-03-06 — Plan 19: Walking Encounters & Supply Drops

### Objective
Implement Plan 19: Supply drop generation during walks, push notifications, claim system, and inbox UI.

### What was done
1. **Task 1 — Enums & type safety**: Created `SupplyDropTrigger` (4 entries with notification messages) and `SupplyDropReward` (4 entries). Updated `SupplyDrop` domain model from raw `String` fields to type-safe enums. Updated `WalkingEncounterRepository` interface and `WalkingEncounterRepositoryImpl` to use enums (stored as `.name` strings in Room).

2. **Task 2 — GenerateSupplyDrop use case**: Seeded random drop generation with 3 active triggers (milestone at 10k, threshold at 2k boundaries with 5% per 100 steps, random at 1% per 500 steps). Step burst deferred. Created `DropGeneratorState` for tracking. 9 unit tests, all green.

3. **Task 3 — ClaimSupplyDrop use case**: Credits reward to correct `PlayerRepository` method based on `SupplyDropReward` type, marks drop claimed. Created `FakeWalkingEncounterRepository`. 6 unit tests, all green.

4. **Task 4 — Inbox cap enforcement**: Added `deleteOldestUnclaimed()` and `countUnclaimedOnce()` to `WalkingEncounterDao`. Added `enforceInboxCap(maxSize)` and `getUnclaimedCount()` to repository interface/impl.

5. **Task 5 — SupplyDropNotificationManager**: Dedicated `supply_drops` notification channel (IMPORTANCE_DEFAULT), unique notification IDs per drop, deep-link intent to supplies screen.

6. **Task 6 — DailyStepManager integration**: Added `WalkingEncounterRepository` and `SupplyDropNotificationManager` as dependencies. After step crediting, calls `GenerateSupplyDrop`, enforces inbox cap, creates drop, and sends notification. Tracks `DropGeneratorState` with day rollover reset.

7. **Task 7 — UnclaimedSuppliesScreen**: Added `Screen.Supplies` route. Created `UnclaimedSuppliesViewModel` (observes unclaimed drops, claim/claimAll), `SuppliesUiState`, and `UnclaimedSuppliesScreen` (LazyColumn with claim buttons, empty state, relative timestamps). Added route to `NavHost` in `MainActivity` with notification deep-link handling.

8. **Task 8 — Home screen inbox badge**: Added `unclaimedDropCount` to `HomeUiState`. Injected `WalkingEncounterRepository` into `HomeViewModel`, added to `combine()`. Added `BadgedBox` button on `HomeScreen` that shows when count > 0, navigates to supplies. Added `onSuppliesClick` callback wired in `MainActivity`.

### Decisions
- No GPS triggers — step-based only, defer to future plan.
- No free Overdrive charges — burst trigger deferred, avoids Room migration.
- Inbox overflow discards oldest unclaimed drop silently.
- No Card Pack reward — Card Dust instead, avoids coupling to OpenCardPack flow.
- 10k milestone gives 5 Gems (single drop); Power Stones deferred to combined reward enhancement.
- No notification action button — tap opens inbox screen (avoids BroadcastReceiver complexity).

### Test results
- 170 total JVM tests (155 existing + 15 new), all green, 0 failures.
- New: GenerateSupplyDropTest (9), ClaimSupplyDropTest (6).

### What remains
- Step burst trigger (needs step velocity tracking in DailyStepManager).
- 10k milestone second reward (Power Stones) — could be two drops or combined.
- Custom notification icons (currently using system placeholders).
- Supply drop notification preferences (on/off toggle — Plan 23).
- Claim animation in UnclaimedSuppliesScreen (polish — Plan 27).

## Run — 2026-03-06 — Plan 20: Power Stone & Gem Economy

### Objective
Implement premium currency earning systems: weekly step challenges, daily login rewards, and wave milestone bonuses.

### What was done
1. **Task 1 — Database**: Created `WeeklyChallengeEntity` + `WeeklyChallengeDao`, `DailyLoginEntity` + `DailyLoginDao`. Added `currentStreak`/`lastLoginDate` to `PlayerProfileEntity`/`PlayerProfile`. Added `updateStreak()` to `PlayerProfileDao`/`PlayerRepository`. Added `sumCreditedSteps()` to `DailyStepDao`. Bumped DB to version 4 (9 entities). Updated `DatabaseModule` with 2 new DAO providers. Updated `FakePlayerRepository` with streak support.

2. **Task 2 — Weekly Step Challenge**: Created `TrackWeeklyChallenge` use case. Queries weekly step sum from `DailyStepDao`, awards PS at 50k (10), 75k (20 total), 100k (35 total) thresholds. Only awards delta PS for newly crossed tiers.

3. **Task 3 — Daily Login & Streak**: Created `TrackDailyLogin` use case. Awards 1 PS when 1k+ steps walked (once/day). Manages 7-day Gem streak: consecutive days increment streak, missed day resets to 1, awards min(streak, 5) Gems. Streak cycles after day 7.

4. **Task 4 — Wave Milestone PS**: Created `AwardWaveMilestone` use case. Awards 1 PS (base), 2 PS (wave % 10 == 0), or 5 PS (wave % 25 == 0) on new personal bests. Integrated into `BattleViewModel.endRound()`. Added `powerStonesAwarded` to `RoundEndState`. Updated `PostRoundOverlay` to display PS earned.

5. **Task 5 — Currency Dashboard**: Created `Screen.Economy` route. Created `CurrencyDashboardViewModel` + `CurrencyDashboardScreen` with weekly progress bar, 3 threshold markers, login streak dots (7-day), daily PS status, and currency balances.

6. **Task 6 — Integration**: Updated `DailyStepManager` with `DailyLoginDao`, `WeeklyChallengeDao`, `DailyStepDao` dependencies. Calls `TrackDailyLogin` and `TrackWeeklyChallenge` after step crediting. Updated `HomeViewModel` to trigger daily login on app open. Made currency row on `HomeScreen` tappable to navigate to economy dashboard.

### Decisions
- Streak fields on PlayerProfileEntity (no separate LoginStreakEntity) — avoids extra table/DAO/repo.
- Long-distance Gem bonuses deferred to Plan 21 (milestones).
- Wave milestone: 1 PS base, 2 PS at multiples of 10, 5 PS at multiples of 25.
- TrackWeeklyChallenge/TrackDailyLogin use DAOs directly (data-layer integration, not pure domain).

### Test results
- 179 total JVM tests (170 existing + 9 new AwardWaveMilestone), all green, 0 failures.

### What remains
- TrackWeeklyChallenge and TrackDailyLogin unit tests (need DAO fakes — deferred to Plan 29).
- Long-distance walking Gem bonuses (Plan 21).
- Weekly challenge reset notification.

## Run — 2026-03-09 — Plan 21: Milestones & Daily Missions

### Objective
Implement lifetime walking milestones and daily missions with progress tracking and claim rewards.

### Design decisions
- Card Pack milestone rewards → equivalent Gems (Tutorial=50, Rare=150, Epic=500). Keeps OpenCardPack untouched.
- Cosmetic milestone rewards → stored as claimed but no-op visually until cosmetics system exists.
- Walking mission progress → DAO query approach (steps already tracked).
- Battle mission progress → accumulated in BattleViewModel.endRound().
- Workshop/Lab mission progress → updated at call sites.
- DB version 5 with destructive fallback (still in dev).

### What was done
1. **Task 1 — Domain models**: Created `MilestoneReward` (sealed class: Gems/PowerStones/Cosmetic), `Milestone` (6 entries matching GDD §16.1 with card pack→Gem equivalents), `DailyMissionType` (6 entries: 2 walking, 2 battle, 2 upgrade), `MissionCategory` enum.

2. **Task 2 — Milestone DB layer**: Created `MilestoneEntity` + `MilestoneDao`. Updated `AppDatabase` (version 5, 11 entities). Updated `DatabaseModule` with 2 new DAO providers.

3. **Task 3 — Mission DB layer**: Created `DailyMissionEntity` + `DailyMissionDao` (with `countClaimable` Flow query).

4. **Task 4 — Use cases**: Created `CheckMilestones` (queries DAO, filters by threshold + unclaimed) and `ClaimMilestone` (credits Gems/PS, marks claimed, cosmetics no-op).

5. **Task 5 — GenerateDailyMissions**: Date-seeded Random, 1 per category, idempotent (skips if missions exist for today).

6. **Task 6 — Progress hooks**: 
   - `BattleViewModel.endRound()` → updates REACH_WAVE and KILL_ENEMIES missions.
   - `WorkshopViewModel.purchase()` → updates SPEND_WORKSHOP_STEPS mission.
   - `LabsViewModel` → updates COMPLETE_RESEARCH mission after rush/completion.

7. **Task 7 — Missions screen**: Created `MissionsUiState`, `MissionsViewModel` (combines missions + milestones + profile + tick), `MissionsScreen` (daily missions with progress bars + claim buttons, milestones with progress + claim, midnight countdown).

8. **Task 8 — Home integration**: Added `Screen.Missions` route, `claimableMissionCount` to `HomeUiState`, missions badge button on `HomeScreen`, `GenerateDailyMissions` call in `HomeViewModel.init`, 5-flow `combine()` with milestone/mission counts.

### Test results
- 206 total JVM tests (179 existing + 27 new), all green, 0 failures.
- New: MilestoneTest (6), DailyMissionTypeTest (7), CheckMilestonesTest (4), ClaimMilestoneTest (4), GenerateDailyMissionsTest (6).
- New fakes: FakeMilestoneDao, FakeDailyMissionDao.

### What remains
- Milestone cosmetic rewards are no-op (needs cosmetics system — Plan 26/27).
- Walking mission auto-progress runs once on MissionsScreen open (not continuously from DailyStepManager) — sufficient since steps flow updates the ViewModel.
- Daily mission notification on completion (deferred to Plan 23).

## Run — 2026-03-09 — Plan 22: Stats & History Screen

### Objective
Build the Stats & History screen with walking history charts, battle stats, and all-time aggregates.

### Design decisions
- Canvas-drawn bar chart (no third-party library, matches existing Canvas patterns).
- Lifetime currency counters (totalGemsEarned/Spent, totalPowerStonesEarned/Spent) on PlayerProfileEntity — tracked at DAO/repository level, zero caller changes.
- Battle stats (totalRoundsPlayed, totalEnemiesKilled, totalCashEarned) on PlayerProfileEntity — no separate entity.
- DB version 6 with destructive fallback.

### What was done
1. **Task 1 — Data layer**: Added 7 new columns to `PlayerProfileEntity` (totalGemsEarned/Spent, totalPowerStonesEarned/Spent, totalRoundsPlayed, totalEnemiesKilled, totalCashEarned). Updated `PlayerProfile` domain model, `PlayerProfileDao` (6 new queries), `PlayerRepositoryImpl` (lifetime tracking in add/spend methods + incrementBattleStats), `PlayerRepository` interface, `FakePlayerRepository`. Bumped DB to version 6.

2. **Task 2 — Battle stats wiring**: Added `playerRepository.incrementBattleStats()` call in `BattleViewModel.endRound()`.

3. **Task 3 — StatsViewModel**: Created `StatsUiState` (DailyBarData, StatsPeriod enum) and `StatsViewModel` (4-flow combine: profile + history + upgrades + period). Builds bar data for 7-day/30-day/12-week views. Computes daysActive, averageDailySteps, totalWorkshopLevels.

4. **Task 4 — Walking history chart**: Created `WalkingHistoryChart` Canvas composable — vertical bars with primary/secondary color split (sensor steps vs step-equivalents), 50k ceiling dashed line, date labels, y-axis scale, FilterChip period toggle, legend.

5. **Task 5 — Stats screen**: Created `StatsScreen` with 4 Card sections (Walking History, Today's Activity, Battle Stats, All-Time Stats). Replaced placeholder in `MainActivity`.

### Test results
- 206 total JVM tests, all green, 0 failures. No new tests (presentation-only plan).

### What remains
- Lifetime currency counters start from 0 (no retroactive backfill).
- Chart tap-for-detail tooltip deferred to Plan 27 polish.
- Pull-to-refresh deferred (data is already reactive via Flows).

## Run — 2026-03-09 — Plan 23: Notifications & Widget

### Objective
Enhanced notifications, home screen widget, smart reminders, milestone alerts, and notification preferences.

### Design decisions
- Traditional AppWidgetProvider + RemoteViews (no Glance dependency).
- Smart reminders piggyback on existing StepSyncWorker (no separate WorkManager job).
- SharedPreferences for notification preferences (consistent with BiomePreferences pattern).

### What was done
1. **Task 1 — NotificationPreferences**: Created `data/NotificationPreferences.kt` — 4 boolean toggles (persistent, supply drops, smart reminders, milestone alerts).

2. **Task 2 — Enhanced persistent notification**: Updated `StepNotificationManager` with Workshop/Battle action buttons via PendingIntents. Updated `StepCounterService` to pass actual step balance from PlayerRepository. Added preference gate. Extended `MainActivity` deep-link handling for workshop/battle/missions routes.

3. **Task 3 — Home screen widget**: Created `widget_step_counter.xml` layout, `step_widget_info.xml` metadata, `StepWidgetProvider` (AppWidgetProvider with SharedPreferences-backed data), `WidgetUpdateHelper` (60s throttle). Integrated into `DailyStepManager`. Registered in AndroidManifest.

4. **Task 4 — Smart reminders**: Created `SmartReminderManager` — checks prefs enabled, not sent today, lastActiveAt > 4h, finds cheapest upgrade within 10k step gap. Uses `reminders` notification channel. Integrated into `StepSyncWorker.doWork()`.

5. **Task 5 — Milestone alerts**: Created `MilestoneNotificationManager` — notifyNewBestWave() and notifyMilestoneAchieved(). Uses `milestones` channel. Integrated into `BattleViewModel.endRound()` (new best wave) and `HomeViewModel.init` (achievable milestones).

6. **Task 6 — Supply drop preference gate**: Updated `SupplyDropNotificationManager` to inject NotificationPreferences and skip if disabled.

7. **Task 7 — Settings UI**: Created `NotificationSettingsViewModel` + `NotificationSettingsScreen` (4 Switch toggles). Added `Screen.Settings` route, wired in NavHost, added settings button on HomeScreen.

### Test results
- 206 total JVM tests, all green, 0 failures. No new tests (Android notification/widget APIs).

### What remains
- Custom notification icons (all channels use system placeholders).
- Widget balance shows 0 (DailyStepManager doesn't query PlayerRepository for balance).
- Widget preview image for widget picker.

## Run — 2026-03-09 — Plan 25: Anti-Cheat & Validation

### Objective
Harden anti-cheat beyond basic rate limiting + daily ceiling + HC escrow. Add velocity analysis, graduated cross-validation, activity minute gaming prevention, and per-minute overlap deduction.

### Design decisions
- No accelerometer sensor — step velocity analysis detects shakers via statistical patterns (zero battery cost).
- No Room entity for logging — SharedPreferences counters + Logcat (no DB migration needed).
- Cross-validation offense count in SharedPreferences (survives DB wipes, matches existing prefs pattern).
- Added mockito-kotlin 5.4.0 as test dependency for mocking Android classes in JVM tests.
- Enabled `unitTests.isReturnDefaultValues = true` in build.gradle.kts for android.util.Log in tests.

### What was done
1. **Task 1 — AntiCheatPreferences**: Created `data/anticheat/AntiCheatPreferences.kt` — SharedPreferences wrapper with daily counters (rate rejected, velocity penalized, activity minutes rejected), cross-validation offense tracking (count + last date), and 7-day offense decay.

2. **Task 2 — StepVelocityAnalyzer**: Created `data/sensor/StepVelocityAnalyzer.kt` — rolling 15-min window, two heuristics: instant jump detection (idle→spike in last 3 pairs) and constant rate detection (CV < 0.05 over 10-min window). Returns penalty multiplier (1.0/0.5/0.0).

3. **Task 3 — DailyStepManager wiring**: Added `StepVelocityAnalyzer` and `AntiCheatPreferences` as constructor dependencies. Pipeline: rate limit → velocity analysis → ceiling → persist. Logs rate-rejected and velocity-penalized steps. Added `stepsPerMinute` map for overlap deduction. Resets on day rollover.

4. **Task 4 — Enhanced StepCrossValidator**: Rewrote with graduated response based on offense count: Level 0 (escrow, 3 syncs), Level 1 (escrow, 2 syncs), Level 2 (cap at HC value), Level 3 (cap at HC minus 10%). Records offenses on discrepancy, decays on reconciliation.

5. **Task 5 — ActivityMinuteValidator**: Created `data/healthconnect/ActivityMinuteValidator.kt` — filters sessions: discards <2min micro-sessions, truncates >4hr sessions to 240min, rejects sessions beyond 5 distinct activity types per day.

6. **Task 6 — StepSyncWorker wiring**: Added `ActivityMinuteValidator` to constructor. Sessions filtered through validator before conversion. Passes `dailyStepManager.getSensorStepsPerMinute()` instead of `emptyMap()`.

7. **Task 7 — Per-minute overlap deduction**: Added `stepsPerMinute` accumulator to `DailyStepManager` (epoch-minute → credited steps). Capped at 1440 entries. Exposed via `getSensorStepsPerMinute()`. `ActivityMinuteConverter` now receives real per-minute data for double-counting prevention.

### Test results
- 222 total JVM tests (206 existing + 16 new), all green, 0 failures.
- New: StepVelocityAnalyzerTest (6), ActivityMinuteValidatorTest (5), StepCrossValidatorTest (5).
- Build: assembleDebug successful.

### What remains
- StepCrossValidator Level 2/3 could also adjust `creditedSteps` in Room (currently only escrows excess).
- AntiCheatPreferences counters not surfaced in any UI (debug screen could be added).
- Step burst trigger for supply drops still deferred.

## Run — 2026-03-09 — Plan 26: Monetization & Ads

### Objective
Implement monetization layer with stub billing/ads, cosmetic store, Season Pass, and reward ads.

### Design decisions
- Stub-first architecture: `BillingManager` and `RewardAdManager` interfaces in domain (pure Kotlin), stub impls in data. Swap via DI bindings when real SDKs integrated.
- Season Pass daily Gem bonus piggybacks on existing `TrackDailyLogin` (automatic, not manual claim).
- Cosmetic store uses placeholder items — visual application deferred to Plan 27.
- `OpenCardPack` gets `isFree: Boolean = false` default param — backward-compatible, zero caller impact.
- No new test dependencies needed — stubs are simple enough to not warrant dedicated tests.
- DB version 7 with destructive fallback (still in dev).

### What was done
1. **Task 1 — Database & Profile**: Added 5 monetization fields to `PlayerProfileEntity` (`adRemoved`, `seasonPassActive`, `seasonPassExpiry`, `freeLabRushUsedToday`, `freeCardPackAdUsedToday`). Created `CosmeticEntity` + `CosmeticDao`. Bumped DB to version 7 (12 entities). Updated `PlayerProfileDao` (4 new queries), `PlayerRepository` interface (4 new methods), `PlayerRepositoryImpl`, `FakePlayerRepository`.

2. **Task 2 — Billing Manager Stub**: Created `BillingProduct` enum (5 products), `PurchaseResult` sealed class, `BillingManager` interface, `StubBillingManager` (500ms delay, always succeeds), `BillingModule` DI binding.

3. **Task 3 — Gem Pack Purchase + Store UI**: Created `PurchaseGemPack` use case, `StoreScreen` (Gem packs, Ad Removal, Season Pass, Cosmetics sections), `StoreViewModel`, `StoreUiState`. Added `Screen.Store` route, wired in `MainActivity` NavHost.

4. **Task 4 — Ad Removal**: Ad Removal card in StoreScreen, `StoreViewModel.purchaseAdRemoval()`, "Already Purchased" state.

5. **Task 5 — Season Pass**: Updated `TrackDailyLogin` with `seasonPassActive`/`seasonPassExpiry` params (+10 Gems/day). Updated `LabsViewModel` with `freeRush()` method and `seasonPassFreeRushAvailable` state. Updated `LabsScreen` with "Free ⭐" button. Season Pass card in StoreScreen.

6. **Task 6 — Reward Ad Stub**: Created `AdPlacement` enum (3 placements), `AdResult` sealed class, `RewardAdManager` interface, `StubRewardAdManager` (1s delay, always rewards), `AdModule` DI binding.

7. **Task 7 — Post-Round Ads**: Added `adRemoved`/`gemAdWatched`/`psAdWatched` to `RoundEndState`. Injected `RewardAdManager` into `BattleViewModel`, added `watchGemAd()`/`watchPsAd()`. Updated `PostRoundOverlay` with ad buttons (hidden if adRemoved, disabled after use).

8. **Task 8 — Free Card Pack Ad**: Added `isFree` param to `OpenCardPack` (backward-compatible default). Injected `RewardAdManager` into `CardsViewModel`, added `watchFreePackAd()`. Updated `CardsScreen` with "🎬 Free Pack (Ad)" button (hidden if adRemoved, disabled if used today).

9. **Task 9 — Cosmetic Store**: Created `CosmeticCategory` enum, `CosmeticItem` domain model, `CosmeticRepository` interface, `CosmeticRepositoryImpl` (7 placeholder items, seed on first access). Added cosmetics section to StoreScreen with buy/equip/unequip.

10. **Task 10 — Integration**: Added Store button to HomeScreen and Economy screen. Season Pass badge on HomeScreen. All ad UI gated on `adRemoved` flag.

### Test results
- 222 total JVM tests, all green, 0 failures. No new tests (stub implementations, presentation-only changes).
- Build: assembleDebug successful.

### What remains (deferred)
- Google Play Billing Library v7 integration (replace StubBillingManager).
- AdMob SDK integration (replace StubRewardAdManager).
- Real purchase verification and receipt validation.
- Subscription renewal handling and grace periods.
- Real cosmetic content and visual application (Plan 27).
- Play Console product configuration and test tracks.
- Ad mediation for fill rate optimization.
- ADR for stub billing decision (documented in plan-26-monetization.md instead).

---

## Run: 2026-03-09 — Plan 27: Polish & Visual Effects

**Objective:** Add visual polish and audio to the battle renderer and UI.

**Decisions:**
- (a) Pooled particle system (200 pre-allocated) over lightweight ad-hoc allocation — avoids GC pressure during combat.
- (a) Minimal sound set (~7 reusable sounds) over full per-type set — sufficient for v1.0, easy to expand later.
- (a) Floating cash text on Canvas (game thread) over Compose overlay — same coordinate space, no latency.
- (a) System ANIMATOR_DURATION_SCALE for reduced motion — no in-app toggle needed.
- (a) Placeholder WAV files as sine wave tones — real audio assets to be sourced separately.

**Created files:**
- `presentation/battle/effects/ParticlePool.kt` — Particle class + ParticlePool (200 capacity, acquire/release/recycle)
- `presentation/battle/effects/ReducedMotionCheck.kt` — Reads system ANIMATOR_DURATION_SCALE
- `presentation/battle/effects/EffectEngine.kt` — Effect interface + EffectEngine (manages effects, owns pool + screen shake)
- `presentation/battle/effects/ScreenShake.kt` — Canvas translate oscillation with decay
- `presentation/battle/effects/ProjectileTrailEffect.kt` — Spawns fading trail particles at projectile positions
- `presentation/battle/effects/DeathEffect.kt` — Per-enemy-type death burst (6 types, 6-20 particles each)
- `presentation/battle/effects/FloatingText.kt` — "+X" cash text that drifts up and fades
- `presentation/battle/effects/UWVisualEffect.kt` — 6 particle-based UW spectacles (replaces old geometric rendering)
- `presentation/battle/effects/OverdriveAuraEffect.kt` — 4 overdrive aura particle emitters
- `presentation/battle/effects/WaveAnnouncement.kt` — Wave number + boss warning text overlay + cooldown countdown
- `presentation/audio/SoundManager.kt` — SoundPool wrapper, 7 sound effects, volume/mute, shoot throttling
- `data/SoundPreferences.kt` — SharedPreferences for sound mute/volume
- `res/raw/sfx_*.ogg` — 7 placeholder WAV audio files (sine wave tones)

**Created tests:**
- `presentation/battle/effects/ParticlePoolTest.kt` — 9 tests (acquire, release, recycle, expire, clear, reset)
- `presentation/battle/effects/ScreenShakeTest.kt` — 6 tests (trigger, decay, override, reset, offset)
- `presentation/battle/effects/DeathEffectTest.kt` — 7 tests (particle count per enemy type)

**Modified files:**
- `presentation/battle/engine/GameEngine.kt` — Full rewrite: integrated EffectEngine, removed old UW rendering (uwEffects list, uwPaint, inline render code), added all trigger points (trail, death, floating text, UW spectacle, overdrive aura, wave announcement, screen shake, sound), added reducedMotion parameter to init()
- `presentation/battle/engine/WaveSpawner.kt` — Made phaseTimer publicly readable (for cooldown text)
- `presentation/battle/entities/ZigguratEntity.kt` — Removed old aura circle rendering (auraPulse, auraPaint), added centerY property, kept overdrive timer bar
- `presentation/battle/GameSurfaceView.kt` — Added SoundManager init, reduced motion check, passes isReducedMotion to engine.init()
- `presentation/battle/BattleViewModel.kt` — Added upgrade purchase sound trigger
- `presentation/settings/NotificationSettingsViewModel.kt` — Added SoundPreferences injection, soundMuted state
- `presentation/settings/NotificationSettingsScreen.kt` — Added Sound section with mute toggle
- `presentation/workshop/UpgradeCard.kt` — Added purchase pulse animation (1.05x scale, 100ms, reduced motion aware)
- `presentation/home/HomeScreen.kt` — Added animateContentSize() to step counter
- `presentation/MainActivity.kt` — Added screen transition animations (fadeIn + slideInHorizontally, reduced motion aware)

**Test results:** 244 JVM tests — all green (was 222, +22 new).
**Build:** assembleDebug successful, 2 minor warnings (redundant conversion, hiltViewModel deprecation).

**What remains:**
- Plan 28: Balancing & Tuning (next on critical path)
- Replace placeholder audio with real royalty-free sound effects
- Plan 29: Testing & QA
- Plan 30: Release Prep

---

## Run: 2026-03-09 — Plan 28: Balancing & Tuning

**Objective:** Validate all game constants against GDD player profiles and progression timeline.

**Approach:** Test-based validation — 39 JUnit tests that compute progression math and assert GDD milestones. Conservative tuning — only adjust constants where tests reveal actual problems.

**Findings:**
- Step economy is more generous than GDD predicted in week 1 (intentional — hooks players). Settles toward GDD rates by week 4-8.
- Enemy scaling (1.05^wave) is correct — outpaces raw Workshop DPS but is balanced by crits, multishot, orbs, cards, and in-round upgrades.
- Tier progression timeline is within tolerance when accounting for full combat system (5x combat multiplier).
- Cash economy supports meaningful in-round decisions. Interest at max level is 59% of kill income (borderline but requires 20 levels of investment).
- All 9 card types are balanced with meaningful tradeoffs. No card exceeds 2.5x effective power.
- UW cooldowns allow 2-3+ activations per 20-minute round. No UW dominates.
- First UW unlock takes ~3 weeks (not 2) — acceptable for mid-game reward.
- Supply drop rates produce 1-5 drops per 10k steps.

**Constants changed:** None. All existing values validated as appropriate.

**Created files:**
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/StepEconomyTest.kt` — 5 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/CostCurveTest.kt` — 5 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/EnemyScalingTest.kt` — 6 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/TierProgressionTest.kt` — 5 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/CashEconomyTest.kt` — 4 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/CardBalanceTest.kt` — 4 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/UWOverdriveBalanceTest.kt` — 5 tests
- `app/src/test/java/com/whitefang/stepsofbabylon/balance/SupplyDropEconomyTest.kt` — 5 tests
- `docs/balance/balance-report.md` — comprehensive balance validation report

**Test results:** 283 JVM tests — all green (was 244, +39 new balance tests).
**Build:** No compilation changes needed.

**What remains:**
- Plan 29: Testing & QA (next on critical path)
- Plan 30: Release Prep

## Run: 2026-03-10 — Plan 29: Testing & QA

**Objective:** Add ViewModel tests and deferred use case tests. JVM-only, no instrumented tests.

**Approach:** StandardTestDispatcher + backgroundScope collector for StateFlow-based ViewModels. advanceTimeBy for VMs with ticker loops. Use-case-level testing for LabsViewModel/MissionsViewModel (infinite ticker loops prevent direct VM testing).

**Created fakes:**
- `FakeStepRepository` — in-memory StepRepository
- `FakeBillingManager` — tracks purchases, configurable result
- `FakeRewardAdManager` — configurable AdResult
- `FakeCosmeticRepository` — in-memory cosmetic store
- `FakeDailyLoginDao` — in-memory daily login
- `FakeWeeklyChallengeDao` — in-memory weekly challenge
- `FakeDailyStepDao` — in-memory daily step records with Flow support

**Created test files (64 new tests):**
- `presentation/stats/StatsViewModelTest.kt` — 6 tests
- `presentation/weapons/UltimateWeaponViewModelTest.kt` — 4 tests
- `presentation/supplies/UnclaimedSuppliesViewModelTest.kt` — 3 tests
- `presentation/workshop/WorkshopViewModelTest.kt` — 6 tests
- `presentation/cards/CardsViewModelTest.kt` — 5 tests
- `presentation/labs/LabsViewModelTest.kt` — 4 tests (use-case level)
- `presentation/home/HomeViewModelTest.kt` — 5 tests
- `presentation/battle/BattleViewModelTest.kt` — 10 tests
- `presentation/missions/MissionsViewModelTest.kt` — 4 tests (use-case level)
- `presentation/economy/CurrencyDashboardViewModelTest.kt` — 3 tests
- `presentation/store/StoreViewModelTest.kt` — 3 tests
- `domain/usecase/TrackDailyLoginTest.kt` — 6 tests
- `domain/usecase/TrackWeeklyChallengeTest.kt` — 5 tests

**Key decisions:**
- StandardTestDispatcher over UnconfinedTestDispatcher — prevents infinite loops from ticker coroutines.
- `backgroundScope.launch { vm.uiState.collect {} }` required for WhileSubscribed StateFlows.
- LabsViewModel/MissionsViewModel tested at use-case level (not VM level) due to `while(true) { delay(1000) }` ticker loops that hang even with advanceTimeBy.
- HomeViewModel init modifies profile (TrackDailyLogin) — assertions check structural correctness, not exact currency values.
- No instrumented tests — deferred to post-release.

**Test results:** 347 JVM tests — all green (was 283, +64 new).
**Build:** testDebugUnitTest successful in 44s.

**What remains:**
- Plan 30: Release Prep (next on critical path)
- Instrumented tests (Room DAOs, Compose UI) — post-release
- LabsViewModel/MissionsViewModel direct VM tests (needs ticker refactoring or injectable clock)

## 2026-03-10 — Plan 30: Release Prep

### What was done
- **Task 1: ProGuard/R8 hardening** — Added keep rules for Health Connect SDK, SensorEventListener callbacks, WorkManager ListenableWorker subclasses, Room entity fields, org.json. Restructured rules file with section headers.
- **Task 2: Remove fallbackToDestructiveMigration** — Removed from DatabaseModule.kt. Added comment about future migration requirements.
- **Task 3: Signing config** — Added `import java.util.Properties`, keystore.properties loader with graceful fallback, signingConfigs block, release build type wiring. Added keystore entries to .gitignore. Created docs/release/signing-guide.md.
- **Task 4: Version bump** — Updated versionName from 0.1.0 to 1.0.0. Updated CHANGELOG.md with comprehensive v1.0.0 release notes covering all features.
- **Task 5: Privacy policy** — Created docs/release/privacy-policy.md covering step data, Health Connect, local storage, third-party SDKs. Updated HealthConnectPermissionActivity with scrollable structured privacy content.
- **Task 6: Play Store listing** — Created docs/release/play-store-listing.md (short/full descriptions, category, content rating notes). Created docs/release/release-checklist.md.
- **Task 7: Build verification** — All 347 tests pass. Release APK builds successfully (26MB unsigned, R8 minification clean). Fixed Gradle DSL issue with java.util.Properties import.

### Build verification results
- `testDebugUnitTest`: BUILD SUCCESSFUL (347 tests, all green)
- `assembleRelease`: BUILD SUCCESSFUL (26MB unsigned APK, R8 clean)
- Only warnings: 4 redundant conversion calls, 6 hiltViewModel() deprecations (pre-existing)

### Files created
- `docs/release/privacy-policy.md`
- `docs/release/play-store-listing.md`
- `docs/release/signing-guide.md`
- `docs/release/release-checklist.md`

### Files modified
- `app/proguard-rules.pro` — hardened R8 rules
- `app/build.gradle.kts` — signing config, version 1.0.0
- `app/src/main/java/.../di/DatabaseModule.kt` — removed fallbackToDestructiveMigration
- `app/src/main/java/.../presentation/HealthConnectPermissionActivity.kt` — expanded privacy content
- `CHANGELOG.md` — v1.0.0 release notes
- `.gitignore` — keystore entries

### What remains
- Plan 31: Play Console & Store Publication
- Generate upload keystore (manual step)
- Host privacy policy at public URL
- Create visual assets (icon, screenshots, feature graphic)
- Replace contact email placeholders

---

## 2026-03-11 — Remediation Plan Creation

### Context
- External code review completed (`docs/external-reviews/REPO_ANALYSIS_BUGS_AND_UX.md`) identifying 12 high-priority findings across step integrity, battle wiring, database safety, widget, missions, notifications, deep-links, premium state, UX feedback, accessibility, and test coverage.
- Plan 30 was complete; Plan 31 was next on the critical path.

### What was done
- Created `docs/plans/plan-R-remediation.md` — 12 sub-plans (R01–R12) organized into 3 priority tiers.
- Updated `docs/plans/master-plan.md`:
  - Added Plan R to plan index table.
  - Updated dependency graph: Plan 30 → Plan R → Plan 31.
  - Updated critical path to include Plan R (Tier 1) before Plan 31.
  - Added Plan R to status tracker.
- Updated `docs/agent/STATE.md` — current objective is now Plan R; priorities and next actions reflect remediation order.

### Key decisions
- Plan R Tier 1 (R01–R05) blocks production release (Plan 31). These are data-integrity and progression-correctness issues.
- Plan R Tier 2 (R06–R09) should complete before release but are user-trust issues, not data corruption risks.
- Plan R Tier 3 (R10–R12) can follow shortly after release.
- R01 → R02 is the only sequential dependency within remediation. All other sub-plans are parallelizable.

### What remains
- Execute R01–R12 per priority tiers.
- Plan 31 after R Tier 1 complete.

---

## 2026-03-11 — R01: Step Ingestion Unification

### What was done
- Created `data/sensor/StepIngestionPreferences.kt` — SharedPreferences wrapper with service heartbeat (2-min threshold) and date-scoped day-start counter.
- Refactored `service/StepSyncWorker.kt` — removed private `last_counter_value` baseline. Worker now checks heartbeat (skips if service alive), uses Room `sensorSteps` as authoritative baseline, and only credits the uncredited gap.
- Updated `service/StepCounterService.kt` — writes heartbeat on every step credit, sets day-start counter on startup via one-shot sensor read.
- Created `StepIngestionPreferencesTest.kt` (11 tests) — heartbeat read/write, isServiceAlive, day-start counter, day rollover.
- Created `StepIngestionTest.kt` (10 tests) — service-active skip, gap recovery, day rollover, no double-credit, counter reboot safety.
- All 368 tests pass. Debug build compiles clean.

### Key design decisions
- Two-mechanism approach: heartbeat (optimization) + Room baseline (correctness). Heartbeat prevents unnecessary sensor reads; Room baseline guarantees no double-credit even under race conditions.
- Day-start counter set by whichever path (service or worker) reads the sensor first today. Service sets it on startup; worker sets it if service never ran.
- Worker's old private `last_counter_value` replaced entirely — no migration needed since it was only used for catch-up delta computation.

### What remains
- R02: Escrow Redesign (next — depends on R01 ✓)
- R03–R12: remaining remediation sub-plans

---

## 2026-03-11 — R02: Escrow Redesign

### What was done
- Modified `PlayerProfileDao.adjustStepBalance` — added `MAX(0, ...)` clamp to prevent negative balances on any spend operation.
- Rewrote `StepCrossValidator.validate()` — escrow now deducts excess from player balance via `spendSteps()`. Release restores via `addSteps()`. Discard leaves deduction in place. Level 0/1 branches track whether escrow was already deducted to avoid double-deduction on subsequent syncs.
- Rewrote `StepCrossValidatorTest` — 10 tests (was 5): added balance deduction verification on all escrow branches, no-double-deduction on subsequent syncs, escrow→release net-zero test, escrow→discard keeps-deduction test.
- All 373 tests pass. Build clean.

### Key design decisions
- Deduct-on-escrow approach: simplest correct fix, no schema changes, no new domain concepts.
- Balance clamped to zero: prevents negative balances if player spent suspicious steps before reconciliation.
- Level 0/1 branches check `record.escrowSteps == 0L` to distinguish first escrow (deduct) from subsequent syncs (metadata only).

### What remains
- R03–R12: remaining remediation sub-plans (all Tier 1 blockers now independent)

---

## 2026-03-11 — R03+R04: Battle Workshop Wiring + Dead Upgrade Cleanup

### What was done
- R03: Exposed `workshopLevels` from BattleViewModel (was private). Replaced `emptyMap()` with real workshop levels in both `BattleScreen.LaunchedEffect` and `BattleViewModel.playAgain()`. CASH_BONUS, CASH_PER_WAVE, and INTEREST now reach the GameEngine.
- R04: Added `hiddenUpgrades` set in WorkshopViewModel filtering out STEP_MULTIPLIER and RECOVERY_PACKAGES from the workshop UI. Enum entries preserved for future implementation.
- All 373 tests pass. Build clean.

### What remains
- R05: Database Safety (last Tier 1 blocker)
- R06–R12: Tier 2 and 3 remediation

---

## 2026-03-11 — R05: Database Safety

### What was done
- Disabled backup in AndroidManifest (`allowBackup="false"`). No valuable state to restore in a local-only game.
- Added `fallbackToDestructiveMigration()` in DatabaseModule for pre-release schema mismatch safety.
- Added try/catch recovery in `DatabaseKeyManager.getPassphrase()` — on decryption failure (keystore mismatch after restore), wipes stale passphrase blob and generates fresh key.
- All 373 tests pass. Build clean.

### Key decisions
- Backup disabled entirely rather than selective exclusion — simpler, eliminates the whole class of restore bugs.
- Destructive migration is pre-release only. CONSTRAINTS.md already mandates explicit migrations post-release.

### Milestone
- **Tier 1 remediation complete** (R01–R05). Plan 31 is now unblocked.

### What remains
- R06–R12: Tier 2 and 3 remediation
- Plan 31: Play Console & Store Publication

---

## 2026-03-11 — Documentation Sweep (Post-R05)

### Objective
Full codebase documentation audit after R01–R05 remediation. Find and fix stale references.

### Issues found and fixed (8 files)

1. **CHANGELOG.md** — Test count 347→373. Added R01–R05 remediation section.
2. **docs/release/release-checklist.md** — Unchecked `fallbackToDestructiveMigration` (R05 re-added it for pre-release safety). Updated test count 347→373.
3. **docs/step-tracking.md** — Added R01 service↔worker coordination section (heartbeat, Room baseline, day-start counter). Updated escrow table for R02 balance deduction behavior. Updated data flow diagram with heartbeat and gap recovery steps.
4. **docs/database-schema.md** — Added R05 key recovery mechanism and backup-disabled note to Security section.
5. **docs/architecture.md** — Added backup-disabled row and key auto-recovery note to Security table.
6. **.kiro/steering/source-files.md** — Added 7 missing test fakes from Plan 29 (FakeStepRepository, FakeCosmeticRepository, FakeBillingManager, FakeRewardAdManager, FakeDailyLoginDao, FakeWeeklyChallengeDao, FakeDailyStepDao).
7. **.kiro/steering/structure.md** — Same 7 missing fakes added to fakes directory listing.
8. **AGENTS.md** — Same 7 missing fakes added. Updated test coverage description with StepIngestionPreferences and StepIngestion test areas.

### Verified as correct (no changes needed)
- Google Fit references in RUN_LOG, ADR-0002, plan-02, plan-03, plan-05 — all historical/contextual.
- AGENTS.md test count (373), use case count (32), route count (12), repository count (8) — all accurate.
- database-schema.md entity schemas — all match actual code.
- monetization.md — accurate, reflects stub implementation status.
- master-plan.md — status tracker correct (Plan R unchecked, all others accurate).
- step-tracking.md anti-cheat rules — all thresholds match code.

### Commands/tests run: N/A (documentation-only changes)
### Memory updated: STATE ✅ / RUN_LOG ✅
