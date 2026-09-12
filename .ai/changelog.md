# CLINK Changelog

All notable changes to the CLINK project will be documented in this file.

## [TASK-009] - Goals Engine

### Added
- Domain Layer:
  - `GoalProgress`: Immutable domain representation combining `Goal` with derived savings progress (`currentAmount`, `targetAmount`, `remainingAmount`, `progressPercent`, `progressFraction`, `isCompleted`).
  - `GoalProgressCalculator`: Pure domain engine for deterministic progress calculations using exclusively integer paise (`Long`). Defensively clamps to 0..100, caps progress percentage at 99% if `current < target`, and prevents division by zero. Zero `Float` or `Double` monetary conversions.
  - `GoalRepository`: Domain interface supporting reactive goal streams per pig (`observeGoalsForPig`), global observation (`observeAllGoals`), single item retrieval (`getGoalById`), creation, updating, and deletion.
  - `CreateGoalUseCase`: Domain use case validating goal title (required, trimmed, max 50 characters), target amount (positive, non-zero `Money`), pig existence, and persisting via repository.
  - `ObserveGoalsUseCase`: Reactive use case combining goal flow and pig flow to dynamically compute `GoalProgress` for all goals; sorts active goals first by `createdAt DESC`, followed by completed goals by `createdAt DESC`.
  - `DeleteGoalUseCase`: Domain use case safely deleting goals without touching pig balance, transactions, or user savings.
- Data Layer:
  - `GoalDao`: Upgraded with deterministic ordering (`ORDER BY createdAt DESC, id DESC`), `getGoalById(Long)`, and `deleteGoal(Long)`.
  - `GoalRepositoryImpl`: Implemented complete CRUD and reactive flows mapping `GoalEntity` to domain `Goal`.
- Presentation Layer:
  - `CreateGoalViewModel`: Handles form state (`title`, `targetAmountRupees`, `canSubmit`, `isProcessing`, `errorMessage`), enforces integer rupee input filtering, suppresses double-taps, and emits navigation events.
  - `CreateGoalScreen`: Full goal creation UI adhering to CLINK design tokens: pig mascot header, title input with 50-character counter, rupee target input with `₹ ` prefix, real-time error banner, and `ClinkButton`.
  - `GoalScreen`: Upgraded goal dashboard supporting empty state with "Create Your First Goal 🎯" CTA, active/completed goal cards with animated progress bars, celebratory "Completed 🎉" badges, "Goal achieved!" feedback, extended FAB, and safe delete confirmation dialog.
  - `HomeScreen` & `HomeViewModel`: Integrated compact goal summary card ("🎯 Your Goal", progress bar, navigation link) into Home dashboard.
  - `PigDetailScreen` & `PigDetailViewModel`: Integrated concise goal summary card into Pig Detail.
  - `Screen.CreateGoal`: Route pattern `"create_goal?pigId={pigId}"` with type-safe argument parsing in `ClinkNavGraph`.
- Unit Testing:
  - Added 35 new unit tests across 7 new test suites, bringing total unit tests to 131 (100% passing):
    - `GoalProgressCalculatorTest` (10 tests)
    - `CreateGoalUseCaseTest` (6 tests)
    - `ObserveGoalsUseCaseTest` (4 tests)
    - `DeleteGoalUseCaseTest` (3 tests)
    - `GoalIntegrationTest` (4 tests)
    - `GoalViewModelTest` (5 tests)
    - `CreateGoalViewModelTest` (8 tests)
    - `GoalRepositoryImplTest` (5 tests)
- Runtime Verification:
  - Verified on Android 16 / API 36 emulator (`emulator-5554`) across Scenarios A through K with zero crashes, zero errors, and clean logcat.


## [TASK-008] - Add Money Experience Polish

