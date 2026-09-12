# CLINK Current Project State

- **Last Updated**: 2026-09-12
- **Active Phase**: Phase 1 - Foundation
- **Current Task**: TASK-012: Multi-Pig Architecture
- **Status**: COMPLETE & VERIFIED (APK assembled, 164/164 unit tests passing, lint 0 errors & 0 warnings, live on-device runtime verified on API 36 emulator across Scenarios A-AE)

## Components Status
- **Build System**: VERIFIED (Gradle 8.11.1 + JDK 21 + Android SDK 35/36; `assembleDebug` SUCCESS)
- **Git Version Control**: INITIALIZED & CLEAN
- **Domain Layer**: VERIFIED (Pure Kotlin, zero UI/Room/Payment leaks, paise Long representation enforced, Math.addExact overflow protection)
  - Models: `Money` (with `isPositive` and `isZero` helpers), `Pig`, `PigState`, `PigProgression`, `PigStateCalculator`, `Transaction`, `Goal`, `GoalProgress`, `GoalProgressCalculator`, `User`
  - Repositories: `PigRepository`, `TransactionRepository`, `GoalRepository`, `PaymentRepository`, `UserPreferencesRepository` (with `selectedPigId` and `setSelectedPigId`)
  - Use Cases:
    - `CreatePigUseCase`: creates new pig with validation, defaults, and auto-selection
    - `GetSelectedPigUseCase`: reactively retrieves currently selected pig from DataStore with primary pig fallback
    - `SelectPigUseCase`: sets active pig in DataStore preferences
    - `UpdatePigUseCase`: updates pig name and metadata
    - `DeletePigUseCase`: safe delete preventing deletion of only pig, cascading transactions and goals, falling back to surviving pig
    - `CreateGoalUseCase`, `ObserveGoalsUseCase` (scoped to explicit pigId or all), `DeleteGoalUseCase`, `AddMoneyUseCase`, `GetTransactionsUseCase`, `GetPigSummaryUseCase`, `GetPrimaryPigUseCase`
- **Data Layer**: VERIFIED (Room v1 schema preserved, atomic `withTransaction` persistence, SQLite CASCADE on Pig foreign keys, DataStore preferences)
  - Room Entities & DAOs: `PigEntity`, `TransactionEntity`, `GoalEntity`, `PigDao`, `TransactionDao`, `GoalDao`
  - Database: `ClinkDatabase` (`clink.db`, Room v1, zero schema bumps required)
  - Preferences: `UserPreferencesRepository` storing `active_pig_id`
  - Repository Implementations: `PigRepositoryImpl`, `TransactionRepositoryImpl`, `GoalRepositoryImpl`, `UserPreferencesRepositoryImpl`, `FakePaymentRepository`
- **Dependency Injection**: VERIFIED (Hilt 2.54 modules compile and inject dependencies)
  - `DatabaseModule`, `RepositoryModule`, `DataStoreModule`, `UseCaseModule` (providing `CreatePigUseCase`, `GetSelectedPigUseCase`, `SelectPigUseCase`, `UpdatePigUseCase`, `DeletePigUseCase`)
- **Presentation Layer**: VERIFIED (Material 3 CLINK Design System + Brand Identity + Multi-Pig UI)
  - Components: `PigSelectorRow` (horizontal chip selector with selected state, active balance, and + New Pig button), `CreatePigDialog`, `RenamePigDialog`, `DeletePigDialog`, `ClinkSavingsToken`, `ClinkCelebrationBadge`, `AnimatedMoneyDisplay`, `ClinkPigIllustration`, `ClinkSavingsAnimation`
  - Navigation: `ClinkNavGraph` with explicit `pigId` route arguments for `AddMoney/{pigId}`, `History/{pigId}`, `Goals?pigId={pigId}`, `CreateGoal?pigId={pigId}`, `PigDetail/{pigId}`
  - Screens:
    - `HomeScreen`: Shows active selected pig, `PigSelectorRow`, reactive balance, isolated recent transactions, isolated goal card, Add Savings targeting active pig
    - `PigDetailScreen`: Shows specific pig details, rename pig, delete pig with safety check dialog and cascade warning
    - `AddMoneyScreen`: Explicit "Saving to <Pig Name>", coin flight and reaction targeting explicit pig
    - `HistoryScreen`: Scoped transaction history for specific pig
    - `GoalScreen` & `CreateGoalScreen`: Scoped goals and goal creation for specific pig
    - `OnboardingScreen`
- **Testing**: VERIFIED (164 unit tests, 0 failures, 100% pass rate via `.\gradlew.bat test`)
  - `MultiPigIsolationTest.kt` (4/4 pass: savings isolation, transaction isolation, goal isolation, selection persistence)
  - `PigCrudUseCaseTest.kt` (8/8 pass: create, update, delete safety, fallback, get selected)
  - `HomeViewModelTest.kt` (2/2 pass)
  - Plus 150 existing tests across 30 test classes (all passing 100%, total 164/164 tests across 32 test classes)


