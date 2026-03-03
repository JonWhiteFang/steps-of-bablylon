# Project Structure

## Root Layout

```
app/src/main/java/com/whitefang/stepsofbabylon/
├── data/               # Android-dependent layer
│   └── local/          # Room database, entities, DAOs
├── domain/             # Pure Kotlin — no Android imports
│   ├── model/          # Data classes and enums
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use case classes
├── presentation/       # Android/Compose layer
│   ├── home/           # Home screen
│   └── ui/theme/       # Compose theme, colors
├── di/                 # Hilt modules
└── service/            # Foreground service, WorkManager workers
```

## Layer Rules

- `domain/` must have zero Android imports — pure Kotlin only
- `data/` implements domain repository interfaces
- `presentation/` depends on domain, never on data directly
- Hilt modules in `di/` wire data implementations to domain interfaces

## Naming Conventions

- Room entities: `*Entity.kt` (e.g., `PlayerProfileEntity`)
- Repository interfaces: `*Repository.kt` in `domain/repository/`
- Repository implementations: `*RepositoryImpl.kt` in `data/`
- Use cases: descriptive verb phrase (e.g., `CalculateUpgradeCost`, `CanAffordUpgrade`)
- ViewModels: `*ViewModel.kt`
- Compose screens: `*Screen.kt`
- Hilt modules: `*Module.kt`

## Domain Models (existing)

All in `domain/model/`:

- `Currency` — enum: STEPS, CASH, GEMS, POWER_STONES
- `PlayerWallet` — holds currency balances
- `Tier`, `TierConfig` — difficulty tier definitions
- `UpgradeType`, `UpgradeCategory`, `UpgradeConfig` — Workshop upgrade system
- `CardType`, `CardRarity`, `CardLoadout` — Cards system
- `EnemyType`, `BattleCondition`, `RoundState` — Battle system
- `OverdriveType`, `UltimateWeaponType`, `UltimateWeaponLoadout` — Special abilities
- `Biome`, `ResearchType` — Progression systems

## Key Files

| File | Purpose |
|---|---|
| `StepsOfBabylonApp.kt` | `@HiltAndroidApp` Application class |
| `di/DatabaseModule.kt` | Hilt module providing Room DB |
| `data/local/AppDatabase.kt` | Room database definition |
| `data/local/PlayerProfileEntity.kt` | Player profile entity |
| `domain/usecase/CalculateUpgradeCost.kt` | Cost formula: `baseCost × scaling^level` |
| `domain/usecase/CanAffordUpgrade.kt` | Affordability check against wallet |
| `presentation/MainActivity.kt` | Single Activity, Compose host |
| `gradle/libs.versions.toml` | All dependency versions |
| `app/schemas/` | Room schema exports (commit these) |
| `docs/plans/` | Numbered implementation plans (01–30) |

## Development Plans

Plans live in `docs/plans/` as `plan-NN-name.md`. The master plan is at `docs/plans/master-plan.md`. All 30 plan files are written. Always check the relevant plan file before implementing a feature — it contains detailed requirements and task breakdowns.
