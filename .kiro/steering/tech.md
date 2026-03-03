# Tech Stack

## Core

- **Language:** Kotlin (JVM target 17)
- **Min SDK:** 34 (Android 14) / Target & Compile SDK: 36
- **Architecture:** MVVM + Clean Architecture
- **Build:** Gradle 9.3.1 with Kotlin DSL, version catalog at `gradle/libs.versions.toml`

## Key Libraries & Versions

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.3.0 | Language |
| AGP | 9.0.1 | Android Gradle Plugin |
| KSP | 2.3.6 | Annotation processing (replaces kapt) |
| Compose BOM | 2026.02.00 | Jetpack Compose UI |
| Hilt | 2.59.2 | Dependency injection |
| Room | 2.8.4 | Local SQLite database |
| Navigation Compose | 2.9.7 | Compose navigation |
| Lifecycle | 2.9.0 | ViewModel, StateFlow integration |
| WorkManager | 2.11.0 | Background step sync |

## Architecture Layers

- **presentation** — ViewModels (expose `StateFlow`), Compose screens, SurfaceView battle renderer
- **domain** — Use cases, repository interfaces, pure Kotlin models. Zero Android imports.
- **data** — Room entities, DAOs, repository implementations, sensor/Google Fit data sources

Data flow: `presentation → domain ← data`. Domain has no Android dependencies.

## UI Approach

- Jetpack Compose for all menus and screens
- Custom `SurfaceView` with dedicated game loop thread for the battle renderer (not Compose)
- Fixed timestep game loop, entity system for ziggurat/enemies/projectiles

## Async

- Kotlin coroutines and `Flow` for all async operations
- Room exposes queries as `Flow`
- ViewModels collect flows and expose `StateFlow` to Compose

## Step Tracking

- Android Sensor API (`TYPE_STEP_COUNTER`) as primary source
- Google Fit SDK for cross-validation and Activity Minute Parity
- WorkManager + Foreground Service for reliable background counting

## Common Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Check for lint issues
./gradlew lint

# Clean build
./gradlew clean
```

## Notes

- All annotation processing uses KSP (not kapt)
- Room schema exports to `app/schemas/` — commit these files
- All new dependencies must be added to `gradle/libs.versions.toml`, not hardcoded in build files
