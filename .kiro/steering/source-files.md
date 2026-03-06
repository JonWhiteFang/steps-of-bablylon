# Source File Index

All paths relative to `app/src/main/java/com/whitefang/stepsofbabylon/`.

## Application & DI

```
StepsOfBabylonApp.kt              # @HiltAndroidApp, Configuration.Provider (HiltWorkerFactory)
di/DatabaseModule.kt               # Hilt: Room DB (SQLCipher) + 7 DAO providers
di/RepositoryModule.kt             # Hilt: 7 repository interface → impl bindings (@Singleton)
di/StepModule.kt                   # Hilt: SensorManager provider
di/HealthConnectModule.kt          # Hilt: Health Connect organizational module
```

## Data Layer — Room

```
data/local/AppDatabase.kt         # @Database: 7 entities, 7 DAOs, version 2, exportSchema=true
data/local/Converters.kt          # @TypeConverters: Map<Int,Int> and Map<String,Int> via JSON
data/local/DatabaseKeyManager.kt  # SQLCipher passphrase via Android Keystore
data/local/PlayerProfileEntity.kt # Player profile entity (single row, id=1)
data/local/PlayerProfileDao.kt    # Player DAO: get() as Flow, atomic currency adjustments
data/local/WorkshopUpgradeEntity.kt # Workshop upgrade entity
data/local/WorkshopDao.kt         # Workshop DAO
data/local/LabResearchEntity.kt   # Lab research entity
data/local/LabDao.kt              # Lab research DAO
data/local/CardInventoryEntity.kt # Card inventory entity
data/local/CardDao.kt             # Card inventory DAO
data/local/UltimateWeaponStateEntity.kt # UW state entity
data/local/UltimateWeaponDao.kt   # UW state DAO
data/local/DailyStepRecordEntity.kt # Daily step record entity (with escrow fields)
data/local/DailyStepDao.kt        # Daily step record DAO (with escrow queries)
data/local/WalkingEncounterEntity.kt # Walking encounter entity
data/local/WalkingEncounterDao.kt # Walking encounter DAO
```

## Data Layer — Repositories

```
data/repository/PlayerRepositoryImpl.kt         # Player profile + wallet (entity→domain mapping)
data/repository/WorkshopRepositoryImpl.kt        # Workshop upgrades
data/repository/LabRepositoryImpl.kt             # Lab research
data/repository/CardRepositoryImpl.kt            # Card inventory
data/repository/UltimateWeaponRepositoryImpl.kt  # Ultimate weapon state
data/repository/StepRepositoryImpl.kt            # Daily step records + escrow + getDailyRecord()
data/repository/WalkingEncounterRepositoryImpl.kt # Walking encounters
```

## Data Layer — Sensor

```
data/sensor/StepSensorDataSource.kt  # TYPE_STEP_COUNTER wrapper, emits deltas via callbackFlow
data/sensor/StepRateLimiter.kt       # Rolling 1-min window rate limiter (200/min, 250 burst)
data/sensor/DailyStepManager.kt      # Orchestrates: rate limit → 50k ceiling → Room persist + activity minutes
```

## Data Layer — Health Connect

```
data/healthconnect/HealthConnectClientWrapper.kt  # HealthConnectClient wrapper, availability, permissions
data/healthconnect/HealthConnectStepReader.kt      # Reads aggregated daily steps via aggregate()
data/healthconnect/StepCrossValidator.kt           # Cross-validation, escrow system (>20% discrepancy)
data/healthconnect/StepGapFiller.kt                # Recovers missed steps from HC when service killed
data/healthconnect/ExerciseSessionReader.kt        # Reads exercise sessions for Activity Minute Parity
data/healthconnect/ActivityMinuteConverter.kt      # Converts exercise minutes to step-equivalents with caps
data/BiomePreferences.kt                          # SharedPreferences wrapper for first-seen biome tracking
```

## Domain Layer — Models

