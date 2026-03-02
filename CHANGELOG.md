# Changelog

All notable changes to Steps of Babylon are documented here, organized by plan completion.

## [Unreleased]

### Plan 01 — Domain Models & Currency System (Complete)

- Added `Currency` enum (STEPS, CASH, GEMS, POWER_STONES)
- Added `PlayerWallet` data class
- Added all 23 `UpgradeType` entries with config (Attack/Defense/Utility)
- Added `UpgradeCategory` enum
- Added `Tier`, `TierConfig` with full tier table (1–10) matching GDD
- Added `BattleCondition` enum (7 conditions)
- Added `Biome` enum with `forTier()` mapping
- Added `EnemyType` enum with speed/health/damage multipliers
- Added `UltimateWeaponType` enum and `UltimateWeaponLoadout` (max 3)
- Added `OverdriveType` enum with step costs and durations
- Added `ResearchType` enum with 10 lab research types
- Added `CardRarity`, `CardType` (9 cards), `CardLoadout` (max 3)
- Added `RoundState` transient battle state model
- Added `CalculateUpgradeCost` use case: `baseCost * (scaling ^ level)`
- Added `CanAffordUpgrade` use case

### Scaffold (Complete)

- Gradle 9.3.1 project with Kotlin DSL and version catalog
- Hilt DI setup with `@HiltAndroidApp`
- Room database skeleton (`AppDatabase`, `PlayerProfileEntity`)
- Compose theme (Color, Theme)
- Single Activity with HomeScreen placeholder
- `DatabaseModule` Hilt provider
