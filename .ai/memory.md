# CLINK Engineering Memory

## 1. Project Overview
- **Project**: CLINK Android Native App
- **Package**: `com.clink.app`
- **Current Phase**: Phase 1 - Foundation and Architecture Bootstrap
- **Target Platform**: Android (minSdk 26, targetSdk 35, compileSdk 35)

## 2. Core Architectural Pillars
- **Language**: Kotlin 2.1.0 with JVM target 17
- **UI Framework**: Jetpack Compose with Material 3
- **Dependency Injection**: Hilt 2.54 + KSP 2.1.0-1.0.29
- **Database**: Room 2.6.1 with schema export configured
- **Preferences**: Jetpack DataStore Preferences 1.1.1
- **Async Runtime**: Kotlin Coroutines 1.10.1 + Flow / StateFlow
- **Navigation**: Navigation Compose 2.8.5 with type-safe route patterns
- **Build System**: Gradle 8.11.1 with Kotlin DSL & Gradle Version Catalog (`gradle/libs.versions.toml`)

## 3. Money Representation Invariant
- All monetary operations operate exclusively on `Long` representing **paise** (`₹10 = 1000L`).
- Implemented via Kotlin inline value class `com.clink.app.domain.model.Money`.
- Under no circumstances should `Float` or `Double` be used for prices, balances, or transactions.
- Arithmetic overflow protected by `Math.addExact` in `Money.plus`.

## 4. Current State (TASK-005 Complete)
- Repository scaffolded with complete Clean Architecture layers (`domain`, `data`, `presentation`, `di`).
- Core local savings mechanics fully implemented and runtime verified.
- Material 3 Design System and Brand Identity established.
- First-launch Onboarding & Persistent User State implemented & verified.
- Pig Engine + Single Pig Progression implemented & verified:
  - `PigState` enum (`NEW`, `GROWING`, `HEALTHY`, `FULL`) derived deterministically via `PigStateCalculator`.
  - Zero database schema churn: progression and state are derived directly from Room balance using pure integer paise `Money`.
  - Dynamic `ClinkPigIllustration` vector mascot reflecting progression state expressions and celebratory accents.
  - Upgraded `HomeScreen` with single pig progression hero card, milestone progress bar, and status badges.
  - Upgraded `PigDetailScreen` aligned with CLINK design system, showing mascot hero, progression status, metadata summary, and actions.
- **Environment & Build Verification**:
  - Android Studio 2026.1.4 (AI-261.26222.65.2614.16204760) verified.
  - JDK: OpenJDK 21 (`C:\Users\jowan\.jdks\jbr-21.0.11`).
  - Android SDK: `C:\Users\jowan\AppData\Local\Android\Sdk` (Platform 35/36 installed & verified).
  - Debug APK build: `.\gradlew.bat assembleDebug` verified (SUCCESS, `app-debug.apk` generated).
  - Unit Tests: `.\gradlew.bat test` verified (68 tests, 0 failures, 100% PASS).
  - Static Analysis: `.\gradlew.bat lint` verified (SUCCESS, 0 errors, 0 warnings).
  - Live Runtime Verification: Tested on Android 16 (`emulator-5554`), 0 crashes, Dark mode tested, full navigation smoke test passed.

