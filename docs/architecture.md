# Architecture

Steps of Babylon follows MVVM + Clean Architecture with three layers.

## Layer Diagram

```
┌─────────────────────────────────────────┐
│  presentation/                          │
│  ViewModels · Compose Screens ·         │
│  SurfaceView Battle Renderer            │
│         │ exposes StateFlow             │
│         ▼                               │
├─────────────────────────────────────────┤
│  domain/                                │
│  Use Cases · Repository Interfaces ·    │
│  Models · Game Logic                    │
│  ★ Pure Kotlin — zero Android imports   │
│         ▲                               │
├─────────────────────────────────────────┤
│  data/                                  │
│  Room Entities · DAOs ·                 │
│  Repository Implementations ·           │
│  Sensor / Google Fit Data Sources       │
└─────────────────────────────────────────┘
```

Data flow: `presentation → domain ← data`

## Layer Rules

- `domain/` has zero Android imports. Pure Kotlin only.
- `data/` implements domain repository interfaces.
- `presentation/` depends on domain, never on data directly.
- Hilt modules in `di/` wire data implementations to domain interfaces.

## Async Model

- Kotlin coroutines and `Flow` everywhere.
- Room queries return `Flow`.
- ViewModels collect flows and expose `StateFlow` to Compose.

## UI Split

| Surface | Technology | Used For |
|---|---|---|
| Menus & screens | Jetpack Compose | Home, Workshop, Labs, Cards, Stats |
| Battle renderer | Custom `SurfaceView` | Real-time wave combat |

The battle renderer runs on a dedicated thread with a fixed-timestep game loop. Rendering code is separate from game logic.

### Game Loop Architecture

```
Game Thread (SurfaceView)
  └─ Fixed timestep loop
       ├─ Update: entity positions, collision, stats resolution
       └─ Render: draw ziggurat, enemies, projectiles, effects

Stats Resolution = Workshop (permanent) × In-Round (temporary)
Wave Timing = 26s spawn phase + 9s cooldown
Speed Controls = 1x / 2x / 4x
```

## Dependency Injection

Hilt with KSP (not kapt). All modules in `di/`.

- `DatabaseModule` — provides Room database and DAOs
- Future modules: `RepositoryModule`, `SensorModule`, `GoogleFitModule`

## Naming Conventions

| Type | Pattern | Example |
|---|---|---|
| Room entity | `*Entity.kt` | `PlayerProfileEntity` |
| Repository interface | `*Repository.kt` | `WorkshopRepository` |
| Repository impl | `*RepositoryImpl.kt` | `WorkshopRepositoryImpl` |
| Use case | Verb phrase | `CalculateUpgradeCost` |
| ViewModel | `*ViewModel.kt` | `WorkshopViewModel` |
| Compose screen | `*Screen.kt` | `HomeScreen` |
| Hilt module | `*Module.kt` | `DatabaseModule` |

## Security

| Layer | Measure | Details |
|---|---|---|
| Database | SQLCipher encryption | AES-256 full database encryption at rest via `net.zetetic:sqlcipher-android` |
| Key management | Android Keystore | DB passphrase encrypted with AES-256-GCM Keystore key, stored in SharedPreferences |
| Network | Network security config | Cleartext traffic blocked via `network_security_config.xml` |
| Release build | R8 / ProGuard | Code shrinking, obfuscation, and resource shrinking enabled for release builds |
