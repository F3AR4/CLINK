# CLINK Lessons Learned

## Build & Architecture
1. **Compose Compiler in Kotlin 2.0+**:
   - In modern Kotlin (2.0+), the standalone `androidx.compose.compiler:compiler` artifact is replaced by the official Jetpack Compose compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`).
   - Using this plugin ensures seamless compatibility with Kotlin versions without version mismatch friction.
2. **Value Classes for Money**:
   - Using Kotlin `@JvmInline value class Money(val paise: Long)` provides compile-time type safety with zero runtime allocation overhead.
   - Forbids accidental addition of raw integers, doubles, or floats into currency fields.
3. **Environment Detection**:
   - Always inspect the host environment before attempting builds.
   - Never claim an APK or test suite was executed if JDK/Android SDK is not installed on the execution machine.
4. **MockK with Constrained Value Classes**:
   - MockK's `any()` matcher for primitive value classes generates synthetic instances using random primitives (e.g. `Random.nextLong()`).
   - If the value class has an `init { require(...) }` constraint (such as non-negative amounts), MockK can throw `IllegalArgumentException` during verification.
   - Solutions: Use `wasNot Called` when verifying that a dependency was untouched, or pass explicit concrete instances into `coVerify` rather than unconstrained `any()` matchers.
5. **Money Overflow Protection**:
   - 64-bit integer paise (`Long`) can store up to ~₹9.22 × 10^16. However, standard integer addition silently overflows to negative values if boundary limits are breached.
   - Using `Math.addExact(this.paise, other.paise)` in `Money.plus` guarantees that overflow throws `ArithmeticException` rather than corrupting user balances.
6. **Room `withTransaction` in Unit Tests**:
   - Calling `RoomDatabase.withTransaction` on a mocked `RoomDatabase` in standard JVM `runTest` can hang or throw `UncompletedCoroutinesError` because transaction dispatchers and sqlite locks expect an active SQLite connection.
   - Providing an internal configurable `transactionRunner` lambda in `PigRepositoryImpl` allows unit tests to execute transaction blocks directly on mock DAOs while production code runs within genuine Room SQLite transactions.
7. **Double-Tap Race Condition Prevention**:
   - Asynchronous checks in `viewModelScope.launch` leave a microtask window where multiple quick taps before the first coroutine suspension can invoke the use case multiple times.
   - Updating UI state to `SaveStatus.Saving` synchronously on the main thread inside `onAddMoney()` prior to launching the coroutine guarantees rapid taps are immediately rejected.
8. **Android Adaptive Icons Resource Folder Placement**:
   - In modern Android Gradle Plugin / AAPT2, adaptive launcher icon XML files require the `-v26` folder qualifier (`mipmap-anydpi-v26`).
   - Renaming to `mipmap-anydpi` without the API qualifier causes resource linking failures during APK packaging. Instead, configure `lint` to disable the redundant `ObsoleteSdkInt` check.
9. **Accessibility & Design Token Discipline**:
   - All interactive components (buttons, chips, icon toggles) should guarantee a minimum touch target size of 48.dp (`LocalDimensions.current.minTouchTarget`).
   - Typography `letterSpacing` in Compose takes `TextUnit` (e.g. `1.2.sp`) rather than raw floats or numeric arguments.
   - Using `String.format(Locale.getDefault(), ...)` prevents lint `DefaultLocale` warnings while correctly formatting currency numbers according to user locale.
10. **DataStore Concurrent Access in Unit Tests**:
   - Multiple `DataStore` instances pointing to the same file in the same process throw `IllegalStateException` unless the first instance's scope is cancelled.
   - In tests verifying persistence across repository instances, create `ds1` in a dedicated `CoroutineScope(job1)` and call `job1.cancel()` before creating `ds2`.
11. **Testing Onboarding StateFlow with StandardTestDispatcher**:
   - When ViewModels use `stateIn(started = SharingStarted.Eagerly)`, `testDispatcher.scheduler.advanceUntilIdle()` must be called to ensure mock flow emissions are processed before asserting state.
   - Using a backing `MutableStateFlow` in tests avoids missed single-shot emissions.
12. **Financial Data Isolation**:
   - Application lifecycle flags (like onboarding completion) must be strictly isolated in preferences (DataStore) away from SQLite/Room financial tables.
   - Toggling onboarding completion states must never touch, modify, or drop pigs, balances, or transactions.
13. **Testing `SharingStarted.WhileSubscribed` StateFlows**:
   - When ViewModels configure `stateIn(SharingStarted.WhileSubscribed(...))`, the upstream flow is only activated when there is at least one active collector.
   - In unit tests asserting on `viewModel.uiState.value`, always launch a collection job in the test's `backgroundScope`:
     `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }`
     prior to advancing the test dispatcher. This ensures the upstream flow emits and produces the updated state without hanging.
14. **Concurrent Startup Initialization (`Mutex.withLock`)**:
   - When multiple ViewModels or use cases start simultaneously on app cold launch (e.g. `MainViewModel`, `HomeViewModel`, `GetPrimaryPigUseCase`), non-synchronized checks like `if (pigDao.getFirstPig() == null)` can execute concurrently before the first insert commits, causing multiple initial entities.
   - Protecting default entity creation with `Mutex.withLock` guarantees strictly idempotent single-entity creation across all concurrent coroutines.
15. **Post-Success Click Lockout in Interactive Flows**:
   - Guarding against rapid double-taps only while `isLoading` allows extra taps after a fast local operation completes (e.g. state transitions to `Success`) before the UI navigation transition pops the back stack.
   - Introducing `isProcessing: Boolean get() = saveStatus is SaveStatus.Saving || saveStatus is SaveStatus.Success` and binding Compose button `enabled = !isProcessing` locks out subsequent taps for the entire remaining lifetime of that screen.
16. **Keyboard Overlay Handling in Automated UI Testing**:
   - When entering text into an `OutlinedTextField` during UI tests, the Android software keyboard window remains elevated and captures touch events intended for lower screen components (such as bottom-anchored action buttons).
   - Explicitly dispatching `adb shell input keyevent 4` (KEYCODE_BACK) dismisses the soft keyboard window while keeping the current activity on screen, allowing subsequent coordinate taps to reach their intended target buttons without occlusion.
17. **MockK Value Class Negative Random Values**:
   - MockK's `any()` on an inline value class (`Money(paise: Long)`) passes random 64-bit Long bits, which can be negative (`paise < 0`).
   - If the inline class's `init` block asserts `require(paise >= 0)`, MockK generates an `IllegalArgumentException` during stubbing or invocation matching.
   - Solution: Use concrete non-negative instances (`Money.zero`, `Money.fromRupees(10)`) or `match { it.isPositive }` rather than unconstrained `any()`.
18. **Parallel Gradle Daemon File Lock Contention in Windows/KSP**:
   - Spawning concurrent Gradle commands in separate background processes on Windows causes file lock contention on KSP / Kotlin compiler caches (`java.nio.file.NoSuchFileException` on `.kotlin\ksp`).
   - Always execute Gradle tasks sequentially (`testDebugUnitTest` -> `assembleDebug` -> `lintDebug`) or wait for the prior process to finish.
19. **Authoritative State vs. Derived Progress**:
   - Instead of storing a mutable `savedAmount` on a Goal entity (which inevitably drifts or desynchronizes when transactions occur), deriving `GoalProgress` reactively by combining `Pig.balance` with `Goal.targetAmount` in `ObserveGoalsUseCase` ensures 100% mathematical consistency without secondary balance tables.
20. **Recomposition-Free Lazy List Formatting**:
   - Formatting timestamps inside `LazyColumn` item composables causes repeated string parsing and calendar allocations on every scroll frame.
   - Pre-mapping domain items into a `TransactionUiModel` with formatted strings and content descriptions in `HistoryViewModel` ensures list scrolling is 100% recomposition-free and allocation-free.
21. **Binary Output Redirection in Windows PowerShell for ADB**:
   - In Windows PowerShell, redirecting `adb exec-out screencap -p > file.png` writes UTF-16LE text with a byte-order mark instead of raw binary bytes, corrupting image files.
   - Safe approaches: Save directly on device and pull (`adb shell screencap -p /sdcard/s.png; adb pull /sdcard/s.png file.png`), or use `[System.IO.File]::WriteAllBytes`.
22. **Presentation-Layer Animation Isolation & Coordinate Mapping**:
   - Animations must never mutate or substitute domain models; visual counters (`AnimatedMoneyDisplay`) animate purely presentation floats while strictly settling on authoritative `money.paise`.
   - Layout-aware coordinates should be captured reactively via `onGloballyPositioned` with relative window offset calculations rather than hardcoded device coordinates, ensuring animation paths adapt dynamically to arbitrary screen densities and orientations.


