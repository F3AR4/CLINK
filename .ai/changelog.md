# CLINK Changelog

All notable changes to the CLINK project will be documented in this file.

## [TASK-005] - Pig Engine + Single Pig Experience

### Added
- Domain Layer:
  - `PigState`: progression enum (`NEW`, `GROWING`, `HEALTHY`, `FULL`) with human-readable labels and descriptions.
  - `PigProgression`: data class modeling current balance, state, next monetary threshold, and normalized progress fraction.
  - `PigStateCalculator`: centralized deterministic engine calculating states and progress using `Money` constants.
  - Derived properties `Pig.state` and `Pig.progression`.
  - `GetPrimaryPigUseCase`: observing the primary piggy bank reactively.
- Presentation Layer:
  - Dynamic `ClinkPigIllustration` rendering distinct facial expressions, coins, and accents for `NEW`, `GROWING`, `HEALTHY`, and `FULL`.
  - Upgraded `HomeScreen` Hero Card with primary pig status badge, tier progress bar, dynamic illustration, and accessible semantics.
  - Upgraded `PigDetailScreen` with CLINK design system, mascot hero, progression status card, detailed metadata card, and action buttons.
- Unit Tests:
  - `PigStateCalculatorTest` (7 tests).
  - `PigProgressionPersistenceTest` (3 tests).
  - `HomeViewModelTest` (2 tests).
  - `PigDetailViewModelTest` (2 tests).

### Changed
- `HomeViewModel`: exposes `primaryPig` in `HomeUiState` for reactive single pig experience.
- `HomeScreen`: `PigListItem` displays status badge and state-aware mascot.
- `UseCaseModule`: provides `GetPrimaryPigUseCase`.

## [TASK-004] - Onboarding + Persistent User State

### Added
- Domain Layer:
  - `UserPreferencesRepository` domain interface for observing and persisting user app preferences.
  - `GetOnboardingStateUseCase` observing `Flow<Boolean>`.
  - `CompleteOnboardingUseCase` persisting onboarding completion.
- Data Layer:
  - `UserPreferencesRepositoryImpl` implementing domain interface with `IOException` recovery and `runCatching` safe writes.
- Dependency Injection:
  - Repository binding in `RepositoryModule`.
  - Use cases in `UseCaseModule`.
- Presentation Layer:
  - `MainViewModel` and `MainUiState` managing startup routing and splash screen state.
  - Branded startup splash screen in `MainActivity` eliminating route flashes.
  - `OnboardingViewModel` with `OnboardingUiState` and synchronous double-tap suppression.
  - Complete redesign of `OnboardingScreen` using CLINK design system (`ClinkPigIllustration`, `ClinkCard`, `ClinkButton`, `ClinkDimens.current`, value propositions, Light & Dark themes).
- Unit Tests (17 new tests, 54 total):
  - `UserPreferencesRepositoryTest` (5 tests).
  - `OnboardingUseCasesTest` (3 tests).
  - `OnboardingViewModelTest` (5 tests).
  - `MainViewModelTest` (3 tests).
  - `SavingsIsolationTest` (1 test).

### Changed
- `MainActivity.kt`: Now observes `MainViewModel.uiState` to decide between Splash, Onboarding, or Home.
- `OnboardingScreen.kt`: Replaced basic text layout with branded design system cards, illustration, and button.
- `UserPreferencesRepository.kt` moved/refactored to cleanly separate domain interface from data layer implementation.

## [TASK-003] - Design System + Branding

### Added
- Standardized Material 3 design system in Jetpack Compose:
  - `Dimensions.kt`: Spacing tokens (`spacingXxs` through `spacingXxxl`), icon sizes, `minTouchTarget = 48.dp`, elevation tokens.
  - `Motion.kt`: Duration tokens (`DurationFast = 150`, `DurationNormal = 300`, `DurationSlow = 500`), easing curves, and spring specs.
  - `Color.kt`: Semantic color palette for Light and Dark mode (`ClinkPink`, `ClinkNavy`, `ClinkTeal`, `CoinGold`, `PiggyBlush`, `SuccessGreen`, `ErrorRed`, surface and background tones).
  - `Shape.kt`: Standardized M3 shapes (8.dp, 12.dp, 16.dp, 24.dp, 32.dp).
  - `Type.kt`: Cohesive typography hierarchy with clear font sizing and weights.
  - `Theme.kt`: Dynamic dark/light `ClinkTheme` providing `LocalDimensions`.
- Reusable Design Components:
  - `ClinkCard`: Outlined card surface with standardized padding and elevation.
  - `ClinkAmountChip`: Accessible denomination chip with selection animation, checkmark, and active border.
  - `ClinkPigIllustration`: Vector/Canvas-rendered piggy bank mascot with coin slot and shiny gold coin.
  - `ClinkSectionHeader`: Section header with optional action button.
  - `ClinkEmptyState`: Pig mascot illustration empty state with call-to-action button.
  - `ClinkOutlinedButton`: Accessible outlined button variant.
- Unit Tests:
  - `ClinkThemeTest`: Validating color contrast, shape tokens, and spacing tokens.
  - `ClinkComponentsTest`: Validating chip denominations, money display string formatting, and button accessibility touch target.

### Changed
- `HomeScreen`: Integrated hero balance card, pig mascot illustration, `PigListItem`, and accessible FAB.
- `AddMoneyScreen`: Upgraded to interactive denomination chips (₹10, ₹20, ₹50, ₹100), hero amount display, and branded CTA.
- `HistoryScreen`: Branded transaction cards with credit pills (+₹) and timestamps, empty state.
- `GoalScreen`: Branded goal progress cards and empty state.
- `MoneyDisplay`: Explicit `Locale.getDefault()` for lint compliance and proportional symbol scaling.
- `app/build.gradle.kts`: Configured lint options for clean analysis.

## [TASK-002] - Core Local Savings Mechanics

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
