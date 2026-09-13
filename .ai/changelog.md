# CLINK Changelog

All notable changes to the CLINK project will be documented in this file.

## [TASK-015] - Phase 1 Integration Test & Reliability Verification

### Added & Verified
- Full Phase 1 Integration Verification:
  - Added `Phase1IntegrationTest.kt` covering complete interconnected user journeys: onboarding, default pig initialization, multi-pig creation ("Vacation", "Gadgets", "Emergency"), savings isolation (zero balance cross-talk), scoped transactions, scoped goals progress, safe deletion with SQLite CASCADE, selection fallback to surviving pigs, and persistence reload across simulated process loss.
  - Quality Gates: `testDebugUnitTest` 198/198 passed (100%), `lintDebug` 0 errors & 0 warnings, `assembleDebug` SUCCESS.
  - Fixed 5 `ConstantLocale` lint warnings in `TransactionDateFormatter` by replacing static `final` formatters with dynamic getters respecting runtime locale changes.
  - Live On-Device Emulator Verification (`emulator-5554`, Android 16 / API 36):
    - Cold launch, branded splash delay, and onboarding completion.
    - Home dashboard with default Primary Pig (₹0 balance, no stale transactions).
    - Process restart persistence (onboarding remains completed, state restored).
    - Savings journey: Saved ₹10, coin flight animation, "CLINK! +₹10 🐷", "Saved! 🎉", rolling balance settlement to ₹10.
    - Scoped history: Transaction entry created once, matches authoritative balance.
    - Multi-pig creation on device: Created "Vacation", verified auto-selection, switched between Primary Pig and Vacation chips with zero balance leakage.
    - Themes: Verified light mode and dark mode (obsidian backgrounds, accessible contrast, no clipping).
    - Logcat crash audit: 0 crashes, 0 fatal errors, 0 ANRs.
  - Financial safety audit: Confirmed 0 Float/Double types in financial calculations; Long paise arithmetic and `Math.addExact` strictly enforced.
  - Architecture audit: Verified unidirectional Clean Architecture flow (presentation -> domain -> data); domain is pure Kotlin.

## [TASK-014] - Accessibility + UX Polish

### Added & Improved
- Accessibility:
  - Standardized component dimension tokens in `Dimensions.kt`: `buttonHeight = 56.dp`, `chipHeight = 48.dp`, `amountChipHeight = 58.dp`, `mascotHero = 88.dp`, `mascotSplash = 140.dp`. All interactive elements guarantee >= 48dp touch targets.
  - Added `liveRegion = LiveRegionMode.Polite` to `AnimatedMoneyDisplay` semantics so dynamic balance roll-ups are announced politely by screen readers.
  - Added checkmark icon (`✓`) to selected chip in `PigSelectorRow` so selection state is not communicated solely by color. Added `Role.Tab` semantics and text truncation (`maxLines = 1, TextOverflow.Ellipsis, maxWidth = 140.dp`) for long pig names.
  - Added destination pig name to "Clink It!" button accessibility semantics: `"Save ₹X to [Pig Name]"`.
  - Expanded delete goal touch target to `ClinkDimens.current.minTouchTarget` (48.dp).
- UX & Visual Hierarchy:
  - Goals Screen: Injected reactive pig resolution in `GoalViewModel` so top bar displays contextual title `Goals • [Pig Name]`.
  - Delete Pig Dialog: Styled destructive "Delete" action button with prominent error container and on-error content colors to prevent accidental confirmation.
  - Create & Rename Pig Dialogs: Added character counters (`0/30`) and IME actions (`ImeAction.Next` and `ImeAction.Done`).
  - Home Screen: Reused unified `TransactionDateFormatter.formatTransactionTime` on recent activity and added accessible transaction descriptions.
- Testing:
  - Added touch target token assertions in `ClinkThemeTest.kt`.
  - Added reactive contextual pig name observation test in `GoalViewModelTest.kt`.
  - All 197 unit tests passing (100% pass rate).
- Quality Gates:
  - `lintDebug`: 0 errors, 0 warnings.
  - `assembleDebug`: PASS.
  - Live runtime verification on `emulator-5554` (API 36) executing Scenarios A through Z.

## [TASK-013] - Testing & Reliability Hardening

### Hardened & Fixed
- Financial Integrity:
  - `Money.kt`: Added `Math.multiplyExact(rupees, 100L)` to `fromRupees(rupees: Long)` to reject multiplication overflow beyond `Long.MAX_VALUE / 100L` with `ArithmeticException`.
  - `GoalProgressCalculator.kt`: Added `BigInteger` calculation fallback when `currentPaise * 100L` would exceed `Long.MAX_VALUE`, guaranteeing overflow-free progress computation even for astronomical balance amounts.
- Multi-Pig & Selection Integrity:
  - `DeletePigUseCase.kt`: Fixed issue where deleting a non-active pig unconditionally reset `selectedPigId` to the first surviving pig. Guarded fallback so `selectedPigId` is only reset when `activePigId == pigId`.
