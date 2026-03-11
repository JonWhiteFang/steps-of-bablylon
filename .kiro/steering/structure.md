# Project Structure

## Root Layout

```
app/src/main/java/com/whitefang/stepsofbabylon/
├── data/
│   ├── local/          # Room database, entities, DAOs, TypeConverters, SQLCipher key manager
│   ├── repository/     # Repository implementations (Room-backed, @Inject constructors)
│   ├── sensor/         # Step sensor data source, rate limiter, velocity analyzer, ingestion preferences, daily step manager
│   ├── healthconnect/  # Health Connect client, step reader, cross-validator, gap filler, activity minutes
│   ├── billing/        # StubBillingManager (simulated IAP purchases)
│   └── ads/            # StubRewardAdManager (simulated reward ads)
├── domain/             # Pure Kotlin — no Android imports
│   ├── model/          # Data classes and enums
│   ├── repository/     # Repository interfaces (Flow-based)
│   └── usecase/        # Use case classes (plain Kotlin, no @Inject)
├── presentation/       # Android/Compose layer
│   ├── navigation/     # Screen routes, BottomNavBar
│   ├── home/           # Home screen, ViewModel, UiState
│   ├── workshop/       # Workshop screen, ViewModel, UpgradeCard
│   ├── battle/         # Battle renderer (SurfaceView, game loop, entities)
│   │   ├── engine/     # GameEngine, Entity, WaveSpawner, EnemyScaler, CollisionSystem
│   │   ├── entities/   # ZigguratEntity, ProjectileEntity, EnemyEntity, EnemyProjectileEntity, OrbEntity
│   │   ├── effects/   # ParticlePool, EffectEngine, ScreenShake, DeathEffect, UWVisualEffect, OverdriveAuraEffect, WaveAnnouncement, FloatingText, ProjectileTrailEffect, ReducedMotionCheck
│   │   ├── biome/      # BiomeTheme, BackgroundRenderer (gradient sky + ambient particles)
│   │   └── ui/         # HealthBarRenderer, InRoundUpgradeMenu, PostRoundOverlay, PauseOverlay, BiomeTransitionOverlay, OverdriveMenu, UltimateWeaponBar
│   ├── weapons/        # UltimateWeaponScreen, UltimateWeaponViewModel
│   ├── labs/           # LabsScreen, LabsViewModel
│   ├── cards/          # CardsScreen, CardsViewModel
│   ├── supplies/       # UnclaimedSuppliesScreen, UnclaimedSuppliesViewModel
│   ├── economy/        # CurrencyDashboardScreen, CurrencyDashboardViewModel
│   ├── missions/       # MissionsScreen, MissionsViewModel
│   ├── settings/       # NotificationSettingsScreen, NotificationSettingsViewModel
│   ├── stats/          # StatsScreen, StatsViewModel, WalkingHistoryChart
│   ├── store/          # StoreScreen, StoreViewModel
│   ├── audio/          # SoundManager (SoundPool wrapper, 7 effects, volume/mute)
│   └── ui/theme/       # Compose theme, colors (Material3)
├── di/                 # Hilt modules (DatabaseModule, RepositoryModule, StepModule, HealthConnectModule, BillingModule, AdModule)
└── service/            # Foreground step-counting service, WorkManager workers, boot receiver

app/src/test/java/com/whitefang/stepsofbabylon/
├── fakes/              # In-memory fake repositories (FakePlayerRepository, FakeWorkshopRepository, FakeUltimateWeaponRepository, FakeLabRepository, FakeCardRepository, FakeWalkingEncounterRepository, FakeStepRepository, FakeCosmeticRepository, FakeBillingManager, FakeRewardAdManager, FakeMilestoneDao, FakeDailyMissionDao, FakeDailyLoginDao, FakeWeeklyChallengeDao, FakeDailyStepDao)
├── domain/
│   ├── model/          # Domain model invariant tests (TierConfig, Biome, Loadouts, UpgradeType, EnemyType)
│   └── usecase/        # Use case tests (cost, damage, defense, stats, purchase, best wave)
├── presentation/
│   └── battle/
│       ├── engine/     # EnemyScaler tests
│       └── biome/      # BiomeTheme tests
│       └── effects/    # ParticlePool, ScreenShake, DeathEffect tests
└── data/sensor/        # StepRateLimiter tests
└── balance/            # Step economy, cost curves, enemy scaling, tier progression, cash, cards, UW, supply drops
```

