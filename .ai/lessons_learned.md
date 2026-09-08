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