```
domain/model/Currency.kt              # Enum: STEPS, CASH, GEMS, POWER_STONES
domain/model/PlayerWallet.kt          # Currency balances data class
domain/model/PlayerProfile.kt         # Full profile (maps from PlayerProfileEntity)
domain/model/ActiveResearch.kt        # In-progress lab research
domain/model/OwnedCard.kt             # Player-owned card instance
domain/model/OwnedWeapon.kt           # Player-owned ultimate weapon
domain/model/DailyStepSummary.kt      # Daily step record domain model (with escrow fields)
domain/model/SupplyDrop.kt            # Walking encounter supply drop
domain/model/UpgradeType.kt           # 23 Workshop upgrade types with configs
domain/model/UpgradeCategory.kt       # Attack, Defense, Utility categories
domain/model/UpgradeConfig.kt         # Upgrade configuration (baseCost, scaling, maxLevel)
domain/model/Tier.kt                  # Tier data class
domain/model/TierConfig.kt            # Full tier table (1–10)
domain/model/BattleCondition.kt       # 7 battle condition types
domain/model/Biome.kt                 # 5 biomes with forTier() mapping
domain/model/BattleConditionEffects.kt # Pre-computed battle condition modifiers from tier
domain/model/EnemyType.kt             # 6 enemy types with multipliers
domain/model/UltimateWeaponType.kt    # 6 UW types with unlock costs
domain/model/UltimateWeaponLoadout.kt # UW loadout (max 3)
domain/model/OverdriveType.kt         # 4 overdrive types with costs
domain/model/ResearchType.kt          # 10 lab research types
domain/model/CardRarity.kt            # Common, Rare, Epic
domain/model/CardType.kt              # 9 card types with effects
domain/model/CardLoadout.kt           # Card loadout (max 3)
domain/model/RoundState.kt            # Transient battle state
domain/model/ZigguratBaseStats.kt     # Base stat constants (HP, damage, attack speed, range, regen, projectile speed)
domain/model/ResolvedStats.kt         # Computed combat stats from workshop + in-round upgrades
```

## Domain Layer — Interfaces & Use Cases

```
domain/repository/PlayerRepository.kt          # Profile/wallet: observe + spend/add currencies
domain/repository/WorkshopRepository.kt         # Workshop upgrades interface
domain/repository/LabRepository.kt              # Lab research interface
domain/repository/CardRepository.kt             # Card inventory interface
domain/repository/UltimateWeaponRepository.kt   # Ultimate weapon interface
domain/repository/StepRepository.kt             # Daily step records + escrow + Health Connect methods
domain/repository/WalkingEncounterRepository.kt # Walking encounter interface
domain/usecase/CalculateUpgradeCost.kt          # Cost formula: baseCost * scaling^level
domain/usecase/CanAffordUpgrade.kt              # Affordability check against wallet
domain/usecase/PurchaseUpgrade.kt               # Deducts Steps, increments upgrade level
domain/usecase/QuickInvest.kt                   # Recommends cheapest affordable upgrade
domain/usecase/ResolveStats.kt                  # Workshop + in-round levels → ResolvedStats
domain/usecase/CalculateDamage.kt               # Raw damage + crit roll + damage/meter bonus → DamageResult
domain/usecase/CalculateDefense.kt              # Damage reduction (cap 75%) + flat block
domain/usecase/UpdateBestWave.kt                # Compares wave to stored best, persists if new record
domain/usecase/CheckTierUnlock.kt               # Checks wave milestones for tier unlock eligibility
domain/usecase/ActivateOverdrive.kt              # Validates overdrive activation (balance + once-per-round)
```

## Presentation Layer

```
presentation/MainActivity.kt                      # Single Activity, Scaffold + NavHost + BottomNavBar, permissions
presentation/HealthConnectPermissionActivity.kt    # Privacy policy stub for Health Connect
presentation/navigation/Screen.kt                 # Sealed class: 5 routes (Home, Workshop, Battle, Labs, Stats)
presentation/navigation/BottomNavBar.kt            # Bottom navigation bar with 5 items
presentation/home/HomeViewModel.kt                 # @HiltViewModel: combines profile + step flows → HomeUiState
presentation/home/HomeUiState.kt                   # UI state: steps, balance, tier, biome, bestWave
presentation/home/HomeScreen.kt                    # Dashboard: step card, currencies, tier selector, battle button
presentation/home/TierSelector.kt                  # Horizontal tier chip row with lock/unlock states
presentation/workshop/WorkshopViewModel.kt         # @HiltViewModel: upgrades + wallet → WorkshopUiState
presentation/workshop/WorkshopUiState.kt           # UI state: upgrade list, balance, selected category
presentation/workshop/WorkshopScreen.kt            # 3-tab layout, upgrade list, Quick Invest FAB
presentation/workshop/UpgradeCard.kt               # Reusable upgrade card (affordable/expensive/maxed states)
presentation/battle/BattleScreen.kt                # Compose wrapper: AndroidView + overlays (HUD, pause, post-round), auto-pause
presentation/battle/BattleViewModel.kt             # @HiltViewModel: round lifecycle (start, end, quit, play again), stats polling
presentation/battle/BattleUiState.kt               # UI state: wave, HP, cash, speed, pause, RoundEndState
presentation/battle/GameSurfaceView.kt             # SurfaceView + SurfaceHolder.Callback, manages game loop thread
presentation/battle/GameLoopThread.kt              # Dedicated thread: fixed timestep (60 UPS), accumulator, speed multiplier
presentation/battle/engine/GameEngine.kt           # Central coordinator: entity list, update/render dispatch, wave/collision integration
presentation/battle/engine/Entity.kt               # Abstract base: x, y, width, height, isAlive, update(), render()
presentation/battle/engine/WaveSpawner.kt          # Wave lifecycle: 26s spawn + 9s cooldown, enemy composition by wave
presentation/battle/engine/EnemyScaler.kt          # Wave-based stat scaling (1.05^wave), cash rewards per type
presentation/battle/engine/CollisionSystem.kt      # Projectile↔enemy and enemy projectile↔ziggurat collision
presentation/battle/entities/ZigguratEntity.kt     # 5-layer ziggurat, nearest-enemy targeting, HP tracking
presentation/battle/entities/ProjectileEntity.kt   # Moves toward target, self-destructs on arrival
presentation/battle/entities/EnemyEntity.kt        # 6 enemy types, movement, melee/ranged attack, mini HP bar
presentation/battle/entities/EnemyProjectileEntity.kt # Ranged enemy projectiles targeting ziggurat
presentation/battle/entities/OrbEntity.kt          # Orbiting projectiles circling ziggurat, per-enemy hit cooldown
presentation/battle/ui/HealthBarRenderer.kt        # HP bar: green→yellow→red gradient, numeric text
presentation/battle/ui/InRoundUpgradeMenu.kt      # In-round upgrade menu: 3 tabs, purchase with Cash
presentation/battle/ui/PostRoundOverlay.kt         # Post-round summary: wave, kills, cash, time, new record banner
presentation/battle/ui/PauseOverlay.kt             # Pause overlay: Resume + Quit Round buttons
presentation/battle/ui/BiomeTransitionOverlay.kt   # Full-screen biome reveal overlay with step count
presentation/battle/ui/OverdriveMenu.kt            # Overdrive type selection (4 options, cost, affordability)
presentation/battle/biome/BiomeTheme.kt            # 5 biome color palettes (sky, ground, ziggurat, enemy, particles)
presentation/battle/biome/BackgroundRenderer.kt    # Gradient sky + ambient particle system per biome
presentation/ui/theme/Color.kt                     # Compose color definitions
presentation/ui/theme/Theme.kt                     # Compose theme setup (Material3)
```

