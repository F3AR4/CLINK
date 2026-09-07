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

## 4. Current State
- Repository scaffolded with complete Clean Architecture layers (`domain`, `data`, `presentation`, `di`).
- Initial Room database (`ClinkDatabase`) and entities (`PigEntity`, `TransactionEntity`, `GoalEntity`) established.
- `FakePaymentRepository` configured for simulated deposits and withdrawals.
- Compose Theme (`ClinkPink`, `ClinkNavy`, `ClinkTeal`) and 6 core screens created with `ClinkNavGraph`.
