# AGENTS.md — Steps of Babylon

## Project Overview

Steps of Babylon is an Android mobile game that combines idle tower defense gameplay with a real-world step counter. Players earn **Steps** by physically walking, then spend them to upgrade an ancient ziggurat that fights wave-based battles against mythic enemies. Progression is gated entirely by physical activity.

See `docs/StepsOfBabylon_GDD.md` for the full game design document.

## Project Memory (read first)

| File | Purpose |
|---|---|
| `docs/agent/START_HERE.md` | Agent contract — what this is, how to work here |
| `docs/agent/STATE.md` | One-page project snapshot (current objective, priorities, next actions) |
| `docs/agent/CONSTRAINTS.md` | Architecture invariants, security rules, "never do" list |
| `docs/agent/RUN_LOG.md` | Append-only log of every agent session |
| `docs/agent/DECISIONS/` | Architecture Decision Records (ADRs) |

Operating rules:
- Always do Context Preflight before planning (see `.kiro/steering/11-agent-protocol.md`).
- Always update STATE.md + append RUN_LOG.md at end of run.
- Record meaningful decisions as ADRs in `docs/agent/DECISIONS/`.

## Tech Stack

- **Language:** Kotlin (JVM target 17)
- **Package:** `com.whitefang.stepsofbabylon`
- **Min SDK:** 34 (Android 14)
- **Target/Compile SDK:** 36
- **Version:** 0.1.0 (versionCode 1)
- **Architecture:** MVVM + Clean Architecture
- **UI:** Jetpack Compose (menus/screens) + SurfaceView (battle renderer)
- **DI:** Hilt (with KSP)
- **Database:** Room (SQLite) with SQLCipher encryption — offline-first, all game state stored locally
- **Background:** WorkManager + Foreground Service (step counting)
- **Step Tracking:** Android Sensor API (`TYPE_STEP_COUNTER`) + Health Connect SDK (cross-validation, Activity Minute Parity)
- **Build:** Gradle 9.3.1 (Kotlin DSL), version catalog at `gradle/libs.versions.toml`
- **Security:** SQLCipher (database encryption), Android Keystore (key management), R8 (obfuscation), network security config (cleartext blocked)

## Architecture

```
app/src/main/java/com/whitefang/stepsofbabylon/
├── data/           # Android-dependent layer
│   ├── local/      # Room database, entities, DAOs
│   ├── repository/ # Repository implementations (Room-backed)
│   ├── sensor/     # Step sensor data source, rate limiter, daily step manager
│   └── healthconnect/ # Health Connect client, step reader, cross-validator, gap filler
├── domain/         # Pure Kotlin — no Android imports
│   ├── model/      # Currency, PlayerWallet, PlayerProfile, BattleConditionEffects, and all game domain models
│   ├── repository/ # Repository interfaces
│   └── usecase/    # CalculateUpgradeCost, CanAffordUpgrade, ResolveStats, CalculateDamage, CalculateDefense, PurchaseUpgrade, QuickInvest, UpdateBestWave, CheckTierUnlock, ActivateOverdrive, UnlockUltimateWeapon, UpgradeUltimateWeapon
├── presentation/   # ViewModels, Compose screens, SurfaceView battle renderer
│   ├── navigation/ # Screen routes, BottomNavBar
│   ├── home/       # HomeScreen, TierSelector
│   ├── workshop/   # WorkshopScreen, UpgradeCard
│   ├── battle/     # BattleScreen, BattleViewModel
│   │   ├── engine/ # GameEngine, WaveSpawner, EnemyScaler, CollisionSystem
│   │   ├── entities/ # ZigguratEntity, EnemyEntity, ProjectileEntity, OrbEntity
│   │   ├── biome/  # BiomeTheme, BackgroundRenderer
│   │   └── ui/     # InRoundUpgradeMenu, PostRoundOverlay, PauseOverlay, HealthBarRenderer, BiomeTransitionOverlay, OverdriveMenu, UltimateWeaponBar
│   ├── weapons/    # UltimateWeaponScreen, UltimateWeaponViewModel
│   └── ui/theme/   # Color, Theme
├── di/             # Hilt modules (DatabaseModule, RepositoryModule, StepModule, HealthConnectModule)
└── service/        # Foreground step-counting service, WorkManager workers (Plan 04)
```