## Layer Rules

- `domain/` must have zero Android imports — pure Kotlin only
- `data/` implements domain repository interfaces via `@Inject constructor`
- `presentation/` depends on domain, never on data directly
- Hilt modules in `di/` wire data implementations to domain interfaces
- Use cases are plain Kotlin classes — no Hilt annotations, injected via constructor

## Naming Conventions

| Pattern | Location | Example |
|---|---|---|
| `*Entity.kt` | `data/local/` | `PlayerProfileEntity` |
| `*Dao.kt` | `data/local/` | `PlayerProfileDao` |
| `*Repository.kt` | `domain/repository/` | `PlayerRepository` |
| `*RepositoryImpl.kt` | `data/repository/` | `PlayerRepositoryImpl` |
| `*ViewModel.kt` | `presentation/*/` | `WorkshopViewModel` |
| `*Screen.kt` | `presentation/*/` | `HomeScreen` |
| `*Module.kt` | `di/` | `DatabaseModule` |
| Use cases | `domain/usecase/` | `CalculateUpgradeCost`, `CanAffordUpgrade` |

## Domain Models

All in `domain/model/`:

- `Currency` — enum: STEPS, CASH, GEMS, POWER_STONES
- `PlayerWallet` — holds currency balances
- `PlayerProfile` — full player profile (maps from `PlayerProfileEntity`)
- `Tier`, `TierConfig` — difficulty tier definitions
- `UpgradeType`, `UpgradeCategory`, `UpgradeConfig` — Workshop upgrade system
- `CardType`, `CardRarity`, `CardLoadout` — Cards system
- `OwnedCard` — player-owned card instance
- `EnemyType`, `BattleCondition`, `RoundState` — Battle system
- `ZigguratBaseStats` — Base stat constants for the ziggurat
- `ResolvedStats` — Computed combat stats from workshop + in-round upgrades
- `OverdriveType`, `UltimateWeaponType`, `UltimateWeaponLoadout` — Special abilities
- `OwnedWeapon` — player-owned ultimate weapon
- `Biome`, `ResearchType`, `ActiveResearch` — Progression systems
- `DailyStepSummary` — daily step record domain model
- `SupplyDrop` — walking encounter supply drop
- `SupplyDropTrigger` — 4 trigger types with notification messages
- `SupplyDropReward` — 4 reward types (Steps, Gems, Power Stones, Card Dust)
- `DropGeneratorState` — generator state tracking
- `Milestone` — 6 walking milestones with step thresholds and rewards
- `MilestoneReward` — sealed class: Gems, PowerStones, Cosmetic
- `DailyMissionType` — 6 daily mission types (walking/battle/upgrade)
- `MissionCategory` — mission categories: WALKING, BATTLE, UPGRADE
- `BillingProduct` — 5 billing products + PurchaseResult sealed class
- `AdPlacement` — 3 ad placements + AdResult sealed class
- `CosmeticCategory` — 3 cosmetic categories (ziggurat, projectile, enemy)
- `CosmeticItem` — cosmetic item domain model

## Key Files

