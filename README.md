# Steps of Babylon

An Android idle tower defense game where real-world walking drives all progression. Players earn **Steps** by physically walking, then spend them to upgrade an ancient ziggurat that fights wave-based battles against mythic enemies.

> **Every Step Builds the Tower.**

## Prerequisites

- JDK 17
- Android SDK 36 (compile/target), min SDK 34 (Android 14)
- Android Studio (latest stable recommended)

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle (version catalog at `gradle/libs.versions.toml`)
4. Connect a device or start an emulator (API 34+)

## Build & Run

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Unit tests
./gradlew test

# Note: Instrumented tests (connectedAndroidTest) are planned but not yet implemented.
# See AGENTS.md for current test coverage.

# Lint
./gradlew lint

# Clean
./gradlew clean
```

### Non-TTY Environments (Kiro CLI, CI, etc.)

Gradle buffers output when stdout isn't a terminal, which can cause builds to appear hung. Use the wrapper script instead:

```bash
./run-gradle.sh assembleDebug
./run-gradle.sh test
```

`run-gradle.sh` runs Gradle in the background and captures output to a temp file, avoiding the buffering issue. It's gitignored — recreate it if needed:

```bash
#!/bin/bash
cd "$(dirname "$0")"
./gradlew "$@" > /tmp/gradle_out.txt 2>&1 &
wait $!
EXIT_CODE=$?
cat /tmp/gradle_out.txt
exit $EXIT_CODE
```

## Project Structure

```
app/src/main/java/com/whitefang/stepsofbabylon/
├── data/           # Room entities, DAOs, repositories impl
├── domain/         # Pure Kotlin: models, use cases, repository interfaces
├── presentation/   # ViewModels, Compose screens, SurfaceView battle renderer
├── di/             # Hilt modules
└── service/        # Foreground step-counting service, WorkManager workers
```

See [docs/architecture.md](docs/architecture.md) for layer rules and data flow.

## Key Documentation

| Document | Description |
|---|---|
| [Game Design Document](docs/StepsOfBabylon_GDD.md) | Full game design spec |
| [Architecture](docs/architecture.md) | Clean Architecture layers and conventions |
| [Master Plan](docs/plans/master-plan.md) | 33-entry development roadmap |
| [Battle Formulas](docs/battle-formulas.md) | All combat and economy math |
| [Database Schema](docs/database-schema.md) | Room entities and migration strategy |
| [Step Tracking](docs/step-tracking.md) | Sensor stack, anti-cheat, background service |
| [Monetization](docs/monetization.md) | IAP, ads, and economy rules |

## Tech Stack

Kotlin · Jetpack Compose · Hilt · Room · WorkManager · Custom SurfaceView renderer

See [AGENTS.md](AGENTS.md) for the full tech stack and conventions.