Follow Clean Architecture layers: `presentation → domain ← data`. The domain layer has zero Android dependencies.

See `.kiro/steering/source-files.md` for the full source file index.

## Plans & Roadmap

Development follows a 30-plan master plan. See `docs/plans/master-plan.md` for the full index, dependency graph, and status tracker.

### Key Documents

| Document | Path |
|---|---|
| Game Design Document | `docs/StepsOfBabylon_GDD.md` |
| Master Plan (30 plans) | `docs/plans/master-plan.md` |
| Plan 01: Domain Models | `docs/plans/plan-01-domain-models.md` |
| Plan 02: Database & DAOs | `docs/plans/plan-02-database.md` |
| Battle Formulas | `docs/battle-formulas.md` |
| Database Schema | `docs/database-schema.md` |
| Step Tracking | `docs/step-tracking.md` |
| Monetization | `docs/monetization.md` |
| Architecture | `docs/architecture.md` |

### Full Plan Index

| # | Plan | Description | Dependencies |
|---|---|---|---|
| 01 | Domain Models & Currency System | Core domain models, enums, cost calculation engine. Pure Kotlin. | Scaffold |
| 02 | Room Database & DAOs | All Room entities, DAOs, migration strategy. | Plan 01 |
| 03 | Repository Layer | Repository interfaces (domain) + Room-backed impls (data). Flows. | Plan 02 |
| 04 | Step Counter Service | Foreground service, TYPE_STEP_COUNTER, WorkManager sync, anti-cheat. | Plan 03 |
| 05 | Health Connect Integration | Cross-validation, Activity Minute Parity, gap-filling. | Plan 04 |
| 06 | Home Screen & Navigation | Compose nav graph, dashboard, bottom nav bar. | Plan 03 |
| 07 | Workshop Screen & Upgrades | Workshop UI (Attack/Defense/Utility tabs), Step purchases. | Plan 06 |
| 08 | Battle Renderer — Game Loop & Ziggurat | Custom SurfaceView, game loop thread, fixed timestep, ziggurat. | Plan 06 |
| 09 | Battle System — Enemies & Waves | Enemy entities, wave spawning (26s+9s), scaling, collision. | Plan 08 |
| 10 | Battle System — Stats & Combat | Stats resolution (Workshop × In-Round), crit, knockback, lifesteal. | Plan 09 |
| 11 | In-Round Upgrades & Cash Economy | Cash from kills, in-round upgrade menu, interest, free upgrade chance. | Plan 10 |
| 12 | Round Lifecycle & Post-Round | Start/end flow, speed controls, post-round summary. | Plan 11 |
| 13 | Tier System & Progression | Tier unlock logic, cash multipliers, battle conditions (Tier 6+). | Plan 12 |
| 14 | Step Overdrive | 4 overdrive types, Step cost, 60s buff, once-per-round. | Plan 12 |
| 15 | Ultimate Weapons | 6 UW types, Power Stone unlock/upgrade, loadout (3 max), cooldowns. | Plan 12 |
| 16 | Labs System | Research projects, Step cost + real-time duration, 1-4 slots, Gem rush. | Plan 07 |
| 17 | Cards System | Card packs (Gem purchase), 3 rarities, Card Dust, loadout (3 max). | Plan 07 |
| 18 | Narrative Biome Progression | 5 biomes, environment art swap, enemy themes, cinematics. | Plan 13 |
| 19 | Walking Encounters & Supply Drops | Seeded random drops, push notifications, Unclaimed Supplies inbox. | Plan 04 |
| 20 | Power Stone & Gem Economy | Weekly challenges, milestones, daily login streaks. | Plan 04 |
| 21 | Milestones & Daily Missions | Walking milestones, 3 daily missions, midnight refresh. | Plan 20 |
| 22 | Stats & History Screen | Walking history charts, battle stats, all-time stats. | Plan 06 |
| 23 | Notifications & Widget | Persistent notification, home widget (2x2), smart reminders. | Plan 04 |
| 24 | Accessibility | TalkBack, audio cues, color-blind modes, adjustable text. | Plan 18 |
| 25 | Anti-Cheat & Validation | Rate limiting, daily ceiling, Health Connect cross-validation. | Plan 05 |
| 26 | Monetization & Ads | Reward ads, ad removal IAP, Gem packs, Season Pass, cosmetics. | Plan 17 |
| 27 | Polish & Visual Effects | Projectile/UW/Overdrive effects, death anims, sound integration. | Plan 18 |
| 28 | Balancing & Tuning | Step economy, Workshop cost curves, enemy scaling, Card balance. | Plan 27 |
| 29 | Testing & QA | Unit tests, ViewModel tests, DAO tests, sensor tests, UI tests. | Plan 28 |
| 30 | Release Prep | ProGuard/R8, signing, Play Store assets, privacy policy, AAB. | Plan 29 |

