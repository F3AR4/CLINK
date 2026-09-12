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

## 4. Current State (TASK-012 Complete)
- Repository scaffolded with complete Clean Architecture layers (`domain`, `data`, `presentation`, `di`).
- Core local savings mechanics fully implemented and runtime verified.
- Material 3 Design System and Brand Identity established.
- First-launch Onboarding & Persistent User State implemented & verified.
- Pig Engine + Multi-Pig Architecture implemented & verified:
  - Full CRUD operations: `CreatePigUseCase`, `GetSelectedPigUseCase`, `SelectPigUseCase`, `UpdatePigUseCase`, `DeletePigUseCase`.
  - Financial isolation: Every pig maintains strictly independent balances, transactions, and goals.
  - Delete safety: Deletion of the sole remaining pig is prohibited; deleting an active pig cascades deletion in SQLite and falls back selection cleanly to a surviving pig.
  - Active selection persistence: Stored in DataStore preferences (`active_pig_id`), surviving process death and cold launches.
  - Presentation: `PigSelectorRow` on `HomeScreen`, `PigDetailScreen` management (rename & safe delete), explicit `pigId` routing across all screens (`AddMoneyScreen`, `HistoryScreen`, `GoalScreen`, `CreateGoalScreen`).
- Transaction Engine hardened and verified with atomic consistency.
- Goals Engine scoped per pig with derived progress from authoritative pig balances.
- CLINK Coin + Pig Animation System verified across multiple pigs with target-aware animations.
- **Environment & Build Verification**:
  - Android Studio 2026.1.4 (AI-261.26222.65.2614.16204760) verified.
  - JDK: OpenJDK 21 (`C:\Users\jowan\.jdks\jbr-21.0.11`).
  - Android SDK: `C:\Users\jowan\AppData\Local\Android\Sdk` (Platform 35/36 installed & verified).
  - Debug APK build: `.\gradlew.bat assembleDebug` verified (SUCCESS, `app-debug.apk` generated).
  - Unit Tests: `.\gradlew.bat test` verified (164 tests across 33 test classes, 0 failures, 100% PASS; baseline from TASK-011 was 150 tests + 14 tests added in TASK-012).
  - Static Analysis: `.\gradlew.bat lintDebug` verified (SUCCESS, 0 errors, 0 warnings).
  - Live Runtime Verification: Tested on Android 16 (`emulator-5554`), 0 crashes, Scenarios A-AE verified.






