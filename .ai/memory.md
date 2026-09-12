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

## 4. Current State (TASK-007 Complete)
- Repository scaffolded with complete Clean Architecture layers (`domain`, `data`, `presentation`, `di`).
- Core local savings mechanics fully implemented and runtime verified.
- Material 3 Design System and Brand Identity established.
- First-launch Onboarding & Persistent User State implemented & verified.
- Pig Engine + Single Pig Progression implemented & verified.
- Transaction Engine hardened and verified with atomic consistency.
- Home Dashboard polished as central everyday savings experience:
  - Hero Card featuring dynamic state-aware mascot (`ClinkPigIllustration`), status badge (`🐣 New`, `🌱 Growing`, `✨ Healthy`, `🏆 Full`), animated milestone progression bar (`animateFloatAsState`), and contextual motivational text.
  - In-dashboard primary "Save Money 🐷" / "Save First ₹10 🐷" CTA button.
  - 2-column shortcut grid for "History" and "Goals".
  - "Recent Activity" section with 3 most recent transactions ordered newest-first, formatted with relative timestamps (`Today, hh:mm a`), transaction notes, and credit pill indicators (`+ ₹50`).
  - First-use empty guidance card welcoming the user and guiding their first save when balance is ₹0.
  - Extended FAB for `+ Add Savings` with elevation and accessible semantics.
- **Environment & Build Verification**:
  - Android Studio 2026.1.4 (AI-261.26222.65.2614.16204760) verified.
  - JDK: OpenJDK 21 (`C:\Users\jowan\.jdks\jbr-21.0.11`).
  - Android SDK: `C:\Users\jowan\AppData\Local\Android\Sdk` (Platform 35/36 installed & verified).
  - Debug APK build: `.\gradlew.bat assembleDebug` verified (SUCCESS, `app-debug.apk` generated).
  - Unit Tests: `.\gradlew.bat test` verified (85 tests, 0 failures, 100% PASS).
  - Static Analysis: `.\gradlew.bat lint` verified (SUCCESS, 0 errors, 0 warnings).
  - Live Runtime Verification: Tested on Android 16 (`emulator-5554`), 0 crashes, Scenarios A-H verified.


