# AGENTS.md — Steps of Babylon

## Project Overview

Steps of Babylon is an Android mobile game that combines idle tower defense gameplay with a real-world step counter. Players earn **Steps** by physically walking, then spend them to upgrade an ancient ziggurat that fights wave-based battles against mythic enemies. Progression is gated entirely by physical activity.

See `docs/StepsOfBabylon_GDD.md` for the full game design document.

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** Android (TBD)
- **Architecture:** MVVM + Clean Architecture
- **UI:** Jetpack Compose (menus/screens) + SurfaceView (battle renderer)
- **DI:** Hilt
- **Database:** Room (SQLite) — offline-first, all game state stored locally
- **Background:** WorkManager + Foreground Service (step counting)
- **Step Tracking:** Android Sensor API (`TYPE_STEP_COUNTER`) + Google Fit SDK
- **Build:** Gradle (Kotlin DSL)

## Architecture

```
app/
├── data/           # Room entities, DAOs, repositories impl, sensor/Google Fit data sources
├── domain/         # Use cases, repository interfaces, game logic, models
├── presentation/   # ViewModels, Compose screens, SurfaceView battle renderer
├── di/             # Hilt modules
└── service/        # Foreground step-counting service, WorkManager workers
```

Follow Clean Architecture layers: `presentation → domain ← data`. The domain layer has zero Android dependencies.

## Key Domain Concepts

- **Steps** — primary permanent currency, earned only from real-world walking/activity. Never generated in-game.
- **Cash** — temporary in-round currency from killing enemies. Resets each round.
- **Gems** — permanent premium currency from milestones and daily logins.
- **Power Stones** — permanent currency for Ultimate Weapons, from weekly challenges.
- **Workshop** — permanent upgrades (Attack/Defense/Utility) purchased with Steps.
- **Labs** — time-gated research projects initiated with Steps, completed over real time.
- **Cards** — per-round bonus items (3 equipped max), acquired via Gem-purchased packs.
- **Ultimate Weapons (UWs)** — activatable abilities (3 equipped max), unlocked with Power Stones.
- **Tiers** — difficulty levels (1–10+) with escalating battle conditions and cash multipliers.
- **Biomes** — narrative environments tied to tier ranges (Hanging Gardens → Burning Sands → Frozen Ziggurats → Underworld of Kur → Celestial Gate).
- **Step Overdrive** — mid-battle mechanic to burn Steps for a 60-second combat boost (once per round).
- **Walking Encounters** — Supply Drop rewards delivered via push notifications during walks.
- **Activity Minute Parity** — Google Fit Active Minutes converted to Step-equivalents for indoor workouts.

## Conventions

- Use Kotlin coroutines and Flow for all async operations.
- ViewModels expose `StateFlow` to Compose UI.
- Room is the single source of truth for game state.
- All upgrade cost formulas follow: `baseCost * (scaling ^ level)`.
- Step counting must work reliably when the app is backgrounded or killed.
- Steps can **never** be generated passively in-game — this is a hard design rule.
- Anti-cheat: rate-limit at 200 steps/min, daily ceiling of 50,000 steps, cross-validate with Google Fit.

## Battle Renderer

The battle screen uses a custom `SurfaceView` with a game loop (not Compose). Keep rendering code separate from game logic:

- **Game loop** runs on a dedicated thread with fixed timestep.
- **Entity system** manages ziggurat, enemies, projectiles, and effects.
- **Stats resolution** combines Workshop (permanent) × In-Round (temporary) upgrades multiplicatively.

## Testing

- Unit test domain use cases and game logic (cost calculations, damage formulas, tier progression).
- Use fakes for repositories in ViewModel tests.
- Instrumented tests for Room DAOs and step sensor integration.

## Important Notes

- This is a solo-experience game — no multiplayer, no server backend required for v1.0.
- All monetization is cosmetic or convenience. Steps are never purchasable with real money.
- Accessibility is a priority: TalkBack support, color-blind modes, Activity Minute Parity for non-ambulatory users.
