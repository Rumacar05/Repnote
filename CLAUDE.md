# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Repnote** is a workout tracking Android application built with Kotlin and Jetpack Compose. It helps users manage their fitness routines, track exercises, and monitor progress over time by logging weights and repetitions for each exercise.

### Core Functionality
- Create and manage workout routines with multiple exercises
- Track exercise sets, repetitions, and weights
- View workout history and progress over time
- User authentication with Firebase Auth
- Cloud sync across devices (planned)

The project uses a modular architecture with custom Gradle convention plugins to enforce consistent build configuration across modules.

## Build Commands

### Building the App
```bash
./gradlew build
```

### Running Tests
```bash
# Run all tests (unit + instrumented)
./gradlew test

# Run only unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Run tests for a specific module
./gradlew :app:test
./gradlew :core:design-system:test
```

### Code Quality
```bash
# Run ktlint (code formatting check)
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Run detekt (static analysis)
./gradlew detekt
```

### Clean Build
```bash
./gradlew clean
```

## Architecture

### Build Configuration System

The project uses **Gradle Convention Plugins** (located in `build-logic/convention/`) to centralize and standardize build configuration. This is a key architectural decision that prevents build file duplication.

#### Available Convention Plugins

- **`convention.android-application`**: Base plugin for the main app module. Automatically applies:
  - Android application plugin
  - Kotlin Android
  - Compose support
  - JVM and Android test configurations

- **`convention.android-library`**: For general Android library modules

- **`convention.android-library-compose`**: For Android libraries using Compose UI

- **`convention.android-feature`**: For feature modules. Automatically includes:
  - Android library with Compose
  - Hilt dependency injection
  - Dependencies on `:core:design-system` and `:core:string-resources`

- **`convention.jvm-library`**: For pure Kotlin/JVM libraries (no Android dependencies)

- **`convention.android-hilt`**: Adds Hilt/Dagger dependency injection

- **`convention.jvm-hilt`**: Adds Hilt for JVM-only modules

- **`convention.android-test`**: Configures Android testing dependencies (JUnit, Espresso, Compose UI testing)

- **`convention.jvm-test`**: Configures JVM testing dependencies (JUnit Jupiter, Kotest, MockK, Kluent)

- **`convention.paparazzi`**: Adds Paparazzi for screenshot testing

- **`convention.detekt`**: Configures Detekt static analysis

- **`convention.ktlint`**: Configures ktlint code formatting

#### How to Use Convention Plugins

When creating a new module, apply the appropriate convention plugin instead of manually configuring build settings:

```kotlin
// For a feature module
plugins {
    alias(libs.plugins.convention.android.feature)
}

// For a UI library
plugins {
    alias(libs.plugins.convention.android.library.compose)
}

// For a domain/data library
plugins {
    alias(libs.plugins.convention.android.library)
}
```

#### Version Configuration

All SDK versions, library versions, and project versioning are centralized in `gradle/libs.versions.toml`:
- compileSdk: 36
- targetSdk: 36
- minSdk: 24
- Java version: 21
- Kotlin: 2.3.0

To change versions, edit `gradle/libs.versions.toml`, not individual build files.

### Module Structure

```
app/                          # Main application module
core/
  design-system/             # Shared UI components and theme
  string-resources/          # (Expected) String resources module
build-logic/
  convention/                # Custom Gradle convention plugins
```

### Code Quality Configuration

Detekt configuration is in `config/detekt/detekt.yml` with custom rules:
- Max line length: 120 characters
- Composable functions exempt from naming conventions
- Test directories excluded from documentation requirements
- Custom complexity thresholds for Android development

## Key Technical Details

### Dependency Injection
The project uses **Hilt (Dagger)** for dependency injection. Feature modules automatically get Hilt configured via `convention.android-feature` plugin.

### Testing Framework
- **Unit tests**: JUnit Jupiter (JUnit 5) with Kotest assertions, Kluent, and MockK
- **Instrumented tests**: AndroidX Test with Espresso
- **UI tests**: Compose UI Test JUnit4
- **Screenshot tests**: Paparazzi

### Compose
The project uses Jetpack Compose with Material3. The `RepnoteTheme` is defined in `app/src/main/java/com/ruma/repnote/ui/theme/`.

## Development Workflow

### Adding a New Feature Module

1. Create module directory under project root or in a feature group
2. Create `build.gradle.kts` with:
   ```kotlin
   plugins {
       alias(libs.plugins.convention.android.feature)
   }

   android {
       namespace = "com.ruma.repnote.feature.yourfeature"
   }
   ```
3. Add to `settings.gradle.kts`: `include(":feature:yourfeature")`
4. The convention plugin automatically provides:
   - Hilt setup
   - Compose dependencies
   - Access to `:core:design-system`
   - Test configurations

### Adding Dependencies

Add dependencies to `gradle/libs.versions.toml` first, then reference them using the version catalog:
```kotlin
dependencies {
    implementation(libs.your.dependency)
}
```

### Package Naming
Base package: `com.ruma.repnote`
- Feature modules: `com.ruma.repnote.feature.featurename`
- Core modules: `com.ruma.repnote.core.modulename`