## Service Layer

```
service/StepCounterService.kt        # Foreground service (health type), START_STICKY, collects sensor flow
service/StepNotificationManager.kt   # Notification channel + builder, 30s throttled updates
service/BootReceiver.kt              # BOOT_COMPLETED → restart StepCounterService
service/StepSyncWorker.kt            # @HiltWorker CoroutineWorker, 15-min periodic: sensor catch-up + HC sync
service/StepSyncScheduler.kt         # Enqueues periodic WorkManager request
```

## Test Layer

All paths relative to `app/src/test/java/com/whitefang/stepsofbabylon/`.

```
fakes/FakePlayerRepository.kt                    # In-memory StateFlow-backed fake for PlayerRepository
fakes/FakeWorkshopRepository.kt                  # In-memory StateFlow-backed fake for WorkshopRepository
domain/usecase/CalculateUpgradeCostTest.kt        # Cost formula: baseCost × scaling^level, all 23 types
domain/usecase/CanAffordUpgradeTest.kt            # Affordability checks against wallet
domain/usecase/QuickInvestTest.kt                 # Cheapest affordable upgrade recommendation
domain/usecase/PurchaseUpgradeTest.kt             # Purchase flow with fake repos
domain/usecase/UpdateBestWaveTest.kt              # Best wave tracking, new record detection
domain/usecase/CheckTierUnlockTest.kt             # Tier unlock logic against wave milestones
domain/usecase/ActivateOverdriveTest.kt           # Overdrive activation validation
domain/usecase/ResolveStatsTest.kt                # Multiplicative stacking, all stat caps
domain/usecase/CalculateDamageTest.kt             # Crit/no-crit with injectable Random, damage/meter bonus
domain/usecase/CalculateDefenseTest.kt            # Percent reduction, flat block, floor at 0
domain/model/TierConfigTest.kt                    # All 10 tiers, battle conditions, invalid tier
domain/model/BiomeTest.kt                         # All tier→biome mappings
domain/model/CardLoadoutTest.kt                   # Max 3, no duplicates, add/remove
domain/model/UltimateWeaponLoadoutTest.kt         # Max 3, no duplicates, add/remove
domain/model/UpgradeTypeTest.kt                   # 23 entries, category counts, valid configs
domain/model/EnemyTypeTest.kt                     # 6 entries, multiplier correctness
domain/model/BattleConditionEffectsTest.kt        # All tier condition modifiers verified
presentation/battle/engine/EnemyScalerTest.kt     # Wave scaling, speed, cash rewards
presentation/battle/biome/BiomeThemeTest.kt       # All 5 biome palettes, ziggurat colors, particles
data/sensor/StepRateLimiterTest.kt                # Normal/burst caps, window expiry, edge cases
```
