# Changelog

All notable changes to Steps of Babylon are documented here.

## [1.0.0] — 2026-03-10

### Core Gameplay
- Step-powered progression: earn Steps currency by real-world walking via device step counter
- Workshop with 23 permanent upgrade types across Attack, Defense, and Utility categories
- Tower defense battle system with custom SurfaceView renderer and fixed-timestep game loop
- 6 enemy types (Basic, Fast, Tank, Ranged, Boss, Scatter) with wave-based spawning
- Stats resolution engine combining Workshop (permanent) × In-Round (temporary) upgrades multiplicatively
- In-round upgrades purchased with Cash earned from kills, with interest mechanic
- Crit system, knockback, lifesteal, thorn damage, death defy, damage/meter bonus
- Advanced combat: orbiting projectiles, multishot, bounce shot

### Progression
- 10 tier system with wave-based unlock requirements and escalating battle conditions (Tier 6+)
- 5 narrative biomes: Hanging Gardens, Burning Sands, Frozen Ziggurats, Underworld of Kur, Celestial Gate
- Labs research system with 10 research types, real-time background timers, up to 4 slots, Gem rush
- Cards system with 9 card types, 3 rarities, Card Dust upgrades, loadout of 3
- 6 Ultimate Weapons unlocked with Power Stones, loadout of 3, cooldown-based activation
- 4 Step Overdrive types for mid-battle 60-second combat buffs

### Economy & Rewards
- Walking Encounters with seeded random Supply Drops delivered via push notification
- Weekly step challenges with Power Stone rewards (50k/75k/100k thresholds)
- Daily login streaks with Gem and Power Stone rewards
- 6 walking milestones from First Steps to Globe Trotter
- 3 random daily missions refreshed at midnight (walking/battle/upgrade categories)
- Wave milestone Power Stone awards on personal-best waves

### Battle Polish
- Particle effects: projectile trails, enemy death bursts (6 types), UW activation spectacles, overdrive auras
- Screen shake with decaying amplitude
- Wave announcements with boss warnings and cooldown countdowns
- Floating text for cash pickups
- Biome-themed color palettes and ambient background particles
- Sound effects (7 types) with volume control and shoot throttling
- Speed controls: 1x / 2x / 4x

### Infrastructure
- Foreground step-counting service (health type, START_STICKY) with boot receiver
- WorkManager 15-minute periodic sync with Health Connect cross-validation and gap-filling
- Activity Minute Parity: indoor workout minutes converted to step-equivalents
- Anti-cheat: 200 steps/min rate limit, step velocity analysis, 50k daily ceiling, graduated Health Connect cross-validation (4 offense levels)
- SQLCipher encrypted Room database with Android Keystore key management
- Home screen widget (2×2) with step count display
- Smart upgrade proximity reminders
- Milestone and wave record notifications

### Monetization (Stub)
- Store screen with Gem packs, ad removal, Season Pass, and cosmetic items
- Stub billing and reward ad implementations (real SDK integration in future update)

### Stats & UI
- Stats screen with walking history bar charts (daily/weekly/monthly), battle stats, all-time aggregates
- Currency dashboard with weekly challenge progress and login streak tracking
- Missions screen with daily missions and walking milestones
- Settings screen with 4 notification toggles
- 12-screen Compose navigation with bottom nav bar

### Testing
- 347 JVM unit tests covering all use cases, domain models, balance validation, ViewModels, anti-cheat, and effects

### Release Prep
- R8/ProGuard rules hardened for Room, Hilt, SQLCipher, Health Connect, sensors, WorkManager
- Release signing configuration with gitignored keystore.properties
- Privacy policy and Play Store listing text

## [Unreleased]

### Documentation — All Plan Files Written

- Written detailed plan files for Plans 02–30 in `docs/plans/`
- Each plan includes: objective, task breakdown, file summary, completion criteria
- Updated `AGENTS.md`, `.kiro/steering/structure.md` to reflect Plan 01 completion

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
