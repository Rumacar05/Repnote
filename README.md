# Repnote

**Repnote** is a modern workout tracking application for Android that helps you manage your fitness routines, exercises, and progress over time.

## 💪 What is Repnote?

Repnote allows you to:
- **Create and manage workout routines** with multiple exercises
- **Track your progress** by logging weights and repetitions for each exercise
- **View your workout history** and see how you're improving
- **Organize exercises** by muscle groups, difficulty, or custom categories
- **Stay motivated** with visual progress tracking and statistics

Built with **Kotlin** and **Jetpack Compose**, Repnote features a clean, modern UI and follows best practices in Android development with a modular architecture.

## 📋 Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Creating a New Screen](#creating-a-new-screen)
- [Design System](#design-system)
- [Build Commands](#build-commands)
- [Module Structure](#module-structure)
- [Convention Plugins](#convention-plugins)
- [Testing](#testing)

## 🏗️ Architecture

Repnote follows **Clean Architecture** principles with a **modular design**:

- **Separation of Concerns**: Each module has a clear responsibility
- **Feature Modules**: Features are isolated and can be developed independently
- **Core Modules**: Shared functionality (auth, design-system, string-resources)
- **API/Implementation Split**: Public interfaces separated from implementation details

### Project Structure

```
Repnote/
├── app/                          # Main application module
│   └── navigation/               # Navigation routes and display
├── core/
│   ├── auth/
│   │   ├── api/                  # Auth domain interfaces
│   │   └── impl/                 # Auth implementation
│   ├── data/                     # Data layer (repositories, mappers)
│   ├── database/                 # Room database (DAOs, entities)
│   ├── design-system/            # Shared UI components (RepnoteScreen, theme)
│   ├── domain/                   # Domain models and repository interfaces
│   └── string-resources/         # Centralized string resources
├── feature/
│   ├── auth/
│   │   ├── api/                  # Auth feature public interfaces
│   │   └── impl/                 # Auth UI (Login, Register screens)
│   ├── home/impl/                # Home screen
│   ├── profile/impl/             # Profile screen
│   ├── routine/impl/             # Routines management
│   └── workout/impl/             # Active workout and history
└── build-logic/
    └── convention/               # Gradle convention plugins
```

## 🛠️ Tech Stack

- **Language**: Kotlin 2.3.0
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Koin
- **Navigation**: AndroidX Navigation 3
- **Authentication**: Firebase Auth with Google Sign-In
- **Database**: Room (local) + Firebase Firestore (cloud sync)
- **Image Storage**: Cloudinary for exercise images
- **Build System**: Gradle with Custom Convention Plugins
- **Testing**: JUnit 5, Kotest, MockK, Espresso
- **Code Quality**: Detekt, ktlint

## 🏋️ Core Features

### ✅ Completed Features
- ✅ **User Authentication** - Email/Password and Google Sign-In
- ✅ **Material 3 Design System** - Modern, consistent UI components
- ✅ **Custom Scaffold System** - Standardized screen structure
- ✅ **Global Exercise Database** - 51 pre-populated exercises organized by muscle group:
  - Chest (5), Back (6), Shoulders (5)
  - Biceps (4), Triceps (4), Forearms (3)
  - Quads (5), Hamstrings (4), Glutes (4), Calves (3)
  - Abs (5), Obliques (3)
- ✅ **Multilingual Support** - English and Spanish translations via Firestore subcollections
- ✅ **Cloudinary Integration** - Image storage for exercise media
- ✅ **Clean Architecture** - Modular design with API/Implementation separation

### 🚧 In Development
- 🏃 **Exercise Management** - Browse and search global exercises
- 📝 **Custom Exercises** - Create and manage personal exercises
- 💪 **Workout Routines** - Create and edit workout routines
- 📊 **Workout Logging** - Log sets, reps, and weights
- 📈 **Progress Tracking** - Track progress over time with charts

### 📋 Planned Features
- 📅 Workout calendar and scheduling
- 💾 Advanced cloud sync features
- 🔔 Workout reminders and notifications
- 📷 Exercise video demonstrations
- 🎯 Workout templates and programs
- 📊 Advanced analytics and insights

## 🚀 Getting Started

### Prerequisites

- JDK 21
- Android Studio Koala or later
- Android SDK 36 (compileSdk)
- Minimum SDK 24

### Setup

1. Clone the repository
   ```bash
   git clone https://github.com/Rumacar05/Repnote.git
   cd Repnote
   ```

2. **Configure Firebase** (required):
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Firestore Database
   - Download `google-services.json` and place it in the `app/` directory

3. **Configure Cloudinary** (required):
   - Create a [Cloudinary](https://cloudinary.com/) account
   - Add your credentials to `local.properties`:
     ```properties
     cloudinary.cloud.name=your_cloud_name
     cloudinary.api.key=your_api_key
     cloudinary.api.secret=your_api_secret
     ```

4. Open the project in Android Studio
5. Sync Gradle
6. Run the app: `./gradlew assembleDebug`

## 📱 Creating a New Screen

Repnote uses a **Screen class pattern** with `RepnoteScreen` for consistent UI structure.

### Step 1: Create the Screen Class

Create a screen class with an internal `Root` function that handles the logic:

```kotlin
package com.ruma.repnote.feature.yourfeature.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import org.koin.androidx.compose.koinViewModel

class YourFeatureScreen {
    @Composable
    fun Screen(
        onNavigateBack: () -> Unit,
        onNavigateToNext: () -> Unit,
    ) {
        YourFeatureRoot(
            onNavigateBack = onNavigateBack,
            onNavigateToNext = onNavigateToNext,
        )
    }
}

@Composable
internal fun YourFeatureRoot(
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    viewModel: YourFeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RepnoteScreen(
        topBar = {
            YourFeatureTopBar(onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            // Optional FAB
        },
    ) { modifier ->
        YourFeatureContent(
            uiState = uiState,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YourFeatureTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Your Feature") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                )
            }
        },
    )
}

@Composable
private fun YourFeatureContent(
    uiState: YourFeatureUiState,
    modifier: Modifier = Modifier,
) {
    // Your content here - modifier includes proper padding
}
```

### Step 2: Add Navigation Route

In `app/src/main/kotlin/com/ruma/repnote/navigation/Route.kt`:

```kotlin
@Serializable
data object YourFeature : NavKey

// Or with parameters:
@Serializable
data class YourFeatureDetail(
    val itemId: String,
) : NavKey
```

### Step 3: Register in NavigationScreens

In `app/src/main/kotlin/com/ruma/repnote/navigation/NavigationScreens.kt`:

```kotlin
class NavigationScreens(
    // ... existing screens
    val yourFeatureScreen: YourFeatureScreen,
)
```

### Step 4: Add to Navigation Display

In `app/src/main/kotlin/com/ruma/repnote/navigation/RepnoteNavDisplay.kt`:

```kotlin
entry<YourFeature> {
    navigationScreens.yourFeatureScreen.Screen(
        onNavigateBack = { navigator.goBack() },
        onNavigateToNext = { navigator.navigate(NextScreen) },
    )
}

// With parameters:
entry<YourFeatureDetail> { route ->
    navigationScreens.yourFeatureDetailScreen.Screen(
        itemId = route.itemId,
        onNavigateBack = { navigator.goBack() },
    )
}
```

### Navigation Patterns

```kotlin
// Navigate forward
navigator.navigate(YourFeature)

// Navigate with parameters
navigator.navigate(YourFeatureDetail(itemId = "123"))

// Navigate back
navigator.goBack()

// Reset navigation and navigate (for auth flows)
navigator.resetAllStacks()
navigator.navigate(Home)
```

### RepnoteScreen Parameters

```kotlin
RepnoteScreen(
    modifier = Modifier,                    // Optional modifier
    topBar = { /* TopAppBar */ },           // Optional top bar
    bottomBar = { /* BottomNavBar */ },     // Optional bottom bar
    floatingActionButton = { /* FAB */ },   // Optional FAB
    snackbarHostState = snackbarHostState,  // For snackbars
) { modifier ->
    // Content receives a modifier with proper padding applied
    YourContent(modifier = modifier)
}
```

## 🎨 Design System

Repnote uses a centralized design system located in `core/design-system`. Always use these values for consistency:

### Spacings

```kotlin
import com.ruma.repnote.core.designsystem.theme.Spacings

Spacer(modifier = Modifier.height(Spacings.spacing16))
padding(Spacings.spacing24)
```

**Available values**: `spacing2`, `spacing4`, `spacing8`, `spacing12`, `spacing16`, `spacing24`, `spacing32`, `spacing40`, `spacing48`, `spacing64`, `spacing80`, `spacing88`, `spacing96`, `spacing104`

### Border Widths

```kotlin
import com.ruma.repnote.core.designsystem.theme.BorderWidths

border(
    width = BorderWidths.borderDefault,
    color = MaterialTheme.colorScheme.outline
)
```

**Available values**: `borderNone`, `borderThin`, `borderDefault`, `borderMedium`, `borderThick`

### Corner Radius

```kotlin
import com.ruma.repnote.core.designsystem.theme.CornerRadius

Card(
    shape = RoundedCornerShape(CornerRadius.cornerLarge)
)
```

**Available values**: `cornerNone`, `cornerExtraSmall`, `cornerSmall`, `cornerMedium`, `cornerLarge`, `cornerExtraLarge`, `cornerFull`

### String Resources

**Always use string resources** instead of hardcoded strings:

```kotlin
import com.ruma.repnote.core.stringresources.R as StringRes

Text(stringResource(StringRes.string.auth_welcome_back))
```

## 🔨 Build Commands

### Building the App

```bash
./gradlew build                    # Full build
./gradlew assembleDebug            # Debug APK
./gradlew assembleRelease          # Release APK
```

### Running Tests

```bash
./gradlew test                     # All tests
./gradlew testDebugUnitTest        # Unit tests only
./gradlew connectedAndroidTest     # Instrumented tests
./gradlew :app:test                # Tests for specific module
```

### Code Quality

```bash
./gradlew ktlintCheck              # Check code formatting
./gradlew ktlintFormat             # Auto-format code
./gradlew detekt                   # Static analysis
```

### Clean Build

```bash
./gradlew clean
```

## 📦 Module Structure

### Creating a New Feature Module

1. **Create module directory**: `feature/your-feature/impl/`

2. **Create `build.gradle.kts`**:
   ```kotlin
   plugins {
       alias(libs.plugins.convention.android.feature)
   }

   android {
       namespace = "com.ruma.repnote.feature.yourfeature"
   }

   dependencies {
       api(projects.feature.yourFeature.api)
       // Add specific dependencies
   }
   ```

3. **Add to `settings.gradle.kts`**:
   ```kotlin
   include(":feature:your-feature:api")
   include(":feature:your-feature:impl")
   ```

4. **The `convention.android-feature` plugin automatically provides**:
   - Koin setup
   - Compose dependencies
   - Access to `:core:design-system`
   - Access to `:core:string-resources`
   - Test configurations

## ⚙️ Convention Plugins

Repnote uses custom Gradle convention plugins to enforce consistent build configuration.

### Available Plugins

- **`convention.android-application`**: Main app module
- **`convention.android-library`**: General Android library
- **`convention.android-library-compose`**: Android library with Compose
- **`convention.android-feature`**: Feature module (includes Koin, Compose, design-system)
- **`convention.jvm-library`**: Pure Kotlin/JVM library
- **`convention.android-koin`**: Adds Koin DI
- **`convention.jvm-koin`**: Koin for JVM modules
- **`convention.android-test`**: Android testing setup
- **`convention.jvm-test`**: JVM testing setup (JUnit 5, Kotest, MockK)
- **`convention.paparazzi`**: Screenshot testing
- **`convention.detekt`**: Static analysis
- **`convention.ktlint`**: Code formatting

### Configuration

All versions are centralized in `gradle/libs.versions.toml`:

```toml
[versions]
compileSdk = "36"
targetSdk = "36"
minSdk = "24"
kotlin = "2.3.0"
```

## 🧪 Testing

### Unit Tests

Located in `src/test/`:

```kotlin
class LoginViewModelTest {
    @Test
    fun `when valid email and password, sign in succeeds`() {
        // Test implementation
    }
}
```

### UI Tests

Located in `src/androidTest/`:

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @Test
    fun loginButton_isDisplayed() {
        // Test implementation
    }
}
```

### Testing Libraries

- **JUnit 5**: Unit testing framework
- **Kotest**: Assertion library
- **MockK**: Mocking framework
- **Kluent**: Fluent assertions
- **Espresso**: UI testing
- **Compose UI Test**: Compose testing

## 📝 Code Style

### Naming Conventions

- **Packages**: `com.ruma.repnote.feature.featurename`
- **Classes**: PascalCase (`LoginViewModel`)
- **Functions**: camelCase (`onSignInClick`)
- **Composables**: PascalCase (`LoginScreen`)
- **Resources**: snake_case (`auth_welcome_back`)

### Detekt Rules

- Max line length: 120 characters
- Composable functions exempt from naming conventions
- Test directories excluded from documentation requirements

### Code Organization

```kotlin
// 1. Package declaration
package com.ruma.repnote.feature.auth.presentation.login

// 2. Imports (sorted)
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

// 3. Constants
private const val MAX_PASSWORD_LENGTH = 128

// 4. Composable functions
@Composable
fun LoginScreen() { }

// 5. Helper functions
private fun validateEmail(email: String): Boolean { }

// 6. Data classes / Sealed interfaces
data class LoginUiState()
sealed interface LoginNavigationEvent
```

## 🔐 Firebase Setup

1. Add `google-services.json` to `app/` directory
2. Configure OAuth 2.0 Client ID in Google Cloud Console
3. Add SHA-1/SHA-256 fingerprints for your app

### Firestore Database Structure

The app uses a translation-based architecture with subcollections for multilingual support:

```
firestore/
├── exercises/                          # Global and custom exercises
│   ├── {exerciseId}/
│   │   ├── id: string
│   │   ├── imageUrl: string | null
│   │   ├── primaryMuscleGroup: string  # MuscleGroup enum name
│   │   ├── secondaryMuscleGroups: array<string>
│   │   ├── global: boolean             # true for global exercises
│   │   ├── createdBy: string | null    # userId or null for global
│   │   │
│   │   └── translations/               # Subcollection for localized content
│   │       ├── en/
│   │       │   ├── name: string
│   │       │   └── description: string
│   │       └── es/
│   │           ├── name: string
│   │           └── description: string
│   │
└── (future collections: routines, workouts, etc.)
```

**Key Points:**
- Exercise metadata (muscle groups, images) is stored in the base document
- Localized content (name, description) is stored in `translations/{lang}` subcollections
- Global exercises have `global: true` and `createdBy: null`
- Custom user exercises have `global: false` and `createdBy: userId`
- Currently supports English (`en`) and Spanish (`es`)

### Cloudinary Setup

1. Create a [Cloudinary](https://cloudinary.com/) account
2. Add credentials to `local.properties`:
   ```properties
   cloudinary.cloud.name=your_cloud_name
   cloudinary.api.key=your_api_key
   cloudinary.api.secret=your_api_secret
   ```
3. Images are uploaded to the `repnote/exercises/` folder

## 🎯 Domain Model

### Current Implementation

```kotlin
// Exercise (Implemented)
data class Exercise(
    val id: String,
    val name: String,                              // From translation
    val description: String,                       // From translation
    val imageUrl: String?,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup>,
    val isGlobal: Boolean,
    val createdBy: String?,
)

enum class MuscleGroup {
    CHEST, BACK, SHOULDERS,
    BICEPS, TRICEPS, FOREARMS,
    QUADS, HAMSTRINGS, GLUTES, CALVES,
    ABS, OBLIQUES
}

// Result wrapper for repository operations
sealed interface ExerciseResult<out T> {
    data class Success<T>(val data: T) : ExerciseResult<T>
    data class Error(val exception: ExerciseException) : ExerciseResult<Nothing>
}
```

### Planned Domain Models

```kotlin
// Workout Routine (Planned)
data class Routine(
    val id: String,
    val name: String,
    val description: String?,
    val exercises: List<RoutineExercise>,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class RoutineExercise(
    val exerciseId: String,
    val order: Int,
    val targetSets: Int,
    val targetReps: Int,
    val restTime: Int? // seconds
)

// Workout Log Entry (Planned)
data class WorkoutLog(
    val id: String,
    val userId: String,
    val routineId: String?,
    val date: Long,
    val exercises: List<ExerciseLog>,
    val duration: Int?, // minutes
    val notes: String?
)

data class ExerciseLog(
    val exerciseId: String,
    val sets: List<SetLog>
)

data class SetLog(
    val weight: Double,
    val reps: Int,
    val completed: Boolean,
    val notes: String?
)
```

## 📄 License

[Your License Here]

## 🤝 Contributing

Contributions are welcome! To get started:

1. Fork the repository
2. Clone your fork and set up the project:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Repnote.git
   cd Repnote
   ```
3. **Configure required files** (see [Getting Started](#-getting-started)):
   - Add `google-services.json` to `app/` (from your Firebase project)
   - Add Cloudinary credentials to `local.properties`:
     ```properties
     cloudinary.cloud.name=your_cloud_name
     cloudinary.api.key=your_api_key
     cloudinary.api.secret=your_api_secret
     ```
4. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
5. Make your changes
6. Run tests and code quality checks:
   ```bash
   ./gradlew test
   ./gradlew ktlintCheck
   ./gradlew detekt
   ```
7. Commit and push your changes
8. Submit a pull request

## 📞 Contact

[Your Contact Information]

---

**Built with ❤️ using Kotlin and Jetpack Compose**
**Track your gains, one rep at a time 💪**