### Dependency Graph

```mermaid
graph TD
    S[Scaffold] --> P01[01: Domain Models]
    P01 --> P02[02: Database & DAOs]
    P02 --> P03[03: Repository Layer]
    P03 --> P04[04: Step Counter Service]
    P03 --> P06[06: Home Screen & Nav]
    P04 --> P05[05: Health Connect]
    P04 --> P19[19: Walking Encounters]
    P04 --> P20[20: Premium Currencies]
    P04 --> P23[23: Notifications & Widget]
    P05 --> P25[25: Anti-Cheat]
    P06 --> P07[07: Workshop Screen]
    P06 --> P08[08: Battle Renderer]
    P06 --> P22[22: Stats & History]
    P07 --> P16[16: Labs System]
    P07 --> P17[17: Cards System]
    P08 --> P09[09: Enemies & Waves]
    P09 --> P10[10: Stats & Combat]
    P10 --> P11[11: In-Round Upgrades]
    P11 --> P12[12: Round Lifecycle]
    P12 --> P13[13: Tier System]
    P12 --> P14[14: Step Overdrive]
    P12 --> P15[15: Ultimate Weapons]
    P13 --> P18[18: Biome Progression]
    P17 --> P26[26: Monetization & Ads]
    P18 --> P24[24: Accessibility]
    P18 --> P27[27: Polish & VFX]
    P20 --> P21[21: Milestones & Missions]
    P27 --> P28[28: Balancing & Tuning]
    P28 --> P29[29: Testing & QA]
    P29 --> P30[30: Release Prep]
```

### Critical Path

01 → 02 → 03 → 06 → 08 → 09 → 10 → 11 → 12 → 13 → 18 → 27 → 28 → 29 → 30

### Current Status

- [x] Project scaffold (Gradle, Hilt, Room skeleton, Compose theme, Home placeholder)
- [x] **Plan 01: Domain Models & Currency System** ✓
- [x] **Plan 02: Room Database & DAOs** ✓
- [x] **Plan 03: Repository Layer** ✓
- [x] **Plan 04: Step Counter Service** ✓
- [x] **Plan 05: Health Connect Integration** ✓
- [x] **Plan 06: Home Screen & Navigation** ✓
- [x] **Plan 07: Workshop Screen & Upgrades** ✓
- [x] **Plan 08: Battle Renderer** ✓
- [x] **Plan 09: Battle System — Enemies & Waves** ✓
- [x] **Plan 10: Battle System — Stats & Combat** ✓
- [x] **Plan 10b: Advanced Combat (Orbs, Multishot, Bounce)** ✓
- [x] **Plan 11: In-Round Upgrades & Cash Economy** ✓
- [x] **Plan 12: Round Lifecycle & Post-Round** ✓
- [x] **Domain Layer Unit Tests** ✓ (93 JVM tests — use cases, models, formulas, anti-cheat, tier system)
- [x] **Plan 13: Tier System & Progression** ✓
- [x] **Plan 18: Narrative Biome Progression** ✓
- [x] **Plan 14: Step Overdrive** ✓
- [x] **Plan 15: Ultimate Weapons** ✓
- [ ] **Plan 27: Polish & Visual Effects** ← next on critical path

### Parallelizable Branches (after dependencies met)

- Round Lifecycle: Plan 12 (complete)
- Battle extensions: Plans 14/15 (ready now — Plan 12 complete, can run in parallel)
- Labs: Plan 16 (ready now — Plan 07 complete)
- Cards: Plan 17 (ready now — Plan 07 complete)
- Stats: Plan 22 (ready now — Plan 06 complete)
- Anti-cheat: Plan 25 (ready now — Plan 05 complete)
- Walking features: Plans 19/20/21/23 (ready now — Plan 04 complete)

