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