- Navigation & Presentation:
  - `Screen.kt`: Removed hardcoded default parameter `pigId = 1L` from `Screen.AddMoney.createRoute(pigId: Long)` to enforce explicit routing destinations.
  - `ClinkNavGraph.kt`: Fixed `HistoryScreen` empty state "Save First ₹10" callback so it routes to the actual history `pigId` rather than defaulting to pig 1.
  - `PigDetailScreen.kt`: Replaced infinite `CircularProgressIndicator` on `pig == null` with an accessible `ClinkEmptyState` ("Pig Not Found" with back navigation) to handle deleted or invalid pig routes gracefully.
- Testing Suite Expansion (+32 tests, total 196 tests / 37 test classes, 100% passing):
  - Added `FinancialIntegrityTest.kt` (9 unit tests).
  - Added `MultiPigIsolationHardeningTest.kt` (6 unit tests).
  - Added `TransactionReliabilityTest.kt` (2 unit tests).
  - Added `GoalReliabilityTest.kt` (7 unit tests).
  - Added `PigSelectionReliabilityTest.kt` (5 unit tests).
  - Extended `PigCrudUseCaseTest.kt` (+1 unit test).
  - Extended `MoneyTest.kt` (+2 unit tests).
- Static Quality Gates:
  - `test`: 196/196 passing.
  - `lintDebug`: 0 errors, 0 warnings.
  - `assembleDebug`: SUCCESS.
- Runtime Verification:
  - Verified Scenarios A through AD on Android 16 / API 36 emulator (`emulator-5554`): multi-pig creation, SQLite balance isolation, transaction isolation, dark mode / light mode, rapid interaction, process restart persistence, and 0 fatal crashes in logcat.

## [TASK-012] - Multi-Pig Architecture


### Added
- Data Layer:
  - `UserPreferencesRepository`: Added `selectedPigId: Flow<Long?>` and `setSelectedPigId(pigId: Long)` backed by DataStore preferences under key `active_pig_id`.
- Domain Layer:
  - `CreatePigUseCase`: Creates new Pig with validated name (trimmed, 1..30 chars), optional target amount, default visual metadata, and automatically sets the new pig as the selected pig.
  - `GetSelectedPigUseCase`: Reactively emits the active pig based on DataStore `selectedPigId`, seamlessly falling back to the primary/first pig if selection is null or refers to a deleted pig.
  - `SelectPigUseCase`: Persists user's active pig selection in DataStore preferences.
  - `UpdatePigUseCase`: Updates pig name and timestamp.
  - `DeletePigUseCase`: Enforces delete safety by forbidding deletion of the last remaining pig; relies on Room SQLite CASCADE foreign keys to clean up related transactions and goals; safely falls back `selectedPigId` to another surviving pig.
  - `ObserveGoalsUseCase`: Updated to support optional `pigId` parameter for pig-scoped goal observation, calculating progress dynamically based strictly on the matching pig's authoritative balance.
- Presentation Layer:
  - `PigSelectorRow`: Horizontal scrollable row on `HomeScreen` displaying all pigs as interactive chips with active badge styling, current balance display, and a "+ New Pig" action chip.
  - `CreatePigDialog`: Accessible dialog for naming and setting optional targets for a new pig.
  - `RenamePigDialog` & `DeletePigDialog`: Modals on `PigDetailScreen` for renaming and safely deleting pigs with confirmation and cascade warnings.
  - Explicit Pig Routing:
    - `AddMoneyScreen` and `AddMoneyViewModel`: Require explicit `pigId`, clearly announce destination ("Saving to <Pig Name>"), and route savings to the specified pig.
    - `ClinkNavGraph`: Navigation routes now accept explicit `pigId` parameters (`AddMoney/{pigId}`, `History/{pigId}`, `Goals?pigId={pigId}`, `CreateGoal?pigId={pigId}`, `PigDetail/{pigId}`).
    - `HistoryViewModel` & `GoalViewModel`: Scope transactions and goals strictly to explicit or selected `pigId`.
- Testing:
  - `MultiPigIsolationTest`: 4 unit tests verifying savings isolation, transaction isolation, goal progress isolation, and selection persistence across multiple pigs.
  - `PigCrudUseCaseTest`: 8 unit tests verifying pig creation, rename update, delete safety preventing sole pig deletion, automatic fallback selection upon deletion, and reactive active pig resolution.
  - `HomeViewModelTest`: 2 unit tests verifying multi-pig reactive state emission and pig selection callbacks.
  - Total project unit tests increased from 150 to 164 across 32 test classes (100% passing).
- Runtime Verification:
  - Verified on Android 16 / API 36 emulator (`emulator-5554`) across all 31 runtime scenarios (A through AE): fresh launch, multi-pig creation, savings isolation, transaction isolation, goal isolation, rename, process restart persistence, non-primary deletion, primary pig deletion safety, explicit Add Money targeting, animation integrity, light/dark mode, rapid switching, and clean logcat audit.

## [TASK-011] - CLINK Coin + Pig Animation System


