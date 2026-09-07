# CLINK Changelog

All notable changes to the CLINK project will be documented in this file.

## [Unreleased] - Phase 1 Foundation

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