### Added
- Presentation Layer:
  - Redesigned `AddMoneyScreen` with high-polish micro-saving experience:
    - Quick denomination selection row with 4 interactive `ClinkAmountChip`s (₹10, ₹20, ₹50, ₹100) featuring highlighted borders, container fills, and checkmarks.
    - Custom Rupee amount numeric input with `₹ ` prefix, clear action, and real-time inline validation feedback ("Amount must be greater than ₹0", "Please enter an amount", "Maximum micro-saving amount is ₹1,00,000").
    - Strict integer paise arithmetic throughout (`Money.fromRupees()`) — zero `Double` or `Float` conversions.
    - Optional note field (capped at 50 characters with live character counter) with automatic whitespace trimming and fallback to `"Clink savings"`.
    - Lightweight celebratory success banner ("CLINK! ₹X saved 🐷") on save before smooth return to Home.
    - Double-tap lockout via `isProcessing` flag disabling button during and post-success.
    - `resetState()` on ViewModel for clean form reuse.
  - `AddMoneyUiState` extended with `customAmountText`, `isCustomAmount`, `noteText`, `validationError`, `savedAmount`, and computed `canSave`.
- Unit Testing:
  - Expanded `AddMoneyViewModelTest` from 5 to 16 tests covering quick amounts, custom amounts, validations (empty, zero, non-numeric, overflow), note trimming, double-tap lockout, event emissions, and state resets. Total unit tests across project increased to 96.

## [TASK-007] - Home Dashboard Polish

### Added
- Presentation Layer:
  - Redesigned `HomeScreen` as a comprehensive daily savings dashboard:
    - Primary Pig Hero Card featuring dynamic state-aware mascot illustration (`ClinkPigIllustration`), status badge (`🐣 New`, `🌱 Growing`, `✨ Healthy`, `🏆 Full`), animated milestone progression bar (`animateFloatAsState`), and contextual motivational text.
    - Top-tier celebratory state handling when full to avoid artificial 100% bars.
    - In-dashboard primary "Save Money 🐷" / "Save First ₹10 🐷" action button.
    - Quick navigation shortcuts for "History" (All transactions) and "Goals" (Savings targets).
    - "Recent Activity" section showing the 3 most recent transactions ordered newest-first, formatted with relative timestamps (`Today, hh:mm a`, `Yesterday, hh:mm a`, `dd MMM, hh:mm a`), transaction notes, and credit pill indicators (`+ ₹50`).
    - First-use empty guidance card welcoming the user and guiding their first save when balance is ₹0.
    - Extended FAB for `+ Add Savings` with elevation and accessible semantics.
  - `HomeUiState` updated with `recentTransactions: List<Transaction> = emptyList()`.
  - `HomeViewModel` injected with `GetTransactionsUseCase`, combining pigs and transactions reactively.
- Unit Testing:
  - `HomeViewModelTest`: Added test case verifying reactive emission of `recentTransactions` limited to the 3 most recent transactions. Total tests increased to 85.

## [TASK-006] - Transaction Engine Hardening

### Added
- Domain Layer:
  - `TransactionRepository.observeTransactions(pigId)` default method for standardized reactive observation.
  - `GetTransactionsUseCase`: dedicated domain use case providing clean boundaries for history queries across all pigs or by specific pig ID.
  - `Money.isPositive` and `Money.isZero` helper properties.
- Data Layer:
  - Deterministic secondary ordering in `TransactionDao`: `ORDER BY timestamp DESC, id DESC`.
  - Atomicity hardening in `PigRepositoryImpl.addSavings`: unified single committed timestamp, note sanitization, and positive amount validation.
  - Thread-safe default pig initialization using `Mutex.withLock` in `PigRepositoryImpl.getOrCreateDefaultPig()`.
- Presentation Layer:
  - `HistoryViewModel` refactored to consume `GetTransactionsUseCase` and handle optional navigation arguments from `SavedStateHandle`.
  - `isProcessing` state lockout in `AddMoneyViewModel` and `AddMoneyScreen` disabling button and ignoring taps after success.
- Unit Tests (14 new tests, 82 total):
  - `TransactionDomainTest` (3 tests).
  - `GetTransactionsUseCaseTest` (2 tests).
  - `TransactionAtomicityTest` (7 tests).
  - `HistoryViewModelTest` (2 tests).

### Changed
- `UseCaseModule`: registered `GetTransactionsUseCase`.
- `TransactionDao`: added deterministic `id DESC` secondary sort key.
- `HistoryScreen`: consumes transactions exclusively via `GetTransactionsUseCase`.

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