### Added
- Motion & Design Tokens:
  - `Motion.kt`: Added `DurationFlight = 450`, `DurationReaction = 350`, `DurationCelebration = 600`, and `FlightEasing` (FastOutSlowIn / custom cubic-bezier).
  - `Color.kt`: Added `CoinGoldDark` (`#C48800`) and `CoinGoldRim` (`#FFE885`) for tactile coin rendering.
- Presentation Components:
  - `ClinkSavingsToken`: Compose-native golden coin medallion with radial gradients, outer rim, inner shadow, and dynamic currency text scaling (`₹10`, `₹20`, `₹50`, `₹100`, custom amounts).
  - `ClinkCelebrationBadge`: Reusable spring-scale "CLINK! + ₹X 🐷" celebration pill badge with icon and animated entry.
  - `AnimatedMoneyDisplay`: Smooth rolling balance interpolator between previous and authoritative balance, using presentation Float strictly for interpolation while guaranteeing settlement on authoritative `money.paise`. Zero Float/Double currency conversions.
  - `ClinkPigIllustration`: Enhanced with presentation-only squash-and-stretch bounce reaction (`reactionTrigger: Any? = null`) using Compose `Animatable` and `graphicsLayer`.
  - `ClinkSavingsAnimation`: Layout-aware flight orchestrator coordinating the 4-phase sequence (`FLIGHT` -> `IMPACT` -> `CELEBRATION` -> `COMPLETED`) with dynamic start/target offset calculations via `onGloballyPositioned`.
- Screen Integrations:
  - `AddMoneyScreen`: Orchestrated complete signature savings sequence: user taps save -> domain transaction commits -> coin token launches upwards towards pig -> pig squashes & stretches -> "CLINK! + ₹X 🐷" badge appears -> button remains locked against double taps during animation -> auto-navigates smoothly back.
  - `HomeScreen`: Integrated `AnimatedMoneyDisplay` on primary card and reactive pig bounce triggered on balance increase.
  - `PigDetailScreen`: Integrated `AnimatedMoneyDisplay` and reactive pig bounce on balance increment.
- Testing:
  - `ClinkSavingsAnimationTest`: 7 unit tests covering animation stage transitions, savings token text and accessibility formatting, balance interpolation safety, and easing parameters. Total project unit tests increased from 143 to 150 (100% passing).
- Runtime Verification:
  - Verified on Android 16 / API 36 emulator (`emulator-5554`) across all 16 runtime scenarios (A through P): fresh launch, ₹10, ₹20, ₹50, custom ₹35, note save, rapid tap protection, app restart persistence, history verification, pig detail verification, light/dark themes, navigation during animation, and clean logcat audit.

## [TASK-010] - History + Transaction UI

### Added
- Presentation Layer:
  - `TransactionDateFormatter`: Pure presentation utility providing contextual relative timestamps ("Today, h:mm a", "Yesterday, h:mm a", "d MMM, h:mm a"), date section headers ("TODAY", "YESTERDAY", "10 SEP"), and accessibility announcements with parameterized `now` and `ZoneId` for deterministic unit testing.
  - `TransactionUiModel`: Data representation decoupling date string formatting from Compose recomposition.
  - `HistoryViewModel`: Dedicated ViewModel extracted into its own file; exposes `HistoryUiState` with reactive `totalSaved` (derived via `Money.plus` / integer paise math), `transactionCount`, `groupedTransactions` (preserving newest-first ordering), loading and error handling with `retry()`.
  - `HistoryScreen`: Redesigned transaction timeline:
    - Aggregate Summary Card: "TOTAL SAVED", large formatted amount, "X savings" count badge in `primaryContainer`.
    - Grouped list in `LazyColumn` with uppercase date headers and stable item keys (`key = { it.transaction.id }`).
    - `TransactionItem`: Circle icon with `ArrowDownward` credit indicator, prominent positive amount (`+ ₹50`), note with fallback ("Clink savings"), "Saved" pill tag, relative timestamp, and accessibility semantics.
    - `ClinkEmptyState`: Mascot illustration, "No savings yet", "Your little savings journey will show up here.", and "Save Your First ₹10" button.
    - Friendly Error State: Warning icon, error description, and "Retry" button.
    - Loading State: Centered `CircularProgressIndicator`.
  - Navigation:
    - `ClinkNavGraph.kt`: Connected `onNavigateToAddMoney` callback to `HistoryScreen` allowing empty state action to open Add Money directly.
    - `Screen.kt`: Added default parameter `pigId: Long = 1L` to `Screen.AddMoney.createRoute()`.
- Unit Testing:
  - `TransactionDateFormatterTest`: 8 tests covering today, yesterday, same-year, different-year, date group headers, and accessibility descriptions.
  - `HistoryViewModelTest`: 7 tests covering loading state, populated transactions, aggregate totals, empty list handling, raw domain flow compatibility, savedStateHandle arguments, error handling, and retry recovery.
- Runtime Verification:
  - Verified on Android 16 (`emulator-5554`) across Scenarios A through L with zero crashes, zero errors, and clean logcat.


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
