# CLINK Changelog

All notable changes to the CLINK project will be documented in this file.

## [Unreleased] - TASK-002: Core Local Savings Mechanics

### Added
- `Money.plus` arithmetic overflow protection using `Math.addExact`.
- `PigDao.getPigCount()` and `PigDao.getFirstPig()` Room queries for idempotent pig initialization.
- `PigRepository.addSavings()` atomic method executing balance update and transaction logging inside Room `database.withTransaction`.
- `PigRepository.getOrCreateDefaultPig()` for safe, non-duplicating initial pig state on startup.
- `SaveStatus` state machine (`Idle`, `Saving`, `Success`, `Error`) in `AddMoneyViewModel`.
- Synchronous rapid double-tap guard in `AddMoneyViewModel.onAddMoney()`.
- New unit test suites:
  - `SavingsEnginePersistenceTest` (4 tests verifying atomicity, multiple consecutive saves, and reload persistence).
  - `AddMoneyViewModelTest` (5 tests verifying state transitions and rapid double-tap prevention).
  - Expanded `MoneyTest` and `AddMoneyUseCaseTest`.

### Changed
- `AddMoneyUseCase`: hardened to validate positive paise, pig existence, and arithmetic overflow before processing.
- `HomeViewModel`: updated `ensureDefaultPig()` to call `pigRepository.getOrCreateDefaultPig()`.
- Adaptive launcher icons restored with `-v26` resource directory qualifier for strict AAPT2 compatibility.

## [TASK-001] - Phase 1 Foundation Bootstrap & Verification

### Added
- Root Gradle Kotlin DSL build setup with `settings.gradle.kts` and `build.gradle.kts`.
- Gradle Version Catalog (`gradle/libs.versions.toml`) configuring stable Kotlin 2.1.0, AGP 8.7.3, Room 2.6.1, Hilt 2.54, Navigation Compose 2.8.5.
- Git repository initialization with Android/Gradle `.gitignore`.
- Domain Layer:
  - `Money` value class enforcing `Long` paise calculations.
  - Domain models: `Pig`, `Transaction`, `Goal`, `User`.
  - Repository interfaces: `PigRepository`, `TransactionRepository`, `GoalRepository`, `PaymentRepository`.
  - Use cases: `AddMoneyUseCase`, `GetPigSummaryUseCase`.
- Data Layer:
  - Room entities: `PigEntity`, `TransactionEntity`, `GoalEntity`.
  - DAOs: `PigDao`, `TransactionDao`, `GoalDao`.
  - `ClinkDatabase` (Room v1).
  - DataStore Preferences repository: `UserPreferencesRepository`.
  - Implementations: `PigRepositoryImpl`, `TransactionRepositoryImpl`, `GoalRepositoryImpl`, `FakePaymentRepository`.
- Dependency Injection:
  - Hilt modules: `DatabaseModule`, `RepositoryModule`, `DataStoreModule`, `UseCaseModule`.
- Presentation Layer:
  - Material 3 theme: `ClinkPink` (#E85D75), `ClinkNavy` (#172033), `ClinkTeal` (#35B8A6).
  - Common components: `ClinkTopBar`, `MoneyDisplay`, `ClinkButton`, `QuickAmountChip`.
  - Navigation: `Screen.kt`, `ClinkNavGraph.kt`.
  - Placeholder screens: `OnboardingScreen`, `HomeScreen`, `AddMoneyScreen`, `HistoryScreen`, `GoalScreen`, `PigDetailScreen`.
- Entry points: `ClinkApplication.kt` (`@HiltAndroidApp`), `MainActivity.kt` (`@AndroidEntryPoint`), `AndroidManifest.xml`.
- Unit tests:
  - `MoneyTest.kt`
  - `AddMoneyUseCaseTest.kt`
  - `FakePaymentRepositoryTest.kt`
- Engineering Memory System: `.ai/` files.
- Documentation: `docs/architecture.md`, `README.md`.
