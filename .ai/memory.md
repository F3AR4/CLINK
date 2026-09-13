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

## 4. Current State (TASK-014 Complete)
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
- Reliability & Financial Integrity Hardening (TASK-013):
  - Zero Float/Double financial arithmetic verified repository-wide.
  - Multi-pig financial and transaction isolation audited and verified at database level.
  - Fixed `DeletePigUseCase` selection bug.
  - Eliminated hardcoded default parameter `pigId = 1L` in `Screen.kt` and wired `HistoryScreen` empty state to route pig.
  - Added arithmetic overflow guards in `Money.fromRupees` (`Math.multiplyExact`) and `GoalProgressCalculator` (`BigInteger` fallback).
  - Handled invalid/deleted pig ID in `PigDetailScreen` with user-friendly empty state and back action.
- Accessibility & UX Polish (TASK-014):
  - Standardized dimension tokens: `buttonHeight` (56dp), `chipHeight` (48dp), `amountChipHeight` (58dp), `mascotHero` (88dp), `mascotSplash` (140dp), ensuring all touch targets meet or exceed 48dp.
  - Dynamic balance announcements: Added `liveRegion = LiveRegionMode.Polite` to `AnimatedMoneyDisplay` semantics.
  - Pig Selector UX: Checkmark indicator on selected chip (`selected` state not communicated solely through color), semantic role `Role.Tab`, and text truncation with ellipsis for long pig names.
  - Add Money UX: "Clink It!" button accessibility semantics announcing "Save ₹X to [Pig Name]".
  - Goals UX: Contextual top bar announcing "Goals • [Pig Name]", 48dp delete button touch target.
  - Destructive Actions UX: `DeletePigDialog` delete button styled with high-contrast error container colors to prevent accidental confirmation. Character counters (`0/30`) and IME actions added to `CreatePigDialog` and `RenamePigDialog`.
- **Environment & Build Verification**:
  - Android Studio 2026.1.4 (AI-261.26222.65.2614.16204760) verified.
  - JDK: OpenJDK 21 (`C:\Users\jowan\.jdks\jbr-21.0.11`).
  - Android SDK: `C:\Users\jowan\AppData\Local\Android\Sdk` (Platform 35/36 installed & verified).
  - Debug APK build: `.\gradlew.bat assembleDebug` verified (SUCCESS, `app-debug.apk` generated).
  - Unit Tests: `.\gradlew.bat test` verified (197 tests across 37 test classes, 0 failures, 100% PASS).
  - Static Analysis: `.\gradlew.bat lintDebug` verified (SUCCESS, 0 errors, 0 warnings).
  - Live Runtime Verification: Tested on Android 16 (`emulator-5554`), 0 fatal crashes, Scenarios A-Z verified.






