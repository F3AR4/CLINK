# CLINK 🐷

> **Save small, smile big.**

CLINK is a native Android micro-savings application designed around making small savings (₹10, ₹20, ₹50, ₹100) delightful, satisfying, and effortless.

---

## Current Status
- **Phase 1**: COMPLETE (16/16 tasks verified)
- **Latest Release Gate**: TASK-016 COMPLETE
- **Automated Tests**: 198/198 passing (38 test classes, 100% pass rate)
- **Static Analysis (Lint)**: 0 errors / 0 warnings (`lintDebug`)
- **Runtime Verification**: Debug APK verified on Android 16 / API 36
- **Production Readiness**: Phase 1 Foundation Complete. (Real UPI/payment integrations and backend/cloud sync are intentionally deferred to future phases)

---

## Technology Stack
- **Platform**: Android (minSdk 26, targetSdk 35, compileSdk 35)
- **Language**: Kotlin 2.1.0 (JVM 17)
- **UI Framework**: Jetpack Compose (BOM 2024.12.01) with Material 3
- **Design System**: CLINK Brand Palette (Pink `#E85D75`, Navy `#172033`, Teal `#35B8A6`)
- **Architecture**: MVVM + Clean Architecture (`presentation`, `domain`, `data`, `di`)
- **Dependency Injection**: Dagger Hilt 2.54 with KSP
- **Local Persistence**: Room Database 2.6.1
- **Preferences**: Jetpack DataStore Preferences 1.1.1
- **Asynchronous**: Kotlin Coroutines 1.10.1 + Flow / StateFlow
- **Navigation**: Jetpack Navigation Compose 2.8.5
- **Build System**: Gradle 8.11.1 with Kotlin DSL & Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Testing**: JUnit 4, Google Truth, Mockk, CashApp Turbine, AndroidX Test

---

## Architectural Principles
1. **Precision Money Model**: Money values are strictly represented as `Long` integers in **paise** (`₹10 = 1000L`). `Float` and `Double` are forbidden for financial arithmetic.
2. **Clean Dependency Inversion**:
   - `presentation` -> `domain` <- `data`
   - The domain layer is pure Kotlin, free from Android UI, Room, or external payment libraries.
3. **Phase 1 Payment Abstraction**:
   - `PaymentRepository` is backed by `FakePaymentRepository` for offline simulation.
   - Zero UPI keys, zero secrets, and zero real money involved in Phase 1.

---

## Project Structure
```
CLINK/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── kotlin/com/clink/app/
│   │   │   │   ├── ClinkApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/            # Room entities, DAOs, DataStore, Repositories
│   │   │   │   ├── domain/          # Models (Money, Pig, etc.), Repositories, Use Cases
│   │   │   │   ├── presentation/    # Compose Theme, Components, Navigation, Screens
│   │   │   │   └── di/              # Hilt Modules
│   │   │   └── res/                 # App icons, strings, colors, XML themes
│   │   └── test/kotlin/com/clink/app/  # Unit tests (Money, UseCases, Repositories)
│   └── build.gradle.kts
├── docs/
│   └── architecture.md     # Detailed architecture documentation
├── gradle/
│   ├── libs.versions.toml  # Centralized dependency catalog
│   └── wrapper/            # Gradle 8.11.1 wrapper
├── build.gradle.kts        # Root build configuration
├── settings.gradle.kts     # Root settings
├── gradlew.bat             # Windows Gradle wrapper script
└── gradlew                 # Unix Gradle wrapper script
```

---

## How to Build & Run

### Prerequisites
1. **JDK**: Java Development Kit 17 or 21 installed.
2. **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer recommended.
3. **Android SDK**: Platform 35 and Build Tools installed.

### Option A: Using Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the repository root folder.
3. Wait for Gradle sync to resolve dependencies from `libs.versions.toml`.
4. Connect an Android device or start an emulator running Android 8.0+ (API 26+).
5. Click **Run** (`Shift + F10`).

### Option B: Command Line (Windows)
Set `JAVA_HOME` to your JDK 17+ installation, then run:
```powershell
# Run Unit Tests
.\gradlew.bat test

# Build Debug APK
.\gradlew.bat assembleDebug

# Run Lint
.\gradlew.bat lintDebug
```

---

## Phase 1 Intentional Boundaries
- **Payment Abstraction**: `FakePaymentRepository` is intentionally used for deterministic, offline testing in Phase 1. Real banking/UPI payment integrations are planned for future phases.
- **Local Persistence**: Room SQLite database (`clink.db`) currently utilizes destructive migration fallback for early development velocity. Formal migration scripts will be introduced before production release.
- **Offline First**: All user savings, pigs, goals, and transaction history are stored 100% locally on device with zero external cloud dependencies.
