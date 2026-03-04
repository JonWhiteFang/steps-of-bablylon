# Source File Index

All paths relative to `app/src/main/java/com/whitefang/stepsofbabylon/`.

```
StepsOfBabylonApp.kt          # @HiltAndroidApp Application class
di/DatabaseModule.kt           # Hilt module providing Room database + DAOs
di/RepositoryModule.kt         # Hilt module binding repository interfaces to impls
data/local/AppDatabase.kt     # Room database definition (7 entities, 7 DAOs)
data/local/Converters.kt      # TypeConverters for JSON maps
data/local/DatabaseKeyManager.kt # SQLCipher passphrase via Android Keystore
data/local/PlayerProfileEntity.kt  # Player profile Room entity
data/local/PlayerProfileDao.kt     # Player profile DAO (with atomic currency adjustments)
data/local/WorkshopUpgradeEntity.kt # Workshop upgrade entity
data/local/WorkshopDao.kt          # Workshop DAO
data/local/LabResearchEntity.kt    # Lab research entity
data/local/LabDao.kt               # Lab research DAO
data/local/CardInventoryEntity.kt  # Card inventory entity
data/local/CardDao.kt              # Card inventory DAO
data/local/UltimateWeaponStateEntity.kt # UW state entity
data/local/UltimateWeaponDao.kt    # UW state DAO
data/local/DailyStepRecordEntity.kt # Daily step record entity
data/local/DailyStepDao.kt         # Daily step record DAO
data/local/WalkingEncounterEntity.kt # Walking encounter entity
data/local/WalkingEncounterDao.kt   # Walking encounter DAO
data/repository/PlayerRepositoryImpl.kt     # Player profile + wallet repository
data/repository/WorkshopRepositoryImpl.kt   # Workshop upgrades repository
data/repository/LabRepositoryImpl.kt        # Lab research repository
data/repository/CardRepositoryImpl.kt       # Card inventory repository
data/repository/UltimateWeaponRepositoryImpl.kt # Ultimate weapon repository
data/repository/StepRepositoryImpl.kt       # Daily step records repository
data/repository/WalkingEncounterRepositoryImpl.kt # Walking encounter repository
domain/model/Currency.kt      # Currency enum (STEPS, CASH, GEMS, POWER_STONES)
domain/model/PlayerWallet.kt  # Wallet data class holding currency balances
domain/model/PlayerProfile.kt # Full player profile (maps from PlayerProfileEntity)
domain/model/ActiveResearch.kt # In-progress lab research
domain/model/OwnedCard.kt     # Player-owned card instance
domain/model/OwnedWeapon.kt   # Player-owned ultimate weapon
domain/model/DailyStepSummary.kt # Daily step record domain model
domain/model/SupplyDrop.kt    # Walking encounter supply drop
domain/model/UpgradeType.kt   # 23 Workshop upgrade types with configs
domain/model/UpgradeCategory.kt # Attack, Defense, Utility categories
domain/model/UpgradeConfig.kt # Upgrade configuration data class
domain/model/Tier.kt          # Tier data class
domain/model/TierConfig.kt    # Full tier table (1–10)
domain/model/BattleCondition.kt # 7 battle condition types
domain/model/Biome.kt         # 5 biomes with forTier() mapping
domain/model/EnemyType.kt     # 6 enemy types with multipliers
domain/model/UltimateWeaponType.kt  # 6 UW types with unlock costs
domain/model/UltimateWeaponLoadout.kt # UW loadout (max 3)
domain/model/OverdriveType.kt # 4 overdrive types with costs
domain/model/ResearchType.kt  # 10 lab research types
domain/model/CardRarity.kt    # Common, Rare, Epic
domain/model/CardType.kt      # 9 card types with effects
domain/model/CardLoadout.kt   # Card loadout (max 3)
domain/model/RoundState.kt    # Transient battle state
domain/repository/PlayerRepository.kt      # Player profile/wallet interface
domain/repository/WorkshopRepository.kt    # Workshop upgrades interface
domain/repository/LabRepository.kt         # Lab research interface
domain/repository/CardRepository.kt        # Card inventory interface
domain/repository/UltimateWeaponRepository.kt # Ultimate weapon interface
domain/repository/StepRepository.kt        # Daily step records interface
domain/repository/WalkingEncounterRepository.kt # Walking encounter interface
domain/usecase/CalculateUpgradeCost.kt  # Cost formula: baseCost * scaling^level
domain/usecase/CanAffordUpgrade.kt      # Affordability check
presentation/MainActivity.kt  # Single Activity (Compose host)
presentation/home/HomeScreen.kt  # Home screen placeholder
presentation/ui/theme/Color.kt   # Compose color definitions
presentation/ui/theme/Theme.kt   # Compose theme setup
```
