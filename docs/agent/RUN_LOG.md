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