| File | Purpose |
|---|---|
| `StepsOfBabylonApp.kt` | `@HiltAndroidApp`, `Configuration.Provider` (HiltWorkerFactory) |
| `di/DatabaseModule.kt` | Hilt module: Room DB (SQLCipher) + all 12 DAOs |
| `di/RepositoryModule.kt` | Hilt module: binds all 8 repository interfaces to impls |
| `di/StepModule.kt` | Hilt module: provides SensorManager |
| `di/HealthConnectModule.kt` | Hilt module: Health Connect organizational module |
| `di/BillingModule.kt` | Hilt module: binds BillingManager to stub |
| `di/AdModule.kt` | Hilt module: binds RewardAdManager to stub |
| `data/local/AppDatabase.kt` | Room database (12 entities, 12 DAOs, version 7) |
| `data/local/DatabaseKeyManager.kt` | SQLCipher passphrase via Android Keystore |
| `data/local/Converters.kt` | TypeConverters for `Map<Int,Int>` and `Map<String,Int>` (JSON) |
| `data/sensor/StepSensorDataSource.kt` | TYPE_STEP_COUNTER wrapper, emits deltas via callbackFlow |
| `data/sensor/StepRateLimiter.kt` | Anti-cheat: 200 steps/min cap (250 burst) |
| `data/sensor/DailyStepManager.kt` | Orchestrates rate limit → 50k ceiling → Room persist |
| `service/StepCounterService.kt` | Foreground service (health type), START_STICKY |
| `service/StepSyncWorker.kt` | @HiltWorker, 15-min periodic catch-up + HC sync |
| `domain/usecase/CalculateUpgradeCost.kt` | Cost formula: `baseCost × scaling^level` |
| `domain/usecase/CanAffordUpgrade.kt` | Affordability check against wallet |
| `domain/usecase/ResolveStats.kt` | Workshop + in-round levels → ResolvedStats |
| `domain/usecase/CalculateDamage.kt` | Raw damage + crit roll + damage/meter bonus |
| `domain/usecase/CalculateDefense.kt` | Damage reduction (cap 75%) + flat block |
| `domain/usecase/UpdateBestWave.kt` | Compares wave to stored best, persists if new record |
| `domain/usecase/CheckTierUnlock.kt` | Checks wave milestones for tier unlock eligibility |
| `domain/usecase/ActivateOverdrive.kt` | Validates overdrive activation (balance + once-per-round) |
| `domain/usecase/UnlockUltimateWeapon.kt` | Checks Power Stone balance, deducts, unlocks UW |
| `domain/usecase/UpgradeUltimateWeapon.kt` | Cost scaling per level, max level 10 |
| `presentation/MainActivity.kt` | Single Activity, Scaffold + NavHost + BottomNavBar (hidden during battle), permissions |
| `presentation/navigation/Screen.kt` | 12 navigation routes (Home, Workshop, Battle, Labs, Stats, Weapons, Cards, Supplies, Economy, Missions, Settings, Store) |
| `presentation/home/HomeViewModel.kt` | Combines profile + step flows into HomeUiState |
| `presentation/battle/GameSurfaceView.kt` | SurfaceView managing game loop thread lifecycle |
| `presentation/battle/GameLoopThread.kt` | Fixed timestep (60 UPS), accumulator, speed multiplier |
| `presentation/battle/engine/GameEngine.kt` | Central coordinator: entity list, update/render dispatch, wave/collision integration |
| `presentation/battle/engine/WaveSpawner.kt` | Wave lifecycle: 26s spawn + 9s cooldown, enemy composition by wave |
| `presentation/battle/engine/EnemyScaler.kt` | Wave-based stat scaling (1.05^wave), cash rewards per type |
| `presentation/battle/engine/CollisionSystem.kt` | Projectile↔enemy and enemy projectile↔ziggurat collision |
| `presentation/battle/BattleViewModel.kt` | Loads tier, polls engine state, exposes BattleUiState + BattleEvent |
| `gradle/libs.versions.toml` | All dependency versions |
| `app/schemas/` | Room schema exports (commit these) |
| `docs/plans/` | Numbered implementation plans (01–30) |

## Development Plans

Plans live in `docs/plans/` as `plan-NN-name.md`. The master plan is at `docs/plans/master-plan.md`. All 30 plan files are written. Always check the relevant plan file before implementing a feature.