## Key Domain Concepts

- **Steps** — primary permanent currency, earned only from real-world walking/activity. Never generated in-game.
- **Cash** — temporary in-round currency from killing enemies. Resets each round.
- **Gems** — permanent premium currency from milestones and daily logins.
- **Power Stones** — permanent currency for Ultimate Weapons, from weekly challenges.
- **Workshop** — permanent upgrades (Attack/Defense/Utility) purchased with Steps. 23 upgrade types total.
- **Labs** — time-gated research projects initiated with Steps, completed over real time. 10 research types.
- **Cards** — per-round bonus items (3 equipped max), acquired via Gem-purchased packs. 9 card types, 3 rarities.
- **Ultimate Weapons (UWs)** — activatable abilities (3 equipped max), unlocked with Power Stones. 6 UW types.
- **Tiers** — difficulty levels (1–10+) with escalating battle conditions and cash multipliers.
- **Biomes** — narrative environments tied to tier ranges (Hanging Gardens → Burning Sands → Frozen Ziggurats → Underworld of Kur → Celestial Gate).
- **Step Overdrive** — mid-battle mechanic to burn Steps for a 60-second combat boost (once per round). 4 types.
- **Walking Encounters** — Supply Drop rewards delivered via push notifications during walks.
- **Activity Minute Parity** — Health Connect Active Minutes converted to Step-equivalents for indoor workouts.
- **Enemies** — 6 types (Basic, Fast, Tank, Ranged, Boss, Scatter) with distinct speed/health/damage multipliers.

## Conventions

- Use Kotlin coroutines and Flow for all async operations.
- ViewModels expose `StateFlow` to Compose UI.
- Room is the single source of truth for game state.
- All upgrade cost formulas follow: `baseCost * (scaling ^ level)`.
- Step counting must work reliably when the app is backgrounded or killed.
- Steps can **never** be generated passively in-game — this is a hard design rule.
- Anti-cheat: rate-limit at 200 steps/min, daily ceiling of 50,000 steps, cross-validate with Health Connect.
- Domain models are pure Kotlin — no Android imports in `domain/`.
- Loadouts enforce max capacity: 3 UWs, 3 Cards.

## Battle Renderer

The battle screen uses a custom `SurfaceView` with a game loop (not Compose). Keep rendering code separate from game logic:

- **Game loop** runs on a dedicated thread with fixed timestep.
- **Entity system** manages ziggurat, enemies, projectiles, and effects.
- **Stats resolution** combines Workshop (permanent) × In-Round (temporary) upgrades multiplicatively.
- **Wave timing:** 26s spawn phase + 9s cooldown between waves.
- **Speed controls:** 1x / 2x / 4x.

## Testing

- Unit test domain use cases and game logic (cost calculations, damage formulas, tier progression).
- Use fakes for repositories in ViewModel tests.
- Instrumented tests for Room DAOs and step sensor integration.
- **Test framework:** JUnit 5 + kotlinx-coroutines-test (pure JVM, no emulator needed).
- **Run tests:** `./run-gradle.sh testDebugUnitTest`
- **Test source:** `app/src/test/java/com/whitefang/stepsofbabylon/`
- **Fakes:** `test/fakes/` — FakePlayerRepository, FakeWorkshopRepository (in-memory StateFlow-backed).
- **Current coverage:** 108 JVM tests — all use cases, domain models with validation logic, battle condition effects, tier unlock logic, biome themes, EnemyScaler, StepRateLimiter.

## Important Notes

- This is a solo-experience game — no multiplayer, no server backend required for v1.0.
- All monetization is cosmetic or convenience. Steps are never purchasable with real money.
- Accessibility is a priority: TalkBack support, color-blind modes, Activity Minute Parity for non-ambulatory users.
- **Gradle in non-TTY environments:** Gradle buffers output when stdout isn't a terminal (e.g., Kiro CLI, CI). Use `./run-gradle.sh <task>` instead of `./gradlew <task>` to avoid hanging. The script is gitignored — see `README.md` for how to recreate it.
